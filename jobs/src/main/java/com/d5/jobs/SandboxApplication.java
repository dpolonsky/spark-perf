package com.d5.jobs;

import com.d5.jobs.common.Config;
import com.d5.jobs.common.ConfigProvider;
import org.apache.commons.cli.*;
import org.apache.spark.api.java.JavaRDD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by dpolonsky on 11/01/2017.
 * <p>
 * Structured data processing
 * HDFS write - v
 */
public class SandboxApplication extends AbstractBaseSparkApplication {
    private static final Logger log = LoggerFactory.getLogger(SandboxApplication.class);

    public SandboxApplication(Config config) throws Exception {
        super(config);
    }

    public static void main(String[] args) throws Throwable {
        new SandboxApplication(ConfigProvider.get()).start(args);
    }

    private void start(String[] args) throws Throwable {
        String inputPath = getInputPath(args);

        try {
            JavaRDD<String> rdd = sc.textFile(inputPath);
            rdd.mapPartitionsWithIndex((index, lines) -> {
                log.info("processing row" + index);
                return lines;
            }, true).count();
        } catch (Exception e) {
            log.error("Failed to process file", e);
        } finally {
            log.info("Ending main...");
            shutDown();
        }
    }

    @Override
    protected String getApplicationName() {
        return "sandbox";
    }

    @Override
    protected boolean isDeleteExecutionFolder(Config config) {
        return config.getBoolean(Config.Keys.DISTRIBUTED_INGESTION_DELETE_EXECUTION_DIR);
    }

    @Override
    public void close() throws IOException {
        shutDown();
    }

    private String getInputPath(String[] args) throws Exception {
        Options options = new Options();

        final Option optionInput = new Option("i", "input", true, "input");
        optionInput.setRequired(true);
        options.addOption(optionInput);


        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(options, args, true);

        return cmd.getOptionValue("input");
    }
}