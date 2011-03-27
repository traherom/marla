;maRla installer
;Written by Andrew Sterling

;--------------------------------
;Include Modern UI

!include "MUI2.nsh"
!include "LogicLib.nsh"
!include "FileAssociation.nsh"
!include "Sections.nsh"

;--------------------------------
;General

!define JRE_VERSION "1.6"
Var /GLOBAL RETURN ; Used by functions for returning values
Var /GLOBAL JavaInstaller
Var /GLOBAL RInstaller
Var /GLOBAL MikTexInstaller
Var /GLOBAL JavaVer
Var /GLOBAL JavaHome
Var /GLOBAL RHome
Var /GLOBAL MikTexVer
Var /GLOBAL MikTexHome

; Settings
SetCompress auto
SetCompressor /SOLID lzma

; Name and file
Name "maRla"
OutFile "Setup.exe"

; Default installation folder
InstallDir "$ProgramFiles\maRla"

; Get installation folder from registry if available
InstallDirRegKey HKCU "Software\maRla" ""

; Request admin privileges for Windows Vista
RequestExecutionLevel admin

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
				MessageBox MB_OK "Java installer could not be downloaded. Manually install and try again."
				Abort
			${EndIf}
		${EndIf}
				
		; Try to install
		ClearErrors
		ExecWait '"$JavaInstaller" /s REBOOT=Suppress'
		
		${If} ${Errors}
			MessageBox MB_OK "Java failed to install properly. Manually install and try again."
			Abort
		${EndIf}

	SectionEnd

	Section "Install maRla" InstallMarla

		AddSize 800
		SectionIn 1 2 RO
		
		SetOutPath "$INSTDIR"
		File ops.xml
		File export_template.xml
		File maRlaIDE.exe
		
		;Store installation folder
		WriteRegStr HKCU "Software\maRla" "" $INSTDIR
		
		;Register .marla files with maRla
		${registerExtension} "$INSTDIR\maRlaIDE.exe" ".marla" "maRla File"

		;Create uninstaller
		WriteUninstaller "$INSTDIR\Uninstall.exe"

	SectionEnd

SectionGroupEnd

Section "Include R-2.12" InstallR
	
	AddSize 67891
	SectionIn 1
	
	; First check if we have an accessible T: copy of R
	StrCpy $RInstaller "T:\TEX\CRAN\R-win.exe"
	
	${IfNot} ${FileExists} $RInstaller
		; Nope, try downloading to temp (if needed)
		StrCpy $RInstaller "$TEMP\R-win.exe"
		
		${IfNot} ${FileExists} $RInstaller
			DetailPrint "Downloading R to $RInstaller"
			NSISdl::download "http://streaming.stat.iastate.edu/CRAN/bin/windows/base/R-2.12.2-win.exe" $RInstaller
			Pop $0
			DetailPrint "Download result: $0"
			
			${If} $0 != "success"
				; Download failed
				DetailPrint "R download failed"
				MessageBox MB_OK "R installer could not be downloaded.$\nTry again later or manually install"
				Abort
			${EndIf}
		${EndIf}
	${EndIf}

	; Install!
	DetailPrint "Installing R from '$RInstaller'"
	ClearErrors
	ExecWait '$RInstaller /SILENT'
	
	${If} ${Errors}
		MessageBox MB_OK "Failed to install R correctly.$\nInstallation aborted."
		Abort
	${EndIf}

SectionEnd

