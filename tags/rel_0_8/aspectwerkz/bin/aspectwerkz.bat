
@REM ----------------------------------------------------------------------------------
@REM Copyright (c) The AspectWerkz Team. All rights reserved.
@REM http://aspectwerkz.codehaus.org
@REM ----------------------------------------------------------------------------------
@REM The software in this package is published under the terms of the BSD style license
@REM a copy of which has been included with this distribution in the license.txt file.
@REM ----------------------------------------------------------------------------------

@ECHO OFF
set ASPECTWERKZ_VERSION=0.8

IF "%1"=="" goto error
IF "%ASPECTWERKZ_HOME%"=="" goto error_no_aw_home
IF "%JAVA_COMMAND%"=="" set JAVA_COMMAND=%JAVA_HOME%\bin\java
IF "%JAVA_HOME%"=="" goto error_no_java_home

set CP=%CLASSPATH%
IF "%CP%"=="" set CP=.
IF "%CP%"=="" set CP=.

set ASPECTWERKZ_LIBS=%ASPECTWERKZ_HOME%\lib\dom4j-1.4.jar;%ASPECTWERKZ_HOME%\lib\qdox-1.2.jar;%ASPECTWERKZ_HOME%\lib\concurrent-1.3.1.jar;%ASPECTWERKZ_HOME%\lib\trove-1.0.2.jar;%ASPECTWERKZ_HOME%\lib\commons-jexl-1.0-beta-2.jar;%ASPECTWERKZ_HOME%\lib\piccolo-1.03.jar;%ASPECTWERKZ_HOME%\lib\jrexx-1.1.1.jar

set OFFLINE="false"
IF "%1"=="-offline" set OFFLINE="true"

IF "%OFFLINE%"==""false"" (
    @rem -Daspectwerkz.transform.verbose=yes to turn on verbose mode
    @rem -Daspectwerkz.transform.dump=package.foo. to turn on dump in ./_dump of package.foo.* class
    "%JAVA_COMMAND%" -cp "%JAVA_HOME%\lib\tools.jar;%ASPECTWERKZ_HOME%\lib\bcel-patch.jar;%ASPECTWERKZ_HOME%\lib\bcel.jar;%ASPECTWERKZ_HOME%\lib\aspectwerkz-core-%ASPECTWERKZ_VERSION%.jar" org.codehaus.aspectwerkz.hook.ProcessStarter -Xbootclasspath/p:"%ASPECTWERKZ_HOME%\lib\bcel-patch.jar;%ASPECTWERKZ_HOME%\lib\bcel.jar;%ASPECTWERKZ_HOME%\lib\aspectwerkz-core-%ASPECTWERKZ_VERSION%.jar" -cp "%CP%" -cp "%ASPECTWERKZ_HOME%\lib\aspectwerkz-%ASPECTWERKZ_VERSION%.jar;%ASPECTWERKZ_LIBS%" -Daspectwerkz.home="%ASPECTWERKZ_HOME%" %*
    @exit /B %ERRORLEVEL%
) ELSE (
    IF "%1"=="" goto error
    IF "%2"=="" goto error
    IF "%3"=="" goto error
    "%JAVA_COMMAND%" -Daspectwerkz.definition.file="%2" -Daspectwerkz.home="%ASPECTWERKZ_HOME%" -cp "%ASPECTWERKZ_HOME%\lib\ant-1.5.2.jar;%ASPECTWERKZ_HOME%\lib\aspectwerkz-core-%ASPECTWERKZ_VERSION%.jar;%ASPECTWERKZ_HOME%\lib\aspectwerkz-%ASPECTWERKZ_VERSION%.jar;%ASPECTWERKZ_LIBS%;%ASPECTWERKZ_HOME%\lib\bcel.jar" org.codehaus.aspectwerkz.compiler.AspectWerkzC %3 %4 %5 %6 %7 %8 %9
    @exit /B %ERRORLEVEL%
)

:error
    IF EXIST "%ASPECTWERKZ_HOME%\bin\usage.txt" (
        type "%ASPECTWERKZ_HOME%\bin\usage.txt"
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
