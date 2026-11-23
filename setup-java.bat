@echo off
REM ---------------------------------------------------------------------------
REM  FindIt - one-time Java installer (Windows)
REM
REM    setup-java.bat
REM
REM  Downloads the official Eclipse Temurin JDK 17 into this project folder
REM  (.jdk) and points the launcher at it. Nothing is installed system-wide and
REM  no administrator rights are needed.
REM ---------------------------------------------------------------------------
setlocal
cd /d "%~dp0"

set JDK_DIR=%CD%\.jdk
set MARKER=%JDK_DIR%\java-home
set API=https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse

echo ==============================================
echo   FindIt - Java 17 installer
echo ==============================================

REM --- already installed? --------------------------------------------------
if exist "%MARKER%" (
  set /p LOCAL_HOME=<"%MARKER%"
  if exist "!LOCAL_HOME!\bin\java.exe" (
    echo A private JDK 17 is already installed in this project:
    echo   !LOCAL_HOME!
    "!LOCAL_HOME!\bin\java.exe" -version
    echo.
    echo Next:  run.bat
    exit /b 0
  )
)

echo Downloading Eclipse Temurin JDK 17 (about 190 MB)...
echo   %API%
echo.

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$ProgressPreference='SilentlyContinue';" ^
  "New-Item -ItemType Directory -Force -Path '%JDK_DIR%' | Out-Null;" ^
  "Invoke-WebRequest -Uri '%API%' -OutFile '%CD%\jdk17-windows.zip';" ^
  "Expand-Archive -Path '%CD%\jdk17-windows.zip' -DestinationPath '%JDK_DIR%' -Force;" ^
  "Remove-Item '%CD%\jdk17-windows.zip' -Force"

if not exist "%JDK_DIR%" (
  echo.
  echo ERROR: the download or extraction failed.
  echo        Try installing the JDK manually from
  echo        https://adoptium.net/temurin/releases/?version=17^&os=windows
  exit /b 1
)

REM --- find the folder that contains bin\java.exe ---------------------------
set JAVA_EXE=
for /f "delims=" %%f in ('dir /b /s "%JDK_DIR%\bin\java.exe" 2^>nul') do set JAVA_EXE=%%f
if "%JAVA_EXE%"=="" (
  echo ERROR: no java.exe found in the downloaded archive.
  exit /b 1
)

for %%i in ("%JAVA_EXE%") do set JAVA_HOME_DIR=%%~dpi
set JAVA_HOME_DIR=%JAVA_HOME_DIR:~0,-5%
> "%MARKER%" echo %JAVA_HOME_DIR%

echo.
echo ==============================================
echo   Java 17 is ready
echo ==============================================
"%JAVA_HOME_DIR%\bin\java.exe" -version
"%JAVA_HOME_DIR%\bin\javac.exe" -version
echo.
echo Next:  run.bat
echo.
echo The JDK lives in this project folder only, so nothing outside it changed.
endlocal
