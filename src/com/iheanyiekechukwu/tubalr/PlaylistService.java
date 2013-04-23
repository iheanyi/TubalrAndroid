package com.iheanyiekechukwu.tubalr;



import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import android.os.ResultReceiver;
import android.os.StrictMode;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;
import com.bugsnag.android.Bugsnag;
import com.echonest.api.v4.Artist;
import com.echonest.api.v4.BasicPlaylistParams;
import com.echonest.api.v4.BasicPlaylistParams.PlaylistType;
import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.Playlist;
import com.echonest.api.v4.PlaylistParams;
import com.echonest.api.v4.Song;
import com.flurry.android.FlurryAgent;
import com.google.gdata.client.Query;
import com.google.gdata.client.youtube.YouTubeQuery;
import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.Category;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;
import com.google.gdata.data.youtube.YouTubeNamespace;
import com.google.gdata.util.ServiceException;

public class PlaylistService extends IntentService {
	
   

	// EchoNest URLs
    private static final String ECHONEST_SONG_URL = "http://developer.echonest.com/api/v4/artist/songs?api_key=OYJRQNQMCGIOZLFIW&name=";    
    private static final String ECHONEST_SIMILAR_URL = "http://developer.echonest.com/api/v4/artist/similar?api_key=OYJRQNQMCGIOZLFIW&name=";
    private static final String ECHONEST_RESULT_URL = "&format=json&callback=?&start=0&results=";
    
    // Youtube URLs
    private static final String YOUTUBE_SEARCH_URL = "https://gdata.youtube.com/feeds/api/videos?q=";
    private static final String YOUTUBE_END_URL = "&orderby=relevance&start-index=1&max-results=10&v=2&format=5&alt=json";
    private static final String YOUTUBE_END_FORTY_URL = "&orderby=relevance&start-index=1&max-results=40&v=2&format=5&alt=json";
    private static final String YOUTUBE_VIDEO_URL = "https://youtube.com/watch?v=";
	private static final String TAG = "PlaylistService";
    private ArrayList<VideoClass> videos;
	private ArrayList<String> artistSongList;
	
	private String artist;
	public static final String TRANSACTION_DONE = "com.iheanyiekechukwu.tubalr.TRANSACTION_DONE";
	
	// Receiver Statuses
	public static final int STATUS_RUNNING = 0;
	public static final int STATUS_FINISHED = 1;
	public static final int STATUS_ERROR = 2;
	
	public EchoNestAPI echoNest;
	
	ResultReceiver receiver;
	
	
	
    public static final String BUG_KEY = "b27d57ef";
    public static final String FLURRY_KEY = "4GF6RX8PZ7DP53V795RF";

