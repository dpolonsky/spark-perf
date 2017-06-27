1.  spark job on mesos needs to have access to all jars from HTTP server.
    to achieve that we have uploaded all jars to public slave and launched simple web server (simple.web.server)
    as marathon job
    dcos marathon app add web.server

2.  for HDFS to work its cofiguation were changed with environment variable
    "TASKCFG_ALL_FRAMEWORK_NAME": "hdfs"

3.  mind to mount voume to docker container instead of copying

4.  running job
    dcos job remove job1dcos
    dcos job add jobs/resources/job1_dcos.json
    dcos job run job1dcos



dcos installation on premise example
https://github.com/jrx/terraform-ansible-dcos

ssh to private ahent
e.g. ssh -A -t core@35.156.121.150 ssh -A -t core@10.0.3.213


tools:
hdfs go client
wget https://github.com/colinmarc/hdfs/releases/download/v1.0.3/gohdfs-v1.0.3-linux-amd64.tar.gz
export HADOOP_NAMENODE="name-0-node.hdfs.mesos:9001"
or
export HADOOP_NAMENODE="name-1-node.hdfs.mesos:9001"


changing mesos role:
sudo vi /var/lib/dcos/mesos-slave-common

MESOS_DEFAULT_ROLE='spark'
sudo systemctl kill -s SIGUSR1 dcos-mesos-slave && sudo systemctl stop dcos-mesos-slave
sudo rm -f /var/lib/mesos/slave/meta/slaves/latest
sudo systemctl start dcos-mesos-slave

# jenetor - https://dcos.io/docs/1.9/deploying-services/uninstall/
sudo systemctl restart dcos-spartan.service
sudo systemctl stop dcos-mesos-slave
sudo rm -fr /var/lib/mesos/slave/
sudo systemctl start dcos-mesos-slave