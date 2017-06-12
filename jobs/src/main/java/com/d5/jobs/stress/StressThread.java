package com.d5.jobs.stress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by danny.polonsky on 6/7/17.
 */
public class StressThread extends Thread{
    private double load;
    private long duration;
    private float bytesMB;
    private final Logger log = LoggerFactory.getLogger(StressThread.class);
    /**
     * Constructor which creates the thread
     * @param name Name of this thread
     * @param load Load % that this thread should generate
     * @param duration Duration that this thread should generate the load for
     */
    public StressThread(String name, double load, float bytesMB, long duration) {
        super(name);
        this.load = load;
        this.bytesMB = bytesMB;
        this.duration = duration * 1000;
    }

    /**
     * Generates the load when run
     */
    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        try {
            if ( bytesMB > 0) {
                log.info("allocating memory block of [" + Math.round(bytesMB) + "][" +  1024 * 1024 + "]");
                byte[][] memBlock = new byte[Math.round(bytesMB)][1024 * 1024];
            }
            // Loop for the given duration
            while (System.currentTimeMillis() - startTime < duration) {
                // Every 100ms, sleep for the percentage of unladen time
                if (System.currentTimeMillis() % 100 == 0) {
                    Thread.sleep((long) Math.floor((1 - load) * 100));
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.error("Failed to run stress !!");
        }
    }
}
