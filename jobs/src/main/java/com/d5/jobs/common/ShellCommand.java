package com.d5.jobs.common;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by dpolonsky on 11/01/2016.
 */
public class ShellCommand {

    private static final Logger LOG = LoggerFactory.getLogger(ShellCommand.class);
    private final String[] cmdArray;
    private File executionDir;
    private boolean writeToLog;
    private long timeout;

    private volatile ReentrantLock shellOutputLock = new ReentrantLock();
    private volatile ShellOutput shellOutput;
    private boolean redirectErrorStream;

    public ShellCommand(String... cmdArray) {
        this.cmdArray = cmdArray;
        this.timeout = -1;
        this.writeToLog = true;
        this.redirectErrorStream = false;
    }

    public ShellCommand setExecutionDir(File executionDir) {
        this.executionDir = executionDir;
        return this;
    }

    public ShellCommand setRedirectErrorStream(boolean redirectErrorStream) {
        this.redirectErrorStream = redirectErrorStream;
        return this;
    }

    public ShellCommand setWriteToLog(boolean writeToLog) {
        this.writeToLog = writeToLog;
        return this;
    }

    public ShellCommand setTimeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    public ShellOutput executeCommand() throws IOException {
        shellOutput = new ShellOutput();
        ProcessBuilder builder = new ProcessBuilder(cmdArray);

        if (executionDir != null) {
            builder.directory(executionDir);
        }

        if (this.redirectErrorStream) {
            builder.redirectErrorStream(true);
        }

        Process process = builder.start();
        OutputThread[] stdArray = processShell(process);

        try {
            Timer t = null;
            if (timeout > -1) {
                t = new Timer();
                t.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        LOG.error("Reached timeout, destroying process");
                        closeProcessStreams(process);
                        process.destroy();
                    }
                }, timeout);
            }
            int exitCode = process.waitFor();
            // waiting for stdout and stderr
            if (stdArray != null) {
                for (Thread thread : stdArray) {
                    thread.join();
                }
            }

            if (t != null) {
                LOG.info("Cancelling timeout");
                t.cancel();
                t.purge();
            }
            shellOutput.exitCode = exitCode;
            return shellOutput;
        } catch (InterruptedException e) {
            LOG.error("Failed to execute command: {}, in execution dir:{}", Arrays.toString(cmdArray), executionDir, e);
            return shellOutput;
        } finally {
            if (stdArray != null) {
                Arrays.stream(stdArray).forEach(OutputThread::close);
            }
            closeProcessStreams(process);
        }
    }

    private void closeProcessStreams(Process process) {
        if (process != null) {
            try {
                IOUtils.closeQuietly(process.getInputStream());
            } catch (Exception e) {
                LOG.info("Failed to close input stream of process");
            }

            try {
                IOUtils.closeQuietly(process.getOutputStream());
            } catch (Exception e) {
                LOG.info("Failed to close input stream of process");
            }

            try {
                IOUtils.closeQuietly(process.getErrorStream());
            } catch (Exception e) {
                LOG.info("Failed to close input stream of process");
            }
        }
    }

    private OutputThread[] processShell(Process p) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
        BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream(), StandardCharsets.UTF_8));

        OutputThread threadStdOut = new OutputThread(input, OutputType.STDOUT, writeToLog);
        threadStdOut.setDaemon(true);
        threadStdOut.start();

        OutputThread threadStdError = new OutputThread(error, OutputType.STDERR, writeToLog);
        threadStdError.setDaemon(true);
        threadStdError.start();

        return new OutputThread[]{threadStdOut, threadStdError};
    }

    private enum OutputType {
        STDERR,
        STDOUT
    }

    public static class ShellOutput {
        public String stdOut;
        public String stdErr;
        public int exitCode;
    }

    /**
     *
     */
    private class OutputThread extends Thread implements Closeable {
        BufferedReader reader = null;
        OutputType outputType;
        boolean writeToLog = false;

        public OutputThread(BufferedReader reader, OutputType outputType, boolean writeToLog) {
            this.reader = reader;
            this.outputType = outputType;
            this.writeToLog = writeToLog;
        }

        @Override
        public void run() {
            String line;
            StringBuilder output = new StringBuilder();

            try {
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    if (writeToLog) {
                        if (outputType == OutputType.STDOUT) {
                            LOG.info(line);
                            System.out.println(line);
                        } else if (outputType == OutputType.STDERR) {
                            System.err.println(line);
                            LOG.error(line);
                        }
                    }
                }
            } catch (IOException e) {
                LOG.error("Failed to process stdout/stderr read/write error :", e);
                throw new RuntimeException("stdout/stderr read/write error :" + e);
            } finally {
                IOUtils.closeQuietly(reader);
            }

            shellOutputLock.lock();
            if (outputType == OutputType.STDOUT) {
                shellOutput.stdOut = output.toString();
            } else if (outputType == OutputType.STDERR) {
                shellOutput.stdErr = output.toString();
            }
            shellOutputLock.unlock();
        }

        @Override
        public void close() {
            if (reader != null)
                IOUtils.closeQuietly(reader);
        }
    }
}
