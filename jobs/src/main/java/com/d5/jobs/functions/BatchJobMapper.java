package com.d5.jobs.functions;

import com.d5.jobs.common.Config;
import com.d5.jobs.common.ShellCommand;
import com.d5.jobs.stress.StressThread;
import com.google.common.collect.Lists;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.broadcast.Broadcast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by danny.polonsky on 5/16/17.
 */
public class BatchJobMapper implements Function<String, Integer>, Serializable {
    private final Logger log = LoggerFactory.getLogger(BatchJobMapper.class);
    private Broadcast<Config> config;
    private String jobName;
    
    public BatchJobMapper(Broadcast<Config> config, String jobName) {
        this.config = config;
        this.jobName = jobName;
    }

    @Override
    public Integer call(String input) throws Exception {
        boolean isExternal = config.getValue().getBoolean("batch." + jobName + ".task.stress.external");
        if ( isExternal){
            log.info("running external stress");
            return launchShellCommand();
        }else{
            log.info("running internal stress");
            int numCores = config.getValue().getInt("batch." + jobName + ".task.cpu");
            ExecutorService executor = Executors.newFixedThreadPool(numCores);
            executor.execute(new StressThread(jobName + ".stress",
                    config.getValue().getFloat("batch." + jobName + ".task.load"),
                    config.getValue().getFloat("batch." + jobName + ".task.memory"),
                    config.getValue().getInt("batch." + jobName + ".task.duration")));
            executor.shutdown();
            return 0;
        }

    }

    private int launchShellCommand() throws Exception{
        List<String> cmd = Lists.newArrayList();
        cmd.add("/usr/local/bin/stress");
        cmd.add("--cpu");
        cmd.add(config.getValue().getString("batch." + jobName + ".task.cpu"));
        cmd.add("--vm-bytes");
        cmd.add(config.getValue().getString("batch." + jobName + ".task.memory") + "M");
        cmd.add("--timeout");
        cmd.add(config.getValue().getFloat("batch." + jobName + ".task.duration") + "s");

        ShellCommand shellCommand = new ShellCommand(cmd.toArray(new String[cmd.size()]));
        log.info("Executing command:" + cmd.toString());
        ShellCommand.ShellOutput shellOutput = shellCommand.executeCommand();
        log.info("shell exit code:{} out:{} err:{}", shellOutput.stdOut, shellOutput.stdErr);
        return shellOutput.exitCode;
    }
}