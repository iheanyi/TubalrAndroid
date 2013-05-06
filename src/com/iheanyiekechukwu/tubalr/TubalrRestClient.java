package com.iheanyiekechukwu.tubalr;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class TubalrRestClient {

	private static final String BASE_URL = "http://tubalr.com/";

	private static AsyncHttpClient client = new AsyncHttpClient();

	// PersistentCookieStore myCookieStore = new PersistentCookieStore(this);

	// client.setCookieStore(myCookieStore);

	public static void get(String url, RequestParams params,
			AsyncHttpResponseHandler responseHandler) {
		client.get(getAbsoluteUrl(url), params, responseHandler);
	}

	public static void post(String url, RequestParams params,
			AsyncHttpResponseHandler responseHandler) {
		client.post(getAbsoluteUrl(url), params, responseHandler);
	}

	private static String getAbsoluteUrl(String relativeUrl) {
		return BASE_URL + relativeUrl;
	}
}
