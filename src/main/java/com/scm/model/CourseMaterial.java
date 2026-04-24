package com.scm.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;

/**
 * Uploaded course material.
 */
@Entity
@Table(name = "course_materials")
public class CourseMaterial implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(length = 36)
    private String materialId;
    
    @Column(nullable = false, length = 100)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "file_type", length = 50)
    private String fileType;
    
    @Column(name = "file_path", length = 255)
    private String filePath;
    
    @Column(name = "file_size")
    private long fileSize;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "upload_date")
    private Date uploadDate;
    
    @Column(name = "is_public")
    private boolean isPublic;
    
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @ManyToOne
    @JoinColumn(name = "uploaded_by")
    private Faculty uploadedBy;

    public CourseMaterial() {
    }

    public CourseMaterial(String materialId, String title, String description,
                          String fileType, String filePath, Course course) {
        this.materialId = materialId;
        this.title = title;
        this.description = description;
        this.fileType = fileType;
        this.filePath = filePath;
        this.uploadDate = new Date();
        this.isPublic = true;
        this.course = course;
        this.fileSize = 0L;
    }

    public boolean upload() {
        return true;
    }

    public String download() {
        return filePath;
    }

    public boolean delete() {
        return true;
    }

    public String getMaterialId() {
        return materialId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getFileType() {
        return fileType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public Course getCourse() {
        return course;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }
}
