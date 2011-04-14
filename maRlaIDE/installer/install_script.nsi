;maRla installer
;Written by Andrew Sterling

SetCompress auto
SetCompressor /SOLID lzma

;--------------------------------
;Include Modern UI

!include "MUI2.nsh"
!include "LogicLib.nsh"
!include "Sections.nsh"
!include "WinMessages.nsh"

!include "FileAssociation.nsh"
!include "StrUtils.nsh"

;--------------------------------
;General

!define JRE_VERSION "1.6"
!define TDRIVE_TEX "T:\TEX\CTAN\basic-miktex.exe"
!define TDRIVE_R "T:\TEX\CRAN\R-win.exe"
!define TDRIVE_TEXMAKER "T:\TEX\CTAN\texmakerwin32_install.exe"
!define TDRIVE_GHOSTSCRIPT "T:\TEX\CTAN\ghostcript_install.exe"
!define TDRIVE_GSVIEW "T:\TEX\CTAN\gsview_install.exe"

Var /GLOBAL RETURN ; Used by functions for returning values
Var /GLOBAL JavaInstaller
Var /GLOBAL RInstaller
Var /GLOBAL MikTexInstaller
Var /GLOBAL TexmakerInstaller
Var /GLOBAL GhostscriptInstaller
Var /GLOBAL GSviewInstaller
Var /GLOBAL JavaVer
Var /GLOBAL JavaHome
Var /GLOBAL RHome
Var /GLOBAL RTexmfRoot
Var /GLOBAL MikTexVer
Var /GLOBAL MikTexHome

; Name and file
Name "maRla"
OutFile "Setup.exe"

; Default installation folder
InstallDir "$ProgramFiles\maRla"

; Get installation folder from registry if available
InstallDirRegKey HKCU "Software\maRla" ""

; Request admin privileges for Windows Vista
RequestExecutionLevel highest

; Quick selection options
InstType "Full"
InstType "Minimal"
	
;--------------------------------
;Interface Settings

!define MUI_ABORTWARNING

;--------------------------------
;Pages

!define MUI_INSTFILESPAGE_FINISHHEADER_TEXT "The maRla Project Installation Complete"
!define MUI_PAGE_HEADER_TEXT "The maRla Project Installation"
!define MUI_PAGE_HEADER_SUBTEXT "Accept maRla's open source license agreement"
!define MUI_INSTFILESPAGE_FINISHHEADER_SUBTEXT "maRla installed successfully."

!insertmacro MUI_PAGE_LICENSE "gpl-3.0.txt"
!insertmacro MUI_PAGE_COMPONENTS
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES

!define MUI_FINISHPAGE_NOAUTOCLOSE
!define MUI_FINISHPAGE_RUN
!define MUI_FINISHPAGE_RUN_CHECKED
!define MUI_FINISHPAGE_RUN_TEXT "Launch maRla"
!define MUI_FINISHPAGE_RUN_FUNCTION "LaunchLink"
!insertmacro MUI_PAGE_FINISH

LangString TEXT_JRE_TITLE ${LANG_ENGLISH} "Java Runtime Environment"
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
	
;--------------------------------
;Languages
 
!insertmacro MUI_LANGUAGE "English"

;--------------------------------
;Installer Sections

