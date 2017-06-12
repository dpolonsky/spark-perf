package com.d5.jobs.common;

/**
 * @author dima on 8/2/16.
 */
public class ConfigProvider {
    private static Config config = new Config();

    public static Config get() {
        return config;
    }
}
