;maRla installer
;Written by Andrew Sterling


;--------------------------------
;Include Modern UI

  !include "MUI2.nsh"
  !include "FileAssociation.nsh"

;--------------------------------
;General

  !define JRE_VERSION "1.6"
  Var /GLOBAL InstallJRE
  Var /GLOBAL JavaInstaller
  Var /GLOBAL RInstaller
  Var /GLOBAL R_LOC
  Var /GLOBAL texInstaller
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

  !define MUI_INSTFILESPAGE_FINISHHEADER_TEXT "Java installation complete"
  !define MUI_PAGE_HEADER_TEXT "Installing Java runtime"
  !define MUI_PAGE_HEADER_SUBTEXT "Please wait while we install the Java runtime"
  !define MUI_INSTFILESPAGE_FINISHHEADER_SUBTEXT "Java runtime installed successfully."
  
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
  StrCmp $InstallJRE "yes" JREInstaller JREEnd

JREInstaller:
  StrCpy $JavaInstaller "$TEMP\jre_setup.exe"
  IfFileExists $JavaInstaller JavaInstall ContinueJavaDL
  ContinueJavaDL:
    DetailPrint "Downloading Java to $JavaInstaller"
	inetc::get "http://javadl.sun.com/webapps/download/AutoDL?BundleId=33787" $JavaInstaller /END

  JavaInstall:
    ExecWait '"$JavaInstaller" /s /v\"/qn REBOOT=Suppress JAVAUPDATE=0 WEBSTARTICON=0\"' $0

JREEnd:
  DetailPrint "Java is up to date"

SectionEnd

Section "Install maRla" InstallMarla

  SetOutPath "$INSTDIR"
  File maRlaIDE.jar
  File ops.xml
  File export_template.xml
  File maRlaIDE.exe
  
  ;Store installation folder
  WriteRegStr HKCU "Software\maRla" "" $INSTDIR

  ;Configure maRla
  DetailPrint "Configuring maRla"
  ExecWait 'java -classpath "$INSTDIR\maRlaIDE.jar" marla.ide.resource.Configuration "--PrimaryOpsXML=$INSTDIR\ops.xml" "--TexTemplate=$INSTDIR\export_template.xml"'
  
  ;Register .marla files with maRla
  ${registerExtension} "$INSTDIR\maRlaIDE.exe" ".marla" "maRla File"

  ;Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"

SectionEnd

Section "Include R-2.12" InstallR
  
  ;installer is both 32 and 64 bit, chooses automatically, so silent works.
  IfFileExists 'T:\TEX\CRAN\R-win.exe' RDrive DownloadR
  DownloadR:
	StrCpy $RInstaller "$TEMP\R-win.exe"
	IfFileExists $RInstaller RInstall ContinueRDL
	ContinueRDL:
      DetailPrint "Downloading R to $RInstaller"
      NSISdl::download "http://cran.r-project.org/bin/windows/base/R-2.12.2-win.exe" $RInstaller
      Goto RInstall

  RDrive:
    DetailPrint "Using R from T drive"
	StrCpy $RInstaller "T:\TEX\CRAN\R-win.exe"

  RInstall:
    ExecWait '$RInstaller /SILENT'
  DetailPrint "Installed R"

SectionEnd

Section "Include MiKTeX" InstallMiKTeX

  ; register R's texmf directory with MiKTeK
  ReadRegStr $R_LOC HKLM Software\R-Core\R InstallPath
  DetailPrint "R is installed at: $R_LOC"

  IfFileExists 'T:\TEX\CTAN\basic-miktex.exe' MiKTeXFromDrive DownloadMiKTeX
  DownloadMiKTeX:
    StrCpy $texInstaller "$TEMP\miktex-install.exe"
	IfFileExists $texInstaller MiKTeXInstall ContinueMiKTeXDL
	ContinueMiKTeXDL:
      DetailPrint "Downloading MiKTeX to $texInstaller"
      NSISdl::download "http://ftp.math.purdue.edu/mirrors/ctan.org/systems/win32/miktex/setup/basic-miktex.exe" $texInstaller
      Goto MiKTeXInstall

  MiKTeXFromDrive:
    DetailPrint "Using MiKTeX from T drive"
    StrCpy $texInstaller "T:\TEX\CTAN\basic-miktex.exe"

  MiKTeXInstall:
  ExecWait '$texInstaller -private "-user-roots=$R_LOC\share\texmf" "-user-install=$ProgramFiles\miktex" -unattended'
  DetailPrint "MiKTeX installed"

  ;ExecWait '$ProgramFiles\miktex\miktex\bin\mpm.exe '
  ;DetailPrint "Enabled automatic package installation on MiKTeX"

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
  LangString DESC_InstallMarla ${LANG_ENGLISH} "Installs the core maRla framework"
  LangString DESC_InstallR ${LANG_ENGLISH} "(~37 mb) R must be installed for maRla to work properly; only uncheck this option if you are certain you already have R installed."
  LangString DESC_InstallMiKTeX ${LANG_ENGLISH} "(~138 mb) MiKTeX must be installed for maRla to work properly; only uncheck this option if you are certain you already have R installed." 
  LangString DESC_StartShortcuts ${LANG_ENGLISH} "Install shortcuts to your Start Menu."
  LangString DESC_DesktopShortcut ${LANG_ENGLISH} "Install shortcut to your Desktop."

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
  Delete "$INSTDIR\maRlaIDE.jar"
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
  ${If} $JavaVer >= JRE_VERSION
	StrCpy $InstallJRE "yes"
  ${Else}
    StrCpy $InstallJRE "no"
	DetailPrint "Java requires an update..."
  ${EndIf}

DetectJREEnd:
FunctionEnd
