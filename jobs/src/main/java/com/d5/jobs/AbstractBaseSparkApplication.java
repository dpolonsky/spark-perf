package com.d5.jobs;//package com.d5.common;

import com.d5.jobs.common.Config;
import com.d5.jobs.common.ConfigProvider;
import com.d5.jobs.common.HdfsItemDataStorage;
import com.d5.jobs.common.ManifestReader;
import org.apache.commons.io.IOUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;

/**
 * Created by danny.polonsky on 6/5/17.
 */
public abstract class AbstractBaseSparkApplication implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractBaseSparkApplication.class);

    public final int parallelism;
    public final Broadcast<Config> config;
    public JavaSparkContext sc;
    public HdfsItemDataStorage hdfsStorage;

    public AbstractBaseSparkApplication(Config config) throws Exception {
        LOG.info("Initializing {} ...", getApplicationName());

        hdfsStorage = new HdfsItemDataStorage();
        boolean exists = hdfsStorage.getHdfs().exists("/data/test");

        config = config == null ? ConfigProvider.get() : config;
        createSparkContext(config);
        parallelism = sc.defaultParallelism();
        this.config = sc.broadcast(config);


        LOG.info("Spark application {} running with parallelism {}, assigned id {}", getApplicationName(), parallelism, sc.sc().applicationId());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> IOUtils.closeQuietly(this)));
    }

    /**
     * Creating spark context, if not specified fall back to local mode
     */
    private void createSparkContext(Config config) {
        SparkConf sparkConf = new SparkConf().setAppName(getApplicationName());

        if (config.hasKey(Config.Keys.INDEX_MODULE_ELASTIC_HOST.getKey())) {
            sparkConf.set("es.nodes", config.getString(Config.Keys.INDEX_MODULE_ELASTIC_HOST, ""));
            sparkConf.set("es.mapping.date.rich", "false");
        }

        if (sparkConf.getOption("spark.master").isEmpty()) {
            LOG.info("Overriding master to local for tests only !!");
            sparkConf.setMaster("local[2]");
//            sparkConf.set("spark.ui.enabled", "true");
        }
        sc = new JavaSparkContext(sparkConf);
        LOG.info("Spark context for {} was created", getApplicationName());
    }

    protected abstract String getApplicationName();

    protected abstract boolean isDeleteExecutionFolder(Config config);

    private String getVersion() {
        ManifestReader manifestReader = new ManifestReader();
        return manifestReader.getVersion();
    }

    protected void shutDown() {
        LOG.info("We have exited the main loop, trying to shutdown spark context");
        if (sc != null) {
            sc.close();
        }
    }
}