SectionGroup "MiKTeX"

	Section "Install MiKTeX" InstallMiKTeX

		AddSize 382976
		SectionIn 1
		
		; First check if we have an accessible T: copy of miktex
		StrCpy $MikTexInstaller 'T:\TEX\CTAN\basic-miktex.exe'
		${IfNot} ${FileExists} $MikTexInstaller
			; Nope, try downloading to temp (if needed)
			StrCpy $MikTexInstaller "$TEMP\basic-miktex.exe"
			
			${IfNot} ${FileExists} $MikTexInstaller
				DetailPrint "Downloading MiKTeX to $MikTexInstaller"
				NSISdl::download "http://ftp.math.purdue.edu/mirrors/ctan.org/systems/win32/miktex/setup/basic-miktex.exe" $MikTexInstaller
				Pop $0
				DetailPrint "Download result: $0"
				
				${If} $0 != "success"
					; Download failed
					DetailPrint "MiKTeX download failed"
					MessageBox MB_OK "MiKTeX installer could not be downloaded.$\nTry again later or manually install"
					Abort
				${EndIf}
			${EndIf}
		${EndIf}

		; Install!
		DetailPrint "Installing MiKTeX from '$MikTexInstaller'"
		ClearErrors
		;ExecWait '$MikTexInstaller -private "-user-roots=$RHome\share\texmf" -unattended'
		ExecWait '$MikTexInstaller -private -unattended'
		
		${If} ${Errors}
			MessageBox MB_OK "Failed to install MikTex correctly$\nInstallation aborted."
			Abort
		${EndIf}

	SectionEnd
	
	Section "Configure MiKTeX" ConfigureMiKTex
		
		SectionIn 1 RO
		
		; Add extra packages that we know it'll need
		Call CheckInstalledMikTex
		DetailPrint "Installing extra MiKTeX packages"
		
		; Write out batch file to avoid a weird "this util doesn't support non-option arguments" error from mpm
		ClearErrors
		ExecWait '"$MikTexHome\miktex\bin\mpm.exe" --install=mptopdf'
		ExecWait '"$MikTexHome\miktex\bin\mpm.exe" --install=fancyvrb'
		
		${If} ${Errors}
			DetailPrint "Failed to install extra packages (could mean they're already installed)"
		${EndIf}
		
		; Try adding user root... is there a better way to do this?
		; Put in registry and tell tex to rebuild database
		DetailPrint "Adding R texmf root to MiKTeX"
		Call CheckInstalledR
		ClearErrors
		WriteRegStr HKCU "Software\MiKTeX.org\MiKTeX\$MikTexVer\Core" "UserRoots" "$RHome\share\texmf"
		ExecWait '"$MikTexHome\miktex\bin\initexmf" --update-fndb"'
		
		${If} ${Errors}
			DetailPrint "Failed to add R to MiKTeX's texmf roots"
			MessageBox MB_OK "Failed to link R's Sweave files with MiKTeX. May cause problems with exporting PDFs."
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
			MessageBox MB_OK "Unable to automatically configure maRla, manual configuration may be required."
		${EndIf}
		
SectionEnd

Section "Start Menu Shortcuts" StartShortcuts

	SectionIn 1 2
	
	CreateDirectory "$SMPROGRAMS\maRla"
	CreateShortCut "$SMPROGRAMS\maRla\Uninstall maRla.lnk" "$INSTDIR\Uninstall.exe"
	CreateShortCut "$SMPROGRAMS\maRla\maRla.lnk" "$INSTDIR\maRlaIDE.exe"
	
SectionEnd

Section "Desktop Shortcut" DesktopShortcut

	SectionIn 1 2

	CreateShortCut "$DESKTOP\maRla.lnk" "$INSTDIR\maRlaIDE.exe"
	
SectionEnd

;--------------------------------
;Descriptions

;Language strings
LangString DESC_InstallMarla ${LANG_ENGLISH} "Installs the core maRla framework."
LangString DESC_InstallJava ${LANG_ENGLISH} "Installs Java Runtime Environment if needed. Will be skipped if already installed and new enough."
LangString DESC_InstallR ${LANG_ENGLISH} "R must be installed for maRla to work properly. Unchecking requires R be installed manually."
LangString DESC_InstallMiKTeX ${LANG_ENGLISH} "MiKTeX must be installed for maRla to work properly. Unchecking requires MiKTeX be installed and configured manually." 
LangString DESC_StartShortcuts ${LANG_ENGLISH} "Create shortcuts on Start Menu."
LangString DESC_DesktopShortcut ${LANG_ENGLISH} "Create shortcut on Desktop."

