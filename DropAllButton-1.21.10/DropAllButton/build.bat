@echo off
setlocal
cd /d "%~dp0"
title Drop All Button - Build
echo ==========================================
echo  Drop All Button - Build (Minecraft 1.21.10)
echo ==========================================
echo.
where java >nul 2>nul
if errorlevel 1 (
  echo [FEHLER] Java wurde nicht gefunden.
  echo Bitte JDK 21 installieren: https://adoptium.net/
  echo Danach dieses Fenster schliessen und build.bat erneut starten.
  pause
  exit /b 1
)
echo Gefundene Java-Version:
java -version
echo.
echo Der erste Build laedt Minecraft und Bibliotheken herunter (Internet noetig, kann einige Minuten dauern).
echo.
call gradlew.bat build
if errorlevel 1 (
  echo.
  echo [FEHLER] Der Build ist fehlgeschlagen. Siehe Meldungen oben.
  echo Haeufige Ursache: Java ist nicht Version 21 oder neuer. Siehe README.md.
  pause
  exit /b 1
)
if not exist fertig mkdir fertig
copy /y build\libs\dropall-*.jar fertig\ >nul
del /q fertig\*-sources.jar >nul 2>nul
echo.
echo ==========================================
echo  FERTIG! Die JAR-Datei liegt im Ordner "fertig".
echo  Kopiere sie nach: %%APPDATA%%\.minecraft\mods
echo ==========================================
explorer fertig
pause
