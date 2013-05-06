package com.iheanyiekechukwu.tubalr;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

public class VideoClass implements Serializable, Parcelable {
	private String id;
	private String title;
	private String url;
	private String imageURL;

	/*
	 * public VideoClass(String id, String title) { this.id = id; this.title =
	 * title; this.url = ""; }
	 */

	public VideoClass(String id, String title, String imageURL) {
		this.id = id;
		this.title = title;
		this.imageURL = imageURL;
		this.url = "";
	}

	public String getImageURL() {
		return this.imageURL;
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

	public void setImageURL(String imgURL) {
		this.imageURL = imgURL;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
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
		dest.writeString(id);
		dest.writeString(title);
		dest.writeString(url);
		dest.writeString(imageURL);

	}

	public static final Parcelable.Creator<VideoClass> CREATOR = new Parcelable.Creator<VideoClass>() {
		@Override
		public VideoClass createFromParcel(Parcel in) {

			return new VideoClass(in);
		}

		@Override
		public VideoClass[] newArray(int size) {
			return new VideoClass[size];
		}
	};

	private VideoClass(Parcel in) {
		id = in.readString();
		title = in.readString();
		url = in.readString();
		imageURL = in.readString();
	}
}