SectionGroup "maRla Core"

	Section "Install JRE" InstallJava

		AddSize 15000
		SectionIn 1 2 RO
		
		StrCpy $JavaInstaller "$TEMP\jre_setup.exe"
		
		${IfNot} ${FileExists} $JavaInstaller
			; Download, it doesn't already exist
			DetailPrint "Downloading Java to $JavaInstaller"
			NSISdl::download "http://javadl.sun.com/webapps/download/AutoDL?BundleId=33787" "$JavaInstaller"
			Pop $0
			
			${If} $0 != "success"
				; Download failed
				MessageBox MB_OK|MB_ICONEXCLAMATION "Java installer could not be downloaded. Manually install and try again."
				Abort
			${EndIf}
		${EndIf}
				
		; Try to install
		ClearErrors
		ExecWait '"$JavaInstaller" /s REBOOT=Suppress'
		
		${If} ${Errors}
			MessageBox MB_OK|MB_ICONEXCLAMATION "Java failed to install properly. Manually install and try again."
			Abort
		${EndIf}

	SectionEnd

	Section "Install maRla" InstallMarla

		AddSize 800
		SectionIn 1 2
		
		; Make sure the install directory is usable
		SetOutPath "$INSTDIR"
		
		ClearErrors
		Push $0
		Push $1
		StrCpy $1 "$INSTDIR/.TESTFILESHOULDBEWRITABLEmarla1231231231"
		FileOpen $0 $1 w
		FileWrite $0 "blah"
		FileClose $0
		Pop $1
		Pop $0
		
		${If} ${Errors}
			; Directory not writable
			MessageBox MB_OK|MB_ICONINFORMATION "'$INSTDIR' cannot be written to. Please choose a new directory."
			SendMessage $HWNDPARENT "0x408" "-1" ""
			Abort
			
		${Else}
			; remove temp file
			Delete $1
		${EndIf}
		
		; Install files!
		ClearErrors
		File "maRlaIDE.exe"
		File "maRla Operation Editor.exe"
		File "export_template.xml"
		File "ops.xml"
		
		${If} ${Errors}
			MessageBox MB_OK|MB_ICONSTOP "Unable to copy to '$INSTDIR'. Please install again using a new directory."
			Abort
		${EndIf}
		
		; Store installation folder
		WriteRegStr HKCU "Software\maRla" "" $INSTDIR
		
		; Register .marla files with maRla
		${registerExtension} "$INSTDIR\maRlaIDE.exe" ".marla" "maRla File"

		; Create uninstaller
		WriteUninstaller "$INSTDIR\Uninstall.exe"

	SectionEnd

SectionGroupEnd

Section "Install R" InstallR
	
	AddSize 67891
	SectionIn 1
	
	; First check if we have a copy in the temp folder already 
	StrCpy $RInstaller "$TEMP\R-win.exe"
	
	${IfNot} ${FileExists} $RInstaller
	
		; Nope, is it on the T drive?
		${IfNot} ${FileExists} ${TDRIVE_R}
		
			; Download
			DetailPrint "Downloading R to $RInstaller"
			NSISdl::download "http://streaming.stat.iastate.edu/CRAN/bin/windows/base/R-2.12.2-win.exe" $RInstaller
			Pop $0
			DetailPrint "Download result: $0"
			
			${If} $0 != "success"
				; Download failed
				DetailPrint "R download failed"
				MessageBox MB_OK|MB_ICONEXCLAMATION "R installer could not be downloaded.$\nTry again later or manually install."
				Abort
			${EndIf}
			
		${Else}
			; Copy from T to temp for speed and to show progress
			ClearErrors
			CopyFiles ${TDRIVE_R} $RInstaller
			
			${If} ${Errors}
				DetailPrint "Failed to copy R to temporary folder"
				StrCpy $RInstaller ${TDRIVE_R}
			${EndIf}
		${EndIf}
	${EndIf}

	; Install!
	DetailPrint "Installing R from '$RInstaller'"
	ClearErrors
	ExecWait "$RInstaller /SILENT"
	
	${If} ${Errors}
		MessageBox MB_OK|MB_ICONEXCLAMATION "Failed to install R correctly. Please install manually or retry later.$\nInstallation aborted."
		Abort
	${EndIf}

SectionEnd

