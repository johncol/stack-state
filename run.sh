#!/bin/bash
#
# 2018-03-05
# Runs stack state java solution
#   java version "9"
#   Java(TM) SE Runtime Environment (build 9+181)
#   Java HotSpot(TM) 64-Bit Server VM (build 9+181, mixed mode)

JAR_FILE=stack-state-1.0.0.jar
STATE_JSON_FILE=$1
EVENTS_JSON_FILE=$2

"${JAVA_HOME}"/bin/java -jar $JAR_FILE $STATE_JSON_FILE $EVENTS_JSON_FILE
