@echo off
setlocal

set "JFREE_JAR=lib\jfreechart-1.5.4.jar"
set "CP=.;%JFREE_JAR%"

if not exist "lib" mkdir "lib"

if not exist "%JFREE_JAR%" (
  echo Downloading JFreeChart version 1.5.4...
  powershell -NoProfile -Command "try { Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/jfree/jfreechart/1.5.4/jfreechart-1.5.4.jar' -OutFile '%JFREE_JAR%' } catch { exit 1 }"
  if errorlevel 1 goto dl_fail
)
)

echo Compiling sources...
javac -cp "%CP%" *.java
if errorlevel 1 (
  echo Build failed. See errors above.
  pause
  exit /b 1
)

set "MAIN=GamifiedPortfolioGUI"
if /i "%1"=="classic" set "MAIN=PortfolioTrackerGUI"

echo Launching %MAIN%...
start "" javaw -cp "%CP%" %MAIN%

goto end

:dl_fail
echo Failed to download dependency.
pause
exit /b 1

:end
endlocal
