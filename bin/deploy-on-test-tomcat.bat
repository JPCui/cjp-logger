@echo off
rem /**
rem  * Copyright &copy; 2012-2016 51cth.com All rights reserved.
rem  *
rem  * Author: Cui
rem  */
title %cd%
echo.
echo [��Ϣ] ʹ��Tomcat7������й��̡�
echo.
set /p a=ȷ�ϲ���(Y / N)
if %a%==N exit

rem pause
rem echo.

cd %~dp0
cd..

call mvn clean compile tomcat7:redeploy -Dmaven.test.skip=true -PonTest
pause

cd bin