#!/bin/sh
SBT_OPTS="-Xms512M -Xmx4G -Xss2M -Dfile.encoding=UTF-8 -XX:MaxMetaspaceSize=1024M"

exec java $SBT_OPTS -jar sbt-launch.jar "$@"
