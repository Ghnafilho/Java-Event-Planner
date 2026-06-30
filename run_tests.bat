@echo off

if exist out rmdir /s /q out
mkdir out

echo Compilando...

javac -cp junit-platform-console-standalone-1.8.2.jar -d out ^
planner\Main.java ^
planner\model\*.java ^
planner\controller\*.java ^
planner\exception\*.java ^
planner\view\*.java ^
planner\model\test\*.java ^
planner\controller\test\*.java

if %ERRORLEVEL% neq 0 (
    echo.
    echo Erro na compilacao.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo Executando testes...

java -jar junit-platform-console-standalone-1.8.2.jar --class-path out --scan-class-path

pause