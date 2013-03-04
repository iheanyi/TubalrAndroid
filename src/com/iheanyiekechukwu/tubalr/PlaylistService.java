package com.iheanyiekechukwu.tubalr;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

public class PlaylistService extends IntentService {
	
   

	// EchoNest URLs
    private static final String ECHONEST_SONG_URL = "http://developer.echonest.com/api/v4/artist/songs?api_key=OYJRQNQMCGIOZLFIW&name=";    
    private static final String ECHONEST_SIMILAR_URL = "http://developer.echonest.com/api/v4/artist/similar?api_key=OYJRQNQMCGIOZLFIW&name=";
    private static final String ECHONEST_RESULT_URL = "&format=json&callback=?&start=0&results=";
    
    // Youtube URLs
    private static final String YOUTUBE_SEARCH_URL = "https://gdata.youtube.com/feeds/api/videos?q=";
    private static final String YOUTUBE_END_URL = "&orderby=relevance&start-index=1&max-results=10&v=2&format=5&alt=json";
    private static final String YOUTUBE_VIDEO_URL = "https://youtube.com/watch?v=";
	
    private ArrayList<VideoClass> videos;
	private ArrayList<String> artistSongList;
	
	private String artist;
	public static final String TRANSACTION_DONE = "com.iheanyiekechukwu.tubalr.TRANSACTION_DONE";
	private static final int STATUS_RUNNING = 0;
	private static final int STATUS_FINISHED = 1;
	ResultReceiver receiver;

	public PlaylistService() {
		super("PlaylistService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		receiver = intent.getParcelableExtra("rec");
		String type = intent.getExtras().getString("type");
		String url = intent.getExtras().getString("url");
		
		videos = new ArrayList<VideoClass>();
		artistSongList = new ArrayList<String>();
		artist = intent.getExtras().getString("artist");
		
		receiver.send(STATUS_RUNNING, Bundle.EMPTY);
		
		String html = getPage(url);
		
        Toast.makeText(this, url, Toast.LENGTH_SHORT).show();
		if(type.equals("just")) {
			
        	processEchoNestJSON(html);

		}
		
		else {
			//processSimilarJSON(result);
		}
		
		

		
	}
	
    private HttpClient createHttpsClient()
    {
        //Toast.makeText(context, "HTTP Client Created", Toast.LENGTH_SHORT);
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
        HttpProtocolParams.setUseExpectContinue(params, true);

        SchemeRegistry schReg = new SchemeRegistry();
        schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);

        return new DefaultHttpClient(conMgr, params);
    }
    
    public String getPage(String URLToFetch) {
        // TODO Auto-generated method stub
        
        String pageHTML = "";
        
        HttpClient client = createHttpsClient();
        HttpGet request = new HttpGet(URLToFetch);
        
        HttpResponse response;
        
        try {
            response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity result =  response.getEntity();
                pageHTML = EntityUtils.toString(result);
            }
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return pageHTML;
    
    }

