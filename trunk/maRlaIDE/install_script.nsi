;maRla installer
;Written by Andrew Sterling

;--------------------------------
;Include Modern UI

	!include "MUI2.nsh"
	!include "LogicLib.nsh"
	!include "FileAssociation.nsh"

;--------------------------------
;General

	!define JRE_VERSION "1.6"
	Var /GLOBAL InstallJRE
	Var /GLOBAL JavaInstaller
	Var /GLOBAL RInstaller
	Var /GLOBAL R_LOC
	Var /GLOBAL MikTexInstaller
	Var /GLOBAL JavaVer
	Var /GLOBAL JavaHome

	;Name and file
	Name "maRla"
	OutFile "Setup.exe"

	;Default installation folder
	InstallDir "$ProgramFiles\maRla"
	
	;Get installation folder from registry if available
	InstallDirRegKey HKCU "Software\maRla" ""

	;Request admin privileges for Windows Vista
	RequestExecutionLevel admin

;--------------------------------
;Interface Settings

	!define MUI_ABORTWARNING

;--------------------------------
;Pages

	!define MUI_INSTFILESPAGE_FINISHHEADER_TEXT "The maRla Project Installation Complete"
	!define MUI_PAGE_HEADER_TEXT "Installing Java runtime"
	!define MUI_PAGE_HEADER_SUBTEXT "Please wait while we install the Java runtime"
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

Section -installjre jre

	Call CheckInstalledJRE
	${If} $InstallJRE == "yes"
		; Need to install
		DetailPrint "Java requires an update..."
		
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
		ExecWait '"$JavaInstaller" /S REBOOT=Suppress JAVAUPDATE=0 WEBSTARTICON=0 /L \"$TEMP\jre_setup.log\"'
		${If} ${Errors}
			MessageBox MB_OK "Java failed to install properly. Manually install and try again."
			Abort
		${EndIf}
		
	${Else}
		; Just informative
		DetailPrint "Java is up-to-date"
	${EndIf}

SectionEnd

Section "Install maRla" InstallMarla

	SetOutPath "$INSTDIR"
	File ops.xml
	File export_template.xml
	File maRlaIDE.exe
	
	;Configure maRla
	DetailPrint "Configuring maRla"
	ClearErrors
	ExecWait '"$INSTDIR\maRlaIDE.exe" configure_only "--PrimaryOpsXML=$INSTDIR\ops.xml" "--TexTemplate=$INSTDIR\export_template.xml"'
	${If} ${Errors}
		DetailPrint "Failed to configure"
		MessageBox MB_OK "Unable to automatically configure maRla, manual configuration may be required"
	${EndIf}
	
	;Store installation folder
	WriteRegStr HKCU "Software\maRla" "" $INSTDIR
	
	;Register .marla files with maRla
	${registerExtension} "$INSTDIR\maRlaIDE.exe" ".marla" "maRla File"

	;Create uninstaller
	WriteUninstaller "$INSTDIR\Uninstall.exe"

SectionEnd

Section "Include R-2.12" InstallR
		
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

Section "Include MiKTeX" InstallMiKTeX

	; Save the R install location so we can register its texmf directory
	ReadRegStr $R_LOC HKLM Software\R-Core\R InstallPath
	DetailPrint "R is installed at: $R_LOC"

	; First check if we have an accessible T: copy of R
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
	ExecWait '$MikTexInstaller -private "-user-roots=$R_LOC\share\texmf" "-user-install=$ProgramFiles\miktex" -unattended'
	${If} ${Errors}
		MessageBox MB_OK "Failed to install MikTex correctly$\nInstallation aborted."
		Abort
	${EndIf}

SectionEnd

Section "Start Menu Shortcuts" StartShortcuts

	CreateDirectory "$SMPROGRAMS\maRla"
	CreateShortCut "$SMPROGRAMS\maRla\Uninstall maRla.lnk" "$INSTDIR\Uninstall.exe"
	CreateShortCut "$SMPROGRAMS\maRla\maRla.lnk" "$INSTDIR\maRlaIDE.exe"
	
SectionEnd

Section "Desktop Shortcut" DesktopShortcut

	CreateShortCut "$DESKTOP\maRla.lnk" "$INSTDIR\maRlaIDE.exe"
	
SectionEnd

;--------------------------------
;Descriptions

	;Language strings
	LangString DESC_InstallMarla ${LANG_ENGLISH} "Installs the core maRla framework."
	LangString DESC_InstallR ${LANG_ENGLISH} "(~37 mb) Install R. R must be installed for maRla to work properly--only uncheck this option if you are certain you already have R installed."
	LangString DESC_InstallMiKTeX ${LANG_ENGLISH} "(~138 mb) Install MiKTeX. MiKTeX must be installed for maRla to work properly--only uncheck this option if you are certain you already have R installed." 
	LangString DESC_StartShortcuts ${LANG_ENGLISH} "Create shortcuts to your Start Menu."
	LangString DESC_DesktopShortcut ${LANG_ENGLISH} "Create shortcut to your Desktop."

	;Assign language strings to sections
	!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
		!insertmacro MUI_DESCRIPTION_TEXT ${InstallMarla} $(DESC_InstallMarla)
		!insertmacro MUI_DESCRIPTION_TEXT ${InstallR} $(DESC_InstallR)
		!insertmacro MUI_DESCRIPTION_TEXT ${InstallMiKTeX} $(DESC_InstallMiKTeX)
		!insertmacro MUI_DESCRIPTION_TEXT ${StartShortcuts} $(DESC_StartShortcuts)
		!insertmacro MUI_DESCRIPTION_TEXT ${DesktopShortcut} $(DESC_DesktopShortcut)
	!insertmacro MUI_FUNCTION_DESCRIPTION_END

;--------------------------------
;Uninstaller Section

Section "Uninstall"

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

; Utility functions

Function LaunchLink
	ExecShell "" "$INSTDIR\marlaIDE.exe"
FunctionEnd

Function CheckInstalledJRE
	DetailPrint "Detecting Java..."
	ClearErrors
	ReadRegStr $JavaVer HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
	ReadRegStr $JavaHome HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$JavaVer" "JavaHome"
	IfErrors 0 GetJRE
	
	;Otherwise check for the JDK
	ClearErrors
	ReadRegStr $JavaVer HKLM "SOFTWARE\JavaSoft\Java Development Kit" "CurrentVersion"
	ReadRegStr $JavaHome HKLM "SOFTWARE\JavaSoft\Java Development Kit\$JavaVer" "JavaHome"
	IfErrors 0 GetJRE
	Goto NoJREFound
 
GetJRE:
	IfFileExists "$JavaHome\bin\java.exe" FoundJRE NoJREFound

NoJREFound:
	StrCpy $InstallJRE "yes"
	Goto DetectJREEnd

FoundJRE:
	;Ensure that the JRE found is at least our lowest compatibile version
	${If} ${JRE_VERSION} > $JavaVer
		StrCpy $InstallJRE "yes"
	${Else}
		StrCpy $InstallJRE "no"
	${EndIf}

DetectJREEnd:

FunctionEnd
