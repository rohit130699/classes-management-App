package com.example.online_class;

public class dataholder {
    String id,name,search, std, mail, cno, date, fees, downp, pimage, pass,uploader;
    dataholder(){

    }

    public dataholder(String id,String name, String search, String std, String mail, String cno, String date, String fees,String downp, String pimage,String pass,String uploader) {
        this.id = id;
        this.name = name;
        this.search = search;
        this.std = std;
        this.mail = mail;
        this.cno = cno;
        this.date = date;
        this.fees = fees;
        this.downp=downp;
        this.pimage = pimage;
        this.pass = pass;
        this.uploader = uploader;
    }

    public String getDownp() {
        return downp;
    }

    public void setDownp(String downp) {
        this.downp = downp;
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

    public String getStd() {
        return std;
    }

    public void setStd(String std) {
        this.std = std;
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

    public String getFees() {
        return fees;
    }

    public void setFees(String fees) {
        this.fees = fees;
    }

    public String getPimage() {
        return pimage;
    }

    public void setPimage(String pimage) {
        this.pimage = pimage;
    }

    public String getPass() { return pass; }

    public void setPass(String pass) { this.pass = pass; }

    public String getUploader() { return uploader; }

    public void setUploader(String uploader) { this.uploader = uploader; }
}
