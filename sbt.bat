set SBT_OPTS=-Xmx1024M -Dinput.encoding=Cp1252 -Dfile.encoding=SJIS -XX:MaxMetaspaceSize=1024M

java %SBT_OPTS% -jar sbt-launch.jar %*
