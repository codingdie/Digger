@echo off
java -jar "%~dp0\..\jrebel.jar" -go-offline %1
echo.
pause
