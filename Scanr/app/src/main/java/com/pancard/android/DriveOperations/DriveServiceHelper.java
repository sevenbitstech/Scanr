package com.pancard.android.DriveOperations;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DriveServiceHelper {

    public static String TYPE_AUDIO = "application/vnd.google-apps.audio";
    public static String TYPE_GOOGLE_DOCS = "application/vnd.google-apps.document";
    public static String TYPE_GOOGLE_DRAWING = "application/vnd.google-apps.drawing";
    public static String TYPE_GOOGLE_DRIVE_FILE = "application/vnd.google-apps.file";
    public static String TYPE_GOOGLE_DRIVE_FOLDER = DriveFolder.MIME_TYPE;
    public static String TYPE_GOOGLE_FORMS = "application/vnd.google-apps.form";
    public static String TYPE_GOOGLE_FUSION_TABLES = "application/vnd.google-apps.fusiontable";
    public static String TYPE_GOOGLE_MY_MAPS = "application/vnd.google-apps.map";
    public static String TYPE_PHOTO = "application/vnd.google-apps.photo";
    public static String TYPE_GOOGLE_SLIDES = "application/vnd.google-apps.presentation";
    public static String TYPE_GOOGLE_APPS_SCRIPTS = "application/vnd.google-apps.script";
    public static String TYPE_GOOGLE_SITES = "application/vnd.google-apps.site";
    public static String TYPE_GOOGLE_SHEETS = "application/vnd.google-apps.spreadsheet";
    public static String TYPE_UNKNOWN = "application/vnd.google-apps.unknown";
    public static String TYPE_VIDEO = "application/vnd.google-apps.video";
    public static String TYPE_3_RD_PARTY_SHORTCUT = "application/vnd.google-apps.drive-sdk";


    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDriveService;

    public DriveServiceHelper(Drive driveService) {
        mDriveService = driveService;
    }

    /**
     * Creates a text file in the user's My Drive folder and returns its file ID.
     */
    public Task<String> createFile() {
        return Tasks.call(mExecutor, () -> {
            File metadata = new File()
                    .setParents(Collections.singletonList("root"))
                    .setMimeType("text/plain")
                    .setName("Untitled file");

            File googleFile = mDriveService.files().create(metadata).execute();
            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }

            return googleFile.getId();
        });
    }

    /**
     * Opens the file identified by {@code fileId} and returns a {@link Pair} of its name and
     * contents.
     */
    public Task<Pair<String, String>> readFile(String fileId) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the metadata as a File object.
            File metadata = mDriveService.files().get(fileId).execute();
            String name = metadata.getName();
            Log.e("found name", name);

            // Stream the file contents to a String.
            try (InputStream is = mDriveService.files().get(fileId).executeMediaAsInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

                if (reader.ready()) {
                    Log.i("Not ready", "Yes");
                    return Pair.create(name, "");
                }
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                String contents = stringBuilder.toString();

                return Pair.create(name, contents);
            }
        });
    }

    /**
     * Updates the file identified by {@code fileId} with the given {@code name} and {@code
     * content}.
     */
    public Task<Void> saveFile(String fileId, String name, String content) {
        return Tasks.call(mExecutor, () -> {
            // Create a File containing any metadata changes.
            File metadata = new File().setName(name);

            // Convert content to an AbstractInputStreamContent instance.
            ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);

            // Update the metadata and contents.
            mDriveService.files().update(fileId, metadata, contentStream).execute();
            return null;
        });
    }

    /**
     * Updates the file identified by {@code fileId} with the given {@code name}
     */
    public Task<Void> renameFile(String fileId, String name) {
        return Tasks.call(mExecutor, () -> {
            // Create a File containing any metadata changes.
            File metadata = new File().setName(name);


            // Convert content to an AbstractInputStreamContent instance.
//            ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);

            // Update the metadata
            mDriveService.files().update(fileId, metadata).execute();
            return null;
        });
    }

    /**
     * Returns a {@link FileList} containing all the visible files in the user's My Drive.
     *
     * <p>The returned list will only contain files visible to this app, i.e. those which were
     * created by this app. To perform operations on files not created by the app, the project must
     * request Drive Full Scope in the <a href="https://play.google.com/apps/publish">Google
     * Developer's Console</a> and be submitted to Google for verification.</p>
     */
    public Task<FileList> queryFiles() {
        return Tasks.call(mExecutor, () ->
                mDriveService.files().list().setSpaces("drive").execute());
    }

    /**
     * Returns an {@link Intent} for opening the Storage Access Framework file picker.
     */
    public Intent createFilePickerIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");

        return intent;
    }

    /**
     * Opens the file at the {@code uri} returned by a Storage Access Framework {@link Intent}
     * created by {@link #createFilePickerIntent()} using the given {@code contentResolver}.
     */
    public Task<Pair<String, String>> openFileUsingStorageAccessFramework(
            ContentResolver contentResolver, Uri uri) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the document's display name from its metadata.
            String name;
            try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    name = cursor.getString(nameIndex);
                } else {
                    throw new IOException("Empty cursor returned for file.");
                }
            }

            // Read the document's contents as a String.
            String content;
            try (InputStream is = contentResolver.openInputStream(uri);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                content = stringBuilder.toString();
            }

            return Pair.create(name, content);
        });
    }

    public Task<GoogleDriveFileHolder> searchFile(String fileName, String mimeType) {
        return Tasks.call(mExecutor, () -> {

            FileList result = mDriveService.files().list()
                    .setQ("name = '" + fileName + "' and mimeType ='" + mimeType + "'")
                    .setSpaces("drive")
                    .setFields("files(id, name,size,createdTime,modifiedTime,starred)")
                    .execute();
            GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
            if (result.getFiles().size() > 0) {

                googleDriveFileHolder.setId(result.getFiles().get(0).getId());
                googleDriveFileHolder.setName(result.getFiles().get(0).getName());
                googleDriveFileHolder.setModifiedTime(result.getFiles().get(0).getModifiedTime());
                googleDriveFileHolder.setSize(result.getFiles().get(0).getSize());
            }


            return googleDriveFileHolder;
        });
    }

    public Task<GoogleDriveFileHolder> searchFolder(String folderName) {
        return Tasks.call(mExecutor, () -> {

            // Retrieve the metadata as a File object.
            FileList result = mDriveService.files().list()
                    .setQ("mimeType = '" + DriveFolder.MIME_TYPE + "' and name = '" + folderName + "' ")
                    .setSpaces("drive")
                    .execute();
            GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
            if (result != null && result.getFiles() != null && result.getFiles().size() > 0) {
                File file = result.getFiles().get(0);

                if (file != null) {
                    Log.e("file is", "not trashed or null");
                    googleDriveFileHolder.setId(file.getId());
                    googleDriveFileHolder.setName(file.getName());
                } else {
                    Log.e("file is", "trashed or null");
                }
            }
            return googleDriveFileHolder;
        });
    }

    public Task<GoogleDriveFileHolder> getCategotyFolder(String folderName, String parentId) {
        return Tasks.call(mExecutor, () -> {
            Log.e("myparentid", parentId);
            // Retrieve the metadata as a File object.
            FileList result = mDriveService.files().list()
                    .setQ("mimeType = '" + DriveFolder.MIME_TYPE + "' and name = '" + folderName + "' and '" + parentId + "' in parents")
                    .setSpaces("drive")
                    .execute();

            GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
            if (result != null && result.getFiles() != null && result.getFiles().size() > 0) {
                File file = result.getFiles().get(0);

                if (file != null) {
                    Log.e("file is", "not trashed or null");
                    googleDriveFileHolder.setId(file.getId());
                    googleDriveFileHolder.setName(file.getName());
                    googleDriveFileHolder.setMimeType(file.getMimeType());
                    if (file.getSize() != null)
                        googleDriveFileHolder.setSize(file.getSize());
                    googleDriveFileHolder.setStarred(file.getStarred());
                    googleDriveFileHolder.setCreatedTime(file.getCreatedTime());
                    googleDriveFileHolder.setModifiedTime(file.getModifiedTime());
                } else {
                    Log.e("file is", "trashed or null");
                }
            }
            return googleDriveFileHolder;
        });
    }

    public Task<List<GoogleDriveFileHolder>> queryFiles(@Nullable final String folderId) {
        return Tasks.call(mExecutor, new Callable<List<GoogleDriveFileHolder>>() {
                    @Override
                    public List<GoogleDriveFileHolder> call() throws Exception {
                        List<GoogleDriveFileHolder> googleDriveFileHolderList = new ArrayList<>();
                        String parent = "root";
                        if (folderId != null) {
                            parent = folderId;
                        }

                        FileList result =
                                mDriveService.files().list()
                                        .setQ("'" + parent + "' in parents and trashed=false")
                                        .setFields("files(id, name,size,createdTime,modifiedTime,starred)")
                                        .setSpaces("drive").execute();

                        for (int i = 0; i < result.getFiles().size(); i++) {

                            GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
                            googleDriveFileHolder.setId(result.getFiles().get(i).getId());
                            googleDriveFileHolder.setName(result.getFiles().get(i).getName());
                            if (result.getFiles().get(i).getSize() != null) {
                                googleDriveFileHolder.setSize(result.getFiles().get(i).getSize());
                            }

                            if (result.getFiles().get(i).getModifiedTime() != null) {
                                googleDriveFileHolder.setModifiedTime(result.getFiles().get(i).getModifiedTime());
                            }

                            if (result.getFiles().get(i).getCreatedTime() != null) {
                                googleDriveFileHolder.setCreatedTime(result.getFiles().get(i).getCreatedTime());
                            }

                            if (result.getFiles().get(i).getStarred() != null) {
                                googleDriveFileHolder.setStarred(result.getFiles().get(i).getStarred());
                            }
                            if (result.getFiles().get(i).getWebContentLink() != null) {
                                Log.e("web content link", result.getFiles().get(i).getWebContentLink());
                                googleDriveFileHolder.setWebContentUrl(result.getFiles().get(i).getWebContentLink());
                            }
                            if (result.getFiles().get(i).getWebViewLink() != null) {
                                Log.e("web view link", result.getFiles().get(i).getWebViewLink());
                                googleDriveFileHolder.setWebViewUrl(result.getFiles().get(i).getWebViewLink());
                            }
                            if (result.getFiles().get(i).getMimeType() != null) {
                                Log.e("mime typer", result.getFiles().get(i).getMimeType());
                                googleDriveFileHolder.setMimeType(result.getFiles().get(i).getMimeType());
                            }

                            googleDriveFileHolderList.add(googleDriveFileHolder);

                        }

                        return googleDriveFileHolderList;
                    }
                }
        );
    }

    public Task<GoogleDriveFileHolder> createTextFile(String fileName, String content, @Nullable String folderId) {
        return Tasks.call(mExecutor, () -> {


            List<String> root;
            if (folderId == null) {
                root = Collections.singletonList("root");
            } else {

                root = Collections.singletonList(folderId);
            }

            File metadata = new File()
                    .setParents(root)
                    .setMimeType("text/plain")
                    .setName(fileName);
            ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);

            File googleFile = mDriveService.files().create(metadata, contentStream).execute();
            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }
            GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
            googleDriveFileHolder.setId(googleFile.getId());
            return googleDriveFileHolder;
        });
    }

    public Task<GoogleDriveFileHolder> updateTextFile(String fileId, String content, @Nullable String folderId) {
        return Tasks.call(mExecutor, () -> {


//            List<String> root;
//            if (folderId == null) {
//                root = Collections.singletonList("root");
//            } else {
//
//                root = Collections.singletonList(folderId);
//            }

            String fileName = "JSON details";

            File metadata = new File()
//                    .setParents(root)
                    .setMimeType("text/plain")
                    .setName(fileName);

            ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);

            File googleFile = mDriveService.files().update(fileId, metadata, contentStream).execute();
            if (googleFile == null) {
                throw new IOException("Null result when requesting file update.");
            }
            GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
            googleDriveFileHolder.setId(googleFile.getId());
            return googleDriveFileHolder;
        });
    }

    public Task<GoogleDriveFileHolder> createFolder(String folderName, @Nullable String folderId) {
        return Tasks.call(mExecutor, () -> {

            GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();

            List<String> root;
            if (folderId == null) {
                root = Collections.singletonList("root");
            } else {

                root = Collections.singletonList(folderId);
            }
            File metadata = new File()
                    .setParents(root)
                    .setMimeType(DriveFolder.MIME_TYPE)
                    .setName(folderName);

            File googleFile = mDriveService.files().create(metadata).execute();

            if (googleFile == null) {
                Log.e("google file", "is null");
                throw new IOException("Null result when requesting file creation.");
            } else {
                Log.e("folder id ", "printing id " + googleFile.getId());
            }


            googleDriveFileHolder.setId(googleFile.getId());
            return googleDriveFileHolder;
        });
    }

    public Task<GoogleDriveFileHolder> uploadFile(final java.io.File localFile,
                                                  final String mimeType, @Nullable final String folderId) {
        return Tasks.call(mExecutor, new Callable<GoogleDriveFileHolder>() {
            @Override
            public GoogleDriveFileHolder call() throws Exception {
                // Retrieve the metadata as a File object.

                List<String> root;
                if (folderId == null) {
                    root = Collections.singletonList("root");
                } else {

                    root = Collections.singletonList(folderId);
                }

                File metadata = new File()
                        .setParents(root)
                        .setMimeType(mimeType)
                        .setName(localFile.getName());

                FileContent fileContent = new FileContent(mimeType, localFile);

                File fileMeta = mDriveService.files().create(metadata, fileContent).execute();
                GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
                googleDriveFileHolder.setId(fileMeta.getId());
                googleDriveFileHolder.setName(fileMeta.getName());
                return googleDriveFileHolder;
            }
        });
    }

    public Task<Void> downloadFile(java.io.File targetFile, String fileId) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the metadata as a File object.
            OutputStream outputStream = new FileOutputStream(targetFile);
            mDriveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            return null;
        });
    }

    public Task<Void> downloadFile(java.io.File targetFile, String fileId, String fileType) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the metadata as a File object.
            OutputStream outputStream = new ByteArrayOutputStream();
            mDriveService.files().export(fileId, fileType).executeMediaAndDownloadTo(outputStream);
            return null;
        });
    }

    public Task<Void> deleteFolderFile(String fileId) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the metadata as a File object.
            if (fileId != null) {
                mDriveService.files().delete(fileId).execute();
            }

            return null;

        });
    }

//    mDriveClient = Drive.getDriveClient(this, GoogleSignIn.getLastSignedInAccount(this));
//     Build a drive resource client.
//    mDriveResourceClient =
//            Drive.getDriveResourceClient(this, GoogleSignIn.getLastSignedInAccount(this));
}
