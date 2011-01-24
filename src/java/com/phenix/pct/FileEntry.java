package com.phenix.pct;

import java.text.MessageFormat;
import java.util.Date;

/**
 * Class representing a file entry in a PL file
 */
public class FileEntry {
    private final boolean valid;
    private final String fileName;
    private final long modDate, addDate;
    private final int offset, size, tocSize;

    /**
     * Invalid file entry - Will be skipped in entries list
     * @param tocSize 
     */
    public FileEntry(int tocSize) {
        this.tocSize = tocSize;
        valid = false;
        fileName = "";
        modDate = addDate = offset = 0;
        size = 0;
    }

    public FileEntry(String fileName, long modDate, long addDate, int offSet, int size,
            int tocSize) {
        this.valid = true;
        this.fileName = fileName;
        this.modDate = modDate;
        this.addDate = addDate;
        this.offset = offSet;
        this.size = size;
        this.tocSize = tocSize;
    }

    public String getFileName() {
        return fileName;
    }

    public int getSize() {
        return size;
    }

    public long getModDate() {
        return modDate;
    }

    public long getAddDate() {
        return addDate;
    }

    public int getOffset() {
        return offset;
    }

    public int getTocSize() {
        return tocSize;
    }

    public boolean isValid() {
        return valid;
    }

    public String toString() {
        return MessageFormat
                .format(Messages.getString("PLReader.6"), new Object[]{this.fileName, Integer.valueOf(size), new Date(addDate), new Date(modDate), Long.valueOf(offset)}); //$NON-NLS-1$
    }
}