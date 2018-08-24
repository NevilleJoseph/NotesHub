package com.pnpj.noteshub;

/**
 * Created by Neville on 19-12-2017.
 */

public class showNotes {

    private String branch;
    private String subject;
    private int noOfPages;
    private String nameOfCollege;
    private String semester;
    private boolean isCompleted;
    private String uploaderName;
    private String uploaderUid;
    private String uploadDate;
    private int views = 0;
    private int likes = 0;
    private int dislikes = 0;
    private int downloads = 0;
    private String pushId;

    public showNotes(String branch, String subject, int noOfPages, String nameOfCollege, String semester, boolean isCompleted, String uploaderName, String uploaderUid, String uploadDate, int views, int likes, int dislikes, int downloads, String pushId) {
        this.branch = branch;
        this.subject = subject;
        this.noOfPages = noOfPages;
        this.nameOfCollege = nameOfCollege;
        this.semester = semester;
        this.isCompleted = isCompleted;
        this.uploaderName = uploaderName;
        this.uploaderUid = uploaderUid;
        this.uploadDate = uploadDate;
        this.views = views;
        this.likes = likes;
        this.dislikes = dislikes;
        this.downloads = downloads;
        this.pushId = pushId;
    }

    public showNotes(String branch, String subject, int noOfPages, String nameOfCollege, String semester, boolean isCompleted, String uploaderName, String uploaderUid, String uploadDate, int views, int likes, int dislikes, int downloads) {
        this.branch = branch;
        this.subject = subject;
        this.noOfPages = noOfPages;
        this.nameOfCollege = nameOfCollege;
        this.semester = semester;
        this.isCompleted = isCompleted;
        this.uploaderName = uploaderName;
        this.uploaderUid = uploaderUid;
        this.uploadDate = uploadDate;
        this.views = views;
        this.likes = likes;
        this.dislikes = dislikes;
        this.downloads = downloads;
    }

    public showNotes()
    {

    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getNoOfPages() {
        return noOfPages;
    }

    public void setNoOfPages(int noOfPages) {
        this.noOfPages = noOfPages;
    }

    public String getNameOfCollege() {
        return nameOfCollege;
    }

    public void setNameOfCollege(String nameOfCollege) {
        this.nameOfCollege = nameOfCollege;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public String getUploaderUid() {
        return uploaderUid;
    }

    public void setUploaderUid(String uploaderUid) {
        this.uploaderUid = uploaderUid;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }

    public int getDownloads() {
        return downloads;
    }

    public void setDownloads(int downloads) {
        this.downloads = downloads;
    }

    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }
}