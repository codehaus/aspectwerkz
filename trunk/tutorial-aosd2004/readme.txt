WLS config - startWebLogic.cmd
*********************************

set AW_AOSD_DIR=D:\aw\cvs_aw\tutorial-aosd2004
set AW_AOSD=
set AW_AOSD=%AW_AOSD% -Daspectwerkz.definition.file=%AW_AOSD_DIR%\src\web\WEB-INF\aspectwerkz.xml
set AW_AOSD=%AW_AOSD% -Djava.security.auth.login.config=%AW_AOSD_DIR%\config\jaas.config -Djava.security.policy=%AW_AOSD_DIR%\config\aosd2004.policy

"%JAVA_HOME%\bin\java" %AW_AOSD%  ...



JAAS setup
*************

Remember to change path to passwd in config/jaas.config
(hardcoded)


Tutor
***********
Build the aw:war:online
Deploy
-> the webapp skeleton without any services

Apply the JISP UOW aspect  and check ServiceManager config
Adapt some small part of the webapp (see newAddressBook())
Deploy
-> there is persistance

Apply the JAAS Aspect and check ServiceManager config
Deploy
-> there is security
No account: refused
Account jboner: can add / remove
Account avasseur: read only (authen but not author)


