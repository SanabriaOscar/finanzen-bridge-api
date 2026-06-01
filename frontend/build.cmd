@echo off
REM Compila el UI del bridge sin depender de "npm run" (evita bug npm+Git Bash: stdin undefined).
cd /d "%~dp0"
node "node_modules\@angular\cli\bin\ng.js" build finanzen-bridge-ui --configuration production
exit /b %ERRORLEVEL%
