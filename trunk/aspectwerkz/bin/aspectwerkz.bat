
@REM --------------------------------------------------------------------------------------
@REM AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
@REM Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
@REM
@REM Script is based on the startup script for JMangler
@REM
@REM This library is free software; you can redistribute it and/or
@REM modify it under the terms of the GNU Lesser General Public
@REM License as published by the Free Software Foundation; either
@REM version 2.1 of the License, or (at your option) any later version.
@REM
@REM This library is distributed in the hope that it will be useful,
@REM but WITHOUT ANY WARRANTY; without even the implied warranty of
@REM MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
@REM Lesser General Public License for more details.
@REM
@REM You should have received a copy of the GNU Lesser General Public
@REM License along with this library; if not, write to the Free Software
@REM Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
@REM --------------------------------------------------------------------------------------

@ECHO OFF

set ASPECTWERKZ_VERSION=0.6.3
set TRANSFORMATION__ALGORITHM=jmangler-order.config

IF "%1"=="" goto error
IF "%ASPECTWERKZ_HOME%"=="" goto error_no_aw_home
IF "%JAVA_COMMAND%"=="" set JAVA_COMMAND=java
IF "%JAVA_HOME%"=="" goto error_no_java_home

set CP=%CLASSPATH%
IF "%CP%"=="" set CP=.

set ASPECTWERKZ_LIBS=%ASPECTWERKZ_HOME%\lib\dom4j-1.4.jar;%ASPECTWERKZ_HOME%\lib\qdox-1.2.jar;%ASPECTWERKZ_HOME%\lib\concurrent-1.3.1.jar;%ASPECTWERKZ_HOME%\lib\trove-1.0.2.jar;%ASPECTWERKZ_HOME%\lib\prevayler-2.00.000dev1.jar;%ASPECTWERKZ_HOME%\lib\commons-jexl-1.0-beta-2.jar;%ASPECTWERKZ_HOME%\config

set OFFLINE="false"
IF "%1"=="-offline" set OFFLINE="true"

IF "%OFFLINE%"==""false"" (
    "%JAVA_COMMAND%" -cp "%ASPECTWERKZ_HOME%\lib\jmangler-core.jar;%JAVA_HOME%\lib\tools.jar;%ASPECTWERKZ_HOME%\lib\bcel.jar" org.cs3.jmangler.hook.starter.CLSubstitutor -cp "%CP%" --jh "%ASPECTWERKZ_HOME%" --cf "%ASPECTWERKZ_HOME%\config\aspectwerkz.conf" --tcp "%ASPECTWERKZ_HOME%\lib\aspectwerkz-%ASPECTWERKZ_VERSION%.jar;%ASPECTWERKZ_LIBS%" -Daspectwerkz.home="%ASPECTWERKZ_HOME%" %*
    @exit /B %ERRORLEVEL%
) ELSE (
    IF "%1"=="" goto error
    IF "%2"=="" goto error
    IF "%3"=="" goto error
    "%JAVA_COMMAND%" -Daspectwerkz.definition.file="%3" -Daspectwerkz.metadata.dir="%4" -Daspectwerkz.home="%ASPECTWERKZ_HOME%" -Dorg.cs3.jmangler.initfile="%ASPECTWERKZ_HOME%\config\%TRANSFORMATION__ALGORITHM%" -cp "%ASPECTWERKZ_HOME%\lib\aspectwerkz-%ASPECTWERKZ_VERSION%.jar;%ASPECTWERKZ_LIBS%;%ASPECTWERKZ_HOME%\lib\jmangler-core.jar;%ASPECTWERKZ_HOME%\lib\bcel.jar" org.cs3.jmangler.offline.starter.Main --cp "%2" --tcp "%ASPECTWERKZ_HOME%\lib\aspectwerkz-%ASPECTWERKZ_VERSION%.jar" --cf "%ASPECTWERKZ_HOME%\config\aspectwerkz.conf"
    @exit /B %ERRORLEVEL%
)

:error
    IF EXIST "%ASPECTWERKZ_HOME%\config\usage.txt" (
        type "%ASPECTWERKZ_HOME%\config\usage.txt"
    ) ELSE (
        echo ASPECTWERKZ_HOME does not point to the aspectwerkz directory
    )
@goto error_exit

:error_no_java_home
	@echo Please specify the JAVA_HOME environment variable.
@goto error_exit

:error_no_aw_home
	@echo Please specify the ASPECTWERKZ_HOME environment variable.
@goto error_exit

:error_exit
@exit /B -1
