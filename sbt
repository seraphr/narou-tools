#!/bin/sh
SBT_OPTS="-Xmx4g -Dfile.encoding=UTF-8 -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=512M"

exec java $SBT_OPTS -jar sbt-launch.jar "$@"
