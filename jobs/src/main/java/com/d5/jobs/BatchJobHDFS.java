package com.d5.jobs;

import com.d5.jobs.common.Config;
import com.d5.jobs.common.ConfigProvider;
import com.d5.jobs.common.HdfsHelper;
import com.d5.jobs.functions.BatchJobMapper;
import com.d5.jobs.functions.CSVMapper;
import com.eaio.uuid.UUID;
import org.apache.commons.cli.*;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.MDC;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by dpolonsky on 11/01/2017.
 * <p>
 * Batch job 1:
 * constraints on es write, CPU
 */
public class BatchJobHDFS extends AbstractBaseSparkApplication {
    private static final Logger log = LoggerFactory.getLogger(BatchJobHDFS.class);
    private UUID jobId;

    BatchJobHDFS(Config config) throws Exception {
        super(config);
        jobId = new UUID();
    }

    public static void main(String[] args) throws Throwable {
        new BatchJobHDFS(ConfigProvider.get()).start(args);
    }

    private void start(String[] args) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            String inputPath = getInput(args);
            String fsName = config.getValue().getString("fs.default.name");
            log.info("Got this inputPath:" + inputPath + " abd fsname:" + fsName);
//            Path fullPath = new Path(fsName, inputPath);
//            log.info("Got this input:" + fullPath.toString());
            List<Integer> lines = sc.textFile(inputPath).mapPartitions(new CSVMapper(config, jobId, "jobHDFS")).collect();
            int totalNumOfLines = lines.stream().mapToInt(Integer::intValue).sum();
            log.info("Processed {}, and we are done !", totalNumOfLines);
        } catch (ParseException e) {
            log.error("Failed to parse input arguments", e);
        } catch (Exception e) {
            log.error("Failed to process", e);
        } finally {
            log.info("Ending main...");
            long duration = System.currentTimeMillis() - start;
            MDC.put("batch_job1_time", duration);
            try {
                log.info("Finished job 1, took:{}", duration);
            } finally {
                MDC.remove("batch_job1_time");
            }
            shutDown();
        }
    }

    private String getInput(String[] args) throws ParseException {
        Options options = new Options();

        final Option optionInput = new Option("i", "input", true, "type");
        optionInput.setRequired(true);
        options.addOption(optionInput);

        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(options, args, true);

        return cmd.getOptionValue("input");
    }

    @Override
    protected String getApplicationName() {
        return "jobhdfs";
    }

    @Override
    protected boolean isDeleteExecutionFolder(Config config) {
        return config.getBoolean(Config.Keys.DISTRIBUTED_INGESTION_DELETE_EXECUTION_DIR);
    }

    @Override
    public void close() throws IOException {
        shutDown();
    }

}