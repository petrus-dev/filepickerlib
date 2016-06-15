/*
 * Copyright Pierre Sagne (09 may 2015)
 *
 * petrus.dev.fr@gmail.com
 *
 * This software is a computer program whose purpose is to let the user
 * select files and/ or folders in an Android application.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */

package fr.petrus.tools.filepicker;

/**
 * This class holds information about a directory child document.
 *
 * @author Pierre Sagne
 * @since 09.05.2015
 */
public class DirectoryItem {
    private String path;
    private String name;
    private String mimeType;
    private long size;

    /**
     * Creates a new {@code DirectoryItem} instance.
     *
     * @param path     the document path
     * @param name     the document name
     * @param mimeType the document MIME type
     * @param size     the document size
     */
    public DirectoryItem(String path, String name, String mimeType, long size) {
        this.path = path;
        this.name = name;
        this.mimeType = mimeType;
        this.size = size;
    }

    /**
     * Returns this document path.
     *
     * @return this document path
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns this document name.
     *
     * @return this document name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns this document MIME type.
     *
     * @return this document MIME type
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Returns this document size.
     *
     * @return this document size
     */
    public long getSize() {
        return size;
    }

    /**
     * Returns whether this document is a directory or not.
     *
     * @return true if this document is a directory, false otherwise
     */
    public boolean isDir() {
        return null == mimeType;
    }

    /**
     * Compares this DirectoryItem with another one, given an comparison criterion.
     *
     * @param orderBy the comparison criterion
     * @param item2   the second document to compare this one with
     * @return the comparison result : -1 if this item is first, 0 if it is equal to item2 based
     *         on the criterion, 1 if it is after item2
     */
    public int compareTo(OrderBy orderBy, DirectoryItem item2) {
        switch(orderBy) {
            case NameAsc:
                return getName().compareTo(item2.getName());
            case NameDesc:
                return - getName().compareTo(item2.getName());
            case SizeAsc:
                if (getSize() < item2.getSize()) {
                    return -1;
                }
                if (getSize() > item2.getSize()) {
                    return 1;
                }
                return 0;
            case SizeDesc:
                if (getSize() > item2.getSize()) {
                    return -1;
                }
                if (getSize() < item2.getSize()) {
                    return 1;
                }
                return 0;
            case MimeTypeAsc:
                return getMimeType().compareTo(item2.getMimeType());
            case MimeTypeDesc:
                return - getMimeType().compareTo(item2.getMimeType());
        }
        return getName().compareTo(item2.getName());
    }
}
