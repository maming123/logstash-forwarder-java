#!/bin/bash

base_dir="$(cd "$(dirname "$0")/.."; pwd)"

echo ${base_dir}

JAR_NAME="logstash-forwarder-java-0.2.9-SNAPSHOT.jar"

CONFGI_NAME="${base_dir}/conf/config.json"
LOGFILENAME="${base_dir}/log/logstash-forwarder.log"
SINCEDBNAME="${base_dir}/conf/sincedb"

JAVAOPS="-Xms2048m -Xmx2048m"
FULLNAME="${base_dir}/${JAR_NAME}"
PARAMS="-config ${CONFGI_NAME} -info -logfile ${LOGFILENAME} -logfilenumber 10 logfilesize 20MB -sincedb ${SINCEDBNAME} -spoolsize 10240 -tail"

echo ${JAVAOPS}
echo ${FULLNAME}
echo ${PARAMS}

COMMAND=$1

if [ "$COMMAND" = "-daemon" ]; then
  nohup java ${JAVAOPS} -jar ${FULLNAME} ${PARAMS}  >/dev/null 2>&1 &
  # $base_dir/libs/$JAR_NAME 2>&1 < /dev/null &
else
  java ${JAVAOPS} -jar ${FULLNAME} ${PARAMS}
fi