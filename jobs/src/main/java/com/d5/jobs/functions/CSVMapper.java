package com.d5.jobs.functions;

import com.d5.jobs.common.Config;
import com.eaio.uuid.UUID;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.broadcast.Broadcast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by danny.polonsky on 5/16/17.
 * <p>
 * Mind that for spark 2.0.1 (dcos) the signature of call is different
 * public Iterator<Integer> call(Iterator<String> bulk) throws Exception {
 */
public class CSVMapper implements FlatMapFunction<Iterator<String>, Integer>, Serializable {
    private final Logger log = LoggerFactory.getLogger(CSVMapper.class);
    private Broadcast<Config> config;
    private UUID jobId;
    private String jobName;

    public CSVMapper(Broadcast<Config> config, UUID jobId, String jobName) {
        this.config = config;
        this.jobName = jobName;
        this.jobId = jobId;
    }

    @Override
    public Iterator<Integer> call(Iterator<String> bulk) throws Exception {
        log.info("parsing bulk of csv lines");
        AtomicInteger counter = new AtomicInteger();
        bulk.forEachRemaining(line -> {
            log.info("processing line:" + line);
            try (final CSVReader csvReader = new CSVReaderBuilder(new StringReader(line))
                    .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS).build()) {
                final String[] parsedLine = csvReader.readNext();
                if (parsedLine != null) {
                    List<String> result = Arrays.asList(parsedLine);
                    Path path = Paths.get("/mapr/mapr.d5.devops/tmp/", jobId.toString(), new UUID().toString());
                    long start = System.currentTimeMillis();
                    Files.write(path, result, Charset.forName("UTF-8"));
                    log.info("Writing to file: %s, took: %d", path, System.currentTimeMillis() - start);
                } else {
                    log.error("Failed to parse line [{}]", line);
                }
            } catch (IOException e) {
                log.error("Failed to parse line [{}]", line, e);
            }
            counter.getAndIncrement();
        });

        // add writing HDFS
        log.info("Processed bulk of {} rows", counter);
        return Arrays.asList(counter.get()).iterator();
    }
}