SectionGroup "MiKTeX"

	Section "Install MiKTeX" InstallMiKTeX

		AddSize 382976
		SectionIn 1
		
		; First check if we have an accessible copy in temp
		StrCpy $MikTexInstaller "$TEMP\basic-miktex.exe"
		
		${IfNot} ${FileExists} $MikTexInstaller
		
			; Is there a copy on the T drive?
			${IfNot} ${FileExists} ${TDRIVE_TEX}
			
				; No T drive copy, get from internet
				DetailPrint "Downloading MiKTeX to '$MikTexInstaller'"
				NSISdl::download "http://ftp.math.purdue.edu/mirrors/ctan.org/systems/win32/miktex/setup/basic-miktex.exe" $MikTexInstaller
				Pop $0
				DetailPrint "Download result: $0"
				
				${If} $0 != "success"
					; Download failed
					DetailPrint "MiKTeX download failed"
					MessageBox MB_OK|MB_ICONEXCLAMATION "MiKTeX installer could not be downloaded.$\nTry again later or manually install"
					Abort
				${EndIf}
			
			${Else}
				; Copy from T to temp for speed and to show progress
				ClearErrors
				CopyFiles ${TDRIVE_TEX} $MikTexInstaller
				
				${If} ${Errors}
					DetailPrint "Failed to copy MiKTeX to temporary folder"
					StrCpy $MikTexInstaller ${TDRIVE_TEX}
				${EndIf}
			${EndIf}
		${EndIf}

		; Install!
		DetailPrint "Installing MiKTeX from '$MikTexInstaller'"
		ClearErrors
		ExecWait '$MikTexInstaller "--user-install=$ProgramFiles\MiKTeX 2.9\" --private --unattended'
		
		${If} ${Errors}
			MessageBox MB_OK|MB_ICONEXCLAMATION "Failed to install MikTex correctly. Please install manually or retry later.$\nInstallation aborted."
			Abort
		${EndIf}

	SectionEnd
	
	Section "Configure MiKTeX" ConfigureMiKTex
		
		SectionIn 1 RO
		
		; Add extra packages that we know it'll need
		Call CheckInstalledMikTex
		DetailPrint "Installing extra MiKTeX packages"
		
		ClearErrors
		ExecWait '"$MikTexHome\miktex\bin\mpm.exe" --install=mptopdf'
		ExecWait '"$MikTexHome\miktex\bin\mpm.exe" --install=fancyvrb'
		
		${If} ${Errors}
			DetailPrint "Failed to install extra packages (could mean they're already installed)"
		${EndIf}

		; Turn on auto-installing of miktex packages for current user
		DetailPrint "Enabling MiKTeX's automatic package installation"
		ClearErrors
		WriteRegStr HKCU "Software\MiKTeX.org\MiKTeX\$MikTexVer\MPM" "AutoInstall" 1
		
		${If} ${Errors}
			DetailPrint "Unable to enable"
		${EndIf}
		
		; Try adding user root... is there a better way to do this?
		; Put in registry and tell tex to rebuild database
		DetailPrint "Adding R texmf root to MiKTeX"
		Call CheckInstalledR
		StrCpy $RTexmfRoot "$RHome\share\texmf"
		
		${If} $RETURN == "exists"
			; Check if there are existing user roots
			ReadRegStr $1 HKCU "Software\MiKTeX.org\MiKTeX\$MikTexVer\Core" "UserRoots"
			
			${If} $1 != ""
				; There are existing roots, are we a part of them?
				${StrStr} $0 $1 $RTexmfRoot 
				
				${If} $0 == ""
					; We aren't included yet, add us to the end
					StrCpy $1 "$1;$RTexmfRoot"
				${EndIf}
				
			${Else}
				; No current roots
				StrCpy $1 $RTexmfRoot
			${EndIf}
			
			; Write root(s) to registry and then tell miktex to refresh the filename database
			DetailPrint "Setting user roots to '$1'"
			ClearErrors
			WriteRegStr HKCU "Software\MiKTeX.org\MiKTeX\$MikTexVer\Core" "UserRoots" $1
			ExecWait '"$MikTexHome\miktex\bin\initexmf" "--update-fndb=$RTexmfRoot"'
			
			${If} ${Errors}
				DetailPrint "Failed to add R to MiKTeX's texmf roots"
				MessageBox MB_OK|MB_ICONINFORMATION "Failed to link R's Sweave files with MiKTeX. May cause problems with exporting PDFs."
			${EndIf}
			
		${Else}
			DetailPrint "R not available to configure MiKTeX with"
			MessageBox MB_OK|MB_ICONINFORMATION "Unable to locate R and register it with MiKTeX.$\nRerun the installer once R is installed or register Sweave.stf manually with MiKTeX."
		${EndIf}
		
	SectionEnd

SectionGroupEnd

