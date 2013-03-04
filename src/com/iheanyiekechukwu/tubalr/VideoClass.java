package com.iheanyiekechukwu.tubalr;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

public class VideoClass implements Serializable, Parcelable {
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
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		
	}
}
