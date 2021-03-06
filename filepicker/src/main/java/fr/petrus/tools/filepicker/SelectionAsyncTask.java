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

import android.os.AsyncTask;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 * This {@code AsyncTask} generates a list of selected documents in the background.
 *
 * @author Pierre Sagne
 * @since 09.05.2015
 */
public class SelectionAsyncTask extends AsyncTask<Selector, Void, Void> {

    private final boolean enableDirSelection;
    private final boolean enableDirRecursiveSelection;
    private final String mimeTypeFilter;

    /**
     * Creates a new {@code SelectionAsyncTask} instance.
     *
     * @param enableDirSelection          if true, directories can be selected
     * @param enableDirRecursiveSelection if true, directory selection is recursive (if a directory
     *                                    is selected, all of its contents is selected too)
     * @param mimeTypeFilter              the MIME type filter for files
     */
    public SelectionAsyncTask(boolean enableDirSelection,
                              boolean enableDirRecursiveSelection,
                              String mimeTypeFilter) {
        this.enableDirSelection = enableDirSelection;
        this.enableDirRecursiveSelection = enableDirRecursiveSelection;
        this.mimeTypeFilter = mimeTypeFilter;
    }

    @Override
    protected Void doInBackground(Selector... selectors) {
        Selection addSelection = new Selection(SelectionActionType.Add);
        Selection removeSelection = new Selection(SelectionActionType.Remove);

        for (Selector selector : selectors) {
            HashSet<String> selected = getSelection(selector.getPath());
            switch (selector.getActionType()) {
                case Add:
                    addSelection.addToSelection(selected);
                    break;
                case Remove:
                    removeSelection.addToSelection(selected);
                    break;
            }
        }

        SelectionEvent selectionEvent = new SelectionEvent();
        if (!addSelection.getSelection().isEmpty()) {
            selectionEvent.addSelection(addSelection);
        }
        if (!removeSelection.getSelection().isEmpty()) {
            selectionEvent.addSelection(removeSelection);
        }

        EventBus.getDefault().postSticky(selectionEvent);
        return null;
    }

    private HashSet<String> getSelection(String path) {
        HashSet<String> selected = new LinkedHashSet<>();
        File file = new File(path);
        if (file.isDirectory()) {
            if (enableDirSelection) {
                selected.add(path);
                if (enableDirRecursiveSelection) {
                    File[] children = file.listFiles();
                    if (children != null) {
                        for (File child : children) {
                            try {
                                selected.addAll(getSelection(child.getCanonicalPath()));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        } else {
            if (FileUtils.isMimeTypeAccepted(FileUtils.getMimeType(path), mimeTypeFilter)) {
                selected.add(path);
            }
        }
        return selected;
    }
}
