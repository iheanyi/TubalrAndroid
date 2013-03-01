package com.iheanyiekechukwu.tubalr;

import java.io.Serializable;

public class VideoClass implements Serializable {
    private String id;
    private String title;
    private String url;
    
    public VideoClass(String id, String title) {
    	this.id  = id;
    	this.title = title;
    	this.url = "";
    }
    public VideoClass(String id, String title, String url) {
        this.id = id;
        this.title = title;
        this.url = url;
    }
    
    
    
    public String getId() {
    	return this.id;
    }
    
    public String getTitle() {
    	return this.title;
    }

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	
	public String toString() {
		return this.title;
	}
}
