package com.d5.jobs.common;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * A Helper Class to perform hadoop dfs operations
 * (init, read-file, write-file)
 * Created by y on 24/06/15.
 */
public class HdfsHelper implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(HdfsHelper.class);

    private static final String HADOOP_CONF_DIR = "HADOOP_CONF_DIR";
    private static final String HDFS_SITE_XML = "hdfs-site.xml";

//    protected Configuration hadoopConf;
    private final FileSystem hadoopFileSystem;
    private final String fsName;
    protected final Path rootStoragePath;
    private final Config config;
    /**
     *
     * @throws IOException
     */
    public HdfsHelper() throws IOException {
        config = new Config();

        this.hadoopFileSystem = initFileSystem(config);
        this.rootStoragePath = new Path(config.getString(Config.Keys.HDFS_ROOT_STORAGE_PATH));
        this.fsName = config.getString(Config.Keys.FS_DEFAULT_NAME);
    }

    private FileSystem initFileSystem(Config configuration) throws IOException {
//        String hadoopUserName = configuration.getString(Config.Keys.HADOOP_USER_NAME);
//        System.setProperty("HADOOP_USER_NAME", hadoopUserName);
//        hadoopConf = createHadoopConf(configuration);
        String hdfsURI = config.getString(Config.Keys.FS_DEFAULT_NAME);
        LOG.info("using hdfs uri:" + hdfsURI);
        return FileSystem.get(URI.create(hdfsURI), new Configuration());
    }

    private Configuration createHadoopConf(Config config) {
        Configuration hadoopConf = new Configuration();
        hadoopConf.set(Config.Keys.FS_DEFAULT_NAME.getKey(), config.getString(Config.Keys.FS_DEFAULT_NAME));
        hadoopConf.set("fs.defaultFS", config.getString(Config.Keys.FS_DEFAULT_NAME) + "/" +  config.getString(Config.Keys.HADOOP_USER_NAME));
        hadoopConf.set(Config.Keys.DFS_REPLICATION.getKey(), config.getString(Config.Keys.DFS_REPLICATION));

        String hadoopConfDir = System.getenv(HADOOP_CONF_DIR);
        LOG.info("Got this dir {} from environment dir {} ", hadoopConfDir, HADOOP_CONF_DIR);
//        if (hadoopConfDir == null || !Files.exists(Paths.get(hadoopConfDir, HDFS_SITE_XML))) {
//            LOG.error("There should be " + HDFS_SITE_XML + " in " + hadoopConfDir);
//        } else {
//            LOG.info("Adding {} to hadoop config", Paths.get(hadoopConfDir, HDFS_SITE_XML));
//            hadoopConf.addResource(new Path(hadoopConfDir, HDFS_SITE_XML));
//        }

        return hadoopConf;
    }

    public Path getRootStoragePath() {
        return rootStoragePath;
    }

    public String getFsName() {
        return fsName;
    }

    public Path getItemStoragePath() {
        return new Path(rootStoragePath.toString(), config.getString(Config.Keys.ITEMS_STORAGE_PATH));
    }

    public FileSystem getHadoopFileSystem() {
        return hadoopFileSystem;
    }

    /**
     * Create a new file on the file-system
     * @param pathStr The path to create the file in.
     * @return an output stream to write the file with
     * (the one that gets the returned OutputStream should make sure to close it after done writing to it)
     * @throws IOException If something goes wrong with creating the file.
     */
    public OutputStream createFile(Path initialPath, String pathStr) throws IOException {
        Path path = new Path(initialPath, pathStr);
        return hadoopFileSystem.create(path);
    }

    public OutputStream createRootFolderFile(String pathStr) throws IOException {
        Path path = new Path(rootStoragePath, pathStr);
        return hadoopFileSystem.create(path);
    }

    public boolean removeFileFromRootFolderStorage(String filePath) throws IOException {
        return removeFileFromStorage(rootStoragePath, filePath);
    }

    public boolean exists(String path) throws IOException {
        return hadoopFileSystem.exists(new Path(path));
    }

    public boolean removeFileFromStorage(Path initialPath, String filePath) throws IOException {
        Path path = new Path(initialPath, filePath);
        LOG.info("removing files from path {}", path);
        return !hadoopFileSystem.exists(path) || hadoopFileSystem.delete(path, true);
    }

    public boolean removeFileFromStorage(String filePath) throws IOException {
        LOG.info("removing files from path {}", filePath);
        return !hadoopFileSystem.exists(new Path(filePath)) || hadoopFileSystem.delete(new Path(filePath), true);
    }


    /**
     * Gets a InputStream of a file on the file-system to read from.
     * @param itemId The path of the file to read
     * @return an InputStram to read the data from the file.
     * (the one that gets the returned InputStream should make sure to close it after done writing to it)
     * @throws IOException If something goes wrong when trying to read and get the stream of the existing file.
     */
    public InputStream getFile(Path initialPath, String itemId) throws IOException {
        if (itemId.startsWith(initialPath.toString()) || itemId.startsWith(fsName)){
            return hadoopFileSystem.open(new Path(itemId));
        }else{
            return hadoopFileSystem.open(new Path(initialPath, itemId));
        }
    }

    public void copyToLocalFile(Path src, Path dst) throws IOException {
        hadoopFileSystem.copyToLocalFile(src, dst);
    }

    public String getRootFilePath(String path) {
        return new Path(new Path(fsName, rootStoragePath), path).toString();
    }

    @Override
    public void close() throws IOException {
        LOG.warn("HDFS is closing !!");
        hadoopFileSystem.close();
    }

    public boolean recoverLease(String path) throws IOException {
        if (hadoopFileSystem instanceof DistributedFileSystem) {
            return ((DistributedFileSystem) hadoopFileSystem).recoverLease(new Path(path));
        } else {
            return false;
        }
    }

    public List<String> getRootFolderFilesInPath(String relativeStoragePath) throws IOException {
        return getFilesInPath(rootStoragePath, relativeStoragePath);
    }

    public List<String> getFilesInPath(Path initialPath, String relativeStoragePath) throws IOException {
        return getFiles(new Path(initialPath, relativeStoragePath));
    }


    public List<String> getFiles(Path path) throws IOException {
        //the second boolean parameter here sets the recursion to true
        //path = new Path("hdfs://192.168.45.217:8020"+path.toString());
        ArrayList<String> filesInPath = new ArrayList<>();
        if (hadoopFileSystem.exists(path)){
                RemoteIterator<LocatedFileStatus> fileStatusListIterator = hadoopFileSystem.listFiles(
                        path, true);
            while (fileStatusListIterator.hasNext()) {
                LocatedFileStatus fileStatus = fileStatusListIterator.next();
                filesInPath.add(fileStatus.getPath().toString());
            }
        }
        return filesInPath;
    }

    public List<Path> getFilePaths(Path path, boolean recursive) throws IOException {
        //the second boolean parameter here sets the recursion to true
        //path = new Path("hdfs://192.168.45.217:8020"+path.toString());
        ArrayList<Path> filesInPath = new ArrayList<>();
        if (hadoopFileSystem.exists(path)){
            RemoteIterator<LocatedFileStatus> fileStatusListIterator = hadoopFileSystem.listFiles(
                    path, recursive);
            while (fileStatusListIterator.hasNext()) {
                LocatedFileStatus fileStatus = fileStatusListIterator.next();
                filesInPath.add(fileStatus.getPath());
            }
        }
        return filesInPath;
    }
}