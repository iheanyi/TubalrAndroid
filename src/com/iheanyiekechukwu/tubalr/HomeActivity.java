
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

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.bugsense.trace.BugSenseHandler;
import com.bugsnag.android.Bugsnag;
import com.flurry.android.FlurryAgent;



public class HomeActivity extends SherlockActivity implements OnItemClickListener, OnClickListener {
    MediaController controller;
    
    private ArrayAdapter<String> menuAdapter;
    private Resources res;
    private String[] menuNames;
    
	private BroadcastReceiver musicServiceBroadcastReceiver;

    
    private MusicService musicService;
	private ServiceConnection serviceConnection = new MusicServiceConnection();

    
    private ListView menuListView;
    private EditText searchText;
    
    private ProgressDialog prog;
    
    private boolean paused = false;
    
    
    // EchoNest URLs
    public static final String ECHONEST_SONG_URL = "http://developer.echonest.com/api/v4/artist/songs?api_key=OYJRQNQMCGIOZLFIW&name=";    
    public static final String ECHONEST_SIMILAR_URL = "http://developer.echonest.com/api/v4/artist/similar?api_key=OYJRQNQMCGIOZLFIW&name=";
    public static final String ECHONEST_RESULT_URL = "&format=json&callback=?&start=0&results=";
    
    // Youtube URLs
    private static final String YOUTUBE_SEARCH_URL = "https://gdata.youtube.com/feeds/api/videos?q=";
    private static final String YOUTUBE_END_URL = "&orderby=relevance&start-index=1&max-results=10&v=2&format=5&alt=json";
    private static final String YOUTUBE_VIDEO_URL = "https://youtube.com/watch?v=";
    
    private static final String TAG = "HomeActivity";
    private Context context;
    
    private String artistName;
    
    private ArrayList<VideoClass> playlist;
    
    private TextView currentArtist;
    
    private ArrayAdapter<String> playlistViewAdapter;
    
    private ArrayList<String> playlistStringArray;
    
    private ListView playlistListView;
    
    private Button justButton;
    private Button similarButton;
    
    private String tempID;
    
    private boolean mBound = false;
    
    private ArrayList<String> artistSongList;
    
    boolean last = false;
    
    private Intent musicServiceIntent;

    MediaPlayer sound;
        
    private YoutubeTask ytTask;
    
    private ImageButton homePlayButton;
    private ImageButton homePauseButton;
    
	private String s_url, s_artist, s_search, s_type = "";

    public static final String APP_KEY = "9efd11ff27117b5000f4d69d9e6aa17a0332e53e";
    public static final String BUG_KEY = "b27d57ef";
    public static final String FLURRY_KEY = "4GF6RX8PZ7DP53V795RF";
    
    private LinearLayout controlLayout;
       
    ArrayList<VideoClass> videos;
    
    private ImageButton nextButton;
    private ImageButton prevButton;
    
	public static final String INTENT_BASE_NAME = "com.iheanyiekechukwu.tubalr.MusicService";
	public static final String NEXT_TRACK = INTENT_BASE_NAME + ".NEXT_TRACK";
	public static final String PLAY_TRACK = INTENT_BASE_NAME + ".PLAY_TRACK";
	public static final String PREV_TRACK = INTENT_BASE_NAME + ".PREV_TRACK";
	public static final String PAUSE_TRACK = INTENT_BASE_NAME + ".PAUSE_TRACK";
	public static final String PLAY_SELECT = INTENT_BASE_NAME + ".PLAY_SELECT";
	public static final String NEW_SONGS = INTENT_BASE_NAME + ".NEW_SONGS";

    
    //public static final BUG_KEY = ""
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
       // Apphance.startNewSession(this, APP_KEY, Apphance.Mode.QA);
        BugSenseHandler.initAndStartSession(this, BUG_KEY);
        Bugsnag.register(this, "1d479c585e3d333a05943f37bef208cf");
        FlurryAgent.onStartSession(this, FLURRY_KEY);

        setContentView(R.layout.activity_home);
        
        //controller = (MediaController) findViewById(R.id.mediaController);
        //playlistListView = (ListView) findViewById(R.id.songListView);
        menuListView = (ListView) findViewById(R.id.homeFixedListView);
        
        context = this.getApplicationContext();
        res = getResources();
        
        
        
        videos = new ArrayList<VideoClass>();
        playlist = new ArrayList<VideoClass>();
        
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		
		currentArtist = (TextView) findViewById(R.id.artistNameText);
		currentArtist.setOnClickListener(this);
		
