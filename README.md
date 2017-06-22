# spark-perf
mesos installation

https://www.digitalocean.com/community/tutorials/how-to-configure-a-production-ready-mesosphere-cluster-on-ubuntu-14-04

check the logs of mesos service in /var/log/syslog
e.g. tail -f  /var/log/syslog

set mesos attributes:
    sudo mkdir -p /etc/mesos-slave/attributes/
	sudo bash -c "echo north-west > /etc/mesos-slave/attributes/rack"
	sudo rm -f /var/lib/mesos/meta/slaves/latest
	sudo service mesos-slave restart

set containerizers:
    sudo bash -c "echo mesos > /etc/mesos-slave/containerizers"
    sudo rm -f /var/lib/mesos/meta/slaves/latest
	sudo service mesos-slave restart

set mesos roles:
    on master node add these to master execution command
    vi change /usr/bin/mesos-init-wrapper
    add --roles=\"structured,similarity,patterns\"
    add --weights=\"structured=60,similarity=10,patterns=30\”

update mesos quota
MASTER=$(mesos-resolve `cat /etc/mesos/zk`)
curl -d @quota.json -X POST http://${MASTER}/quota


sample mesos job
mesos-execute --master=$MASTER --name="cluster-test" --command="sleep 5"