package com.d5.jobs.common;

import com.typesafe.config.ConfigFactory;

import java.io.File;
import java.io.Serializable;
import java.util.Optional;

/**
 * Created by y on 24/06/15.
 * A Class holding configurations, the class holds an inner map of default configuration and enables loading
 * external configuration from a file/input stream.
 */
public class Config implements Serializable {

    private static final String[] EXTERNAL_CONFIG_FILES = {"env.properties", "app.properties"};

    public enum Keys { // [Key Constant] ([Configuration Entry Name], [Default Value]),
        //Hadoop
        HADOOP_USER_NAME("hadoop.user.name"),

        // HDFS
        FS_DEFAULT_NAME("fs.default.name"),
        DFS_REPLICATION("dfs.replication"),
        RDD_CHECKPOINT_DIR("rdd.checkpoint.dir"),

        HDFS_ROOT_STORAGE_PATH("hdfs.ci_system.root.storage.path"),

        // Items Module
        ITEMS_STORAGE_PATH("items.module.hdfs.storage.path"),

        // Flow Manager
        FLOW_QUEUE_IP("flow.manager.queue.ip"),
        FLOW_QUEUE_USER("flow.manager.queue.username"),
        FLOW_QUEUE_PASSWORD("flow.manager.queue.password"),
        FLOW_QUEUE_PREFIX("flow.manager.queue.queue_prefix"),

        // ActiveMQ

        AMQ_IDLE_TIMEOUT("amq.idle.timeout"),
        AMQ_MAX_CONNECTIONS("amq.max.connections"),
        AMQ_MAX_ACTIVE_SESSIONS("amq.max.active.sessions"),

        // Elastic Search
        INDEX_MODULE_ELASTIC_HOST("index.module.elastic.host"),
        INDEX_MODULE_ELASTIC_HOST_LIST("index.module.elastic.host_list"),
        INDEX_MODULE_ELASTIC_PORT("index.module.elastic.port"),
        INDEX_MODULE_CLUSTER_NAME("index.module.cluster.name"),
        INDEX_MODULE_INDEX_PREFIX("index.module.elastic.index_prefix"),
        INDEX_MODULE_MAX_BULK_SIZE("index.module.max_bulk_size"),
        INDEX_MODULE_ELASTIC_BULK_TIMEOUT_SEC("index.module.elastic.bulk.timeout_seconds"),
        INDEX_MODULE_ELASTIC_BULK_MAX_SIZE_MB("index.module.elastic.bulk.max_size_mb"),
        INDEX_MODULE_ELASTIC_BULK_MAX_ACTIONS_NUMBER("index.module.elastic.bulk.max_actions_number"),
        INDEX_MODULE_ELASTIC_BULK_CONCURRENT_REQUESTS("index.module.elastic.bulk.concurrent_requests"),
        INDEX_MODULE_CACHE_ENABLED("index.module.cache.enabled"),
        INDEX_MODULE_HOST_SNIFFING_ENABLED("index.module.host.sniffing.enabled"),

        QUERY_ENGINE_RESULTS_MAX_CACHE_SIZE_MB("query.engine.results.cache.max.size.mb"),
        QUERY_ENGINE_IDS_MAX_CACHE_SIZE_MB("query.engine.ids.cache.max.size.mb"),
        QUERY_ENGINE_CACHE_VALUE_EXPIRATION_TIME_MINUTES("query.engine.cache.value.expiration.time.minutes"),

        //algorithm wrapper - cdr
        AW_CDR_CALLER_SUMMERY_STORAGE_PATH("algorithm.wrapper.caller.summary.storage.path"),

        CUD_AUDIT_HDFS_FOLDER("cud.audit.storage.path"),
        CUD_AUDIT_HDFS_ENABLE("cud.audit.storage.enable"),

        // algorithm wrapper - phone call
        AW_PHONE_CALL_LOCATION_PATTERN_INPUTS_STORAGE_PATH("algorithm.wrapper.phone_call.location.pattern.inputs.storage.path"),
        AW_PHONE_CALL_COMMUNICATION_PATTERN_INPUTS_STORAGE_PATH("algorithm.wrapper.phone_call.communication.pattern.inputs.storage.path"),
        AW_PHONE_CALL_COMMUNICATION_PATTERN_STAGING_TABLE_NAME("algorithm.wrapper.phone_call.communication.pattern.staging.table.name"),
        AW_PHONE_CALL_LOCATION_PATTERN_STAGING_TABLE_NAME("algorithm.wrapper.phone_call.location.pattern.staging.table.name"),
        AW_PHONE_CALL_COMMUNICATION_PATTERN_TABLE_NAME("algorithm.wrapper.phone_call.communication.pattern.table.name"),
        AW_PHONE_CALL_LOCATION_PATTERN_TABLE_NAME("algorithm.wrapper.phone_call.location.pattern.table.name"),
        AW_PHONE_CALL_DROP_STAGING_HIVE_TABLES("algorithm.wrapper.phone_call.drop.staging.hive.tables"),
        PHONE_CALL_DELETE_EXECUTION_DIR("phone_call.delete.execution.dir"),

