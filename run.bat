@echo off
REM ---------------------------------------------------------------------------
REM  FindIt - Campus Lost & Found System
REM  One-command launcher for Segment 1 (no database needed).
REM
REM    run.bat          -> start the app in memory mode (no database needed)
REM    run.bat --mysql  -> start the app against MySQL
REM
REM  Requirements: JDK 17 or newer.  Nothing else - no Maven, no MySQL.
REM ---------------------------------------------------------------------------
setlocal EnableDelayedExpansion
cd /d "%~dp0"

echo ==============================================
echo   FindIt - Campus Lost ^& Found Management
echo ==============================================

REM --- use the project-local JDK if setup-java.bat installed one -------------
if exist "%CD%\.jdk\java-home" (
  set /p LOCAL_HOME=<"%CD%\.jdk\java-home"
  if exist "!LOCAL_HOME!\bin\java.exe" (
    set JAVA_HOME=!LOCAL_HOME!
    set PATH=!JAVA_HOME!\bin;!PATH!
  )
)

where java >nul 2>nul
if errorlevel 1 (
  echo.
  echo ERROR: Java was not found on this computer.
  echo.
  echo   FindIt is a Java application, so it needs the JDK 17 or newer.
  echo   It is free and takes about 3 minutes to install:
  echo.
  echo   1. open https://adoptium.net/temurin/releases/?version=17^&os=windows
  echo   2. pick "Windows" + "x64" + "JDK" + the .msi installer
  echo   3. open the downloaded file and click through the installer
  echo   4. close and reopen this window, then double-click run.bat again
  echo.
  echo   Easiest - let the project install it for you (no admin rights):
  echo       setup-java.bat
  echo.
  echo   After installing, check it with:  java -version
  echo.
  exit /b 1
)
for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr "version"') do set JAVA_MAJOR=%%v
set JAVA_MAJOR=%JAVA_MAJOR:~0,2%
if %JAVA_MAJOR% LSS 17 (
  echo ERROR: Java 17 or newer is required.
  exit /b 1
)
echo Java:  %JAVA_MAJOR%

echo Compiling...
if not exist target\classes mkdir target\classes
dir /s /b src\main\java\*.java > target\sources.txt
javac -encoding UTF-8 -d target\classes @target\sources.txt
if errorlevel 1 (
  echo Compilation failed.
  exit /b 1
)
xcopy /E /Y /I src\main\resources target\classes >nul
echo Compiled.

REM The MySQL driver lives in lib\ (fetched by setup-java.bat) - put it on the
REM classpath when it is there.
set CP=target\classes
if exist lib\mysql-connector-j-8.4.0.jar set CP=target\classes;lib\*

set MODE=--memory
if "%1"=="--mysql" (
  set MODE=
  echo Starting with MySQL ^(check app.properties for the credentials^)...
) else (
  echo Starting in memory mode - no database required.
  echo Login: admin@findit.edu / Admin@1234   ^(ADMIN^)
  echo    or: ayesha@buet.edu / Student@123   ^(STUDENT^)
)

java -cp %CP% com.bubt.findit.Main %MODE%
endlocal
