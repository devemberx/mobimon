@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0setup_avd.ps1"
set "setup_exit=%ERRORLEVEL%"
if not "%setup_exit%"=="0" echo Setup failed.
pause
exit /b %setup_exit%
