#!/bin/bash

base_dir="$(cd "$(dirname "$0")/.."; pwd)"

echo ${base_dir}

JAR_NAME="logstash-forwarder-java-0.2.7-SNAPSHOT.jar"

FULLNAME="${base_dir}/${JAR_NAME}"

echo $FULLNAME

ps -ef| grep $FULLNAME | grep -v grep | awk '{print $2}'| while read pid
do
echo "$JAR_NAME running pid $pid"
done