SectionGroup "Extra Software" 
	
	Section "Ghostscript" InstallGhostscript
	
		AddSize 53568
		
		; First check if we have a copy in the temp folder already 
		StrCpy $GhostscriptInstaller "$TEMP\ghostscript_install.exe"
		
		${IfNot} ${FileExists} $GhostscriptInstaller
		
			; Nope, is it on the T drive?
			${IfNot} ${FileExists} ${TDRIVE_GHOSTSCRIPT}
			
				; Download
				DetailPrint "Downloading Ghostscript to $GhostscriptInstaller"
				NSISdl::download "http://mirror.cs.wisc.edu/pub/mirrors/ghost/GPL/gs902/gs902w32.exe" $GhostscriptInstaller
				Pop $0
				DetailPrint "Download result: $0"
				
				${If} $0 != "success"
					; Download failed
					DetailPrint "Ghostscript download failed"
					MessageBox MB_OK|MB_ICONEXCLAMATION "Ghostscript installer could not be downloaded.$\nTry again later or manually install."
					Abort
				${EndIf}
				
			${Else}
				; Copy from T to temp for speed and to show progress
				ClearErrors
				CopyFiles ${TDRIVE_GHOSTSCRIPT} $GhostscriptInstaller
				
				${If} ${Errors}
					DetailPrint "Failed to copy Ghostscript to temporary folder"
					StrCpy $GhostscriptInstaller ${TDRIVE_GHOSTSCRIPT}
				${EndIf}
			${EndIf}
		${EndIf}

		; Install!
		DetailPrint "Installing Ghostscript from '$GhostscriptInstaller'"
		ClearErrors
		ExecWait "$GhostscriptInstaller /S"
		
		${If} ${Errors}
			MessageBox MB_OK|MB_ICONEXCLAMATION "Failed to install Ghostscript correctly. Please install manually or retry later.$\nInstallation aborted."
			Abort
		${EndIf}
	
	SectionEnd
	
	Section "GSview" InstallGSview
	
		AddSize 53568
		
		; First check if we have a copy in the temp folder already 
		StrCpy $GSviewInstaller "$TEMP\gsview_zip.exe"
		
		${IfNot} ${FileExists} $GSviewInstaller
		
			; Nope, is it on the T drive?
			${IfNot} ${FileExists} ${TDRIVE_GSVIEW}
			
				; Download
				DetailPrint "Downloading GSview to $GSviewInstaller"
				NSISdl::download "http://mirror.cs.wisc.edu/pub/mirrors/ghost/ghostgum/gsv49w32.exe" $GSviewInstaller
				Pop $0
				DetailPrint "Download result: $0"
				
				${If} $0 != "success"
					; Download failed
					DetailPrint "GSview download failed"
					MessageBox MB_OK|MB_ICONEXCLAMATION "GSview installer could not be downloaded.$\nTry again later or manually install."
					Abort
				${EndIf}
				
			${Else}
				; Copy from T to temp for speed and to show progress
				ClearErrors
				CopyFiles ${TDRIVE_GSVIEW} $GSviewInstaller
				
				${If} ${Errors}
					DetailPrint "Failed to copy GSview to temporary folder"
					StrCpy $GSviewInstaller ${TDRIVE_GSVIEW}
				${EndIf}
			${EndIf}
		${EndIf}

		; Install!
		DetailPrint "Installing GSview from '$GSviewInstaller'"
		ClearErrors
		ExecWait "$GSviewInstaller /auto $TEMP\gsview_install.exe"
		
		${If} ${Errors}
			MessageBox MB_OK|MB_ICONEXCLAMATION "Failed to install GSview correctly. Please install manually or retry later.$\nInstallation aborted."
			Abort
		${EndIf}
	
	SectionEnd
	
	Section "Texmaker" InstallTexmaker
	
		AddSize 53568
		
		; First check if we have a copy in the temp folder already 
		StrCpy $TexmakerInstaller "$TEMP\texmaker_install.exe"
		
		${IfNot} ${FileExists} $TexmakerInstaller
		
			; Nope, is it on the T drive?
			${IfNot} ${FileExists} ${TDRIVE_TEXMAKER}
			
				; Download
				DetailPrint "Downloading Texmaker to $TexmakerInstaller"
				NSISdl::download "http://www.xm1math.net/texmaker/texmakerwin32_install.exe" $TexmakerInstaller
				Pop $0
				DetailPrint "Download result: $0"
				
				${If} $0 != "success"
					; Download failed
					DetailPrint "Texmaker download failed"
					MessageBox MB_OK|MB_ICONEXCLAMATION "Texmaker installer could not be downloaded.$\nTry again later or manually install."
					Abort
				${EndIf}
				
			${Else}
				; Copy from T to temp for speed and to show progress
				ClearErrors
				CopyFiles ${TDRIVE_TEXMAKER} $TexmakerInstaller
				
				${If} ${Errors}
					DetailPrint "Failed to copy Texmaker to temporary folder"
					StrCpy $TexmakerInstaller ${TDRIVE_TEXMAKER}
				${EndIf}
			${EndIf}
		${EndIf}

		; Install!
		DetailPrint "Installing Texmaker from '$TexmakerInstaller'"
		ClearErrors
		ExecWait "$TexmakerInstaller /S"
		
		${If} ${Errors}
			MessageBox MB_OK|MB_ICONEXCLAMATION "Failed to install Texmaker correctly. Please install manually or retry later.$\nInstallation aborted."
			Abort
		${EndIf}
	
	SectionEnd

