package com.example.snc_teachers;
public class pdfupload {
    String id,filetitle,filename,search,filter_search,stu_filter,fileurl,filesize,time,std,uploader;

    public pdfupload() {
    }

    public pdfupload(String id,String filetitle,String filename,String search,String filter_search, String stu_filter, String fileurl, String filesize, String time,String std,String uploader) {
        this.id = id;
        this.filetitle = filetitle;
        this.filename = filename;
        this.search = search;
        this.filter_search = filter_search;
        this.stu_filter = stu_filter;
        this.fileurl = fileurl;
        this.filesize = filesize;
        this.time = time;
        this.std = std;
        this.uploader = uploader;
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getFiletitle() { return filetitle; }

    public void setFiletitle(String filetitle) { this.filetitle = filetitle; }

    public String getFilename() { return filename; }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFileurl() {
        return fileurl;
    }

    public void setFileurl(String fileurl) {
        this.fileurl = fileurl;
    }

    public String getFilesize() { return filesize; }

    public void setFilesize(String filesize) { this.filesize = filesize; }

    public String getTime() { return time; }

    public void setTime(String time) { this.time = time; }

    public String getStd() { return std; }

    public void setStd(String std) { this.std = std; }

    public String getUploader() { return uploader; }

    public void setUploader(String uploader) { this.uploader = uploader; }

    public String getSearch() { return search; }

    public void setSearch(String search) { this.search = search; }

    public String getFilter_search() { return filter_search; }

    public void setFilter_search(String filter_search) { this.filter_search = filter_search; }

    public String getStu_filter() { return stu_filter; }

    public void setStu_filter(String stu_filter) { this.stu_filter = stu_filter; }
}
