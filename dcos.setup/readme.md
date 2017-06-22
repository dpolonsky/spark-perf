1.  spark job on messo need to hve access to all jars from HTTP server.
    to achieve it we have uploaded all jars to public slave and launched simple web server (simple.web.server)
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