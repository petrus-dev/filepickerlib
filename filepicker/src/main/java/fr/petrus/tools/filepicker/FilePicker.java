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
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * The FilePicker activity, where most of the work is done.
 *
 * @author Pierre Sagne
 * @since 09.05.2015
 */
public class FilePicker extends Activity implements CreateDirDialogFragment.DialogListener {
    /**
     * The constant TAG, used for logging.
     */
    public static final String TAG = "FilePicker";

    /**
     * The name of the Intent parameter used to set the title of the FilePicker screen.
     */
    public static final String INTENT_PARAM_TITLE = "title";

    /**
     * The name of the Intent parameter used to set the title of the root directory.
     */
    public static final String INTENT_PARAM_ROOT_DIR = "rootDir";

    /**
     * The name of the Intent parameter used to set the MIME type filter for the files
     * which can be selected.
     */
    public static final String INTENT_PARAM_MIME_TYPE_FILTER = "mimeTypeFilter";

    /**
     * The name of the Intent parameter used to set the FilePicker selection mode.
     */
    public static final String INTENT_PARAM_SELECTION_MODE = "selectionMode";

    /**
     * The name of the Intent parameter used by this {@code Activity} to return the selection.
     */
    public static final String INTENT_RESULT_FILES = "resultFiles";

    /**
     * The selection mode for the {@code INTENT_PARAM_SELECTION_MODE} Intent parameter which lets
     * the user choose only one file.
     */
    public static final int SELECTION_MODE_SINGLE_FILE        = 0;

    /**
     * The selection mode for the {@code INTENT_PARAM_SELECTION_MODE} Intent parameter which lets
     * the user choose only one directory.
     */
    public static final int SELECTION_MODE_SINGLE_DIR         = 1;

    /**
     * The selection mode for the {@code INTENT_PARAM_SELECTION_MODE} Intent parameter which lets
     * the user choose multiple documents (files and directories).
     */
    public static final int SELECTION_MODE_MULTIPLE           = 2;

    /**
     * The selection mode for the {@code INTENT_PARAM_SELECTION_MODE} Intent parameter which lets
     * the user choose multiple documents (files and directories), and enables recursive selection
     * when selecting a directory.
     */
    public static final int SELECTION_MODE_MULTIPLE_RECURSIVE = 3;

    private String mimeTypeFilter;
    private int selectionMode;

    private String currentPath;
    private OrderBy orderBy;
    private FileCursorAdapter filesCursorAdapter;
    private Cursor currentFolderCursor;
    private HashSet<String> selection;

    private HorizontalScrollView foldersScrollView;
    private LinearLayout parentPathLayout;
    private Button currentFolderButton;
    private ProgressBar progressBar;
    private Button okButton;

    /*
     * the constants used to query the FileCursorAdapter
     */
    private static final String FILE_CURSOR_COLUMN_ID = "_id";
    private static final String FILE_CURSOR_COLUMN_PATH = "path";
    private static final String FILE_CURSOR_COLUMN_NAME = "name";
    private static final String FILE_CURSOR_COLUMN_MIMETYPE = "mimeType";
    private static final String FILE_CURSOR_COLUMN_SIZE = "size";
    private static final String[] FILE_DEFAULT_PROJECTION = new String[] {
            FILE_CURSOR_COLUMN_ID,
            FILE_CURSOR_COLUMN_PATH,
            FILE_CURSOR_COLUMN_NAME,
            FILE_CURSOR_COLUMN_MIMETYPE,
            FILE_CURSOR_COLUMN_SIZE
    };

