package com.d5.jobs.common;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * an implementation of the IItemDataStorage that saves/reads items from hadoop HDFS
 * Created by y on 24/06/15.
 */
public class HdfsItemDataStorage implements IItemDataStorage, Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(HdfsItemDataStorage.class);

    private HdfsHelper hdfs;
    private final Path itemsStoragePath;

    public HdfsItemDataStorage() throws IOException {
        this.hdfs = new HdfsHelper();

        this.itemsStoragePath = new Path(hdfs.getRootStoragePath(), new Config().getString(Config.Keys.ITEMS_STORAGE_PATH));
    }

    public HdfsHelper getHdfs() {
        return hdfs;
    }

    /**
     * Create a new file on the file-system
     * @param pathStr The path to create the file in.
     * @return an output stream to write the file with
     * (the one that gets the returned OutputStream should make sure to close it after done writing to it)
     * @throws IOException If something goes wrong with creating the file.
     */
    public OutputStream createItemFile(String pathStr) throws IOException {
        return hdfs.createFile(itemsStoragePath, pathStr);
    }

    public boolean removeFileFromItemStorage(String filePath) throws IOException {
        return hdfs.removeFileFromStorage(itemsStoragePath, filePath);
    }

    /**
     * Gets a InputStream of a file on the file-system to read from.
     * @param itemId The path of the file to read
     * @return an InputStram to read the data from the file.
     * (the one that gets the returned InputStream should make sure to close it after done writing to it)
     * @throws IOException If something goes wrong when trying to read and get the stream of the existing file.
     */
    public InputStream getItemFile(String itemId) throws IOException {
        return hdfs.getFile(itemsStoragePath, itemId);
    }

    public String getItemsStoragePath() {
        return hdfs.getFsName() + itemsStoragePath.toString();
    }

    public String getItemsStorageRelativePath() {
        return itemsStoragePath.toString();
    }

    public String saveItemTempFile(InputStream inputStream) throws IOException{
        UUID uuid = UUID.randomUUID();
        Path tempPath = new Path("tmp", uuid.toString());
        Path path = new Path(itemsStoragePath, tempPath);
        try (OutputStream dfsFile = hdfs.getHadoopFileSystem().create(path)) {
            IOUtils.copy(inputStream, dfsFile);
        }
        return tempPath.toString();
    }

    public boolean deleteItemFile(String path) throws IOException{
        return hdfs.removeFileFromStorage(itemsStoragePath, path);
    }

    public List<String> getFilesInPathByItemId(String itemId) throws IOException {
        return hdfs.getFiles(new Path(itemsStoragePath, itemId));
    }


    @Override
    public String saveItemData(String source, InputStream itemDataStream) throws IOException {
        return saveItemData(source, itemDataStream, null);
    }

    @Override
    public String saveItemData(String source, InputStream itemDataStream, String fileName) throws IOException {
        String newPath = generateNewPath(source, fileName);
        LOG.info("saving item data to:{}", newPath);
        try (OutputStream dfsFile = hdfs.createFile(itemsStoragePath, newPath)) {
            IOUtils.copy(itemDataStream, dfsFile);
        }
        return newPath;
    }

    @Override
    public String saveItemData(String source, byte[] itemData) throws IOException {
       return saveItemData(source, itemData, null);
    }

    @Override
    public String saveItemData(String source, byte[] itemData, String fileName) throws IOException {
        try (InputStream byteStream = new ByteArrayInputStream(itemData)) {
            return saveItemData(source, byteStream, fileName);
        }
    }

    @Override
    public String resolvePath(String itemId) {
        String initialPath = getItemsStoragePath();
        StringBuilder path = new StringBuilder(initialPath);

        if (itemId.startsWith(initialPath))
            return itemId;

        if (!initialPath.endsWith("/") && !itemId.startsWith("/"))
            path.append("/");

        return path.append(itemId).toString();
    }

    @Override
    public String resolveItemId(String path) {
        String storagePath = getItemsStoragePath();
        if (path != null && path.startsWith(storagePath))
            return path.substring(storagePath.length() + 1);

        throw new IllegalArgumentException("Invalid Path: " + path);
    }

    /**
     * Resolves the UUID only of the full item id - for ones using the uuid only as the item ID.
     * @param itemId the full item ID
     * @return the UUID portion of the item id
     * @throws IllegalArgumentException if the itemId is not valid.
     */
    public static UUID resolveUUID(String itemId) {
        int lastSlash = itemId.lastIndexOf("/");
        int lastUnderScore = itemId.lastIndexOf("_");
        if (lastSlash > 0 && lastUnderScore < lastSlash) {
            LOG.info("resolveUUID: {} from item id {} ", itemId.substring(lastSlash + 1), itemId);
            return UUID.fromString(itemId.substring(lastSlash + 1));
        } else if (lastSlash > 0) {
            final String itemIdSubstring = itemId.substring(lastSlash + 1, lastUnderScore);
            LOG.info("resolveUUID: {} from item id combined with entity id {} ", itemId.substring(lastSlash+1, lastUnderScore), itemId);
            return UUID.fromString(itemIdSubstring.split("_")[0]);
        }
        throw new IllegalArgumentException("input string is not a valid path " + itemId);
    }

    public String getStoragePath(){
       return  getItemsStoragePath();
    }

    @Override
    public void close() throws IOException {
        hdfs.close();
    }

    private String generateNewPath(String source, String fileName) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        fileName = (fileName == null) ? generateUuidFileName() : fileName;

        return String.format("%s%04d/%02d/%02d/%s",
                formatSource(source),
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH),
                fileName);

    }

    public String getGeneratedFolder(String source){
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());

        return String.format("%s%04d/%02d/%02d/",
                formatSource(source),
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH));
    }

    public String generateUuidFileName(){
        return new com.eaio.uuid.UUID().toString();
    }

    private String formatSource(String source) {
        if (source == null) {
            return "";
        } else {
            // remove trailing and prefix slashes
            source = source.replaceAll("/$", "");
            source = source.replaceAll("^/", "");
            // replace mid slashes with "_"
            source = source.replaceAll("/", "_");
        }
        return source + "/";
    }


}
