package com.d5.jobs.common;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by y on 23/06/15.
 */
public interface IItemDataStorage extends Closeable {
   /**
     * Save an Item to the Items repository
     * @param itemDataStream an InputStream of the raw data to create.
     * @return the ID of the saved item.
     * @throws IOException If something goes wrong.
     */
    String saveItemData(String source, InputStream itemDataStream) throws IOException;

    String saveItemData(String source, InputStream itemDataStream, String entityId) throws IOException;

    /**
     * Save an Item to the Items repository
     * @param itemData a byte-array of the raw data to create.
     * @return the ID of the saved item.
     * @throws IOException If something goes wrong.
     */
    String saveItemData(String source, byte[] itemData) throws IOException;

    String saveItemData(String source, byte[] itemData, String entityId) throws IOException;

    /**
     * resolve the real path of the item to be used with external sources.
     * @param itemId the item id.
     * @return the real path to the item.
     */
    String resolvePath(String itemId);

    /**
     * Opposite to resolvePath. gets a path and resolves the item id.
     * @param path the full path.
     * @return the item id.
     * @throws IllegalArgumentException if the path is not an item path.
     */
    String resolveItemId(String path);
}
