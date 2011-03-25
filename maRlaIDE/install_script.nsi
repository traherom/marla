;maRla installer
;Written by Andrew Sterling


;--------------------------------
;Include Modern UI

  !include "MUI2.nsh"

;--------------------------------
;General

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
  
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES
  
;--------------------------------
;Languages
 
  !insertmacro MUI_LANGUAGE "English"

;--------------------------------
;Installer Sections

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

  ;Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"

SectionEnd

Section "Include R-2.12" InstallR

  Var /GLOBAL RInstaller
  
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
  Var /GLOBAL R_LOC
  ReadRegStr $R_LOC HKLM Software\R-Core\R InstallPath
  DetailPrint "R is installed at: $R_LOC"

  Var /GLOBAL texInstaller

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
	!insertmacro MUI_DESCRIPTION_TEXT ${DesktopShortcuts} $(DESC_DesktopShortcut)
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

SectionEnd

Function LaunchLink
  ExecShell "" "$INSTDIR\marlaIDE.exe"
FunctionEnd
