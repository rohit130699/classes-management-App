package com.example.online_class;

public class teachupload {
    String id,name,search, mail, cno, date, qual, pimage,uploader ;

    public teachupload() {
    }

    public teachupload(String id, String name, String search, String mail, String cno, String date, String qual, String pimage, String uploader) {
        this.id = id;
        this.name = name;
        this.search = search;
        this.mail = mail;
        this.cno = cno;
        this.date = date;
        this.qual = qual;
        this.pimage = pimage;
        this.uploader = uploader;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getCno() {
        return cno;
    }

    public void setCno(String cno) {
        this.cno = cno;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getQual() {
        return qual;
    }

    public void setQual(String qual) {
        this.qual = qual;
    }

    public String getPimage() {
        return pimage;
    }

    public void setPimage(String pimage) {
        this.pimage = pimage;
    }

    public String getUploader() {
        return uploader;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }
}