;Assign language strings to sections
!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
	!insertmacro MUI_DESCRIPTION_TEXT ${InstallJava} $(DESC_InstallJava)
	!insertmacro MUI_DESCRIPTION_TEXT ${InstallMarla} $(DESC_InstallMarla)
	!insertmacro MUI_DESCRIPTION_TEXT ${InstallR} $(DESC_InstallR)
	!insertmacro MUI_DESCRIPTION_TEXT ${InstallMiKTeX} $(DESC_InstallMiKTeX)
	!insertmacro MUI_DESCRIPTION_TEXT ${StartShortcuts} $(DESC_StartShortcuts)
	!insertmacro MUI_DESCRIPTION_TEXT ${DesktopShortcut} $(DESC_DesktopShortcut)
!insertmacro MUI_FUNCTION_DESCRIPTION_END

;--------------------------------
;Uninstaller Section

Section "Uninstall"

	; Ensure it's not running
	System::Call 'kernel32::OpenMutex(i 0x100000, b 0, t "themarlaproject") i .R0'
	IntCmp $R0 0 notRunning
		System::Call 'kernel32::CloseHandle(i $R0)'
		MessageBox MB_OK|MB_ICONEXCLAMATION "The maRla Project is running. Please close it first" /SD IDOK
		Abort
	notRunning:

	Delete "$INSTDIR\Uninstall.exe"
	Delete "$INSTDIR\export_template.xml"
	Delete "$INSTDIR\maRlaIDE.exe"
	Delete "$INSTDIR\ops.xml"
	Delete "$SMPROGRAMS\maRla\Uninstall.lnk"
	Delete "$SMPROGRAMS\maRla\maRla.lnk"
	Delete "$DESKTOP\maRla.lnk"
	
	RMDir "$SMPROGRAMS\maRla"
	RMDir "$INSTDIR"

	DeleteRegKey /ifempty HKCU "Software\maRla"
	${unregisterExtension} ".marla" "maRla File"

SectionEnd

Function .onInit
	Call SetSectionConfiguration
	Call .onSelChange
FunctionEnd

Function .onSelChange

	; Keey installing marla and configuring marla in sync
	!insertmacro SectionFlagIsSet ${InstallMarla} ${SF_SELECTED} setConfMarla unsetConfMarla
	setConfMarla:
		!insertmacro SelectSection ${ConfigureMarla}
		Goto checkMT
	unsetConfMarla:
		!insertmacro UnselectSection ${ConfigureMarla}
	
	; Only enable miktex configuration if either we're installing it or we
	; were able to locate where it's installed
	checkMT:
	!insertmacro SectionFlagIsSet ${InstallMiKTeX} ${SF_SELECTED} setConfMT unsetConfMT
	setConfMT:
		; We're installing MT, so no matter what configure it
		!insertmacro SetSectionFlag ${ConfigureMiKTex} ${SF_RO}
		!insertmacro SelectSection ${ConfigureMiKTex}
		Return
	unsetConfMT:
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
	
	; Try checking for on Vista/7 first
	ClearErrors
	StrCpy $MikTexVer "2.9"
	ReadRegStr $MikTexHome HKLM "SOFTWARE\Wow6432Node\Microsoft\Windows\CurrentVersion\Uninstall\MiKTeX $MikTexVer" "InstallLocation"
	
	${If} ${Errors}
		; Hm... XP?
		ClearErrors
		ReadRegStr $MikTexHome HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\MiKTeX $MikTexVer" "InstallLocation"
	${EndIf}
	
	${If} ${Errors}
		DetailPrint "MiKTeX not found in registry, searching for in '$ProgramFiles'"
		
		ClearErrors
		FindFirst $0 $MikTexHome "$ProgramFiles\miktex*"
		FindClose $0
		
		${If} ${Errors}
			; Nothing found
			DetailPrint "MiKTeX directory not found"
			StrCpy $RETURN "install"
			Return
		${EndIf}
		
		; Done with the search, found something
		DetailPrint "Found possible MiKTeX installation at '$MikTexHome'"
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
