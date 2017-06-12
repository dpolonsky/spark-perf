package com.d5.jobs.functions;

import com.d5.jobs.common.Config;
import com.d5.jobs.common.ShellCommand;
import com.google.common.collect.Lists;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.broadcast.Broadcast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

/**
 * Created by danny.polonsky on 5/16/17.
 */
public class BatchJob3Mapper implements Function<String, Integer>, Serializable {
    private final Logger log = LoggerFactory.getLogger(BatchJob3Mapper.class);
    private Broadcast<Config> config;

    public BatchJob3Mapper(Broadcast<Config> config) {
        this.config = config;
    }

    @Override
    public Integer call(String input) throws Exception {
        List<String> cmd = Lists.newArrayList();

        cmd.add("/usr/local/bin/stress");
        cmd.add("--cpu");
        cmd.add(config.getValue().getString("batch.job3.task.cpu"));
        cmd.add("--vm-bytes");
        cmd.add(config.getValue().getInt("batch.job3.task.memory") + "M");
        cmd.add("--timeout");
        cmd.add(config.getValue().getInt("batch.job3.task.duration") + "s");
        ShellCommand shellCommand = new ShellCommand(cmd.toArray(new String[cmd.size()]));
        log.info("Executing comamnd:" + cmd.toString());
        ShellCommand.ShellOutput shellOutput = shellCommand.executeCommand();
        log.info("shell exit code:{} out:{} err:{}", shellOutput.stdOut, shellOutput.stdErr);

        return shellOutput.exitCode;
    }

}