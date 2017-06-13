package com.d5.jobs.common;

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Created by Haim.
 * <p>
 * Date: 8/22/16.
 *
 * Reads version from manifest file.
 */
public class ManifestReader {

    public String getVersion() {
        String version = "Unknown";
        Manifest mf = new Manifest();
        try {
            mf.read(Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/MANIFEST.MF"));
            Attributes attributes = mf.getMainAttributes();

            String buildTime = attributes.getValue("Build-Time");
            String implementationVersion = attributes.getValue("Implementation-Version");
            if (implementationVersion != null) {
                version = String.format("%s-%s", implementationVersion, buildTime);
            }
        } catch (IOException ignore) {
        }

        return version;
    }
}
