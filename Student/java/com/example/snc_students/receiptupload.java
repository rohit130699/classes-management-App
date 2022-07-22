package com.example.snc_students;

public class receiptupload {
    String invoice_no, rec_name, url, stu_id, stu_name, sizeinKB, time, timeinms;

    public receiptupload() {


    }

    public receiptupload(String invoice_no, String rec_name, String url, String stu_id, String stu_name, String sizeinKB, String time, String timeinms) {
        this.invoice_no = invoice_no;
        this.rec_name = rec_name;
        this.url = url;
        this.stu_id = stu_id;
        this.stu_name = stu_name;
        this.sizeinKB = sizeinKB;
        this.time = time;
        this.timeinms = timeinms;
    }

    public String getInvoice_no() {
        return invoice_no;
    }

    public void setInvoice_no(String invoice_no) {
        this.invoice_no = invoice_no;
    }

    public String getRec_name() {
        return rec_name;
    }

    public void setRec_name(String rec_name) {
        this.rec_name = rec_name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStu_id() {
        return stu_id;
    }

    public void setStu_id(String stu_id) {
        this.stu_id = stu_id;
    }

    public String getSizeinKB() { return sizeinKB; }

    public void setSizeinKB(String sizeinKB) { this.sizeinKB = sizeinKB; }

    public String getTime() { return time; }

    public void setTime(String time) { this.time = time; }

    public String getTimeinms() { return timeinms; }

    public void setTimeinms(String timeinms) { this.timeinms = timeinms; }

    public String getStu_name() { return stu_name; }

    public void setStu_name(String stu_name) { this.stu_name = stu_name; }
}