SectionGroupEnd

Section "-configure-marla" ConfigureMarla

		SectionIn 1 2 RO

		; Configure maRla
		DetailPrint "Configuring maRla"
		
		Call CheckInstalledR
		Call CheckInstalledMikTex
		
		ClearErrors
		ExecWait '"$INSTDIR\maRlaIDE.exe" configure_only "--PrimaryOpsXML=$INSTDIR\ops.xml" "--TexTemplate=$INSTDIR\export_template.xml" "--R=$RHome\bin\R.exe" "--PdfTex=$MikTexHome\miktex\bin\pdflatex.exe"'
		
		${If} ${Errors}
			DetailPrint "Failed to configure"
			MessageBox MB_OK|MB_ICONINFORMATION "Unable to automatically configure maRla, manual configuration may be required."
		${EndIf}
		
SectionEnd

SectionGroup "Shortcuts" CreateShortcuts

	Section "Start Menu" StartShortcuts

		SectionIn 1 2
		
		CreateDirectory "$SMPROGRAMS\maRla"
		CreateShortCut "$SMPROGRAMS\maRla\Uninstall maRla.lnk" "$INSTDIR\Uninstall.exe"
		CreateShortCut "$SMPROGRAMS\maRla\maRla.lnk" "$INSTDIR\maRlaIDE.exe"
		CreateShortCut "$SMPROGRAMS\maRla\maRla Operation Editor.lnk" "$INSTDIR\maRla Operation Editor.exe"
		
	SectionEnd

	Section "Desktop" DesktopShortcut

		SectionIn 1 2

		CreateShortCut "$DESKTOP\maRla.lnk" "$INSTDIR\maRlaIDE.exe"
		
	SectionEnd

SectionGroupEnd

;--------------------------------
;Descriptions

; Language strings
LangString DESC_InstallMarla ${LANG_ENGLISH} "Installs the core maRla framework."
LangString DESC_InstallJava ${LANG_ENGLISH} "Installs Java Runtime Environment 1.6. Automatically unchecked if Java is already installed."
LangString DESC_InstallR ${LANG_ENGLISH} "Installs the statistics engine R. Required for maRla to run."
LangString DESC_InstallMiKTeX ${LANG_ENGLISH} "Installs MiKTeX. Required for maRla to run. " 
LangString DESC_ConfigureMiKTeX ${LANG_ENGLISH} "Registers R with MiKTeX and changes other MiKTeX settings. Unchecking requires R be registered with MiKTeX manually or PDF exports will not work correctly." 
LangString DESC_InstallTexmaker ${LANG_ENGLISH} "Editor for TeX files. Not required for maRla." 
LangString DESC_StartShortcuts ${LANG_ENGLISH} "Create shortcuts on Start Menu."
LangString DESC_DesktopShortcut ${LANG_ENGLISH} "Create shortcut on Desktop."

; Assign language strings to sections
!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
	!insertmacro MUI_DESCRIPTION_TEXT ${InstallJava} $(DESC_InstallJava)
	!insertmacro MUI_DESCRIPTION_TEXT ${InstallMarla} $(DESC_InstallMarla)
	!insertmacro MUI_DESCRIPTION_TEXT ${InstallR} $(DESC_InstallR)
	!insertmacro MUI_DESCRIPTION_TEXT ${InstallMiKTeX} $(DESC_InstallMiKTeX)
	!insertmacro MUI_DESCRIPTION_TEXT ${ConfigureMiKTeX} $(DESC_ConfigureMiKTeX)
	!insertmacro MUI_DESCRIPTION_TEXT ${InstallTexmaker} $(DESC_InstallTexmaker)
	!insertmacro MUI_DESCRIPTION_TEXT ${InstallGhostscript} $(DESC_InstallGhostscript)
	!insertmacro MUI_DESCRIPTION_TEXT ${InstallGSview} $(DESC_InstallGSview)
	!insertmacro MUI_DESCRIPTION_TEXT ${StartShortcuts} $(DESC_StartShortcuts)
	!insertmacro MUI_DESCRIPTION_TEXT ${DesktopShortcut} $(DESC_DesktopShortcut)
