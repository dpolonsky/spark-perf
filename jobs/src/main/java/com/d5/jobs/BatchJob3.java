package com.d5.jobs;

import com.d5.jobs.common.Config;
import com.d5.jobs.common.ConfigProvider;
import com.d5.jobs.functions.BatchJobMapper;
import com.eaio.uuid.UUID;
import org.apache.commons.cli.*;
import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class BatchJob3 extends AbstractBaseSparkApplication {
    private static final Logger log = LoggerFactory.getLogger(BatchJob3.class);

    public BatchJob3(Config config) throws Exception {
        super(config);
    }

    public static void main(String[] args) throws Throwable {
        new BatchJob3(ConfigProvider.get()).start(args);
    }

    private void start(String[] args) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            List<String> listOfItemIds = fetchItemIds("PERSON_TYPE");
            log.info("Fetched {} persons , running mapper...", listOfItemIds.size());

            sc.parallelize(listOfItemIds).repartition(parallelism).map(new BatchJobMapper(config, "job3")).collect();
            log.info("And we are done !");
        } catch (ParseException e) {
            log.error("Failed to parse input arguments", e);
        } catch (Exception e) {
            log.error("Failed to process", e);
        } finally {
            log.info("Ending main...");
            long duration = System.currentTimeMillis() - start;
            MDC.put("batch_job3_time", duration);
            try {
                log.info("Finished job 3, took:{}", duration);
            } finally {
                MDC.remove("batch_job3_time");
            }
            shutDown();
        }
    }

    private String getInputType(String[] args) throws ParseException {
        Options options = new Options();

        final Option optionInput = new Option("t", "type", true, "type");
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