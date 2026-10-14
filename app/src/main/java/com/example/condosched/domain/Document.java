package com.example.condosched.domain;

public class Document {
    private long id;
    private String title;
    private String description;
    private String documentType;
    private String fileName;
    private String filePath;
    private String mimeType;
    private long fileSize;
    private long unitId;
    private long maintenanceRequestId;
    private long maintenanceInterventionId;
    private boolean isPublic;
    private String publishedDate;

    public Document() {}

    public Document(long id, String title, String description, String documentType, String fileName, String filePath,
                    String mimeType, long fileSize, long unitId, long maintenanceRequestId,
                    long maintenanceInterventionId, boolean isPublic, String publishedDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.documentType = documentType;
        this.fileName = fileName;
        this.filePath = filePath;
        this.mimeType = mimeType;
        this.fileSize = fileSize;
        this.unitId = unitId;
        this.maintenanceRequestId = maintenanceRequestId;
        this.maintenanceInterventionId = maintenanceInterventionId;
        this.isPublic = isPublic;
        this.publishedDate = publishedDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getUnitId() {
        return unitId;
    }

    public void setUnitId(long unitId) {
        this.unitId = unitId;
    }

    public long getMaintenanceRequestId() {
        return maintenanceRequestId;
    }

    public void setMaintenanceRequestId(long maintenanceRequestId) {
        this.maintenanceRequestId = maintenanceRequestId;
    }

    public long getMaintenanceInterventionId() {
        return maintenanceInterventionId;
    }

    public void setMaintenanceInterventionId(long maintenanceInterventionId) {
        this.maintenanceInterventionId = maintenanceInterventionId;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }
}
