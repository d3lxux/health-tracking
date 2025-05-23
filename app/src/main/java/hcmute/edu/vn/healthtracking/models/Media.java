package hcmute.edu.vn.healthtracking.models;

import java.util.Date;

public class Media {
    String id;
    String filename;
    String url;
    String description;
    String ownerId;
    Date dateCreated;
    boolean isVideo;

    public Media() {
    }

    public Media(String id, String filename, String url, String description, String ownerId, Date dateCreated, boolean isVideo) {
        this.id = id;
        this.filename = filename;
        this.url = url;
        this.description = description;
        this.ownerId = ownerId;
        this.dateCreated = dateCreated;
        this.isVideo = isVideo;
    }

    public String getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public void setId(String number) {
        this.id = number;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean video) {
        isVideo = video;
    }
}
