@echo off
chcp 65001 >nul
rem EasyChat-Rebuild 一键启动（M3：MySQL 后端）
rem 前提：target\easychat-rebuild-1.0.0.jar 已存在（改了代码才需要先重新打包，见 README）
set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.12.1"
cd /d %~dp0
if not exist "target\easychat-rebuild-1.0.0.jar" (
    echo ERROR: jar not found. Rebuild first:
    echo   E:\houduan\dev-tools\apache-maven-3.9.9\bin\mvn -s E:\houduan\dev-tools\settings.xml clean package
    pause
    exit /b 1
)
echo Starting EasyChat-Rebuild on port 18001 ^(MySQL: easychat_rebuild^)...
echo Startup takes about 2 seconds. Press Ctrl+C to stop.
echo.
"%JAVA_HOME%\bin\java.exe" -jar target\easychat-rebuild-1.0.0.jar
