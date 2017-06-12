#!/usr/bin/env bash

NAME="spark-perf-job3"
LOG_FILE="/var/log/d5/${NAME}.log"

LOG_JARS="/usr/local/share/jsonlog/jsonevent-layout-1.7.jar:/usr/local/share/jsonlog/json-smart-1.1.1.jar"

BASEDIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
APP_HDFS_BASE="maprfs:///d5/apps/spark-test/recent"
APP_JARS="${APP_HDFS_BASE}/lib/"
EXEC_JARS=$(cd ${BASEDIR}; ls lib/ | awk -v prefix="${APP_JARS}" '{print prefix $1}' | paste -sd ',')

PROPERTIES="log4j.properties"

FILES="${PROPERTIES}"
HADOOP_CONF_DIR=/etc/hadoop/conf
SPARK_HOME=/opt/mapr/spark/spark-1.6.1/

echo "EXEC_JARS:${EXEC_JARS}"

${SPARK_HOME}bin/spark-submit --class com.d5.jobs.BatchJob3 \
--name ${NAME} \
--master mesos://mapr1:7077 \
--deploy-mode cluster \
--driver-memory 1G --executor-memory 2G --total-executor-cores 2 \
--jars ${EXEC_JARS} -v \
${APP_HDFS_BASE}/jobs.jar 2>&1 | tee -a ${LOG_FILE}

