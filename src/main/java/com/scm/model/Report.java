package com.scm.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Generated report metadata.
 */
public class Report implements Serializable {
    private static final long serialVersionUID = 1L;

    private String reportId;
    private String reportType;
    private String generatedBy;
    private Date generatedDate;
    private String filePath;
    private String format;

    public Report(String reportId, String reportType, String generatedBy, String format) {
        this.reportId = reportId;
        this.reportType = reportType;
        this.generatedBy = generatedBy;
        this.generatedDate = new Date();
        this.format = format;
        this.filePath = "/reports/" + reportId + "." + format.toLowerCase();
    }

    public String generate() {
        return filePath;
    }

    public String export(String format) {
        this.format = format;
        return filePath;
    }

    public String getReportId() {
        return reportId;
    }

    public String getReportType() {
        return reportType;
    }

    public String getGeneratedBy() {
        return generatedBy;
    }

    public Date getGeneratedDate() {
        return generatedDate;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFormat() {
        return format;
    }
}
