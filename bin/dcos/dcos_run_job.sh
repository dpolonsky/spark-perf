#!/usr/bin/env bash
JOB_ID=$1
JOB_PATH=$2

log() {
  echo "[$(date +'%Y-%m-%dT%H:%M:%S%z')]: $@ "  ## no colors
  echo "[$(date +'%Y-%m-%dT%H:%M:%S%z')]: $@" >> /tmp/build.log
}

exec(){
    log "$@"
    eval $@
}

log "removing job"
exec "dcos job remove ${JOB_ID}"
exec "dcos job add ${JOB_PATH}"
exec "dcos job run ${JOB_ID}"