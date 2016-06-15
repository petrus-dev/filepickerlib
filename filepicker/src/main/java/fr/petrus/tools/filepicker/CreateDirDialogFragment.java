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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

/**
 * The {@code DialogFragment} used to ask for name of a new directory.
 *
 * @author Pierre Sagne
 * @since 09.05.2015
 */
public class CreateDirDialogFragment extends DialogFragment {
    /**
     * The constant TAG used for logging.
     */
    public static final String TAG = "CreateDirDialogFragment";

    /**
     * Creates a new {@code CreateDirDialogFragment} instance.
     */
    public CreateDirDialogFragment() {
        super();
        setRetainInstance(true);
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    private DialogListener dialogListener;

    /**
     * The interface used to send back information to the Activity.
     */
    public interface DialogListener {
        /**
         * This method is called when the name of the new directory is entered.
         *
         * @param name the name of the new directory
         */
        void onCreateDirName(String name);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof DialogListener) {
            dialogListener = (DialogListener) activity;
        } else {
            throw new ClassCastException(activity.toString()
                    + " must implement "+DialogListener.class.getName());
        }
    }

    @Override
    public void onDetach() {
        dialogListener = null;
        super.onDetach();
    }

    private EditText editText = null;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.fragment_create_dir, null);
        dialogBuilder.setView(view);

        editText = (EditText) view.findViewById(R.id.edit_text);
        editText.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (isEditorValidated(actionId, event)) {
                            done();
                            return true; // consume.
                        }
                        return false; // pass on to other listeners.
                    }
                });

        dialogBuilder.setTitle(getString(R.string.create_dir_fragment_title));
        dialogBuilder.setPositiveButton(
                getString(R.string.ok_button_text),
                new AlertDialog.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int which) {
                        done();
                    }
                });

        dialogBuilder.setNegativeButton(getString(R.string.cancel_button_text), null);

        AlertDialog dialog = dialogBuilder.create();
        return dialog;
    }

    private void done() {
        String text = editText.getText().toString();
        dialogListener.onCreateDirName(text);
        dismiss();
    }

    /**
     * Creates and shows the {@code CreateDirDialogFragment}.
     *
     * @param fragmentManager the FragmentManager of the Activity
     * @return the CreateDirDialogFragment
     */
    public static CreateDirDialogFragment showFragment(FragmentManager fragmentManager) {
        CreateDirDialogFragment fragment = new CreateDirDialogFragment();
        fragment.show(fragmentManager, TAG);
        return fragment;
    }

    /**
     * Determines if the editor field was validated by using the virtual keyboard action, or the
     * RETURN / ENTER key.
     *
     * @param actionId the actionId passed by the Editor listener
     * @param event    the event passed by the Editor listener
     * @return true if the field was validated, false otherwise
     */
    public static boolean isEditorValidated(int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
            return true;
        }
        if (null!=event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN  && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                if (!event.isShiftPressed()) {
                    return true;
                }
            }
        }
        return false;
    }
}
