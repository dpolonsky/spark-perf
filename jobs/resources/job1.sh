#!/usr/bin/env bash

INPUT=$1
NAME="spark-perf-job1"
LOG_FILE="/var/log/d5/${NAME}.log"

LOG_JARS="/usr/local/share/jsonlog/jsonevent-layout-1.7.jar:/usr/local/share/jsonlog/json-smart-1.1.1.jar"

BASEDIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
APP_HDFS_BASE="hdfs:///d5/apps/jobs/recent"
#APP_HDFS_BASE="maprfs:///d5/apps/jobs/recent"
APP_JARS="${APP_HDFS_BASE}/lib/"
EXEC_JARS=$(cd ${BASEDIR}; ls lib/ | awk -v prefix="${APP_JARS}" '{print prefix $1}' | paste -sd ',')
EXEC_JARS_COLON=$(cd ${BASEDIR}; ls lib/ | awk -v prefix="${APP_JARS}" '{print prefix $1}' | paste -sd ':')

PROPERTIES="log4j.properties"

FILES="${PROPERTIES}"
HADOOP_CONF_DIR=/etc/hadoop/conf
SPARK_HOME=/opt/mapr/spark/spark-1.6.1/

echo "EXEC_JARS:${EXEC_JARS}"

${SPARK_HOME}bin/spark-submit --class com.d5.jobs.BatchJob1 \
--name ${NAME} \
--master mesos://mapr1:7077 \
--deploy-mode cluster \
--driver-java-options "-verbose:class -Dlog4j.configuration=file:log4j.properties" \
--conf "spark.driver.extraClassPath=${LOG_JARS}" \
--conf "spark.executor.extraJavaOptions=-Dlog4j.configuration=file:log4j.properties" \
--conf "spark.executor.extraClassPath=${LOG_JARS}" \
--driver-memory 1G --executor-memory 2G --total-executor-cores 2 \
--conf "spark.mesos.uris=${EXEC_JARS}" \
--jars ${EXEC_JARS} -v \
${APP_HDFS_BASE}/jobs.jar -i ${INPUT} 2>&1 | tee -a ${LOG_FILE}