package com.pnpj.noteshub;

/**
 * Created by Neville on 09-11-2017.
 */

public class userclass {
    private String College;
    private String RollNo;
    private String Branch;
    private String Semester;
    private boolean IsUploader;
    private boolean isVerifiedUploader;

    public userclass(String college, String rollNo, String branch, String semester, boolean isUploader, boolean isVerifiedUploader) {
        College = college;
        RollNo = rollNo;
        Branch = branch;
        Semester = semester;
        IsUploader = isUploader;
        this.isVerifiedUploader = isVerifiedUploader;
    }

    public userclass() {
    }

    public void setCollege(String college) {
        College = college;
    }

    public void setRollNo(String rollNo) {
        RollNo = rollNo;
    }

    public void setBranch(String branch) {
        Branch = branch;
    }

    public void setSemester(String semester) {
        Semester = semester;
    }

    public void setUploader(boolean uploader) {
        IsUploader = uploader;
    }

    public String getCollege() {
        return College;
    }

    public String getRollNo() {
        return RollNo;
    }

    public String getBranch() {
        return Branch;
    }

    public String getSemester() {
        return Semester;
    }

    public boolean isUploader() {
        return IsUploader;
    }

    public boolean isVerifiedUploader() {
        return isVerifiedUploader;
    }

    public void setVerifiedUploader(boolean verifiedUploader) {
        isVerifiedUploader = verifiedUploader;
    }
}