        // algorithm wrapper - nce
        AW_NCE_LOCATION_PATTERN_INPUTS_STORAGE_PATH("algorithm.wrapper.nce.location.pattern.inputs.storage.path"),
        AW_NCE_LOCATION_PATTERN_STAGING_TABLE_NAME("algorithm.wrapper.nce.location.pattern.staging.table.name"),
        AW_NCE_LOCATION_PATTERN_TABLE_NAME("algorithm.wrapper.nce.location.pattern.table.name"),

        INDEX_MODULE_ALERTS_INDEX_TYPE("index_module.alerts_index.type"),

        // AW
        AW_TMP_FOLDER("algo.temp.folder"),
        VOICE_RECOGNITION_WITH_SEPARATION("voice_recognition.with.separation"),

        // algorithm wrapper - speaker
        RECORDING_INDEXING_DELETE_EXECUTION_DIR("recording.indexing.delete.execution.dir"),

        RECORDING_SIMILARITY_DELETE_EXECUTION_DIR("recording.similarity.delete.execution.dir"),
        // signatures files
        RECORDING_SIGSET_DIR("recording.signatures.file.name"),

        //distributed-ingestion
        DISTRIBUTED_INGESTION_DELETE_EXECUTION_DIR("distributed-ingestion.delete.execution.dir"),
        DISTRIBUTED_INGESTION_DELETE_FILE_FROM_HDFS("distributed-ingestion.delete.file.from.hdfs"),

        // algorithm wrapper - face
        FACE_RECOGNITION_INITIAL_SIGNATURES_DIR_PATH("face.recognition.signatures.dir.pat"),
        FACE_RECOGNITION_DELETE_EXECUTION_DIR("face.recognition.delete.execution.dir"),

        // algorithm wrapper - document
        DOCUMENT_INITIAL_SIGNATURES_FILE_NAME("text.analysis.signatures.file.name"),
        DOCUMENT_SIGNATURES_FILE_NAME_PREFIX("text.analysis.signatures.file.name.prefix"),
        DOCUMENT_DELETE_EXECUTION_DIR("text.delete.execution.dir"),

        // client service
        CLIENT_SERVICE_IP("client.service.ip"),
        CLIENT_SERVICE_PORT("client.service.port"),
        CLIENT_SERVICE_STOP_TIMEOUT("client.service.stop.timeout"),

        // default configs for client service request log (non-default properties are on client-service's log4j.properties)
        // these are here in case this appender is not defined on the log4j.properties.
        CLIENT_SERVICE_DEFAULT_JETTY_APPENDER_FILE("jetty.appender.client_service.File"),
        CLIENT_SERVICE_DEFAULT_JETTY_APPENDER_RETAIN_DAY("jetty.appender.client_service.RetainDay"),
        CLIENT_SERVICE_DEFAULT_JETTY_APPENDER_LOG_LATENCY("jetty.appender.client_service.LogLatency"),

        // entity extraction
        ENTITY_EXTRACTION_SERVICE_URL("entity.extraction.service.url"),
        ENTITY_EXTRACTION_SERVICE_PORT("entity.extraction.service.port"),
        ENTITY_EXTRACTION_SERVICE_STOP_TIMEOUT("entity.extraction.stop.timeout"),

        // hive
        HIVE_HOST("hive.host"),
        HIVE_PORT("hive.port"),
        HIVE_SCHEMA("hive.schema"),
        HIVE_USER_NAME("hive.user.name"),
        HIVE_PASSWORD("hive.password"),
        HIVE_METASTORE_PORT("hive.metastore.port"),

        IMPORT_ENTITIES_BATCH_SIZE("import.entities.batch-size"),
        IMPORT_AGGREGATIONS_BATCH_SIZE("import.aggregations.batch.size"),

        PATTERN_SIGNAL_PRODUCER_EXECUTOR("pattern.signal.producer.executor"),
        PATTERN_ANOMALY_DETECTOR_EXECUTOR("pattern.anomaly.detector.executor"),
        COMMUNICATION_PATTERN_DELETE_EXECUTION_DIR("communication.pattern.delete.execution.dir"),
        LOCATION_PATTERN_DELETE_EXECUTION_DIR("location.pattern.delete.execution.dir"),

        // importers
        IMPORTER_BATCH_SIZE_UNIFIED("importer.batchsize.unified"),

        IMPORTER_SERVER_PORT("importer.server.port"),
        AUTO_IMPORT_TIMEOUT_MINUTE("300"),

        // items tracker
        ITEM_TRACKER_BATCH_SIZE("item.tracker.batch.size"),

