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

  ;Request application privileges for Windows Vista
  RequestExecutionLevel user

;--------------------------------
;Interface Settings

  !define MUI_ABORTWARNING

;--------------------------------
;Pages

  !insertmacro MUI_PAGE_LICENSE "gpl-3.0.txt"
  !insertmacro MUI_PAGE_COMPONENTS
  !insertmacro MUI_PAGE_DIRECTORY
  !insertmacro MUI_PAGE_INSTFILES
  
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES
  
;--------------------------------
;Languages
 
  !insertmacro MUI_LANGUAGE "English"

;--------------------------------
;Installer Sections


Section "Start Menu Shortcuts" StartShortcuts

  SetOutPath "$INSTDIR"
  CreateDirectory "$SMPROGRAMS\maRla"
  CreateShortCut "$SMPROGRAMS\maRla\Uninstall.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\uninstall.exe" 1
  CreateShortCut "$SMPROGRAMS\maRla\maRla.lnk" "$INSTDIR\maRlaIDE.exe" "" "$INSTDIR\maRla.ico" 1
  
SectionEnd

Section "Include R-2.12" InstallR

  ;installer is both 32 and 64 bit, chooses automatically, so silent workss.
  NSISdl::download http://cran.r-project.org/bin/windows/base/release.htm $TEMP\R-win.exe
  ExecWait "'$TEMP\R-win.exe' /SILENT"
  
  DetailPrint "R installed"

SectionEnd

Section "Include MiKTeX" InstallMikTex

  ; register R's texmf directory with MiKTeK
  Var /GLOBAL R_LOC
  ReadRegStr $R_LOC HKLM Software\R-Core\R InstallPath
  DetailPrint "R is installed at: $R_LOC"
  
  NSISdl::download http://mirrors.ibiblio.org/pub/mirrors/CTAN/systems/win32/miktex/setup/basic-miktex.exe $TEMP\miktex-install.exe
  ExecWait "$TEMP\miktex-install.exe -shared -common-roots=$R_LOC\share\texmf"
  
  DetailPrint "MiKTeX installed"

SectionEnd

Section "Install maRla" InstallMarla

  SetOutPath "$INSTDIR"
  File maRlaIDE.jar
  File ops.xml
  File export_template.xml
  File maRla.ico
  File maRlaIDE.exe
  
  ;Store installation folder
  WriteRegStr HKCU "Software\maRla" "" $INSTDIR
  
  ;Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"

SectionEnd



;--------------------------------
;Descriptions

  ;Language strings
  LangString DESC_InstallMarla ${LANG_ENGLISH} "Installs maRla"
  LangString DESC_InstallR ${LANG_ENGLISH} "(~37 mb) Only select this if you do not already have R installed. R is the statistical engine needed to run operations."
  LangString DESC_InstallMikTex ${LANG_ENGLISH} "(~138 mb) Only select this if you do not already have MikTex or an equivalent LaTeX editor installed." 
  LangString DESC_StartShortcuts ${LANG_ENGLISH} "Installs start menu shortcuts to the user's account."

  ;Assign language strings to sections
  !insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
    !insertmacro MUI_DESCRIPTION_TEXT ${InstallMarla} $(DESC_InstallMarla)
	!insertmacro MUI_DESCRIPTION_TEXT ${InstallR} $(DESC_InstallR)
	!insertmacro MUI_DESCRIPTION_TEXT ${InstallMikTex} $(DESC_InstallMikTex)
	!insertmacro MUI_DESCRIPTION_TEXT ${StartShortcuts} $(DESC_StartShortcuts)
  !insertmacro MUI_FUNCTION_DESCRIPTION_END

;--------------------------------
;Uninstaller Section

Section "Uninstall"

  Delete "$INSTDIR\Uninstall.exe"
  Delete "$INSTDIR\maRlaIDE.jar"
  Delete "$INSTDIR\export_template.xml"
  Delete "$INSTDIR\maRla.ico"
  Delete "$INSTDIR\maRlaIDE.exe"
  Delete "$INSTDIR\ops.xml"
  Delete "$SMPROGRAMS\maRla\Uninstall.lnk"
  Delete "$SMPROGRAMS\maRla\maRla.lnk"
  
  RMDir "$SMPROGRAMS\maRla"
  RMDir "$INSTDIR"

  DeleteRegKey /ifempty HKCU "Software\maRla"

SectionEnd