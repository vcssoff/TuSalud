@echo off
cd /d "%~dp0"
title TuSalud
echo Iniciando aplicacion TuSalud...
start "" "C:\Program Files\Java\jdk-21.0.10\bin\javaw.exe" -jar "%~dp0target\TuSalud-1.0-SNAPSHOT-jar-with-dependencies.jar"
exit
