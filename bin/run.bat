@echo off
rem /**
rem  * Copyright &copy; 2012-2016 51cth.com All rights reserved.
rem  *
rem  * Author: Cui
rem  */
title %cd%
echo.
echo [��Ϣ] ����  - run
echo ע�⣺��Ҫ���� how_to_build_an_executable_war.md ����
echo.

rem pause
rem echo.

cd %~dp0
cd..

call java -jar ./target/jp-logger-1.1.0.Release-war-exec.jar

cd bin
pause