		controlLayout = (LinearLayout) findViewById(R.id.controlLayout);
		
		if(!isMyServiceRunning()) {
			controlLayout.setVisibility(View.GONE);
		}
		
		prevButton = (ImageButton) findViewById(R.id.homePrevButton);
		nextButton = (ImageButton) findViewById(R.id.homeNextButton);
		
		homePlayButton = (ImageButton) findViewById(R.id.homePlayButton);
		homePauseButton = (ImageButton) findViewById(R.id.homePrevButton);
		
		prevButton.setOnClickListener(this);
		nextButton.setOnClickListener(this);
		
	   /* int actionBarTitleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
	    TextView actionBarTextView = (TextView) findViewById(actionBarTitleId);
	    actionBarTextView.setTextColor(Color.WHITE);*/
	    
	    Typeface bookman = Typeface.createFromAsset(getAssets(), "fonts/bookos.ttf");
	    //actionBarTextView.setTypeface(bookman);
		getSupportActionBar().setTitle("tubalr");
        
        Drawable d = getResources().getDrawable(R.drawable.navbar);
        
        getSupportActionBar().setBackgroundDrawable(d);
        
        //playlistStringArray = new ArrayList<String>();
       //playlistViewAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, playlistStringArray);
        menuNames = res.getStringArray(R.array.home_menu);
        
		Typeface bolded = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Regular.ttf");
		Typeface light = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
		
        currentArtist.setTypeface(bolded);
//      playlistListView.setAdapter(playlistViewAdapter);
        
        menuAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, menuNames);
        menuListView.setAdapter(menuAdapter);
        menuListView.setOnItemClickListener(this);
        
        //playlistListView.setOnItemClickListener(this);
        
        artistSongList = new ArrayList<String>();
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getSupportMenuInflater();
    	inflater.inflate(R.menu.activity_home, menu);
        return true;
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
    	
    		Log.d(TAG, "On activity result!");
    		
    		switch(requestCode) {
    			case 1:
    	    		if(resultCode == RESULT_OK) {
    	    			
    	    			Log.d(TAG, "Activity for Result! RESULT OK!");	
    	    			videos = (ArrayList<VideoClass>) data.getSerializableExtra("videos");
    		    		
    	    			if(isMyServiceRunning()) {
    	    				controlLayout.setVisibility(View.VISIBLE);
    	    			}
    	    			
    	    			
    		    		if(musicServiceIntent == null) {
    		    			MusicService.setMainActivity(HomeActivity.this);
    		    			musicServiceIntent = new Intent(this, MusicService.class);

    			    		bindService(musicServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    		    		}
    		    		
    		    		
    	    			
    			    	musicServiceBroadcastReceiver = new MusicServiceBroadcastReceiver();
    			    	IntentFilter filter = new IntentFilter(MusicService.UPDATE_PLAYLIST);
    			    	filter.addAction(MusicService.NEXT_TRACK);
    			    	filter.addAction(MusicService.PREV_TRACK);
    			    	filter.addAction(MusicService.PAUSE_TRACK);
    			    	filter.addAction(MusicService.PLAY_SELECT);
    			    	filter.addAction(MusicService.NEW_SONGS);
    			    	registerReceiver(musicServiceBroadcastReceiver, filter);
    	    			
    	    			//artist = data.getStringExtra("artist");
    			    	Log.d(TAG, "DONE REGISTERING JUNTS");
    	    	}
    	    		
    	    		break;
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

    public void onItemClick(AdapterView<?> adapter, View v, int id, long arg3) {
       // TODO Auto-generated method stub
        		
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
			    		YoutubeTask myTask = new YoutubeTask();
			    		myTask.execute(yt_url);
										

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
                this.tempID = splitArray[3];
                JSONObject titleNode = entry.getJSONObject("title");
                String videoTitle = titleNode.getString("$t");
                
                VideoClass newVideo = new VideoClass(id, videoTitle, "swag");
                
                //Toast.makeText(this.context, id, Toast.LENGTH_SHORT).show();

                // Validity Checking
                
                
            	//if(isUnique(entry) && isNotBlocked(entry) && isMusic(entry) && isNotCoverOrRemix(entry) && isNotLive(entry) && hasTitle(entry)) {
                if(checkVideo(entry)) {
            	        Toast.makeText(this.context, "Adding new video . . . " + videoTitle + " - " + id, Toast.LENGTH_SHORT).show();
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
            	String short_search = this.artistName;
            	String yt_short_url = YOUTUBE_SEARCH_URL + URLEncoder.encode(this.artistName, "UTF-8") + YOUTUBE_END_URL;
            	YoutubeTask myTask = new YoutubeTask();
            	myTask.execute(yt_short_url);
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
                        
                        String new_search = this.artistName + " - " + title;
                        artistSongList.add(new_search);
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
        
        processSongList();

                
    }

    private void processSongList() {
		// TODO Auto-generated method stub
        for(String search : artistSongList) {
            String yt_url;
			try {
				if(artistSongList.indexOf(search) >= artistSongList.size() - 1) {
					last = true;
				}
				
				if(videos.size() != 40 || videos.size() < artistSongList.size()) {

					ytTask = new YoutubeTask();
					yt_url = YOUTUBE_SEARCH_URL + URLEncoder.encode(search, "UTF-8") + YOUTUBE_END_URL;
					Intent i = new Intent(this, PlaylistService.class);
					i.putExtra("url", yt_url);
					//YoutubeTask myTask = new YoutubeTask();
		            ytTask.execute(yt_url);
		            Log.d("URL", yt_url); 
				}

			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        // After the song list has been processed and whatnot, start the new playlist activity


        if(!ytTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
        	//startPlaylistActivity();
        }
	}

	@Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
          switch (v.getId()) {
          
          		case R.id.artistNameText:
          			
          			break;                                   
                
        		case R.id.homeNextButton:

        			Log.d(TAG, "NextButton in Home Activity clicked!");
        			Intent nextTrackIntent = new Intent(NEXT_TRACK);
        			this.sendBroadcast(nextTrackIntent);
        			
        			//playNextTrack();
        	    
        			break;
        			
        		case R.id.homePrevButton:

        			Log.d(TAG, "previousButton in Home Activity clicked!");

        			Intent prevTrackIntent = new Intent(PREV_TRACK);
        			this.sendBroadcast(prevTrackIntent);
        			
        			break;
    			
        			
        		default: 
        			break;
        		}
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
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
	    	case R.id.menu_search:
	    		
	    		showSearchDialog();
	    		return true;
	    		
	    	
	    	
	    	default:
	    		return super.onOptionsItemSelected(item);
    	}
    }

	private void showSearchDialog() {
		// TODO Auto-generated method stub
		
		String url, artist, search, type = "";
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		
		alert.setTitle("Build New Playlist . . . ");
		alert.setMessage("Enter Artist's Name");
		final EditText input = new EditText(this);
		//String artist = "";
		alert.setView(input);
		alert.setPositiveButton("Just", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
				s_artist = input.getEditableText().toString().trim();
				BugSenseHandler.sendEvent("User searched for Just: " + s_artist);

				
				if(s_artist.length() > 0) {
					s_type = "just";
					try {
						s_url = ECHONEST_SONG_URL + URLEncoder.encode(s_artist, "UTF-8") + ECHONEST_RESULT_URL + "40";
					
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					dialog.dismiss();
					
			        Intent i = new Intent(getApplicationContext(), PlaylistActivity.class);
			        i.putExtra("url", s_url);
			        i.putExtra("type", s_type);
			        i.putExtra("artist", s_artist);
			        i.putExtra("new", true);
			        
			        if(mBound) {
			        	unbindService(serviceConnection);
			        }
			        startActivityForResult(i, 1);
				}
				
				else {
					Toast.makeText(getApplicationContext(), "Invalid input into the search box!", Toast.LENGTH_SHORT).show();
					dialog.dismiss();
				}

			}
		});
		
		alert.setNegativeButton("Similar", new DialogInterface.OnClickListener() {
			
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
				s_artist = input.getEditableText().toString().trim();
				BugSenseHandler.sendEvent("User searched for Similar to " + s_artist);

				
				if(s_artist.length() > 0) {
					s_type = "similar";
					try {
						s_url = ECHONEST_SIMILAR_URL + URLEncoder.encode(s_artist, "UTF-8") + ECHONEST_RESULT_URL + "40";
					
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					dialog.dismiss();
					
			        Intent i = new Intent(getApplicationContext(), PlaylistActivity.class);
			        i.putExtra("url", s_url);
			        i.putExtra("type", s_type);
			        i.putExtra("artist", s_artist);
			        i.putExtra("new", true);
			        
			        
			        if(mBound) {
			        	unbindService(serviceConnection);
			        }
			        
			        
			        startActivityForResult(i, 1);
				}
				
				else {
					Toast.makeText(getApplicationContext(), "Invalid input into the search box!", Toast.LENGTH_SHORT).show();
					dialog.dismiss();
				}	
			}
		});
    
		AlertDialog showAlert = alert.create();
		showAlert.show();
	
		
	}
    
    
    protected void onStart() {
    	super.onStart();	
        BugSenseHandler.initAndStartSession(this, BUG_KEY);
        Bugsnag.register(this, "1d479c585e3d333a05943f37bef208cf");
        FlurryAgent.onStartSession(this, FLURRY_KEY);
    }
    
    private final class MusicServiceConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName className, IBinder baBinder) {
			// TODO Auto-generated method stub
			Log.d(TAG, "MusicServiceConnection: Service connected");
			musicService = ((MusicService.MusicServiceBinder) baBinder).getService();
			musicServiceIntent = new Intent(getApplicationContext(), MusicService.class);
    		//musicServiceIntent.putExtra("videos", videos);
    		//musicServiceIntent.putExtra("artists", s_artist);
			MusicService.setMainActivity(HomeActivity.this);
			
			mBound = true;
			
			//bindService(musicServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

    		//musicServiceIntent.putExtra("videos", videoList);
			//startService(musicServiceIntent);
			
			//musicService.playCurrentSong();
			
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			// TODO Auto-generated method stub
			Log.d(TAG, "ServiceConnection: Service disconnected.");
			
			musicService = null;
			musicServiceIntent = null;
			serviceConnection = null;
			
			mBound = false;
			//startService(musicServiceIntent);
		}
    	
    }
    

	private boolean isMyServiceRunning() {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (MusicService.class.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
    
    private class MusicServiceBroadcastReceiver extends BroadcastReceiver {
    	
    	public void onReceive(Context context, Intent intent) {
    		Log.d("PlaylistActivity", "MusicServiceBroadcastReceive.onReceive action =  " + intent.getAction());
    		if(MusicService.UPDATE_PLAYLIST.equals(intent.getAction())) {
    			Log.d(TAG, "updatePlaylist is called!");
    			//updatePlayQueue();
    			
    			updatePlayPanel(musicService.getCurrentVideo());

    			
    			currentArtist.setText(musicService.getCurrentVideo().getTitle());
    		}
    	}
    	
    }
    
	private void updatePlayPanel(final VideoClass video) {
		Log.v(TAG, "PlaylistActivity(): Updating the Play Panel");

	}
	
	private void updatePlayPauseButtonState() {
		Log.v(TAG, "updatePlayPauseButtonState()");
		
		//Log.v(TAG, Boolean.toString(musicService.isPlaying()));
		
		if(musicService.isPlaying()) {
			homePauseButton.setVisibility(View.VISIBLE);
			homePlayButton.setVisibility(View.GONE);
		} else {
			homePauseButton.setVisibility(View.GONE);
			homePlayButton.setVisibility(View.VISIBLE);
		}
		
	}
    
    @Override
	public void onResume() {
		  super.onResume();
		  
		  
		  if(musicService != null || mBound) {
			  musicService.updateButtons();
		  }
		  //musicServiceIntent = new Intent(this, MusicService.class);
		  //bindService(musicServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
		  
		//registerReceiver(musicServiceBroadcastReceiver, filter);
		  
		  //refreshScreen();
	      

		  //registerReceiver(playlistReceiver, intentFilter);
	  }
    
    public void onPause() {
	    //super.onPause();
	    
		  if(musicServiceBroadcastReceiver != null) {
			  unregisterReceiver(musicServiceBroadcastReceiver);
			  musicServiceBroadcastReceiver = null;
		  }
		
		  if(mBound) {
			 // unbindService(serviceConnection);
		  }

	    
	    
	    super.onPause();
	    //unregisterReceiver(playlistReceiver);
	    //playlistReceiver = null;
	    
	  }
    
    
    public void onDestroy() {
	    //super.onPause();
	    
		  if(musicServiceBroadcastReceiver != null) {
			  unregisterReceiver(musicServiceBroadcastReceiver);
			  musicServiceBroadcastReceiver = null;
		  }
		
		  if(mBound) {
			 unbindService(serviceConnection);
		  }

	    
	    
	    super.onDestroy();
	    //unregisterReceiver(playlistReceiver);
	    //playlistReceiver = null;
	    
	  }
    
}
