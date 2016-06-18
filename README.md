# Android File Picker #
Android component to select files and/ or folders.

## Build it ##
1. Get the sources
```bash
git clone https://github.com/petrus-dev/filepickerlib.git
```
2. Build it and add it to your local maven repo
```bash
cd filepickerlib
../gradlew mvnInstall
```

## Use it in your project ##
1. Add it to your project dependencies in the build.gradle file :
```groovy
compile "fr.petrus.tools:filepicker:0.9.7@aar"
```
2. Call it like this:
* To open one existing file
```java
Intent intent = new Intent(this, FilePicker.class);
intent.putExtra(FilePicker.INTENT_PARAM_TITLE, "The title displayed in the file picker");
intent.putExtra(FilePicker.INTENT_PARAM_ROOT_DIR, "/the/starting/path);
intent.putExtra(FilePicker.INTENT_PARAM_MIME_TYPE_FILTER, "*/*"); // if you want to select audio files only : "audio/*"
intent.putExtra(FilePicker.INTENT_PARAM_SELECTION_MODE, FilePicker.SELECTION_MODE_SINGLE_FILE);
startActivityForResult(intent, 42); // the requestCode used to get the results (see below)
```
* To open multiple existing files and/or folders
```java
Intent intent = new Intent(this, FilePicker.class);
intent.putExtra(FilePicker.INTENT_PARAM_TITLE, "The title displayed in the file picker");
intent.putExtra(FilePicker.INTENT_PARAM_ROOT_DIR, "/the/starting/path);
intent.putExtra(FilePicker.INTENT_PARAM_MIME_TYPE_FILTER, "*/*"); // if you want to select audio files only : "audio/*"
intent.putExtra(FilePicker.INTENT_PARAM_SELECTION_MODE, FilePicker.SELECTION_MODE_MULTIPLE);
startActivityForResult(intent, 42); // the requestCode used to get the results (see below) 
```
* To open multiple existing files and/or folders (with recursive selection : when you select a folder, all the included files are selected)
```java
Intent intent = new Intent(this, FilePicker.class);
intent.putExtra(FilePicker.INTENT_PARAM_TITLE, "The title displayed in the file picker");
intent.putExtra(FilePicker.INTENT_PARAM_ROOT_DIR, "/the/starting/path);
intent.putExtra(FilePicker.INTENT_PARAM_MIME_TYPE_FILTER, "*/*"); // if you want to select audio files only : "audio/*"
intent.putExtra(FilePicker.INTENT_PARAM_SELECTION_MODE, FilePicker.SELECTION_MODE_MULTIPLE_RECURSIVE);
startActivityForResult(intent, 42); // the requestCode used to get the results (see below) 
```
* To open one existing folder
```java
Intent intent = new Intent(this, FilePicker.class);
intent.putExtra(FilePicker.INTENT_PARAM_TITLE, "The title displayed in the file picker");
intent.putExtra(FilePicker.INTENT_PARAM_ROOT_DIR, "/the/starting/path);
intent.putExtra(FilePicker.INTENT_PARAM_SELECTION_MODE, FilePicker.SELECTION_MODE_SINGLE_DIR);
startActivityForResult(intent, 42); // the requestCode used to get the results (see below)
```
3. Receive the result on the `onActivityResult` method of your `Activity`
```java
@Override
public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
    super.onActivityResult(requestCode, resultCode, resultData);
    switch (requestCode) {
        case 42:
            if (resultCode == Activity.RESULT_OK) {
                String[] documents = resultData.getStringArrayExtra(FilePicker.INTENT_RESULT_FILES);
                // do something with the selected documents
            }
            break;
        //...
    }
}
```