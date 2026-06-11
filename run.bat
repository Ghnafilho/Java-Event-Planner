@echo off

if exist out rmdir /s /q out

javac -d out planner\Main.java planner\model\*.java planner\controller\*.java planner\exception\*.java planner\view\*.java

if %ERRORLEVEL% neq 0 exit /b %ERRORLEVEL%

java -cp out planner.Main