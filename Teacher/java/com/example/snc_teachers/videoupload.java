package com.example.snc_teachers;

public class videoupload {
    private String id,videotitle,videoName,search,filter_search,stu_filter,videoUri,videosize,time,std,uploader;
    private videoupload(){

    }

    public videoupload(String id,String videotitle, String videoName, String search,String filter_search, String stu_filter, String videoUri, String videosize, String time, String std, String uploader) {
        this.id = id;
        this.videotitle = videotitle;
        this.videoName = videoName;
        this.search = search;
        this.filter_search = filter_search;
        this.stu_filter = stu_filter;
        this.videoUri = videoUri;
        this.videosize = videosize;
        this.time = time;
        this.std = std;
        this.uploader = uploader;
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getVideoUri() {
        return videoUri;
    }

    public String getVideotitle() {
        return videotitle;
    }

    public void setVideotitle(String videotitle) {
        this.videotitle = videotitle;
    }

    public String getVideosize() {
        return videosize;
    }

    public void setVideosize(String videosize) {
        this.videosize = videosize;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStd() {
        return std;
    }

    public void setStd(String std) {
        this.std = std;
    }

    public String getUploader() {
        return uploader;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }

    public void setVideoUri(String videoUri) {
        this.videoUri = videoUri;
    }

    public String getSearch() { return search; }

    public void setSearch(String search) { this.search = search; }

    public String getFilter_search() { return filter_search; }

    public void setFilter_search(String filter_search) { this.filter_search = filter_search; }

    public String getStu_filter() { return stu_filter; }

    public void setStu_filter(String stu_filter) { this.stu_filter = stu_filter; }
}
