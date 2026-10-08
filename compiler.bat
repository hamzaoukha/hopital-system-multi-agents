@echo off

if not exist bin mkdir bin
javac -encoding UTF-8 -cp lib\jade.jar -d bin src\util\*.java src\model\*.java src\gui\*.java src\agents\*.java src\containers\*.java
echo Compilation terminee.
pause
