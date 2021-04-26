package com.pancard.android.DriveOperations;

import android.util.Log;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.pancard.android.model.CardDetail;
import com.pancard.android.model.SqliteDetail;

import java.io.Serializable;

@DatabaseTable(tableName = "drivedocinfotable")
public class DriveDocModel implements Serializable {

    public static final String TABLE_NAME = "drivedocinfotable";

    public static final String ID = "id";
    public static final String FOLDER_ID = "folder_id";
    public static final String FOLDER_NAME = "folder_name";
    public static final String IMAGE_PATH = "image_path";
    public static final String ORIGINAL_IMAGE_PATH = "origina_image_path";
    public static final String ORIGINAL_IMAGE_ID = "original_image_id";
    public static final String PUBLIC_GUID = "public_guid";
    public static final String JSON_TEXT = "json_text";
    public static final String TEXT_FILE_ID = "text_file_id";
    public static final String IMAGE_FILE_ID = "image_file_id";
    public static final String PDF_FILE_ID = "pdf_file_id";
    public static final String IMAGE_STRING = "image_string";
    public static final String SYNC_STATUS = "sync_status";
    public static final String WHICH_CARD = "which_card";
    public static final String FILE_NAME = "file_name";
    public static final String PDF_FILE_PATH = "pdf_file_path";

    //todo: folder name with _ and page number.
    //todo:
    @DatabaseField(columnName = WHICH_CARD)
    public String whichCard;
    @DatabaseField(columnName = FILE_NAME)
    public String fileName;
    @DatabaseField(columnName = PDF_FILE_PATH)
    public String pdfFilePath;
    @DatabaseField(columnName = ID, generatedId = true)
    int id;
    @DatabaseField(columnName = FOLDER_ID)
    String folderId;
    @DatabaseField(columnName = FOLDER_NAME)
    String folderName;
    @DatabaseField(columnName = IMAGE_PATH)
    String imagePath;
    @DatabaseField(columnName = ORIGINAL_IMAGE_PATH)
    String originalImagePath;
    @DatabaseField(columnName = PUBLIC_GUID)
    String publicGuid;
    @DatabaseField(columnName = JSON_TEXT)
    String jsonText;
    SqliteDetail sqliteDetail;
    CardDetail cardDetail;
    @DatabaseField(columnName = TEXT_FILE_ID)
    String textfileId;
    @DatabaseField(columnName = IMAGE_FILE_ID)
    String imagefileId;
    @DatabaseField(columnName = ORIGINAL_IMAGE_ID)
    String originalImageId;
    @DatabaseField(columnName = PDF_FILE_ID)
    String pdfFileID;
    @DatabaseField(columnName = IMAGE_STRING)
    String imageString;
    @DatabaseField(columnName = SYNC_STATUS)
    String syncStatus;

    public DriveDocModel() {

    }

//    public DriveDocModel(String imagePath, String jsonText, SqliteDetail cardDetails) {
//        this.imagePath = imagePath;
//        this.jsonText = jsonText;
//        this.sqliteDetail = cardDetails;
//    }

    public DriveDocModel(String imagePath, String jsonText, String whichCard, String syncStatus) {
        this.imagePath = imagePath;
        this.jsonText = jsonText;
        this.whichCard = whichCard;
        this.syncStatus = syncStatus;
    }

    //todo: it is used in the new flow
    public DriveDocModel(String public_guid, String imagePath, String originalImagePath, String jsonText, String whichCard, String syncStatus) {
        this.imagePath = imagePath;
        this.jsonText = jsonText;
        this.whichCard = whichCard;
        this.syncStatus = syncStatus;
        this.publicGuid = public_guid;
        this.originalImagePath = originalImagePath;
    }

//    public DriveDocModel(String folderId, String folderName, String imagePath, String originalImagePath, String publicGuid, String jsonText, CardDetail cardDetail, String textfileId, String imagefileId, String originalImageId, String syncStatus, String whichCard) {
//        this.folderId = folderId;
//        this.folderName = folderName;
//        this.imagePath = imagePath;
//        this.originalImagePath = originalImagePath;
//        this.publicGuid = publicGuid;
//        this.jsonText = jsonText;
//        this.cardDetail = cardDetail;
//        this.textfileId = textfileId;
//        this.imagefileId = imagefileId;
//        this.originalImageId = originalImageId;
//        this.syncStatus = syncStatus;
//        this.whichCard = whichCard;
//    }

    public DriveDocModel(GoogleDriveFileHolder googleDriveFileHolder) {
        this.folderId = googleDriveFileHolder.getId();
        this.folderName = googleDriveFileHolder.getName();
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWhichCard() {
        return whichCard;
    }

    public void setWhichCard(String whichCard) {
        this.whichCard = whichCard;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getJsonText() {
        return jsonText;
    }

    public void setJsonText(String jsonText) {
        this.jsonText = jsonText;
    }

    public SqliteDetail getSqliteDetail() {
        return sqliteDetail;
    }

    public void setSqliteDetail(SqliteDetail sqliteDetail) {
        this.sqliteDetail = sqliteDetail;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }


    public CardDetail getCardDetail() {
        return cardDetail;
    }

    public void setCardDetail(CardDetail cardDetail) {
        this.cardDetail = cardDetail;
    }

    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public String getTextfileId() {
        return textfileId;
    }

    public void setTextfileId(String textfileId) {
        this.textfileId = textfileId;
    }

    public String getImagefileId() {
        return imagefileId;
    }

    public void setImagefileId(String imagefileId) {
        this.imagefileId = imagefileId;
    }

    public String getImageString() {
        return imageString;
    }

    public void setImageString(String imageString) {
        this.imageString = imageString;
    }

    public String getOriginalImagePath() {
        return originalImagePath;
    }

    public void setOriginalImagePath(String originalImagePath) {
        this.originalImagePath = originalImagePath;
    }

    public String getPublicGuid() {
        return publicGuid;
    }

    public void setPublicGuid(String publicGuid) {
        this.publicGuid = publicGuid;
    }

    public String getOriginalImageId() {
        return originalImageId;
    }

    public void setOriginalImageId(String originalImageId) {
        this.originalImageId = originalImageId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPdfFilePath() {
        return pdfFilePath;
    }

    public void setPdfFilePath(String pdfFilePath) {
        this.pdfFilePath = pdfFilePath;
    }

    public String getPdfFileID() {
        return pdfFileID;
    }

    public void setPdfFileID(String pdfFileID) {
        this.pdfFileID = pdfFileID;
    }

    public void setDriveDocInfo(MetadataModel metadataModel) {

        Log.e("setting up drive info", "from metadata");

        this.folderId = metadataModel.getDocFolderID();
        Log.e("new folder id", folderId);

        this.folderName = metadataModel.getFileDocFolderName();
        Log.e("new folder name", folderName);

        this.textfileId = metadataModel.getJsonFileID();
        Log.e("new text file id", textfileId);

        this.imagefileId = metadataModel.getImageFileId();
        Log.e("new image file id", imagefileId);

        this.whichCard = metadataModel.getDocumentCategoryTag();
        Log.e("new which card", whichCard);

    }
}