        // stats
        STATSD_PREFIX("statsd.prefix"),
        STATSD_HOST("statsd.host"),

        OMS_DB_HOST("oms.db.host"),
        OMS_DB_PORT("oms.db.port"),
        OMS_DB_DATABASE("oms.db.database"),
        OMS_DB_SCHEMA("oms.db.schema"),
        OMS_DB_USER("oms.db.user"),
        OMS_DB_PASSWD("oms.db.passwd"),

        SERVICE_REGISTRY_PORT("oms.service.registry.port"),

        // client service
        OBJECT_DETECTION_SERVICE_HOST("object.detection.host"),
        OBJECT_DETECTION_SERVICE_PORT("object.detection.port"),

        // Pattern service
        PATTERN_SERVER_PORT("pattern.server.port"),
		PATTERN_SERVER_HOST("pattern.server.host"),

        SPARK_SPARK_SUBMIT("spark.spark_submit"),
        SPARK_LOG_JARS("spark.log_jars"),
        SPARK_APP_HDFS_DIR("spark.app.hdfs.dir"),
        SPARK_YARN_JAR("spark.yarn.jar"),
        SPARK_DRIVER_MEMORY_MB("spark.driver_memory_mb"),
        SPARK_EXECUTOR_MEMORY_MB("spark.executor_memory_mb"),
        SPARK_EXECUTOR_MEMORY_OVERHEAD_PER_CORE_MB("spark.executor_memory_overhead_per_core_mb"),
        SPARK_EXECUTORS("spark.executors"),
        SPARK_CORES_PER_EXECUTOR("spark.cores_per_executor"),
        SPARK_MAX_TASK_SERIALIZATION_SIZE("spark.max_task_serialization_size"),
        SPARK_APP_DEPLOY_DIR("spark.app_deploy_dir"),
        SPARK_SPARK_MASTER("spark.spark_master"),
        SPARK_YARN_QUEUE("spark.yarn.queue"),
        SPARK_USER("spark.user"),

        // data-in
        GEO_CONVERSION_ENABLED("geo.conversion.enabled"),

        // ontology
        ONTOLOGY_ALT_LOCATION("ontology.alt.location")
        ;

        private final String key;

        Keys(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

    }

    private com.typesafe.config.Config configuration;

    private void init(String configFolder) {
        // Load application.properties from each module.
        com.typesafe.config.Config configuration = ConfigFactory.load();

        // Load overrides in app.properties, env.properties.
        for (String configFile : EXTERNAL_CONFIG_FILES) {
            com.typesafe.config.Config externalFile = ConfigFactory.parseFile(new File(configFolder, configFile));
            configuration = externalFile.withFallback(configuration);
        }

        this.configuration = configuration;
    }

    public Config() {
        init(Optional.ofNullable(System.getProperty("com.d5.config")).orElse("."));
    }

    public String getString(String key) {
        return this.configuration.getAnyRef(key).toString();
    }

    public String getString(Keys key) {
        return getString(key.getKey());
    }

    public int getInt(Keys key) {
        return Integer.parseInt(this.getString(key));
    }

    public int getInt(String key) {
        return Integer.parseInt(getString(key));
    }

    public boolean getBoolean(Keys key) {
        return Boolean.valueOf(this.getString(key));
    }

    public boolean getBoolean(String key) {
        return Boolean.valueOf(getString(key));
    }

    public float getFloat(String key) {
        return Float.valueOf(getString(key));
    }
    public String getString(Keys key, String defaultValue) {
        return getString(key.getKey(), defaultValue);
    }

    public String getString(String key, String defaultValue) {
        if (configuration.hasPath(key)) {
            return configuration.getString(key);
        } else {
            return defaultValue;
        }
    }

    public boolean hasKey(String key) {
       return configuration.hasPath(key);
    }

    private <T> T get(String key, T defaultValue, Class<T> returnValType) {
        String value = getString(key);
        if (value != null) {
            if (returnValType == Integer.class) {
                return (T) Integer.valueOf(value);
            } else if (returnValType == Long.class) {
                return (T) Long.valueOf(value);
            } else if (returnValType == Float.class) {
                return (T) Float.valueOf(value);
            } else if (returnValType == Double.class) {
                return (T) Double.valueOf(value);
            }
        }
        return defaultValue;
    }

    public <T> T get(Keys key, T defaultValue, Class<T> returnValType) {
        return get(key.getKey(), defaultValue, returnValType);
    }

    public <T> T get(String key, Class<T> returnValType) {
        return get(key, null, returnValType);
    }

    public <T> T get(Keys key, Class<T> returnValType) {
        return get(key.getKey(), null, returnValType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Keys key : Keys.values()) {
            if (this.configuration.hasPath(key.getKey())) {
                sb.append(String.format("%s:%s\n", key.getKey(), this.getString(key)));
            }
        }
        return sb.toString();

    }
}