!insertmacro MUI_FUNCTION_DESCRIPTION_END

;--------------------------------
;Uninstaller Section

Section "Uninstall"

	; Ensure it's not running
	System::Call 'kernel32::OpenMutex(i 0x100000, b 0, t "themarlaproject") i .R0'
	IntCmp $R0 0 notRunning1
		System::Call 'kernel32::CloseHandle(i $R0)'
		MessageBox MB_OK|MB_ICONEXCLAMATION "The maRla Project IDE is running. Please close it first" /SD IDOK
		Abort
		
	notRunning1:
	System::Call 'kernel32::OpenMutex(i 0x100000, b 0, t "themarlaprojectopeditor") i .R0'
	IntCmp $R0 0 notRunning2
		System::Call 'kernel32::CloseHandle(i $R0)'
		MessageBox MB_OK|MB_ICONEXCLAMATION "The maRla Project Operation Editor is running. Please close it first" /SD IDOK
		Abort
		
	notRunning2:

	; Program files
	Delete "$INSTDIR\Uninstall.exe"
	Delete "$INSTDIR\maRlaIDE.exe"
	Delete "$INSTDIR\maRla Operation Editor.exe"
	
	Delete "$INSTDIR\export_template.xml"
	Delete "$INSTDIR\ops.xml"
	
	Delete "$INSTDIR\log.dat"
	
	RMDir "$INSTDIR"
	
	; Shortcuts
	Delete "$SMPROGRAMS\maRla\Uninstall.lnk"
	Delete "$SMPROGRAMS\maRla\maRla.lnk"
	Delete "$SMPROGRAMS\maRla\maRla Operation Editor.lnk"
	RMDir "$SMPROGRAMS\maRla\"
	
	Delete "$DESKTOP\maRla.lnk"

	; Registry info
	DeleteRegKey /ifempty HKCU "Software\maRla"
	${unregisterExtension} ".marla" "maRla File"

SectionEnd

Function .onInit
	Call SetSectionConfiguration
	Call .onSelChange
FunctionEnd

Function .onSelChange

	; Keep installing marla and configuring marla in sync
	${If} ${SectionIsSelected} ${InstallMarla}
		!insertmacro SelectSection ${ConfigureMarla}
		
		; Install shortcuts by default
		!insertmacro ClearSectionFlag ${StartShortcuts} ${SF_RO}
		!insertmacro ClearSectionFlag ${DesktopShortcut} ${SF_RO}
		!insertmacro SelectSection ${StartShortcuts}
		!insertmacro SelectSection ${DesktopShortcut}
		
	${Else}
		!insertmacro UnselectSection ${ConfigureMarla}
		
		; Don't install shortcuts and don't let them be added
		!insertmacro SetSectionFlag ${StartShortcuts} ${SF_RO}
		!insertmacro SetSectionFlag ${DesktopShortcut} ${SF_RO}
		!insertmacro UnselectSection ${StartShortcuts}
		!insertmacro UnselectSection ${DesktopShortcut}
		
	${EndIf}
	
	; Only enable miktex configuration if either we're installing it or we
	; were able to locate where it's installed
	${If} ${SectionIsSelected} ${InstallMiKTeX}
		; We're installing MT, so no matter what configure it
		!insertmacro SetSectionFlag ${ConfigureMiKTex} ${SF_RO}
		!insertmacro SelectSection ${ConfigureMiKTex}
		
	${Else}
		; Can we find it?
		Call CheckInstalledMikTex
		
		${If} $RETURN == "exists"
			; Found it, go ahead and select, by default, to configure it
			!insertmacro SelectSection ${ConfigureMiKTex}
			!insertmacro ClearSectionFlag ${ConfigureMiKTex} ${SF_RO}
			
		${Else}
			; Not installing and can't find it, so don't configure
			!insertmacro SetSectionFlag ${ConfigureMiKTex} ${SF_RO}
			!insertmacro UnselectSection ${ConfigureMiKTex}
		${EndIf}
	${EndIf}
	
FunctionEnd

;---------------------------------
; Utility functions

Function LaunchLink
	ExecShell "" "$INSTDIR\marlaIDE.exe"
FunctionEnd