	public PlaylistService() {
		super("PlaylistService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		
		BugSenseHandler.initAndStartSession(this, BUG_KEY);
        Bugsnag.register(this, "1d479c585e3d333a05943f37bef208cf");
        FlurryAgent.onStartSession(this, FLURRY_KEY);
        
		receiver = intent.getParcelableExtra("rec");
		String type = intent.getExtras().getString("type");
		String url = intent.getExtras().getString("url");
		
		videos = new ArrayList<VideoClass>();
		artistSongList = new ArrayList<String>();
		artist = intent.getExtras().getString("artist");
		
		Log.d(TAG, "In the Service Now!");
			
		//String html = getPage(url);
		
		
        //Toast.makeText(this, url, Toast.LENGTH_SHORT).show();
		
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

		StrictMode.setThreadPolicy(policy); 
		
		if(type.equals("just")) {
			
			//EchoJustTask newTask = new EchoJustTask();
			//newTask.execute(artist);
        	processEchoNestJSON(artist);
		} else if(type.equals("genre")) {
			processGenreJSON(artist);
		}
		else {
			processSimilarJSON(artist);
		}
		
		
		stopSelf();
		
		

		
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
    
    public void processGenreJSON(String genre) {
		echoNest = new EchoNestAPI("OYJRQNQMCGIOZLFIW");
    	PlaylistParams params = new PlaylistParams();
    	params.setType(PlaylistParams.PlaylistType.GENRE_RADIO);
    	params.addGenre(genre);
    	params.setResults(40);
		try {
			Playlist playlist = echoNest.createStaticPlaylist(params);
	    	if(playlist.getSongs().size() > 10) {
	    		for(Song song: playlist.getSongs()) {
	    			String search = song.getArtistName() + " - " + song.getTitle();
	    			String type = "just";
	    			Log.d(TAG, "Processing YouTube JSON for " + search);
	    			processYoutubeJSON(search, type);
	    		}
	    }
		} catch (EchoNestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
		Bundle b = new Bundle();
		b.putSerializable("videos", videos);
		b.putString("artist", artist);
		receiver.send(STATUS_FINISHED, b);
		Log.d(TAG, "Attempting to stop service. . . ");

    }

	public void processSimilarJSON(String result) {
		// TODO Auto-generated method stub
		echoNest = new EchoNestAPI("OYJRQNQMCGIOZLFIW");

		try {
			List<Artist> artists = echoNest.searchArtists(result);
			 
			if(artists.size() > 0) {
				Artist searched = artists.get(0);
				String first = searched.getSongs().get(0).getTitle();
				
				String search_url = searched + " - " + first;
				List<Artist> similar = searched.getSimilar(40);
				
				String type = "just";
				processYoutubeJSON(search_url, type);
				
				for(Artist a : similar) {
					List<Song> songs = a.getSongs();
					Collections.shuffle(songs);
					String song = songs.get(0).getTitle();
					type = "similar";
					search_url = a.getName() + " - " + song;
					processYoutubeJSON(search_url, type);	
				}
				

				
			} else {
			     String short_search = artist;
			     //String yt_short_url = YOUTUBE_SEARCH_URL + URLEncoder.encode(short_search, "UTF-8") + YOUTUBE_END_FORTY_URL;
			     //String html = getPage(yt_short_url);
			     String type = "youtube";
			     
			     processYoutubeJSON(artist, type);
			}
		} catch (EchoNestException e) {
			// TODO Auto-generated catch block
			Log.d(TAG, "ERROR IN THE ECHONEST SIMILAR QUERY.");

			e.printStackTrace();
		}
		
		Log.d(TAG, "Similar task finished executing . . . ");
		Bundle b = new Bundle();
		b.putSerializable("videos", videos);
		b.putString("artist", artist);
		receiver.send(STATUS_FINISHED, b);
		/*Log.d(TAG, "Attempting to stop service. . . ");
		stopSelf();
		Log.d(TAG, "Trying again. . . ");
		this.stopSelf();
		Log.d(TAG, "Trying stop foreground. . . ");
		this.stopForeground(true);*/
		//try {
			
			/*
			JSONObject object;
			object = new JSONObject(result);
			JSONObject response = object.getJSONObject("response");
	        JSONObject status = response.getJSONObject("status");
	        Integer code = status.getInt("code");
	        Log.d("TEST", Integer.toString(code));
	        String message = status.getString("message");
	        
	        
	        
	        // If the artist doesn't exist or the length of the array is less than 10, execute YouTube command to fetch songs
	        if(code == 5) {
	        	String short_search = artist;
	        	String yt_short_url = YOUTUBE_SEARCH_URL + URLEncoder.encode(short_search, "UTF-8") + YOUTUBE_END_FORTY_URL;
	        	String html = getPage(yt_short_url);
	        	String type = "youtube";
	        	processYoutubeJSON(html, type);
	        	//YoutubeTask myTask = new YoutubeTask();
	        	//myTask.execute(yt_short_url);
	        }
	        
	        
	        
	        else {
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
				        	String html = getPage(yt_url);
				        	String type = "similar";
				        	processYoutubeJSON(html, type);			
		
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}    		
		    	}	
	        }

	    	
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d("SIM", "Similar task finished executing . . .");*/

	
	
	}

	public void processYoutubeJSON(String result, String type) {
	    // TODO Auto-generated method stub
		
		
	    try {

			
			YouTubeService service = new YouTubeService("Tubalr");
			YouTubeQuery query = new YouTubeQuery(new URL("http://gdata.youtube.com/feeds/api/videos"));
			query.setOrderBy(YouTubeQuery.OrderBy.RELEVANCE);
			query.setIncludeRacy(false);
			query.setFullTextQuery(result);
			if(type != "youtube") {
				query.setMaxResults(10);
			} else {
				query.setMaxResults(30);
			}
			query.setStartIndex(1);
			query.setSafeSearch(YouTubeQuery.SafeSearch.NONE);
			Query.CategoryFilter musicFilter = new Query.CategoryFilter();
			musicFilter.addCategory(new Category(YouTubeNamespace.CATEGORY_SCHEME, "Music"));
			if(type.equals("just") || type.equals("similar")) {
				query.addCategoryFilter(musicFilter);
			}
			
			VideoFeed feed = service.query(query, VideoFeed.class);
			
			Log.d(TAG, "Size of Videos " + Integer.toString(videos.size()));
			
			for(VideoEntry entry : feed.getEntries()) {
				String id = entry.getMediaGroup().getVideoId();
				String title = entry.getTitle().getPlainText();
				Log.d(TAG, "TITLE: " + title);
				String thumbnailString = entry.getMediaGroup().getThumbnails().get(3).getUrl();
				
				
				Log.d(TAG, "ID: " + id);
				VideoClass newVideo = new VideoClass(id, title, thumbnailString);
				
				//if(type.equals("just")) {
					/*if(title.contains("cover") || title.contains("remix") 
							|| entry.getMediaGroup().getDescription().toString().contains("live") || entry.getMediaGroup().getDescription().toString().contains("concert") 
							|| title.trim().equals("") || isUnique(entry)) {
						continue;
					} else {
						videos.add(newVideo);
					}*/
					
					if(!type.equals("youtube")) {
						
						if(isUnique(entry)) {
							videos.add(newVideo);
							break;
						}

					} else {
						videos.add(newVideo);
					}
					


				//}
			}
			
	    
	    } catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    /*
	    try {
	        JSONObject object = new JSONObject(result);
	        JSONObject feed = object.getJSONObject("feed");
	        JSONArray entries = feed.getJSONArray("entry");
	        
	        Log.d("ENTRY", "Length is " + Integer.toString(entries.length()));
	        for(int i = 0; i < entries.length(); ++i) {
	            JSONObject entry = entries.getJSONObject(i);
	            JSONObject idNode = entry.getJSONObject("id");
		        JSONObject media = entry.getJSONObject("media$group");
		        JSONArray mediaThumbnail = media.getJSONArray("media$thumbnail");
		        JSONObject thumbnail = mediaThumbnail.getJSONObject(2);
		        String thumbnailString = thumbnail.getString("url");
		        
		        Log.v("PlayListService", "Thumbnail URL: " + thumbnailString);

	            String full_string = idNode.getString("$t");
	            String[] splitArray = full_string.split(":");
	            
	            String id = splitArray[3];
	            JSONObject titleNode = entry.getJSONObject("title");
	            String videoTitle = titleNode.getString("$t");
	            
	            
	            VideoClass newVideo = new VideoClass(id, videoTitle, thumbnailString);
	            
	            //Toast.makeText(this.context, id, Toast.LENGTH_SHORT).show();
	
	            // Validity Checking
	            
	            
	        	//if(isUnique(entry) && isNotBlocked(entry) && isMusic(entry) && isNotCoverOrRemix(entry) && isNotLive(entry) && hasTitle(entry)) {
	           
	            if(type.equals("just")) {
		            if(isUnique(entry) && isNotBlocked(entry) && isMusic(entry) && isNotCoverOrRemix(entry) && isNotLive(entry) && hasTitle(entry)) {
	        	        //Toast.makeText(this, "Adding new video . . . " + videoTitle + " - " + id, Toast.LENGTH_SHORT).show();
	                	videos.add(newVideo);
	                	
	                    Log.d("TUB", Integer.toString(videos.size()));
	                    Log.d("TUB", id + " - " + videoTitle);
	                    Log.d("TUB", "From the Just type");
	                    
	                    break;
	                    
		            }
	            }
	            
	            else if(type.equals("similar")) {
		            if(isNotBlocked(entry) && isMusic(entry) && isNotCoverOrRemix(entry) && isNotLive(entry)) {
	        	        //Toast.makeText(this, "Adding new video . . . " + videoTitle + " - " + id, Toast.LENGTH_SHORT).show();
	                	videos.add(newVideo);
	                	
	                    Log.d("TUB", Integer.toString(videos.size()));
	                    Log.d("TUB", id + " - " + videoTitle);
	                    Log.d("TUB", "From the similar type . . . ");
	                    
	                    break;
	                    

		            }
	            }*/
	            
	            /*else if(type.equals("youtube")) {
	            	if(isNotUserBanned(entry) && isNotBlocked(entry)) {
	                	videos.add(newVideo);
	                	
	                    Log.d("TUB", Integer.toString(videos.size()));
	                    Log.d("TUB", id + " - " + videoTitle);
	                    Log.d("TUB", "From the Youtube Type");
	                    
	                    //break;
	            	}
	            }

	  	
	        }
	                    
	
	    } catch (JSONException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }*/
	    
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
	
	public boolean isUnique(VideoEntry entry) {
		String id = entry.getMediaGroup().getVideoId();
		String title = entry.getTitle().getPlainText();
		boolean unique = true;
		
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
			if(title.contains("cover") || title.contains("remix") || title.contains("alternate") || title.contains("interview")) {
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
		echoNest = new EchoNestAPI("OYJRQNQMCGIOZLFIW");

	    try {
	    	
	    	/*BasicPlaylistParams params = new BasicPlaylistParams();
	    	params.addArtist(result);
	    	params.setType(BasicPlaylistParams.PlaylistType.ARTIST_RADIO);
	    	params.setResults(40);
	    	Playlist playlist = echoNest.createBasicPlaylist(params);
	    	
	    	if(playlist.getSongs().size() > 10) {
	    		for(Song song: playlist.getSongs()) {
	    			String search = result + " - " + song.getTitle();
	    			String type = "just";
	    			Log.d(TAG, "Processing YouTube JSON for " + search);
	    			processYoutubeJSON(search, type);
	    		}
	    	}*/
	    	
			List<Artist> artists = echoNest.searchArtists(result);
			if(artists.size() > 0) {
				Artist searched = artists.get(0);
				
				String name = searched.getName();
				List<Song> justSongs = searched.getSongs(0, 40);
				
				System.out.print(Integer.toString(justSongs.size()));
				Log.d(TAG, "Size of Songs: " + Integer.toString(justSongs.size()));
				
				for(int i = 0; i < justSongs.size(); ++i) {
					String search = name + " - " + justSongs.get(i).getTitle();
					String type = "just";
					
					Log.d(TAG, "Processing Youtube JSON for " + search);
					processYoutubeJSON(search, type);
				}
			} else {
				
		     String short_search = artist;
		     //String yt_short_url = YOUTUBE_SEARCH_URL + URLEncoder.encode(short_search, "UTF-8") + YOUTUBE_END_FORTY_URL;
		     //String html = getPage(yt_short_url);
		     String type = "youtube";
		     
		     processYoutubeJSON(artist, type);
		     //processYoutubeJSON(html, type);
		        	//YoutubeTask myTask = new YoutubeTask();
		        	//myTask.execute(yt_short_url);
		        
			}
		} catch (EchoNestException e1) {
			// TODO Auto-generated catch block
			Log.d(TAG, "ERROR IN THE ECHONEST QUERY.");
			e1.printStackTrace();
		}
	    
	    /*
	    try {
	        JSONObject object = new JSONObject(result);
	        JSONObject resp = object.getJSONObject("response");
	        JSONObject status = resp.getJSONObject("status");
	        Integer code = status.getInt("code");
	        Log.d("TEST", Integer.toString(code));
	        String message = status.getString("message");
	        
	        
	        
	        // If the artist doesn't exist or the length of the array is less than 10, execute YouTube command to fetch songs
	        if(code == 5) {
	        	String short_search = artist;
	        	String yt_short_url = YOUTUBE_SEARCH_URL + URLEncoder.encode(short_search, "UTF-8") + YOUTUBE_END_FORTY_URL;
	        	String html = getPage(yt_short_url);
	        	String type = "youtube";
	        	processYoutubeJSON(html, type);
	        	//YoutubeTask myTask = new YoutubeTask();
	        	//myTask.execute(yt_short_url);
	        }
	        
	        
	        
	        
	        else {
	        	JSONArray songs = resp.getJSONArray("songs");
		        Log.d("TEST", Integer.toString(songs.length()));

		        if(songs.length() <= 10) {
		           	String short_search = artist;
		        	String yt_short_url = YOUTUBE_SEARCH_URL + URLEncoder.encode(short_search, "UTF-8") + YOUTUBE_END_FORTY_URL;
		        	String html = getPage(yt_short_url);
		        	String type = "youtube";
		        	processYoutubeJSON(html, type);
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
								String type = "just";
								processYoutubeJSON(html, type);
		                    } catch (UnsupportedEncodingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

		                    
		                    //String new_search = this.artistName + " - " + title;
		                 }                	
		            }
		            

		            //notifyFinished(videos, artist); 
		        }
		        
	        }
	    } catch (JSONException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    } catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	    
	    
		Log.d(TAG, "Just task finished executing . . .");

		
		//notifyFinished(videos, artist);
		Bundle b = new Bundle();
		b.putSerializable("videos", videos);
		b.putString("artist", artist);
		receiver.send(STATUS_FINISHED, b);
		Log.d(TAG, "Attempting to stop service. . . ");
		//stopSelf();
		//Log.d(TAG, "Trying again. . . ");
		//this.stopSelf();
		//Log.d(TAG, "Trying stop foreground. . . ");
		//this.stopForeground(true);

		//b.putSerializable("videos", videos);

	            
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
		    	String type = "youtube";
		        processYoutubeJSON(result, type);
		   	
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
		
		private class EchoJustTask extends AsyncTask<String, Void, List<Song>> {
			
			@Override
			protected List<Song> doInBackground(String... params) {
				// TODO Auto-generated method stub
				String URLToFetch = params[0];
				//String html = getPage(URLToFetch);
				List<Artist> artists = null;
				List<Song> justSongs = null;
				
				echoNest = new EchoNestAPI("OYJRQNQMCGIOZLFIW");
				
				Log.d(TAG, "Currently in the EchoJustTask");

			    try {
					artists = echoNest.searchArtists(artist);
					if(artists.size() > 0) {
						Artist searched = artists.get(0);
						artist = artists.get(0).getName();
						justSongs = searched.getSongs(0, 40);
						
						System.out.print(Integer.toString(justSongs.size()));
						
						for(int i = 0; i < justSongs.size(); ++i) {
							String search = artist + " - " + justSongs.get(i).getTitle();
							String type = "just";
							
							Log.d(TAG, "Processing Youtube JSON for " + search);
							processYoutubeJSON(search, type);
						}
					} else {
				     String short_search = artist;
				     //String yt_short_url = YOUTUBE_SEARCH_URL + URLEncoder.encode(short_search, "UTF-8") + YOUTUBE_END_FORTY_URL;
				     //String html = getPage(yt_short_url);
				     String type = "youtube";
				     
				     processYoutubeJSON(artist, type);
				     //processYoutubeJSON(html, type);
				        	//YoutubeTask myTask = new YoutubeTask();
				        	//myTask.execute(yt_short_url);
				        
					}
				} catch (EchoNestException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				return justSongs;
				//return null;
			}
			
			protected void onPostExecute(List<Song> result) {
				
				Log.d(TAG, "Currently in Post Execute junts . . . ");
				Bundle b = new Bundle();
				b.putSerializable("videos", videos);
				b.putString("artist", artist);
				receiver.send(STATUS_FINISHED, b);
				PlaylistService.this.stopSelf();
				//PlaylistService.this.stopSelf();
				//processEchoNestJSON(result);
				
				
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
