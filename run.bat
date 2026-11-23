@echo off
REM ---------------------------------------------------------------------------
REM  FindIt - Campus Lost & Found System
REM  One-command launcher for Segment 1 (no database needed).
REM
REM    run.bat          -> start the app with demo data
REM    run.bat --mysql  -> start the app against MySQL
REM
REM  Requirements: JDK 17 or newer.  Nothing else - no Maven, no MySQL.
REM ---------------------------------------------------------------------------
setlocal
cd /d "%~dp0"

echo ==============================================
echo   FindIt - Campus Lost ^& Found Management
echo ==============================================

where java >nul 2>nul
if errorlevel 1 (
  echo ERROR: java was not found on your PATH.
  echo        Install JDK 17 or newer: https://adoptium.net/
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

set MODE=--memory --demo
if "%1"=="--mysql" (
  set MODE=
  echo Starting with MySQL ^(check app.properties for the credentials^)...
) else (
  echo Starting in demo mode - no database required.
  echo Login: admin@findit.edu / Admin@1234   ^(ADMIN^)
  echo    or: ayesha@buet.edu / Student@123   ^(STUDENT^)
)

java -cp target\classes com.bubt.findit.Main %MODE%
endlocal
