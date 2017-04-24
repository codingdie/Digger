@echo off
IF "%~1" == "" (
    echo Usage: %0 ^<file^>^|^[^<URL^> ^<EMAIL^>^]^|^<license-code^>
    goto:eof
)
java -jar "%~dp0\..\jrebel.jar" -activate %*
echo.
pause