    /*
     * the bundle parameter names used to retain the FilePicker Activity state
     */
    private static final String BUNDLE_CURRENT_FOLDER = "currentFolder";
    private static final String BUNDLE_ORDER_BY = "orderBy";
    private static final String BUNDLE_SELECTION = "selection";
    private static final String BUNDLE_PROGRESS_BAR_VISIBLE = "progressBarVisible";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_file_picker);

        Intent intent = getIntent();
        String title = intent.getStringExtra(INTENT_PARAM_TITLE);
        String rootDir = intent.getStringExtra(INTENT_PARAM_ROOT_DIR);
        mimeTypeFilter = intent.getStringExtra(INTENT_PARAM_MIME_TYPE_FILTER);
        selectionMode = intent.getIntExtra(INTENT_PARAM_SELECTION_MODE, SELECTION_MODE_SINGLE_FILE);

        if (null != title) {
            setTitle(title);
        }

        foldersScrollView = (HorizontalScrollView) findViewById(R.id.folders_scrollview);
        parentPathLayout = (LinearLayout) findViewById(R.id.parent_path_layout);
        currentFolderButton = (Button) findViewById(R.id.current_folder_button);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        okButton = (Button) findViewById(R.id.ok_button);
        okButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnResultOK();
            }
        });
        Button cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnResultCanceled();
            }
        });

        if (null== rootDir || rootDir.isEmpty()) {
            rootDir = "/";
        }

        selection = new LinkedHashSet<>();

        if (savedInstanceState != null) {
            currentPath = savedInstanceState.getString(BUNDLE_CURRENT_FOLDER);
            orderBy = OrderBy.valueOf(savedInstanceState.getString(BUNDLE_ORDER_BY));
            String[] selectionArray = savedInstanceState.getStringArray(BUNDLE_SELECTION);
            if (null != selectionArray) {
                for (String s : selectionArray) {
                    selection.add(s);
                }
            }
            showProgress(savedInstanceState.getBoolean(BUNDLE_PROGRESS_BAR_VISIBLE, false));
        } else {
            currentPath = rootDir;
            orderBy = OrderBy.NameAsc;
        }

        updateHeader();
        updateSelectedText();

        ListView filesListView = (ListView) findViewById(R.id.files_list_view);
        filesListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onClickFile(id);
            }
        });

        currentFolderCursor = getFileCursorByParentPath(currentPath, orderBy);
        filesCursorAdapter = new FileCursorAdapter(this, currentFolderCursor);
        filesListView.setAdapter(filesCursorAdapter);

        switch (selectionMode) {
            case SELECTION_MODE_SINGLE_DIR:
                select(currentPath);
                break;
            case SELECTION_MODE_SINGLE_FILE:
            case SELECTION_MODE_MULTIPLE:
            case SELECTION_MODE_MULTIPLE_RECURSIVE:
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (null==savedInstanceState) {
            savedInstanceState = new Bundle();
        }
        savedInstanceState.putString(BUNDLE_CURRENT_FOLDER, currentPath);
        savedInstanceState.putString(BUNDLE_ORDER_BY, orderBy.name());
        savedInstanceState.putStringArray(BUNDLE_SELECTION, hashSetToArray(selection));
        savedInstanceState.putBoolean(BUNDLE_PROGRESS_BAR_VISIBLE, progressBar.getVisibility() == View.VISIBLE);
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        switch (selectionMode) {
            case SELECTION_MODE_SINGLE_DIR:
                getMenuInflater().inflate(R.menu.menu_filepicker_folder, menu);
                return true;
        }
        getMenuInflater().inflate(R.menu.menu_filepicker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (R.id.action_create_folder == id) {
            CreateDirDialogFragment.showFragment(getFragmentManager());
            return true;
        } else if (R.id.action_order_by_mimetype_asc == id) {
            orderBy = OrderBy.NameAsc;
            updateFilesList();
            return true;
        } else if (R.id.action_order_by_name_desc == id) {
            orderBy = OrderBy.NameDesc;
            updateFilesList();
            return true;
        } else if (R.id.action_order_by_mimetype_asc == id) {
            orderBy = OrderBy.MimeTypeAsc;
            updateFilesList();
            return true;
        } else if (R.id.action_order_by_mimetype_desc == id) {
            orderBy = OrderBy.MimeTypeDesc;
            updateFilesList();
            return true;
        } else if (R.id.action_order_by_size_asc == id) {
            orderBy = OrderBy.SizeAsc;
            updateFilesList();
            return true;
        } else if (R.id.action_order_by_size_desc == id) {
            orderBy = OrderBy.SizeDesc;
            updateFilesList();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addItemRow(MatrixCursor cursor, long id, DirectoryItem item) {
        boolean displayRow = true;
        switch (selectionMode) {
            case SELECTION_MODE_SINGLE_FILE:
            case SELECTION_MODE_MULTIPLE:
            case SELECTION_MODE_MULTIPLE_RECURSIVE:
                displayRow = (null==item.getMimeType() || FileUtils.isMimeTypeAccepted(item.getMimeType(), mimeTypeFilter));
                break;
            case SELECTION_MODE_SINGLE_DIR:
                displayRow = (null==item.getMimeType());
                break;
        }
        if (displayRow) {
            MatrixCursor.RowBuilder row = cursor.newRow();
            row.add(FILE_CURSOR_COLUMN_ID, id);
            row.add(FILE_CURSOR_COLUMN_PATH, item.getPath());
            row.add(FILE_CURSOR_COLUMN_NAME, item.getName());
            row.add(FILE_CURSOR_COLUMN_MIMETYPE, item.getMimeType());
            row.add(FILE_CURSOR_COLUMN_SIZE, item.getSize());
        }
    }

    private Cursor getFileCursorByParentPath(String parentPath, OrderBy orderBy) {
        MatrixCursor cursor = new MatrixCursor(FILE_DEFAULT_PROJECTION);
        File folder = new File(parentPath);
        if (folder.isDirectory()) {
            DirectoryContent directoryContent = new DirectoryContent(folder);
            List<DirectoryItem> items = directoryContent.getOrderedItems(orderBy);
            long id = 1;
            for (DirectoryItem item : items) {
                addItemRow(cursor, id++, item);
            }
        }
        return cursor;
    }

    private DirectoryItem cursorToItem(Cursor cursor) {
        if (cursor.isAfterLast()) {
            return null;
        }
        return new DirectoryItem(
                cursor.getString(cursor.getColumnIndex(FILE_CURSOR_COLUMN_PATH)),
                cursor.getString(cursor.getColumnIndex(FILE_CURSOR_COLUMN_NAME)),
                cursor.getString(cursor.getColumnIndex(FILE_CURSOR_COLUMN_MIMETYPE)),
                cursor.getLong(cursor.getColumnIndex(FILE_CURSOR_COLUMN_SIZE)));
    }

    private DirectoryItem findItemById(Cursor cursor, long id) {
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                if (cursor.getLong(cursor.getColumnIndex(FILE_CURSOR_COLUMN_ID)) == id) {
                    return cursorToItem(cursor);
                }
                cursor.moveToNext();
            }
        }
        return null;
    }

    private void onClickFile(long id) {
        DirectoryItem item = findItemById(currentFolderCursor, id);
        if (null != item) {
            if (item.isDir()) {
                switch (selectionMode) {
                    case SELECTION_MODE_SINGLE_DIR:
                        select(item.getPath());
                        break;
                    case SELECTION_MODE_SINGLE_FILE:
                    case SELECTION_MODE_MULTIPLE:
                    case SELECTION_MODE_MULTIPLE_RECURSIVE:
                        break;
                }
                currentPath = item.getPath();
                updateHeader();
                updateFilesList();
            } else {
                toggleSelection(item.getPath());
            }
        }
    }

    private void updateFilesList() {
        currentFolderCursor = getFileCursorByParentPath(currentPath, orderBy);
        filesCursorAdapter.changeCursor(currentFolderCursor);
    }

    @Override
    public void onBackPressed() {
        if (!onBackToParent()) {
            super.onBackPressed();
        }
    }

    private boolean onBackToParent() {
        if (currentPath.equals("/")) {
            return false;
        }
        File file = new File(currentPath);
        goToFolder(file.getParent());
        return true;
    }

    private void goToFolder(String path) {
        currentPath = path;
        updateFilesList();
        updateHeader();
    }

    private void updateHeader() {
        parentPathLayout.removeAllViews();
        if (currentPath.equals("/")) {
            currentFolderButton.setText(currentPath);
        } else {
            File file = new File(currentPath);
            currentFolderButton.setText(file.getName());
            FrameLayout.LayoutParams nextIconLayoutParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER_VERTICAL);

            List<File> parents = FileUtils.getParents(file);
            for (final File folder : parents) {
                Button button = new Button(this);
                if (Build.VERSION.SDK_INT >= 23) {
                    button.setTextAppearance(android.R.style.TextAppearance_Small);
                } else {
                    button.setTextAppearance(this, android.R.style.TextAppearance_Small);
                }
                button.setTransformationMethod(null);
                String folderName = folder.getName();
                if (null==folderName || folderName.isEmpty()) {
                    folderName = "/";
                }
                button.setText(folderName);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        goToFolder(folder.getAbsolutePath());
                        updateHeader();
                    }
                });
                parentPathLayout.addView(button);

                ImageView nextIcon = new ImageView(this);
                nextIcon.setLayoutParams(nextIconLayoutParams);
                nextIcon.setImageResource(R.drawable.ic_next_black_24dp);
                parentPathLayout.addView(nextIcon);
            }
        }

        foldersScrollView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                foldersScrollView.removeOnLayoutChangeListener(this);
                foldersScrollView.fullScroll(View.FOCUS_RIGHT);
            }
        });
    }

    private void updateSelectedText() {
        if (selection.isEmpty()) {
            okButton.setVisibility(View.GONE);
        } else {
            okButton.setVisibility(View.VISIBLE);
            switch (selectionMode) {
                case SELECTION_MODE_SINGLE_FILE:
                case SELECTION_MODE_SINGLE_DIR:
                    break;
                case SELECTION_MODE_MULTIPLE:
                case SELECTION_MODE_MULTIPLE_RECURSIVE:
                    okButton.setText(getString(R.string.select_button_text, selection.size()));
                    break;
            }
        }
    }

    private static String[] hashSetToArray(HashSet<String> hashSet) {
        return hashSet.toArray(new String[hashSet.size()]);
    }

    private void returnResultOK() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putStringArray(INTENT_RESULT_FILES, hashSetToArray(selection));
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void returnResultCanceled() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private boolean isSelected(String path) {
        return selection.contains(path);
    }

    private void select(String path) {
        switch (selectionMode) {
            case SELECTION_MODE_SINGLE_FILE:
            case SELECTION_MODE_SINGLE_DIR:
                selection.clear();
                selection.add(path);
                updateSelectedText();
                break;
            case SELECTION_MODE_MULTIPLE: {
                showProgress(true);
                SelectionAsyncTask task = new SelectionAsyncTask(true, false, mimeTypeFilter);
                task.execute(new Selector(path, SelectionActionType.Add));
                break;
            }
            case SELECTION_MODE_MULTIPLE_RECURSIVE: {
                showProgress(true);
                SelectionAsyncTask task = new SelectionAsyncTask(true, true, mimeTypeFilter);
                task.execute(new Selector(path, SelectionActionType.Add));
                break;
            }
        }
    }

    private void deselect(String path) {
        switch (selectionMode) {
            case SELECTION_MODE_SINGLE_FILE:
            case SELECTION_MODE_SINGLE_DIR:
                selection.remove(path);
                updateSelectedText();
                break;
            case SELECTION_MODE_MULTIPLE: {
                showProgress(true);
                SelectionAsyncTask task = new SelectionAsyncTask(true, false, mimeTypeFilter);
                task.execute(new Selector(path, SelectionActionType.Remove));
                break;
            }
            case SELECTION_MODE_MULTIPLE_RECURSIVE: {
                showProgress(true);
                SelectionAsyncTask task = new SelectionAsyncTask(true, true, mimeTypeFilter);
                task.execute(new Selector(path, SelectionActionType.Remove));
                break;
            }
        }
    }

    private void toggleSelection(String path) {
        if (isSelected(path)) {
            deselect(path);
        } else {
            select(path);
        }
    }

    /**
     * Sets whether the progression indicator should be shown or not.
     *
     * @param visible the progression bar visibility
     */
    public void showProgress(boolean visible) {
        if (visible) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    /**
     * The EventBus Callback receiving SelectionEvents from the SelectionAsyncTask.
     *
     * @param event the selection event
     */
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(SelectionEvent event) {
        EventBus.getDefault().removeStickyEvent(event);
        for (Selection eventSelection : event.getSelections()) {
            switch (eventSelection.getActionType()) {
                case Add:
                    selection.addAll(eventSelection.getSelection());
                    break;
                case Remove:
                    selection.removeAll(eventSelection.getSelection());
                    break;
            }
        }
        showProgress(false);
        updateSelectedText();
        updateFilesList();
    }

    @Override
    public void onCreateDirName(String name) {
        if (null!=name && !name.isEmpty()) {
            File newFolder = new File(new File(currentPath), name);
            if (!newFolder.exists()) {
                newFolder.mkdir();
                updateFilesList();
            }
        }
    }

    /**
     * This {@code CursorAdapter} populates the ListView which displays the documents.
     */
    public class FileCursorAdapter extends CursorAdapter {

        private static final int KB = 1024;
        private static final int MB = 1024*KB;
        private static final int GB = 1024*MB;

        private final LayoutInflater cursorInflater;

        /**
         * Creates a new {@code FileCursorAdapter} instance.
         *
         * @param context the context
         * @param cursor  the cursor
         */
        public FileCursorAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
            cursorInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return cursorInflater.inflate(R.layout.row_file, parent, false);
        }

        /**
         * Returns the size in a textual form.
         *
         * @param size the size (in bytes) of an item
         * @return the textual form of the item size
         */
        public String getSizeText(long size) {
            String sizeText;
            if (size >= GB) {
                sizeText = String.format("%.2f GB", (size / (float) GB));
            } else if (size >= MB) {
                sizeText = String.format("%.2f MB", (size / (float) MB));
            } else if (size >= KB) {
                sizeText = String.format("%.2f KB", (size / (float) KB));
            } else {
                sizeText = size + " B";
            }
            return sizeText;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final DirectoryItem item = cursorToItem(cursor);

            if (null==item) {
                return;
            }

            CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
            ImageView icon = (ImageView) view.findViewById(R.id.icon);
            TextView textViewName = (TextView) view.findViewById(R.id.name_text);
            TextView textViewLeft = (TextView) view.findViewById(R.id.left_text);
            TextView textViewRight = (TextView) view.findViewById(R.id.right_text);

            switch (selectionMode) {
                case SELECTION_MODE_SINGLE_DIR:
                case SELECTION_MODE_SINGLE_FILE:
                    checkBox.setVisibility(View.GONE);
                    break;
                case SELECTION_MODE_MULTIPLE:
                case SELECTION_MODE_MULTIPLE_RECURSIVE:
                    checkBox.setVisibility(View.VISIBLE);
                    checkBox.setChecked(isSelected(item.getPath()));
                    checkBox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            toggleSelection(item.getPath());
                        }
                    });
                    break;
            }

            textViewName.setText(item.getName());

            if (item.isDir()) {
                icon.setImageResource(R.drawable.ic_folder_black_36dp);
                textViewLeft.setVisibility(View.GONE);
                textViewRight.setVisibility(View.GONE);
                textViewRight.setVisibility(View.GONE);
            } else {
                icon.setImageResource(R.drawable.ic_file_black_36dp);
                textViewLeft.setVisibility(View.VISIBLE);
                textViewLeft.setText(item.getMimeType());

                textViewRight.setVisibility(View.VISIBLE);
                textViewRight.setText(getString(R.string.size_text, getSizeText(item.getSize())));
            }
        }
    }
}
