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

import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 * This class holds a set of selected documents, along with a selection action (add or remove).
 *
 * @author Pierre Sagne
 * @since 09.05.2015
 */
public class Selection {
    private final HashSet<String> selection;
    private SelectionActionType actionType;

    /**
     * Creates a new empty {@code Selection} instance.
     *
     * @param actionType the action type
     */
    public Selection(SelectionActionType actionType) {
        this.selection = new LinkedHashSet<>();
        this.actionType = actionType;
    }

    /**
     * Adds the path of a document to this selection.
     *
     * @param path the path of the document to adds to this selection
     */
    public void addToSelection(String path) {
        selection.add(path);
    }

    /**
     * Adds a group of paths of documents to the selection.
     *
     * @param selected the group of paths of documents to the selection
     */
    public void addToSelection(HashSet<String> selected) {
        selection.addAll(selected);
    }

    /**
     * Returns the paths of the documents in this selection.
     *
     * @return the paths of the documents in this selection
     */
    public HashSet<String> getSelection() {
        return selection;
    }

    /**
     * Returns the action type (Add or Remove)
     *
     * @return the action type (Add or Remove)
     */
    public SelectionActionType getActionType() {
        return actionType;
    }
}