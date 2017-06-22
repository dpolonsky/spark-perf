#!/usr/bin/env bash

INPUT=$1
NAME="spark-perf-job1"
LOG_FILE="/var/log/d5/${NAME}.log"

LOG_JARS="jsonevent-layout-1.7.jar:json-smart-1.1.1.jar"

BASEDIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
#APP_HDFS_BASE="hdfs:///d5/apps/jobs/recent"
APP_HDFS_BASE="/mapr/awsmk/d5/apps/jobs"
APP_JARS="${APP_HDFS_BASE}/lib/"
EXEC_JARS=$(cd ${BASEDIR}; ls lib/ | awk -v prefix="${APP_HDFS_BASE}/lib/" '{print prefix $1}' | paste -sd ',')
EXEC_JARS_COLON=$(cd ${BASEDIR}; ls lib/ | awk -v prefix="lib/" '{print prefix $1}' | paste -sd ':')

PROPERTIES="log4j.properties"

FILES="${PROPERTIES}"
HADOOP_CONF_DIR=/etc/hadoop/conf
SPARK_HOME=/opt/mapr/spark/spark-2.0.1/

echo "EXEC_JARS:${EXEC_JARS}"

# --master mesos://35.157.102.212:7077 \
# --deploy-mode cluster \ -verbose:class

${SPARK_HOME}bin/spark-submit --class com.d5.jobs.BatchJob1 \
--name ${NAME} \
--master mesos://35.157.102.212:5050 \
--driver-memory 1G --executor-memory 2G --total-executor-cores 2 \
--conf spark.cores.max=10 \
--driver-java-options "-Dlog4j.configuration=file:log4j.properties" \
--conf spark.driver.extraClassPath=${LOG_JARS} \
--conf "spark.executor.extraJavaOptions=-Dlog4j.configuration=file:log4j.properties" \
--conf "spark.executor.extraClassPath=${LOG_JARS}" \
--conf spark.eventLog.enabled=true \
--conf spark.mesos.uris=${APP_HDFS_BASE}/jobs-tar-archive.tar,${APP_HDFS_BASE}/log4j.properties \
--jars ${EXEC_JARS} -v \
${APP_HDFS_BASE}/jobs.jar -i ${INPUT} 2>&1 | tee -a ${LOG_FILE}