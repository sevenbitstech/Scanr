package com.pancard.android.DriveOperations;

import com.google.gson.annotations.SerializedName;

public class MetadataModel {

    @SerializedName("fileDocFolderName")
    public String fileDocFolderName;
    @SerializedName("imageFileId")
    public String imageFileId;
    @SerializedName("jsonFileId")
    public String jsonFileID;
    @SerializedName("docFolderId")
    public String docFolderID;
    @SerializedName("documentCategoryTag")
    public String documentCategoryTag;
    @SerializedName("syncStatus")
    public String syncStatus;


    public MetadataModel() {
    }

    public MetadataModel(String documentCategoryTag, String fileDocFolderName, String docFolderID) {
        this.fileDocFolderName = fileDocFolderName;
        this.docFolderID = docFolderID;
        this.documentCategoryTag = documentCategoryTag;
    }

    public MetadataModel(String fileDocFolderName, String imageFileId, String jsonFileID, String docFolderID, String documentCategoryTag, String syncStatus) {
        this.fileDocFolderName = fileDocFolderName;
        this.imageFileId = imageFileId;
        this.jsonFileID = jsonFileID;
        this.docFolderID = docFolderID;
        this.documentCategoryTag = documentCategoryTag;
        this.syncStatus = syncStatus;
    }

    public MetadataModel(DriveDocModel driveDocModel) {
        this.fileDocFolderName = driveDocModel.getFolderName();
        this.imageFileId = driveDocModel.getImagefileId();
        this.jsonFileID = driveDocModel.getTextfileId();
        this.docFolderID = driveDocModel.getFolderId();
        this.documentCategoryTag = driveDocModel.getCardDetail().whichcard;
        this.syncStatus = driveDocModel.getSyncStatus();
    }

    public String getFileDocFolderName() {
        return fileDocFolderName;
    }

    public void setFileDocFolderName(String fileDocFolderName) {
        this.fileDocFolderName = fileDocFolderName;
    }

    public String getFileName() {
        return fileDocFolderName;
    }

    public void setFileName(String fileDocFolderName) {
        this.fileDocFolderName = fileDocFolderName;
    }

    public String getImageFileId() {
        return imageFileId;
    }

    public void setImageFileId(String imageFileId) {
        this.imageFileId = imageFileId;
    }

    public String getJsonFileID() {
        return jsonFileID;
    }

    public void setJsonFileID(String jsonFileID) {
        this.jsonFileID = jsonFileID;
    }

    public String getDocFolderID() {
        return docFolderID;
    }

    public void setDocFolderID(String docFolderID) {
        this.docFolderID = docFolderID;
    }

    public String getDocumentCategoryTag() {
        return documentCategoryTag;
    }

    public void setDocumentCategoryTag(String documentCategoryTag) {
        this.documentCategoryTag = documentCategoryTag;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }

    public void resetAfterSync(MetadataModel metadataModel) {
        imageFileId = metadataModel.imageFileId;
        jsonFileID = metadataModel.jsonFileID;
        docFolderID = metadataModel.docFolderID;
        syncStatus = metadataModel.syncStatus;
    }
}
