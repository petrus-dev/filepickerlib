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

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This class holds the content of a directory (the documents it contains).
 *
 * @author Pierre Sagne
 * @since 09.05.2015
 */
public class DirectoryContent {
    /**
     * The constant TAG used for logging.
     */
    public static final String TAG = "DirectoryContent";

    private List<DirectoryItem> subDirItems;
    private List<DirectoryItem> fileItems;

    /**
     * Creates a new empty {@code DirectoryContent} instance.
     */
    public DirectoryContent() {
        subDirItems = new ArrayList<>();
        fileItems = new ArrayList<>();
    }

    /**
     * Creates a new {@code DirectoryContent} instance from the content of the given {@code directory}.
     *
     * @param directory the {@code File} which content will be used to populate the new
     *                  {@code DirectoryContent}
     */
    public DirectoryContent(File directory) {
        this();
        if (directory.isDirectory()) {
            File[] children = directory.listFiles();
            if (children != null) {
                for (File child : children) {
                    try {
                        if (child.isDirectory()) {
                            subDirItems.add(new DirectoryItem(child.getCanonicalPath(),
                                    child.getName(), null, 0));
                        } else {
                            fileItems.add(new DirectoryItem(child.getCanonicalPath(),
                                    child.getName(),
                                    FileUtils.getMimeType(child.getName()),
                                    child.length()));
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Could not get file or directory canonical path", e);
                    }
                }
            }
        }
    }

    /**
     * Returns the list of the items this {@code DirectoryContent} contains, sorted with the given
     * {@code orderBy} criterion.
     *
     * @param orderBy the sorting criterion used to generate the ordered list
     * @return the list of the items this {@code DirectoryContent} contains, sorted with the given
     *         {@code orderBy} criterion
     */
    public List<DirectoryItem> getOrderedItems(OrderBy orderBy) {
        List<DirectoryItem> tempSubDirItems = new ArrayList<>();
        tempSubDirItems.addAll(subDirItems);
        List<DirectoryItem> tempFileItems = new ArrayList<>();
        tempFileItems.addAll(fileItems);
        switch(orderBy) {
            case NameAsc:
            case NameDesc:
                // sort the two lists by the specified parameter
                sortList(tempSubDirItems, orderBy);
                sortList(tempFileItems, orderBy);
                break;
            case SizeAsc:
            case SizeDesc:
            case MimeTypeAsc:
            case MimeTypeDesc:
                // sort the two lists by ascending name order
                sortList(tempSubDirItems, OrderBy.NameAsc);
                sortList(tempFileItems, OrderBy.NameAsc);
                // sort the file list by the specified parameter
                sortList(tempFileItems, orderBy);
                break;
        }
        List<DirectoryItem> items = new ArrayList<>();
        items.addAll(tempSubDirItems);
        items.addAll(tempFileItems);
        return items;
    }

    private static void sortList(List<DirectoryItem> items, final OrderBy orderBy) {
        Collections.sort(items, new Comparator<DirectoryItem>() {
            @Override
            public int compare(DirectoryItem item1, DirectoryItem item2) {
                return item1.compareTo(orderBy, item2);
            }
        });
    }
}