Function SetSectionConfiguration

	Call CheckInstalledJRE
	${If} $RETURN == "install"
		!insertmacro SelectSection ${InstallJava}
	${Else}
		!insertmacro UnselectSection ${InstallJava}
	${EndIf}
	
	Call CheckInstalledR
	${If} $RETURN == "install"
		!insertmacro SelectSection ${InstallR}
	${Else}
		!insertmacro UnselectSection ${InstallR}
	${EndIf}
	
	Call CheckInstalledMikTex
	${If} $RETURN == "install"
		!insertmacro SelectSection ${InstallMikTex}
	${Else}
		!insertmacro UnselectSection ${InstallMikTex}
	${EndIf}
	
FunctionEnd

Function CheckInstalledJRE

	ClearErrors
	ReadRegStr $JavaVer HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
	ReadRegStr $JavaHome HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$JavaVer" "JavaHome"
	
	${If} ${Errors}		; Check for the JDK
		ClearErrors
		ReadRegStr $JavaVer HKLM "SOFTWARE\JavaSoft\Java Development Kit" "CurrentVersion"
		ReadRegStr $JavaHome HKLM "SOFTWARE\JavaSoft\Java Development Kit\$JavaVer" "JavaHome"
		
		DetailPrint "$JavaVer  - $JavaHome"
		
		${If} ${Errors}
			; Neither one was found
			DetailPrint "Java not found"
			StrCpy $RETURN "install"
			Return
		${EndIf}
	${EndIf}
 
	; Ensure the file exists
	${IfNot} ${FileExists} "$JavaHome\bin\java.exe"
		; Not found, need to install
		DetailPrint "Java found in registry but executable not correct"
		StrCpy $RETURN "install"
		Return
	${EndIf}

	; Ensure that the JRE found is at least our lowest compatibile version
	${If} ${JRE_VERSION} > $JavaVer
		DetailPrint "Java is out of date"
		StrCpy $RETURN "install"
	${Else}
		DetailPrint "Java is up-to-date"
		StrCpy $RETURN "exists"
	${EndIf}

FunctionEnd

Function CheckInstalledR
	
	ClearErrors
	ReadRegStr $RHome HKLM "SOFTWARE\R-Core\R" "InstallPath"
	
	${If} ${Errors}
		DetailPrint "R not found in registry, assuming not installed"
		StrCpy $RETURN "install"
		Return
	${EndIf}
 
	; Ensure the file exists
	${If} ${FileExists} "$RHome\bin\R.exe"
		DetailPrint "R found at: $RHome"
		StrCpy $RETURN "exists"
		Return
	${Else}
		; Not found, need to install
		DetailPrint "R found in registry but executable not correct"
		StrCpy $RETURN "install"
		Return
	${EndIf}

FunctionEnd

Function CheckInstalledMikTex
	
	; Try checking for current user install on Vista/7 first
	ClearErrors
	StrCpy $MikTexVer "2.9"
	; TODO determine this actual key. It's a guess!
	ReadRegStr $MikTexHome HKCU "SOFTWARE\Wow6432Node\Microsoft\Windows\CurrentVersion\Uninstall\MiKTeX $MikTexVer" "InstallLocation"
	
	${If} ${Errors}
		; 7 system install?
		ClearErrors
		ReadRegStr $MikTexHome HKLM "SOFTWARE\Wow6432Node\Microsoft\Windows\CurrentVersion\Uninstall\MiKTeX $MikTexVer" "InstallLocation"
	${EndIf}
	
	${If} ${Errors}
		; XP current user?
		ClearErrors
		ReadRegStr $MikTexHome HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\MiKTeX $MikTexVer" "InstallLocation"
	${EndIf}
	
	${If} ${Errors}
		; XP system install?
		ClearErrors
		ReadRegStr $MikTexHome HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\MiKTeX $MikTexVer" "InstallLocation"
	${EndIf}
	
	${If} ${Errors}
		; No key was found, apparently
		DetailPrint "MiKTeX not found in registry"
		StrCpy $RETURN "install"
		Return
	${EndIf}
 
	; Ensure the file exists
	${If} ${FileExists} "$MikTexHome\miktex\bin\mpm.exe"
		DetailPrint "MiKTeX found at: $MikTexHome"
		StrCpy $RETURN "exists"
		Return
	${Else}
		; Not found, need to install
		DetailPrint "MiKTeX found but executable not correct"
		StrCpy $RETURN "install"
		Return
	${EndIf}

FunctionEnd
