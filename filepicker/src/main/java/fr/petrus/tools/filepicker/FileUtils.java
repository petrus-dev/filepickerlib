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

import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * The FileUtils class provides some static methods related to File MIME type.
 *
 * @author Pierre Sagne
 * @since 09.05.2015
 */
public class FileUtils {

    /**
     * Returns the MIME type of a document, from its URL.
     *
     * @param url the document url
     * @return the document MIME type
     */
    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        if (null==type) {
            type = "application/octet-stream";
        }
        return type;
    }

    /**
     * Returns whether a MIME type is accepted, according to the filter.
     *
     * @param mimeType       the MIME type
     * @param mimeTypeFilter the MIME type filter
     * @return true if the MIME type is compatible with the filter, false otherwise
     */
    public static boolean isMimeTypeAccepted(String mimeType, String mimeTypeFilter) {
        if (null==mimeTypeFilter) {
            return true;
        }
        if (mimeTypeFilter.equals("*/*")) {
            return true;
        }
        String[] mimeTypeComponents = mimeType.split("/");
        String[] mimeTypeFilterComponents = mimeTypeFilter.split("/");
        for (int i=0; i<2; i++) {
            if (!mimeTypeFilterComponents[i].equals("*")) {
                if (!mimeTypeFilterComponents[i].equalsIgnoreCase(mimeTypeComponents[i])) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns the parent folders of this file, starting from the root.
     *
     * @return the parent folders of this file, starting from the root
     */
    public static List<File> getParents(File file) {
        LinkedList<File> parents = new LinkedList<>();
        File parent = file.getParentFile();
        while (null!=parent) {
            parents.addFirst(parent);
            parent = parent.getParentFile();
        }
        return parents;
    }
}
