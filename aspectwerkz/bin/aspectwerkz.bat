
@REM --------------------------------------------------------------------------------------
@REM AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
@REM Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
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

set ASPECTWERKZ_VERSION=0.7.2

IF "%1"=="" goto error
IF "%ASPECTWERKZ_HOME%"=="" goto error_no_aw_home
IF "%JAVA_COMMAND%"=="" set JAVA_COMMAND=java
IF "%JAVA_HOME%"=="" goto error_no_java_home

set CP=%CLASSPATH%
IF "%CP%"=="" set CP=.

set ASPECTWERKZ_LIBS=%ASPECTWERKZ_HOME%\lib\dom4j-1.4.jar;%ASPECTWERKZ_HOME%\lib\qdox-1.2.jar;%ASPECTWERKZ_HOME%\lib\concurrent-1.3.1.jar;%ASPECTWERKZ_HOME%\lib\trove-1.0.2.jar;%ASPECTWERKZ_HOME%\lib\commons-jexl-1.0-beta-2.jar;%ASPECTWERKZ_HOME%\lib\piccolo-1.03.jar;%ASPECTWERKZ_HOME%\lib\jrexx-1.1.1.jar;%ASPECTWERKZ_HOME%\config

set OFFLINE="false"
IF "%1"=="-offline" set OFFLINE="true"

IF "%OFFLINE%"==""false"" (
    @rem -Daspectwerkz.transform.verbose=yes to turn on verbose mode
    @rem -Daspectwerkz.transform.dump=package.foo. to turn on dump in ./_dump of package.foo.* class
    "%JAVA_COMMAND%" -cp "%JAVA_HOME%\lib\tools.jar;%ASPECTWERKZ_HOME%\lib\bcel.jar;%ASPECTWERKZ_HOME%\lib\aspectwerkz-core-%ASPECTWERKZ_VERSION%.jar" org.codehaus.aspectwerkz.hook.ProcessStarter -Daspectwerkz.classloader.preprocessor=org.codehaus.aspectwerkz.transform.AspectWerkzPreProcessor -Xbootclasspath/p:"%ASPECTWERKZ_HOME%\lib\bcel-patch.jar;%ASPECTWERKZ_HOME%\lib\bcel.jar;%ASPECTWERKZ_HOME%\lib\aspectwerkz-core-%ASPECTWERKZ_VERSION%.jar" -cp "%CP%" -cp "%ASPECTWERKZ_HOME%\lib\aspectwerkz-%ASPECTWERKZ_VERSION%.jar;%ASPECTWERKZ_LIBS%" -Daspectwerkz.home="%ASPECTWERKZ_HOME%" -Daspectwerkz.transform.verbose=yes -Daspectwerkz.transform.dump=test %*
    @exit /B %ERRORLEVEL%
) ELSE (
    IF "%1"=="" goto error
    IF "%2"=="" goto error
    IF "%3"=="" goto error
    "%JAVA_COMMAND%" -Daspectwerkz.definition.file="%3" -Daspectwerkz.metadata.dir="%4" -Daspectwerkz.home="%ASPECTWERKZ_HOME%" -cp "%ASPECTWERKZ_HOME%\lib\ant-1.5.2.jar;%ASPECTWERKZ_HOME%\lib\aspectwerkz-core-%ASPECTWERKZ_VERSION%.jar;%ASPECTWERKZ_HOME%\lib\aspectwerkz-%ASPECTWERKZ_VERSION%.jar;%ASPECTWERKZ_LIBS%;%ASPECTWERKZ_HOME%\lib\bcel.jar" org.codehaus.aspectwerkz.compiler.AspectWerkzC -verbose org.codehaus.aspectwerkz.transform.AspectWerkzPreProcessor %2%
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