	public void processSimilarJSON(String result) {
		// TODO Auto-generated method stub
		
		JSONObject object;
		try {
			object = new JSONObject(result);
			JSONObject response = object.getJSONObject("response");
		  	JSONArray artists = response.getJSONArray("artists");
	    	
	    	for(int i = 0; i < artists.length(); ++i) {
	    		JSONObject artist = artists.getJSONObject(i);
	    		String name = artist.getString("name");
	    		
	    		String yt_url;
				try {
					// Fetching Youtube Songs
						yt_url = YOUTUBE_SEARCH_URL + URLEncoder.encode(name, "UTF-8") + YOUTUBE_END_URL;
			    		//YoutubeTask myTask = new YoutubeTask();
			    		//myTask.execute(yt_url);
										
	
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	
	    		
	    		
	    	}
	    	
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

	public void processYoutubeJSON(String result) {
	    // TODO Auto-generated method stub
	    
	    try {
	        JSONObject object = new JSONObject(result);
	        JSONObject feed = object.getJSONObject("feed");
	        JSONArray entries = feed.getJSONArray("entry");
	        
	        for(int i = 0; i < entries.length(); ++i) {
	            JSONObject entry = entries.getJSONObject(i);
	            JSONObject idNode = entry.getJSONObject("id");
	            String full_string = idNode.getString("$t");
	            String[] splitArray = full_string.split(":");
	            
	            String id = splitArray[3];
	            JSONObject titleNode = entry.getJSONObject("title");
	            String videoTitle = titleNode.getString("$t");
	            
	            VideoClass newVideo = new VideoClass(id, videoTitle);
	            
	            //Toast.makeText(this.context, id, Toast.LENGTH_SHORT).show();
	
	            // Validity Checking
	            
	            
	        	//if(isUnique(entry) && isNotBlocked(entry) && isMusic(entry) && isNotCoverOrRemix(entry) && isNotLive(entry) && hasTitle(entry)) {
	            if(checkVideo(entry)) {
	        	        Toast.makeText(this, "Adding new video . . . " + videoTitle + " - " + id, Toast.LENGTH_SHORT).show();
	                	videos.add(newVideo);
	                	
	                    Log.d("TUB", Integer.toString(videos.size()));
	                    Log.d("TUB", id + " - " + videoTitle);
	                    
	                    break;
	                    
	                    //break;
	                    /*String yt_video_url = YOUTUBE_VIDEO_URL + id;
	                    YoutubeVideoTask myTask = new YoutubeVideoTask();
	                    myTask.execute(yt_video_url);*/
	        	}
	  	
	        }
	                    
	
	    } catch (JSONException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	    
	    // If the size of the videos list is 40 or the size of the number of results fetched from the
	    // JSON of EchoNest, start a new task.
	    
	
	    
	}

	public boolean isUnique(JSONObject video) {
		JSONObject idNode;
		
		boolean unique = true;
		
		try {
			idNode = video.getJSONObject("id");
	    	String id = idNode.getString("$t").split(":")[3];
	    	
	    	
	    	String title = video.getJSONObject("title").getString("$t");
	    	for(int i = 0; i < videos.size(); ++i) {
	    		if(videos.get(i).getId().equalsIgnoreCase(id)) {
	    			Log.d("UNI", "NOT UNIQUE - ID");
	    			unique = false;
	    			break;
	    		} 
	    		
	    		else {
	    			 
	    		//Pattern titleStrip = Pattern.compile("")
	    		String arrayTitle = videos.get(i).getTitle().toLowerCase().replaceAll(" *\\([^)]*\\)* ", "").replaceAll("[^a-zA-z ]", "");
	    		String videoTitle = title.toLowerCase().replaceAll(" *\\([^)]*\\)* ", "").replaceAll("[^a-zA-z ]/", "");
	    			if(videoTitle.equals(arrayTitle)) {
	    				//Toast.makeText(this.context, "NOT UNIQUE", Toast.LENGTH_SHORT).show();
	    				Log.d("UNI", "NOT UNIQUE TITLE");
	    				unique = false;
	    				break;
	    			}
	    		}
	    	}	    	
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return unique;
	}

	public boolean checkVideo(JSONObject video) {
		if(isUnique(video) && isNotBlocked(video) && isMusic(video) && isNotCoverOrRemix(video) && isNotLive(video) && hasTitle(video)) {
			//Log.d("TEST", "Valid video");
			return true;
		}
		
		else {
			return false;
		}
	}

	public boolean isNotBlocked(JSONObject video) {
		return !video.has("app$control");
	}

	public boolean isMusic(JSONObject video) {
		JSONObject mediaGroup;
		
		try {
			mediaGroup = video.getJSONObject("media$group");
	    	JSONArray mediaCategory = mediaGroup.getJSONArray("media$category");
	    	JSONObject mediaType = mediaCategory.getJSONObject(0);
	    	String type = mediaType.getString("$t");
	    	
			//Toast.makeText(this.context, type.equals("Music") + " - isMusic", Toast.LENGTH_SHORT).show();
	
	    	return type.equals("Music");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.d("CHECK", "Check isMusic Method");
			e.printStackTrace();
		}
		
		return true;
	}

	public boolean isNotCoverOrRemix(JSONObject video) {
		try {
			String title = video.getJSONObject("title").getString("$t").toLowerCase();
			if(title.contains("cover") || title.contains("remix") || title.contains("alternate")) {
				return false;
			}
			
			else {
				return true;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.d("CHECK", "Check Not Cover Method");
			e.printStackTrace();
		}
	
		return true;
	}

	public boolean isNotUserBanned(JSONObject video) {
		return true;
	}

	public boolean hasTitle(JSONObject video) {
		String title = "";
		
		try {
			title = video.getJSONObject("title").getString("$t").trim();    	
	    	if(title.equals("")) {
	    		return false;
	    	}
	    	
	    	else {
	    		return true;
	    	}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.d("CHECK", "Check hasTitle Method");
			e.printStackTrace();
		}
		
		return true;
	
	}

	public boolean isNotLive(JSONObject video) {
		try {
			String description = video.getJSONObject("media$group").getJSONObject("media$description").getString("$t").toLowerCase();
			String title = video.getJSONObject("title").getString("$t").toLowerCase();
			
			if(description.contains("live") || description.contains("concert") || title.contains("live") || title.contains("concert")) {
				return false;
			}
			else {
				return true;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.d("CHECK", "Check Not Live Method");
			e.printStackTrace();
		}
		return true;
	}

	/* TODO Rather than fetching each song URL/ID immediately, 
	 * just build up the array first, then execute it? 
	 * Could lead to a possible performance increase for the application */
	public void processEchoNestJSON(String result) {
	    // TODO Auto-generated method stub
		
	    
	    try {
	        JSONObject object = new JSONObject(result);
	        JSONObject resp = object.getJSONObject("response");
	        JSONObject status = resp.getJSONObject("status");
	        Integer code = status.getInt("code");
	        String message = status.getString("message");
	        
	        JSONArray songs = resp.getJSONArray("songs");
	        
	        // If the artist doesn't exist or the length of the array is less than 10, execute YouTube command to fetch songs
	        if(code == 5 || songs.length() <= 10) {
	        	String short_search = artist;
	        	String yt_short_url = YOUTUBE_SEARCH_URL + URLEncoder.encode(short_search, "UTF-8") + YOUTUBE_END_URL;
	        	String html = getPage(yt_short_url);
	        	processYoutubeJSON(html);
	        	//YoutubeTask myTask = new YoutubeTask();
	        	//myTask.execute(yt_short_url);
	        }
	        
	        else {
	            // For each song, hit Youtube's Public API
	            for(int i = 0; i < songs.length(); ++i) {
	            	if(artistSongList.size() == 40 || videos.size() == 40) {
	            		Log.d("STOP", "Size reached 40!");
	            		break;
	            	}
	            	
	            	else {
	                    JSONObject song = songs.getJSONObject(i);
	                    String title = song.getString("title");
	                    
	                    String search = artist + " - " + title;
	                    artistSongList.add(search);
	                    
	                    try {
							String yt_url = YOUTUBE_SEARCH_URL + URLEncoder.encode(search, "UTF-8") + YOUTUBE_END_URL;
						
							//YoutubeTask myTask = new YoutubeTask();
							//myTask.execute(yt_url);
							
							String html = getPage(yt_url);
							processYoutubeJSON(html);
	                    } catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

	                    
	                    //String new_search = this.artistName + " - " + title;
	                 }                	
	            }
	            
	    		Bundle b = new Bundle();
	    		b.putSerializable("videos", videos);
	    		b.putString("artist", artist);
	    		receiver.send(STATUS_FINISHED, b);
	    		this.stopSelf();
	            //notifyFinished(videos, artist);   
	        }
	    } catch (JSONException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    } catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	    	
	            
	}

	private void notifyFinished(ArrayList<VideoClass> videos2, String artist2) {
		// TODO Auto-generated method stub
		
		Log.d("TEST", "Finished shit");
		Intent i = new Intent(TRANSACTION_DONE);
		Bundle b = new Bundle();
		//b.putParcelableArrayList("videos", videos2);
		b.putSerializable("videos", videos2);
		b.putString("artist", artist2);
		i.putExtra("videos", videos2);
		i.putExtra("artist", artist2);
		
		PlaylistService.this.sendBroadcast(i);
		
	}
	
	 private class YoutubeTask extends AsyncTask<String, Integer, String> {
		    @Override
		    protected String doInBackground(String... params) {
		        // TODO Auto-generated method stub
		        
		        String URL = params[0];
		        String html = getPage(URL);
		        
		        
		        return html;
		    }
		    
		    protected void onPostExecute(String result) {
		        //Toast.makeText(context, "Youtube Post Execute Task", Toast.LENGTH_SHORT).show();
		        //prog.dismiss();
		        processYoutubeJSON(result);
		   	
		    }   
		}

		private class EchoSongTask extends AsyncTask<String, Integer, String> {
		
		    @Override
		    protected String doInBackground(String... params) {
		        // TODO Auto-generated method stub
		        
		        String URLToFetch = params[0];
		        String html = getPage(URLToFetch);
		        
		        return html;
		    
		    }
		    
		    protected void onPostExecute(String result) {
		        //textView.setText(result);  // CAN access views in main thread
		        //prog.dismiss();
		        //prog  = null;
		    	//Toast.makeText(context, "EchoNest Post Execute Task", Toast.LENGTH_SHORT);
		        processEchoNestJSON(result);
		   }
		    
		}

		private class EchoSimilarTask extends AsyncTask<String, Integer, String> {
		
			@Override
			protected String doInBackground(String... params) {
				// TODO Auto-generated method stub
				String URLToFetch = params[0];
				String html = getPage(URLToFetch);
				
				return html;
				//return null;
			}
			
			protected void onPostExecute(String result) {
				
				processSimilarJSON(result);
				
				
			}
			
		}

}
