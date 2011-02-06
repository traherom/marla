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

  CreateDirectory "$SMPROGRAMS\maRla"
  CreateShortCut "$SMPROGRAMS\maRla\Uninstall.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\uninstall.exe" 1
  CreateShortCut "$SMPROGRAMS\maRla\maRla.lnk" "$INSTDIR\maRlaIDE.jar" "" "$INSTDIR\maRlaIDE.jar" 1
  
SectionEnd

Section "Include R-2.12" InstallR

  ;installer is both 32 and 64 bit, chooses automatically, so silent workss.
  NSISdl::download http://cran.r-project.org/bin/windows/base/R-2.12.1-win.exe $TEMP\R-2.12.1-win.exe
  ExecWait '"$TEMP\R-2.12.1-win.exe" /SILENT'

SectionEnd

Section "Include MiKTeX" InstallMikTex

  NSISdl::download http://mirrors.ibiblio.org/pub/mirrors/CTAN/systems/win32/miktex/setup/setup.exe $TEMP\setup-mt.exe
  ExecWait "$TEMP\setup-mt.exe"
  ;DetailPrint "Some Program returned $0"
  
SectionEnd

Section "Install maRla" InstallMarla

  SetOutPath "$INSTDIR"
  
  ;ADD YOUR OWN FILES HERE...
  File maRlaIDE.jar
  File ops.xml
  
  ;Store installation folder
  WriteRegStr HKCU "Software\maRla" "" $INSTDIR
  
  ;Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"

SectionEnd



;--------------------------------
;Descriptions

  ;Language strings
  LangString DESC_InstallMarla ${LANG_ENGLISH} "Installs maRla"
  LangString DESC_InstallR ${LANG_ENGLISH} "Installs R from the internet. Make sure you are connected."
  LangString DESC_InstallMikTex ${LANG_ENGLISH} "Installs MiKTeX from the internet. Make sure you are connected."
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

  ;ADD YOUR OWN FILES HERE...

  Delete "$INSTDIR\Uninstall.exe"
  Delete "$INSTDIR\maRlaIDE.jar"
  Delete "$INSTDIR\ops.xml"
  Delete "$SMPROGRAMS\maRla\Uninstall.lnk"
  Delete "$SMPROGRAMS\maRla\maRla.lnk"
  
  RMDir "$SMPROGRAMS\maRla"
  RMDir "$INSTDIR"

  DeleteRegKey /ifempty HKCU "Software\maRla"

SectionEnd