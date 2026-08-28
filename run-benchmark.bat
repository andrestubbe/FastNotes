@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo ========================================================================================================================
echo   Building FastNotes & JMH Benchmarks...
echo ========================================================================================================================

where mvn >nul 2>nul
if %errorlevel% equ 0 (
    set "MVN_CMD=mvn"
) else if exist "C:\Users\andre\tools\apache-maven-3.9.9\bin\mvn.cmd" (
    set "MVN_CMD=C:\Users\andre\tools\apache-maven-3.9.9\bin\mvn.cmd"
) else (
    echo [ERROR] Maven not found.
    pause
    exit /b 1
)

echo Building Main Project...
call %MVN_CMD% clean install -DskipTests -q

echo Building Benchmark Uber-JAR...
cd examples\Benchmark
call %MVN_CMD% clean package -DskipTests -q

echo Running JMH Benchmarks...
java -jar target\benchmarks.jar -f 1 -i 3 -wi 2 -w 1s -r 1s

cd ..\..
pause
