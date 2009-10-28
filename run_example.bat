@echo off
if "%1" == "" goto usage

call ant compile >nul 2>&1
if errorlevel 1 goto error
java -cp "build/classes;lib/cglib/cglib-nodep-2.2.jar" %1

goto end

:error
echo.
echo Compile failed, run 'ant compile' and check the error message(s)!
goto end

:usage
echo Missing argument!
echo Example usage: run_example org.parboiled.examples.calculator.Calculator

:end
