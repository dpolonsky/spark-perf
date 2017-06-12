#!/usr/bin/env bash
ENV=$1
ACTION=$2

BASEDIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

log() {
  echo "[$(date +'%Y-%m-%dT%H:%M:%S%z')]: $@ "  ## no colors
  echo "[$(date +'%Y-%m-%dT%H:%M:%S%z')]: $@" >> /tmp/build.log
}

mvn_build(){
    log "mvn clean install -DskipTests -Pproduction"
    mvn clean install -DskipTests -Pproduction
}

ssh_cmd() {
    log "ssh ${1}@${2} ${3}"
    ssh ${1}@${2} ${3}
}

scp_file_to_server() {
  log "executing  scp -r ${1} ${2}@${3}:${4}"
  scp -r ${1} ${2}@${3}:${4}
}

scp_file_dcos() {
  log "executing  scp -r ${1} ${2}@${3}:${4}"
  scp -r ${1} ${2}@${3}:${4}
}

exec(){
    log "$@"
    eval $@
}
function deploy_mapr(){
    mvn_build
    ssh_cmd "administrator" "mapr1" "mkdir -p /home/administrator/apps/jobs/recent/"
    scp_file_to_server "jobs/target/jobs*" "administrator" "mapr1" "/home/administrator/apps/jobs/recent/"
    scp_file_to_server "env.properties" "administrator" "mapr1" "/home/administrator/apps/jobs/recent/"
    scp_file_to_server "jobs/resources/*.sh" "administrator" "mapr1" "/home/administrator/apps/jobs/recent/"
    ssh_cmd "administrator" "mapr1" "cd /home/administrator/apps/jobs/recent/;mv jobs-v1.0.jar jobs.jar;tar -xvf jobs-tar-archive.tar"
    ssh_cmd "administrator" "mapr1" "hadoop fs -mkdir -p /d5/apps/jobs; hadoop fs -rm -r -f -skipTrash /d5/apps/jobs/*;hadoop fs -put ~/apps/jobs/recent /d5/apps/jobs"
}

function deploy_dcos(){
#    mvn_build
    exec "rm -rf /tmp/tmp_lib; mkdir -p /tmp/tmp_lib;cp ${BASEDIR}/jobs/target/jobs*.tar /tmp/tmp_lib;cd /tmp/tmp_lib;tar -xvf jobs-tar-archive.tar;"
    EXEC_JARS=$(cd  /tmp/; ls /tmp/tmp_lib/lib/ | awk -v prefix="hdfs:\\\/\\\/\\\/d5\\\/apps\\\/jobs\\\/lib\\\/" '{print prefix $1}' | paste -sd ',' -)
    ARTIFACTS_JARS=$(cd  /tmp/; ls /tmp/tmp_lib/lib/ | awk -v prefix="{\"uri\": \"hdfs:\\\/\\\/\\\/d5\\\/apps\\\/jobs\\\/lib\\\/" '{print prefix $1 "\"extract\": false, \"executable\": false, \"cache\": true}"}' | paste -sd ',' -)
    ALL_JARS="${EXEC_JARS},hdfs:\\/\\/\\/d5\\/apps\\/jobs\\/jobs.jar"

    exec "cp ${BASEDIR}/jobs/resources/job1_dcos.json /tmp/tmp_lib/job1_dcos.json"
    exec "cd /tmp/tmp_lib/;"
    sed -i.bak s/{REPLACE_EXEC_JARS}/${EXEC_JARS}/g job1_dcos.json
    sed -i.bak s/{ALL_JARS}/${ALL_JARS}/g job1_dcos.json
    sed -i.bak s/{ARTIFACTS}/"${ARTIFACTS_JARS}"/g job1_dcos.json
    cat job1_dcos.json
    exec "cd ${BASEDIR}"

    scp_file_dcos "jobs/target/jobs*" "core" "35.156.121.150" "/home/core/apps/jobs"
    ssh_cmd "core" "35.156.121.150" "cd /home/core/apps/jobs/;mv jobs-v1.0.jar jobs.jar;tar -xvf jobs-tar-archive.tar; rm jobs-tar-archive.tar;"
    hdfs_docker_id=$(ssh core@35.156.121.150 "docker ps | grep mesosphere/hdfs-client:1.0.0-2.6.0")
    hdfs_docker_id=$(echo $hdfs_docker_id | awk '{print $1}')
    echo "got this docker id:${hdfs_docker_id}";
    # mount folder
    ssh_cmd "core" "35.156.121.150" "
        docker exec -i ${hdfs_docker_id} sed -i 's/\.\./\.hdfs\./g' /hadoop-2.6.0-cdh5.9.1/etc/hadoop/hdfs-site.xml;
        docker exec -i ${hdfs_docker_id} ./bin/hdfs dfs -mkdir -p /d5/apps/jobs;
        docker exec -i ${hdfs_docker_id} ./bin/hdfs dfs -rm -r -f -skipTrash /d5/apps/jobs/*;
        docker exec -i ${hdfs_docker_id} mkdir -p /d5/apps/jobs; rm -rf /d5/apps/jobs/*;
        docker cp /home/core/apps/jobs ${hdfs_docker_id}:/d5/apps/jobs;
        docker exec -i ${hdfs_docker_id} ./bin/hdfs dfs  -put /d5/apps/jobs /d5/apps/jobs;"
}

if [[ "${ENV}" == "dcos" ]]; then
    if [[ "${ACTION}" == "build" ]]; then
        log "building dcos"
        deploy_dcos
    elif [[ "${ACTION}" == "run" ]]; then
        log "running dcos job"
        dcos job add jobs/resources/job1.dcos.json
    fi
else
    log "deploying mapr"
    deploy_mapr
fi

