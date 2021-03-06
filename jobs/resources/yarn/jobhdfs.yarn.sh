#!/usr/bin/env bash

INPUT=$1
NAME="spark-perf-jobhdfs"
LOG_FILE="/var/log/d5/${NAME}.log"

LOG_JARS="jsonevent-layout-1.7.jar:json-smart-1.1.1.jar"

BASEDIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
#APP_HDFS_BASE="hdfs:///d5/apps/jobs/recent"
APP_HDFS_BASE="/mapr/mapr.d5.devops/d5/apps/jobs"
APP_JARS="${APP_HDFS_BASE}/lib/"
EXEC_JARS=$(cd ${BASEDIR}; ls lib/ | awk -v prefix="${APP_HDFS_BASE}/lib/" '{print prefix $1}' | paste -sd ',')
EXEC_JARS_COLON=$(cd ${BASEDIR}; ls lib/ | awk -v prefix="lib/" '{print prefix $1}' | paste -sd ':')

PROPERTIES="log4j.properties"

FILES="${PROPERTIES}"
HADOOP_CONF_DIR=/etc/hadoop/conf
SPARK_HOME=/opt/mapr/spark/spark-2.0.1/
# --conf spark.driver.extraClassPath=${LOG_JARS} \
# --driver-java-options "-verbose:class -Dlog4j.configuration=file:log4j.properties" \
# --driver-memory 1G --executor-memory 3G --conf spark.cores.max=8 \
echo "EXEC_JARS:${EXEC_JARS}"

MASTER="mapr1.5d.devops"
#10.9.1.221
${SPARK_HOME}bin/spark-submit --class com.d5.jobs.BatchJobHDFS \
--name ${NAME} \
--master yarn \
--deploy-mode cluster \
--driver-memory 1G --executor-memory 3G --conf spark.cores.max=5 \
--conf "spark.driver.extraJavaOptions=-Dlog4j.configuration=file:log4j.properties" \
--conf "spark.driver.extraClassPath=${EXEC_JARS_COLON}" \
--conf "spark.executor.extraJavaOptions=-Dlog4j.configuration=file:log4j.properties" \
--conf "spark.executor.extraClassPath=${EXEC_JARS_COLON}" \
--conf spark.eventLog.enabled=true \
--files "${FILES}" \
--queue high \
--jars ${EXEC_JARS} -v \
${APP_HDFS_BASE}/jobs.jar -i ${INPUT} 2>&1 | tee -a ${LOG_FILE}