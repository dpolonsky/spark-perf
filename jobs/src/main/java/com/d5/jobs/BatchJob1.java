package com.d5.jobs;

import com.d5.jobs.common.Config;
import com.d5.jobs.common.ConfigProvider;
import com.d5.jobs.functions.BatchJobMapper;
import com.eaio.uuid.UUID;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.log4j.MDC;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by dpolonsky on 11/01/2017.
 * <p>
 * Batch job 1:
 * constraints on es write, CPU
 */
public class BatchJob1 extends AbstractBaseSparkApplication {
    private static final Logger log = LoggerFactory.getLogger(BatchJob1.class);

    BatchJob1(Config config) throws Exception {
        super(config);
    }

    public static void main(String[] args) throws Throwable {
        new BatchJob1(ConfigProvider.get()).start(args);
    }

    private void start(String[] args) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            String type = getInputType(args);
            List<String> listOfItemIds = fetchItemIds(type);
            log.info("Fetched {} of type:{} , running mapper...", listOfItemIds.size(), type);
            sc.parallelize(listOfItemIds).repartition(parallelism).map(new BatchJobMapper(config, "job1")).collect();
            log.info("And we are done !");
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

    private String getInputType(String[] args) throws ParseException {
        Options options = new Options();

        final Option optionInput = new Option("i", "input", true, "type");
        optionInput.setRequired(true);
        options.addOption(optionInput);

        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(options, args, true);

        return cmd.getOptionValue("type");
    }

    private List<String> fetchItemIds(String type) throws Exception {
        return IntStream.range(1, 10000).mapToObj(i -> new UUID().toString()).collect(Collectors.toList());
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

}