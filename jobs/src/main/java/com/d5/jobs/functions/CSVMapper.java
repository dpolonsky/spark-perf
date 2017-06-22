package com.d5.jobs.functions;

import com.d5.jobs.common.Config;
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by danny.polonsky on 5/16/17.
 *
 * Mind that for spark 2.0.1 (dcos) the signature of call is different
 * public Iterator<Integer> call(Iterator<String> bulk) throws Exception {
 */
public class CSVMapper implements FlatMapFunction<Iterator<String>, Integer>, Serializable {
    private final Logger log = LoggerFactory.getLogger(CSVMapper.class);
    private Broadcast<Config> config;
    private String jobName;

    public CSVMapper(Broadcast<Config> config, String jobName) {
        this.config = config;
        this.jobName = jobName;
    }

    @Override
    public Iterator<Integer> call(Iterator<String> bulk) throws Exception {
        log.info("parsing bulk of csv lines");
        AtomicInteger counter = new AtomicInteger();
        bulk.forEachRemaining(line -> { try (final CSVReader csvReader = new CSVReaderBuilder(new StringReader(line))
                    .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS).build()) {
                final String[] parsedLine = csvReader.readNext();
                if (parsedLine != null) {
                    List<String> result = Arrays.asList(parsedLine);
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