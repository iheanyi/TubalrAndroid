package com.iheanyiekechukwu.tubalr;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NavUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.bugsense.trace.BugSenseHandler;
import com.bugsnag.android.Bugsnag;
import com.flurry.android.FlurryAgent;

public class PlaylistActivity extends SherlockActivity implements OnClickListener, MyResultReceiver.Receiver, OnItemClickListener, OnSeekBarChangeListener, OnCompletionListener, OnBufferingUpdateListener, OnPreparedListener, Callback, OnAudioFocusChangeListener {

	private UpdateCurrentTrackTask updateCurrentTrackTask;
	

	private ListView playlistView;
	//private PlaylistAdapter playlistAdapter;
	
	private ArrayList<String> stringList;
	private ArrayAdapter<VideoClass> playlistStringAdapter;
	
	private PlaylistAdapter adapter;
	private Context context;
	private Intent intent;
	
	private Handler serviceHandler;
	
	private ImageView videoImageView;
	private ArrayList<VideoClass> videoList;
	
	private int selectedPosition = 0;
	
    private static final String YOUTUBE_VIDEO_URL = "https://youtube.com/watch?v=";

	private static final int FINISHED = 1;
    
    private MediaPlayer player;

    private SeekBar songSeek;
    
    private TextView timeText, maxText;
    
    private int current = 0;
    
    private SurfaceView vidV;
    private SurfaceHolder sh;
    
    private String artist;    
    
    private Boolean paused = false;
    public MyResultReceiver mReceiver;
    
    private TextView currentTextView;
    
	private ProgressDialog pd;
	
	private boolean mBound = false;
	
	private BroadcastReceiver musicServiceBroadcastReceiver = new MusicServiceBroadcastReceiver();
	private ServiceConnection serviceConnection = new MusicServiceConnection();
	private MusicService musicService;
	
	private ImageButton playButton;
	private ImageButton pauseButton;
	private ImageButton nextButton;
	private ImageButton prevButton;
	private ImageButton shuffleButton;
	
	private Handler handler = new Handler();
	private Timer waitForMusicServiceTimer = new Timer();
	
	public static final String TAG = "PlaylistActivity";
	
	private View listDivider;
	
    // Strings
    public static final String ECHONEST_SONG_URL = "http://developer.echonest.com/api/v4/artist/songs?api_key=OYJRQNQMCGIOZLFIW&name=";    
    public static final String ECHONEST_SIMILAR_URL = "http://developer.echonest.com/api/v4/artist/similar?api_key=OYJRQNQMCGIOZLFIW&name=";
    public static final String ECHONEST_RESULT_URL = "&format=json&callback=?&start=0&results=";
    public static final String BUG_KEY = "b27d57ef";
    private String s_url, s_artist, s_search, s_type = "";
    public static final String FLURRY_KEY = "4GF6RX8PZ7DP53V795RF";
    
    private Intent musicServiceIntent;
    
	public static final String INTENT_BASE_NAME = "com.iheanyiekechukwu.tubalr.MusicService";
	public static final String NEXT_TRACK = INTENT_BASE_NAME + ".NEXT_TRACK";
	public static final String PLAY_TRACK = INTENT_BASE_NAME + ".PLAY_TRACK";
	public static final String PREV_TRACK = INTENT_BASE_NAME + ".PREV_TRACK";
	public static final String PAUSE_TRACK = INTENT_BASE_NAME + ".PAUSE_TRACK";
	public static final String PLAY_SELECT = INTENT_BASE_NAME + ".PLAY_SELECT";
	public static final String NEW_SONGS = INTENT_BASE_NAME + ".NEW_SONGS";
   
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Show the Up button in the action bar.
		

		
        BugSenseHandler.initAndStartSession(this, BUG_KEY);
        Bugsnag.register(this, "1d479c585e3d333a05943f37bef208cf");
        FlurryAgent.onStartSession(this, FLURRY_KEY);

		setContentView(R.layout.activity_playlist);

		setupActionBar();
		
		Drawable d = getResources().getDrawable(R.drawable.navbar);
        
        getSupportActionBar().setBackgroundDrawable(d);
        
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		
		/*if (savedInstanceState != null) {
			videoList = (ArrayList<VideoClass>) savedInstanceState.getSerializable("videos");
		}*/
		
		intent = this.getIntent();
		
		String url = intent.getExtras().getString("url");
		String type = intent.getExtras().getString("type");
		String name = intent.getExtras().getString("artist");
		boolean newInstance = intent.getExtras().getBoolean("new");
		
		timeText = (TextView) findViewById(R.id.timeText);
		maxText = (TextView) findViewById(R.id.maxText);
		context = this.getApplicationContext();
		stringList = new ArrayList<String>();
		
		playButton = (ImageButton) findViewById(R.id.playButton);
		pauseButton = (ImageButton) findViewById(R.id.pauseButton);
		nextButton = (ImageButton) findViewById(R.id.nextButton);
		prevButton = (ImageButton) findViewById(R.id.previousButton);
		shuffleButton = (ImageButton) findViewById(R.id.shuffleButton);
		
		//playButton.setOnClickListener(this);
		//pauseButton.setOnClickListener(this);
		nextButton.setOnClickListener(this);
		prevButton.setOnClickListener(this);
		//shuffleButton.setOnClickListener(this);
		
		mReceiver = new MyResultReceiver(new Handler());
		mReceiver.setReceiver(this);
		

		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(PlaylistService.TRANSACTION_DONE);
		
		listDivider = (View) findViewById(R.id.listDivider);
		listDivider.setBackgroundDrawable(d);

		
		serviceHandler = new Handler();
		
		
		currentTextView = (TextView) findViewById(R.id.currentTextView);
		
		Typeface bolded = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Medium.ttf");
		Typeface light = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
		currentTextView.setTypeface(bolded);
		maxText.setTypeface(light);
		timeText.setTypeface(light);
		
		videoImageView = (ImageView) findViewById(R.id.videoImageView);
		
		videoList = new ArrayList<VideoClass>();
		Intent i = new Intent(this, PlaylistService.class);
	    i.putExtra("url", url);
	    i.putExtra("type", type);
	    i.putExtra("artist", name);
	    i.putExtra("rec", mReceiver);
	    startService(i);
			
		pd = ProgressDialog.show(this, "Building Playlist", "Finding Songs Relevant For Query: " + name);
		pd.setCancelable(true);

		//videoList = new ArrayList<VideoClass>();
		
		Log.v(TAG, "onCreate() method junts");
		//registerReceiver(playlistReceiver, intentFilter);
		
		//playButton.setBackgroundColor(Color.TRANSPARENT);
		//pauseButton.setBackgroundColor(Color.TRANSPARENT);
		
		
		playlistView = (ListView) findViewById(R.id.playlistView);
		adapter = new PlaylistAdapter(this, R.layout.basicitem, videoList);
		playlistStringAdapter = new ArrayAdapter<VideoClass>(context, android.R.layout.simple_list_item_1, videoList);
		playlistView.setAdapter(adapter);
		
		playlistView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		songSeek = (SeekBar) findViewById(R.id.songSeekBar);
		
		songSeek.setOnSeekBarChangeListener(new TimelineChangeListener());

		//playlistView.setOnItemClickListener(this);
		


		
	   /* int actionBarTitleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
	    
	    TextView actionBarTextView = (TextView) findViewById(actionBarTitleId);
	    actionBarTextView.setTextColor(Color.WHITE);*/
	    
	    Typeface bookman = Typeface.createFromAsset(getAssets(), "fonts/bookos.ttf");
	    //actionBarTextView.setTypeface(bookman);
		getSupportActionBar().setTitle("tubalr");
		
		//Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		WindowManager w = getWindowManager();

		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		int width = metrics.widthPixels;
		int height = metrics.heightPixels;
		
		int newHeight = (int) (height * 0.25);
		
		videoImageView.setMaxHeight(newHeight);
		// Adapter has been loaded properly . . . Sooo, yeah, play the first song.
		
		
		
		/*vidV = (SurfaceView) findViewById(R.id.videoStream);
		sh = vidV.getHolder();
		sh.setFixedSize(width, newHeight);					
		sh.addCallback(this);*/ 

        

        
        Log.d("LOC", "In Playlist Activity now");
       
		
		
		/*while(it.hasNext()) {
			VideoClass currentVideo = it.next();
			
			String info = currentVideo.getTitle();
			stringList.add(info);
			playlistStringAdapter.add(info);
			playlistStringAdapter.notifyDataSetChanged();
			Log.d("ADD", "Added " + info + " to list.");
		}*/		

	}
	
	Runnable mStatusChecker = new  Runnable() {
		
		@Override
		public void run() {
			
			int currentPosition = 0;
			int total = musicService.maxDuration();
			
		
			while(musicService.isPlaying()) {
				
				Log.v(TAG, "Music Streaming, and I'm trying to update . .. ");
				
				try {
					Thread.sleep(1000);
					total = musicService.maxDuration();
					currentPosition = musicService.elapsed();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				} catch (Exception e) {
					return;
				}
				songSeek.setProgress(currentPosition);
				timeText.setText(getTimeString(currentPosition));
				maxText.setText(getTimeString(total));
				//VideoClass video = musicService.getCurrentVideo();
				
				
				//updatePlayPanel(video);
				
				//serviceHandler.postDelayed(mStatusChecker, musicService.maxDuration());
			}

		}
	};

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getSupportMenuInflater();
		//getMenuInflater().inflate(R.menu.playlist, menu);
		inflater.inflate(R.menu.playlist, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			
			Log.d(TAG, "Home as up called?");
			Intent returnIntent = new Intent();
			returnIntent.putExtra("videos", videoList);
			setResult(RESULT_OK, returnIntent);
			
			if(mBound) {
				unbindService(serviceConnection);
			}
			PlaylistActivity.this.finish();
			//NavUtils.navigateUpFromSameTask(this);
			return true;
			
    	case R.id.menu_search: 		
    		showSearchDialog("Please enter an artist's name");
    		return true;
    		
		}
		
	
			
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
		// TODO Auto-generated method stub
		
		selectedPosition = pos;
		current = pos;
		
		
		
		Log.v(TAG, "Clicking on number " + Integer.toString(current));

		Intent selectIntent = new Intent(PLAY_SELECT);
		intent.putExtra("position", selectedPosition);
		this.sendBroadcast(selectIntent);
		
		
		
		//playlistView.setSelection(pos);
		//currentTextView.setText(videoList.get(pos).getTitle());
		
		//VideoClass selectedVideo = videoList.get(pos);
		
		//String id = selectedVideo.getId();
		
		
		/*
        String yt_video_url = YOUTUBE_VIDEO_URL + videoList.get(pos).getId();
        YoutubeVideoTask myTask = new YoutubeVideoTask();
        myTask.execute(yt_video_url);*/
		
		
		
	}
	
	public void onRestoreInstanceState(Bundle savedInstanceState) {
	    // Always call the superclass so it can restore the view hierarchy
	    super.onRestoreInstanceState(savedInstanceState);
	   
	    // Restore state members from saved instance
	    videoList = (ArrayList<VideoClass>) savedInstanceState.getSerializable("videos");
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
	
    public void decodeURL(String content) {
		// TODO Auto-generated method stub
		Pattern startPattern = Pattern.compile("url_encoded_fmt_stream_map\\\": \\\"");
		Pattern endPattern = Pattern.compile("\\\", \\\"");
		Matcher matcher = startPattern.matcher(content);
		
		String title = findVideoFilename(content);
		String id = findVideoID(content);
		
		//String id = this.tempID;
		if(matcher.find()) {
			try {
				String[] start = content.split(startPattern.toString());
				String[] end = start[1].split(endPattern.toString());
				
				// Other decoding stuff
				String contentDecoded = URLDecoder.decode(end[0], "UTF-8");
				contentDecoded = contentDecoded.replaceAll(", ", "-");
				contentDecoded = contentDecoded.replaceAll("sig=", "signature=");
				contentDecoded = contentDecoded.replaceAll("x-flv", "flv");
				contentDecoded = contentDecoded.replaceAll("\\\\u0026", "&");
				Log.d("DEBUG", "contentDecoded: " + contentDecoded);
				findCodecs(contentDecoded, title, id);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
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
    
	private String findVideoFilename(String content) {
		// TODO Auto-generated method stub		
        Pattern videoPattern = Pattern.compile("<title>(.*?)</title>");
        Matcher matcher = videoPattern.matcher(content);
        String title;
        String titleRaw;
        if (matcher.find()) {
            titleRaw = matcher.group().replaceAll("(<| - YouTube</)title>", "").replaceAll("&quot;", "\"").replaceAll("&amp;", "&").replaceAll("&#39;", "'");
            title = titleRaw;
        } else {
            title = "Youtube Video";
        }

        Log.d("DEBUG", "findVideoFilename: " + title);
        
		return title;
	}
	
	private String findVideoID(String content) {
		//Pattern videoPattern = Pattern.compile("<input type=\\\"hidden\\\" name=\\\"video_id\\\" value=\\\"(.*?)\\\">");
		Pattern videoPattern = Pattern.compile("\\\"video_id\\\": (.*?) \\\"");
		//Pattern endPattern = Pattern.compile("\\\"");
		
		Matcher matcher = videoPattern.matcher(content);
		String id;
		
		if(matcher.find()) {
			id = matcher.group();
			id = id.replaceAll("\\\"video_id\\\": \\\"", "");
			id = id.replaceAll("\\\", \\\"", "").trim();
		}
		
		else {
			id = "error";
		}
		
		Log.d("DEBUG", "FindVideoID: " + id);
		
		return id;
	}

	private void findCodecs(String contentDecoded, String title, String id) {
		// TODO Auto-generated method stub
		Pattern trimPattern = Pattern.compile(",");
		Matcher matcher = trimPattern.matcher(contentDecoded);
		if(matcher.find()) {
			String[] CQS = contentDecoded.split(trimPattern.toString());
			
			// Just go with first quality for now
 			linksComposer(CQS[0], 1, title, id);
		}
		
	}

	private void linksComposer(String block, int i, String title, String id) {
		// TODO Auto-generated method stub
		Pattern urlPattern = Pattern.compile("http://.*");
		Matcher urlMatcher = urlPattern.matcher(block);
		if(urlMatcher.find()) {
			Pattern sigPattern = Pattern.compile("signature=[[0-9][A-Z]]{40}\\.[[0-9][A-Z]]{40}");
			Matcher sigMatcher = sigPattern.matcher(block);
			if(sigMatcher.find()) {
				String url = urlMatcher.group();
    			url = url.replaceAll("&type=.*", "");
    			url = url.replaceAll("&signature=.*", "");
    			url = url.replaceAll("&quality=.*", "");
    			url = url.replaceAll("&fallback_host=.*", "");
    			Log.d("DEBUG", "url: " + url);
    			String sig = sigMatcher.group();
    			Log.d("DEBUG", "sig: " + sig);
				String linkToAdd = url + "&" + sig;
				linkToAdd.replaceAll("&itag=[0-9][0-9]&signature", "&signature");
				String testString = new String(linkToAdd.getBytes());
				Log.d("DEBUG", "link:" + testString);
				
				videoList.get(selectedPosition).setUrl(testString);
				adapter.notifyDataSetChanged();

		        Uri testUri = Uri.parse(testString);

		        if(player != null && paused) {
		        	player.start();
		        	paused = false;
		        	return;
		        } else if (player != null) {
		        	releasePlayer();
		        }
		        
		        //sh = vidV.getHolder();
		        try {
			        player = new MediaPlayer();
			        player.setOnBufferingUpdateListener(this);
		            player.setOnPreparedListener(this);
		            player.setDataSource(this, testUri);
		           /* if(sh != null) {
		            	player.setDisplay(sh);
		            }*/
		            //player.setDisplay(sh);
		            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		            player.prepareAsync(); // buffer it asynchronously		            
		            player.setOnCompletionListener(this);
		            
		            //seek.setProgress(0);

		        } catch (IOException e) {
		            // TODO Auto-generated catch block
		            e.printStackTrace();
		        }
		        
			}
		}
		
	}

    private class YoutubeVideoTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			String URL = params[0];
			String html = getPage(URL);
			return html;
		}
    	
		protected void onPostExecute(String result) {
			decodeURL(result);
		}
    }
    
    public class PlaylistItem {
    	TextView songTextView;
    	LinearLayout rowLayout;
    }
    private class PlaylistAdapter extends ArrayAdapter<VideoClass> {
    	
    	private LayoutInflater inflater;
    	private ArrayList<VideoClass> data;
    	private PlaylistItem item;
    	
    	
    	
    	public PlaylistAdapter(Context context, int resource, ArrayList<VideoClass> data) {
    		super(context, resource, data);
    		PlaylistItem item;
    	}
    	

    	@SuppressWarnings("deprecation")
		public View getView(int position, View convertView, ViewGroup parent) {
    		
    		String myText = getItem(position).toString();
 
    		if(convertView == null) {
    			LayoutInflater inflater = LayoutInflater.from(this.getContext());
    			convertView = inflater.inflate(R.layout.basicitem, parent, false);
    			item = new PlaylistItem();
    			item.songTextView = (TextView) convertView.findViewById(R.id.text1);
    			item.rowLayout = (LinearLayout) convertView.findViewById(R.id.rowLayout);
    			convertView.setTag(item);
    			
    		} else {
    			item = (PlaylistItem) convertView.getTag();
    		}
    		
    		/*if(data.get(position).isisChecked(position)) {
    			item.rowLayout.setBackgroundColor(Integer.parseInt("409DBD", 16) + 0xFF000000);

    		} else {
				item.rowLayout.setBackgroundColor(Integer.parseInt("409DBD", 16) + 0x00000000);
    		}*/
    		
    		
    		Typeface bolded = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Regular.ttf");

    		item.songTextView.setText(myText);
        	item.songTextView.setTextColor(Color.WHITE);
        	item.songTextView.setTypeface(bolded);
        	item.songTextView.setTextSize(14);
        	
        	return convertView;

    	}
    	
    	public int getSize() {
    		return data.size();
    	}
    	
    	
    	
    }
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		int secondaryPosition = seekBar.getSecondaryProgress();
		if(fromUser && player != null) {
			if(progress > secondaryPosition) {
				player.seekTo(progress);
	            //String time = String.format("%d:%d", TimeUnit.MILLISECONDS.toMinutes(player.getDuration()), TimeUnit.MILLISECONDS.toSeconds(player.getDuration()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(player.getDuration())));
				timeText.setText(getTimeString(seekBar.getProgress()));
			}
		}
		
		else {
            //String time = String.format("%d:%d", TimeUnit.MILLISECONDS.toMinutes(player.getDuration()), TimeUnit.MILLISECONDS.toSeconds(player.getDuration()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(player.getDuration())));
			timeText.setText(getTimeString(seekBar.getProgress()));
		}
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		
		releasePlayer();
		playNextTrack();
	}
	
	private void playNextTrack() {
		// TODO Auto-generated method stub
		current++;
		// Loop around if you reach the end of the playlist
		if(current == videoList.size()) {
			current = 0;
		}
		
		playlistView.setSelection(current);
		
		currentTextView.setText(videoList.get(current).getTitle());
        String yt_video_url = YOUTUBE_VIDEO_URL + videoList.get(current).getId();
        YoutubeVideoTask myTask = new YoutubeVideoTask();
        myTask.execute(yt_video_url);
		
	}

	private String getTimeString(long millis) {
		StringBuffer buf = new StringBuffer();
		
		
		int hours =(int) millis/(1000*60*60);
	    int minutes = (int) ( millis % (1000*60*60) ) / (1000*60);
	    int seconds = (int) (( millis % (1000*60*60) ) % (1000*60) ) / 1000;

	    
	    
	    if(hours > 0) {
	    	buf.append(String.format("%02d", hours))
	        .append(":");	
	    }

	    buf.append(String.format("%02d", minutes))
        .append(":")
        .append(String.format("%02d", seconds));
	    
	    return buf.toString();
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		// TODO Auto-generated method stub
		
		if(mp != null && songSeek != null && mp.isPlaying()) {
			songSeek.setProgress(mp.getCurrentPosition());
			timeText.setText(getTimeString(songSeek.getProgress()));
		}
		
	}
	
	public MediaPlayer getMediaPlayer() {
		if(player == null) {
			player = new MediaPlayer();		
		}
		
		return player;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		// TODO Auto-generated method stub
        /*songSeek.setMax(mp.getDuration());
        maxText.setText(getTimeString(mp.getDuration()));
        
        
        player.start();
        player.setOnCompletionListener(this);

        
        if(player.isPlaying()) {
        	playButton.setVisibility(View.GONE);
        	pauseButton.setVisibility(View.VISIBLE);
        }*/

	}
	
	@SuppressLint("NewApi")
	private void updatePlayPanel(final VideoClass video) {
		Log.v(TAG, "PlaylistActivity(): Updating the Play Panel");
		
		int visiblePosition = playlistView.getFirstVisiblePosition();
		for(int i = visiblePosition; i < playlistView.getLastVisiblePosition(); ++i) {
			//playlistView.setSelection(musicService.getCurrentSong());
			VideoClass vid = adapter.getItem(i);
			
			Log.d(TAG, video.getId() + " " +  vid.getId());

			//v.setSelected(true);
			//newView.setSelected(true);
			//View testView = 
			Drawable d = getResources().getDrawable(R.drawable.list_select);
			int firstVisible = playlistView.getFirstVisiblePosition();
			
			if(vid.getId().trim().equals(video.getId().trim())) {
				Log.d(TAG, "MATCH FOUND FOR ID: " + vid.getId());
				View v = playlistView.getChildAt(i - firstVisible);
				//v.setSelected(true);
				playlistView.setItemChecked(i, true);

				View newView = playlistView.getAdapter().getView(i, v, playlistView);
				
			    if (newView instanceof Checkable) {
					playlistView.setItemChecked(i, true);
			    } else if (getBaseContext().getApplicationInfo().targetSdkVersion
			            >= android.os.Build.VERSION_CODES.HONEYCOMB) {
					newView.setActivated(true);
			        //child.setActivated(mCheckStates.get(position));
			    }
				
				//newView.setSelected(true);
				//newView.setBackgroundColor(color)
				//newView.setBackgroundDrawable(d);
				//newView.setBackgroundColor(Color.parseColor("#409DBD"));
				//newView.setBackgroundColor(Integer.parseInt("409DBD", 16) + 0xFF000000);
				 
			} else {
				View v = playlistView.getChildAt(i - firstVisible);
				//v.setSelected(false);
				//playlistView.setItemChecked(i, false);
				View newView = playlistView.getAdapter().getView(i, v, playlistView);
				//newView.setSelected(false);
				//newView.setBackgroundColor(Color.parseColor("#00000000"));
				playlistView.setItemChecked(i, false);

			    if (newView instanceof Checkable) {
					playlistView.setItemChecked(i, false);
			        //((Checkable) newView).setChecked(mCheckStates.get(position));
			    } else if (getBaseContext().getApplicationInfo().targetSdkVersion
			            >= android.os.Build.VERSION_CODES.HONEYCOMB) {
					newView.setActivated(false);
			        //child.setActivated(mCheckStates.get(position));
			    }
				//newView.setSelected(false);
				//newView.setBackgroundColor(Integer.parseInt("409DBD", 16) + 0x00000000);
			}
			
		}
		
		runOnUiThread(new Runnable() {
			
			public void run() {
				
				Log.v(TAG, "PlaylistActivity(): Updating the Play Panel");
				int elapsedMillis = musicService.elapsed();

				if(musicService.isPlaying()) {
					songSeek.setProgress(elapsedMillis);
					//timeText.setText(getTimeString(elapsedMillis));
				}

				
				
				
			/*	Log.v(TAG, "PlaylistActivity: " + Integer.toString(elapsedMillis) + Integer.toString(musicService.maxDuration()));
				currentTextView.setText(video.getTitle());
				
				Log.v(TAG, "The image URL is: " + video.getImageURL());
				UrlImageViewHelper.setUrlDrawable(videoImageView, video.getImageURL(), R.drawable.tubalr_icon);
				
				maxText.setText(getTimeString(musicService.maxDuration()));
				songSeek.setMax(musicService.maxDuration()); */
			}
		});
	}
	
	private void updatePlayPauseButtonState() {
		Log.v(TAG, "updatePlayPauseButtonState()");
		
		//Log.v(TAG, Boolean.toString(musicService.isPlaying()));
		
		if(musicService.isPlaying()) {
			pauseButton.setVisibility(View.VISIBLE);
			playButton.setVisibility(View.GONE);
		} else {
			pauseButton.setVisibility(View.GONE);
			playButton.setVisibility(View.VISIBLE);
		}
		
	}
	
	private class UpdateCurrentTrackTask extends AsyncTask<Void, VideoClass, Void> {
		public boolean stopped = false;
		public boolean paused = false;
		
		protected void onPreExecute() {
			super.onPreExecute();
			
			VideoClass currentVideo = musicService.getCurrentVideo();
					
			if(currentVideo != null) {
				updatePlayPanel(currentVideo);
			}
		}
		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
							
				//updatePlayPauseButtonState();
				if(!paused) {
					
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//Log.v(TAG, "PlaylistActivity: Updating current Track Task junts");
					VideoClass currentVideo = musicService.getCurrentVideo();
					
					Log.v(TAG, currentVideo.getTitle());
					//updatePlayPauseButtonState();
					
					if(currentVideo != null) {
						//publishProgress(currentVideo);
						//updatePlayPanel(currentVideo);
					}
					//publishProgress(currentVideo);
				}
				
	
		return null;
		

		}
		
		protected void OnProgressUpdate(VideoClass... video) {
			super.onProgressUpdate(video);
			/*if(stopped || paused) {
				return;
			}*/
			
			Log.d(TAG, "PlaylistActivity: onProgressUpdate");
			updatePlayPanel(video[0]);
		}
		
        public void stop() {
            stopped = true;
        }
        
        public void pause() {
            this.paused = true;
        }

        public void unPause() {
            this.paused = false;
        }
		
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		if(player == null) {
			player = new MediaPlayer();
		}
		//sh = holder;
		
		if( holder != null) {
			player.setDisplay(holder);
		}
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
	 public void onReceiveResult(int resultCode, Bundle resultData) {
	     switch (resultCode) {
	        case 0:
	            //show progress
	            break;
	           
	        case FINISHED:
	        	Log.d("RESULT", "Using onReceiveResult");
	            videoList = (ArrayList<VideoClass>) resultData.getSerializable("videos");
	            artist = resultData.getString("artist");
	            
	            playlistView = (ListView) findViewById(R.id.playlistView);
	            playlistView.setSelector(R.drawable.highlight_selector);
	            adapter = new PlaylistAdapter(context, R.layout.basicitem, videoList);
	    		playlistView.setAdapter(adapter);
	    		
	    		playlistView.setOnItemClickListener(this);
	    		pd.dismiss();
	    		adapter.notifyDataSetChanged();
	    		
	    		//serviceConnection = new MusicServiceConnection();
	    		
	    		if(musicServiceIntent == null) {
	    			MusicService.setMainActivity(PlaylistActivity.this);
	    			musicServiceIntent = new Intent(this, MusicService.class);
		    		musicServiceIntent.putExtra("videos", videoList);
		    		musicServiceIntent.putExtra("artist", artist);
		    		bindService(musicServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
	    		}
	    		
			  
	    		/*else {
	    			
	    			Intent newSongIntent = new Intent(NEW_SONGS);
	    			newSongIntent.putExtra("videos", videoList);
	    			this.sendBroadcast(newSongIntent);
	    			
	    			/*unbindService(serviceConnection);
	    			Log.v(TAG, "Recreating intent?");
	    			MusicService.setMainActivity(PlaylistActivity.this);
	    			musicServiceIntent = new Intent(this, MusicService.class);
		    		musicServiceIntent.putExtra("videos", videoList);
		    		musicServiceIntent.putExtra("artist", artist);
		    		bindService(musicServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);*/
	    		//}*/
	    		
		    	musicServiceBroadcastReceiver = new MusicServiceBroadcastReceiver();
		    	IntentFilter filter = new IntentFilter(MusicService.UPDATE_PLAYLIST);
		    	filter.addAction(MusicService.NEXT_TRACK);
		    	filter.addAction(MusicService.PREV_TRACK);
		    	filter.addAction(MusicService.PAUSE_TRACK);
		    	filter.addAction(MusicService.PLAY_SELECT);
		    	filter.addAction(MusicService.NEW_SONGS);
		    	registerReceiver(musicServiceBroadcastReceiver, filter);	
	    		

	            // do something interesting
	            // hide progress
	            break;

	    }
	}
	 
	 public void onSaveInstanceState(Bundle savedInstanceState) {
		 savedInstanceState.putSerializable("videos", videoList);
	 }
	  @Override
	  public void onPause() {
	    //super.onPause();
	    
		  if(musicServiceBroadcastReceiver != null) {
			  unregisterReceiver(musicServiceBroadcastReceiver);
			  musicServiceBroadcastReceiver = null;
		  }
		  
		 if(mBound) {
			 //unbindService(serviceConnection);
		 }

	    
	    
	    super.onPause();
	    
	    
	    if(mReceiver != null) {
			mReceiver.setReceiver(null);
	    }
	    //unregisterReceiver(playlistReceiver);
	    //playlistReceiver = null;
	    
	  }
	  
	  @Override
	  public void onStop() {
		  super.onStop();

		 // unbindService(serviceConnection);
	  }
	  
	  @Override
	  public void onResume() {
		  super.onResume();
		  
		  //musicServiceIntent = new Intent(this, MusicService.class);
		  //bindService(musicServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
		  
		  musicServiceBroadcastReceiver = new MusicServiceBroadcastReceiver();
  		  IntentFilter filter = new IntentFilter(MusicService.UPDATE_PLAYLIST);
  		  filter.addAction(MusicService.NEXT_TRACK);
  		  filter.addAction(MusicService.PREV_TRACK);
  		  filter.addAction(MusicService.PLAY_SELECT);
  		  filter.addAction(MusicService.PAUSE_TRACK);
  		  filter.addAction(MusicService.NEW_SONGS);
  		  registerReceiver(musicServiceBroadcastReceiver, filter);
  		  
  		  refreshScreen();
  		//registerReceiver(musicServiceBroadcastReceiver, filter);
		  
		  //refreshScreen();
		  IntentFilter intentFilter = new IntentFilter();
		  intentFilter.addAction(PlaylistService.TRANSACTION_DONE);
		  
		  BugSenseHandler.initAndStartSession(this, BUG_KEY);
	      Bugsnag.register(this, "1d479c585e3d333a05943f37bef208cf");
	      FlurryAgent.onStartSession(this, FLURRY_KEY);
	      

		  //registerReceiver(playlistReceiver, intentFilter);
	  }
	  
	  @Override
	  public void onRestart() {
		  super.onRestart();
	  }
	  
	 @Override
	 protected void onDestroy() {
		 super.onDestroy();
		 
		 if(musicServiceBroadcastReceiver != null) {
			 unregisterReceiver(musicServiceBroadcastReceiver);
			 musicServiceBroadcastReceiver = null;
		 }
		 
/*		 if(mBound) {
			 unbindService(serviceConnection);
		 }*/

		 
		 
	  	//unregisterReceiver(playlistReceiver);
	 }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		switch (v.getId()) {
		
		case R.id.playButton:
			if(musicService != null && !musicService.isPlaying()) {
				Log.d(TAG, "PLAY BUTTON CLICKED. AWW YISS.");

				/*player.start();
				pauseButton.setVisibility(View.VISIBLE);
				playButton.setVisibility(View.GONE);*/
				
				paused = false;
				Intent playTrackIntent = new Intent(PLAY_TRACK);
				this.sendBroadcast(playTrackIntent);
				
				updatePlayPauseButtonState();
			}
			break;
			
		case R.id.pauseButton:
			if(musicService != null && musicService.isPlaying()) {
				Log.d(TAG, "PAUSE BUTTON CLICKED. AWW YISS.");
				Intent pauseTrackIntent = new Intent(PAUSE_TRACK);
				this.sendBroadcast(pauseTrackIntent);
				updatePlayPauseButtonState();
				
				//player.pause();
				paused = true;
				//pauseButton.setVisibility(View.GONE);
				//playButton.setVisibility(View.VISIBLE);
			}
			break;
			
		case R.id.nextButton:
			if(player != null) {
				//releasePlayer();
			}
			
			Log.d(TAG, "NEXT TRACK CLICKED. HELL YES.");
			Intent nextTrackIntent = new Intent(NEXT_TRACK);
			this.sendBroadcast(nextTrackIntent);
			
			//playNextTrack();
	    
			break;
			
		case R.id.previousButton:

			Intent prevTrackIntent = new Intent(PREV_TRACK);
			this.sendBroadcast(prevTrackIntent);
			
			break;
			
		default: 
			break;
		}
		
	}
	
	private void showSearchDialog(String message) {
		// TODO Auto-generated method stub
		
		String url, artist, search, type = "";
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		
		alert.setTitle("Build New Playlist . . . ");
		alert.setMessage(message);
		final EditText input = new EditText(this);
		//String artist = "";
		alert.setView(input);

		alert.setPositiveButton("Just", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
				s_artist = input.getEditableText().toString().trim();
				BugSenseHandler.sendEvent("User searched for just " + s_artist + " in playlist");

				if(s_artist.length() > 0) {
					s_type = "just";
					try {
						s_url = ECHONEST_SONG_URL + URLEncoder.encode(s_artist, "UTF-8") + ECHONEST_RESULT_URL + "40";
					
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					dialog.dismiss();
					
					if(player != null) {
						//player.stop();
						//player.reset();
						Log.d("SWAG", "Testing junts");
					}
					
					Toast.makeText(getBaseContext(), "Building new playlist . . . ", Toast.LENGTH_LONG).show();

					
					Intent i = new Intent(getBaseContext(), PlaylistService.class);
			        i.putExtra("url", s_url);
			        i.putExtra("type", s_type);
			        i.putExtra("artist", s_artist);
			        i.putExtra("rec", mReceiver);
			        
					startService(i);
				}
				
				else {
					Toast.makeText(getApplicationContext(), "Invalid input into the search box!", Toast.LENGTH_SHORT).show();
					//dialog.dismiss();
				}

			}
		});
		alert.setNegativeButton("Similar", new DialogInterface.OnClickListener() {
			
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
				s_artist = input.getEditableText().toString();
				BugSenseHandler.sendEvent("User searched for similar to " + s_artist + " from Playlist");

				if(s_artist.length() > 0) {
					s_type = "similar";
					try {
						s_url = ECHONEST_SIMILAR_URL + URLEncoder.encode(s_artist, "UTF-8") + ECHONEST_RESULT_URL + "40";
					
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					dialog.dismiss();
					
					if(player != null) {
						//player.stop();
						//player.reset();
						Log.d("SWAG", "Testing junts");
					}
					
					Toast.makeText(getBaseContext(), "Building new playlist . . . ", Toast.LENGTH_LONG).show();
	
					
					Intent i = new Intent(getBaseContext(), PlaylistService.class);
			        i.putExtra("url", s_url);
			        i.putExtra("type", s_type);
			        i.putExtra("artist", s_artist);
			        i.putExtra("rec", mReceiver);
			        
					startService(i);
				}
				
				else {
					Toast.makeText(getApplicationContext(), "Invalid input into the search box!", Toast.LENGTH_SHORT).show();
					//dialog.dismiss();
				}
			}
		});
    
		AlertDialog showAlert = alert.create();
		showAlert.show();
		
		
		/*if(!showAlert.isShowing()) {
			if(player != null) {
				player.stop();
				player.reset();
			}
			
			Log.d("DEBUG", "VIDEO LIST NEW SEARCH FROM DIALOG.");

		}*/
	}

	@Override
	public void onAudioFocusChange(int focusChange) {
		// TODO Auto-generated method stub
		//AudioManager manager = new AudioManager();
		
	}
	
	private void releasePlayer() {
		if(player == null) {
			return;
		}
		
		if(player.isPlaying()) {
			player.stop();
		}
		
		player.release();
		player = null;
	}
	
	public void updatePlayQueue() {
		updatePlayPauseButtonState();
		
		if(musicService.getCurrentVideo() != null) {
			updatePlayPanel(musicService.getCurrentVideo());
		}
		
		//mStatusChecker.run();
		if(updateCurrentTrackTask == null) {
			Log.v(TAG, "PlaylistActivity: currently in updateCurrentTrackTask pre-execute");
			
			//updateCurrentTrackTask = new UpdateCurrentTrackTask();
			//updateCurrentTrackTask.execute();
		} else {
			//updateCurrentTrackTask.stop();
			//updateCurrentTrackTask = null;
			Log.e("PlaylistActivity", "updateCurrentTrackTask is not null");
		}
	}
	
    protected void onStart() {
    	super.onStart();	
        BugSenseHandler.initAndStartSession(this, BUG_KEY);
        Bugsnag.register(this, "1d479c585e3d333a05943f37bef208cf");
        FlurryAgent.onStartSession(this, FLURRY_KEY);
    }
    
    private class MusicServiceBroadcastReceiver extends BroadcastReceiver {
    	
    	public void onReceive(Context context, Intent intent) {
    		Log.d("PlaylistActivity", "MusicServiceBroadcastReceive.onReceive action =  " + intent.getAction());
    		if(MusicService.UPDATE_PLAYLIST.equals(intent.getAction())) {
    			Log.d(TAG, "updatePlaylist is called!");
    			//updatePlayQueue();
    			
    			updatePlayPanel(musicService.getCurrentVideo());
    		}
    	}
    }
    
    private final class MusicServiceConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName className, IBinder baBinder) {
			// TODO Auto-generated method stub
			Log.d(TAG, "MusicServiceConnection: Service connected");
			musicService = ((MusicService.MusicServiceBinder) baBinder).getService();
			MusicService.setMainActivity(PlaylistActivity.this);
    		musicServiceIntent.putExtra("videos", videoList);
			startService(musicServiceIntent);
			
			mBound = true;
			
			
			if(!musicService.isPlaying() && videoList.size() > 0) {
				musicService.playCurrentSong();
			}
			
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			// TODO Auto-generated method stub
			Log.d(TAG, "ServiceConnection: Service disconnected.");
			
			musicService = null;
			serviceConnection = null;
			
			mBound = false;
			//startService(musicServiceIntent);
		}
    	
    }
    
    private void refreshScreen() {
    	if(musicService == null) {
    		//Log.v(TAG, "musicService is ")
    		//updateScreenAsync();
    	} else {
    		//updatePlayQueue();
    	}
    }

	private void updateScreenAsync() {
		// TODO Auto-generated method stub
		waitForMusicServiceTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				Log.d(TAG, "updateScreenAsync running timer");
				if(musicService != null) {
					waitForMusicServiceTimer.cancel();
					handler.post(new Runnable() {
						public void run() {
							updatePlayQueue();
						}
					});
				}
			}
		}, 10, 250);
	}
	
	private class TimelineChangeListener implements OnSeekBarChangeListener {
		private Timer delayedSeekTimer;

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			
			if(fromUser) {
				Log.d(TAG, "TimelineChangeListener progress received from user: " + progress);
				
				scheduleSeek(progress);
				
				return;
			}
			
		}
		
		private void scheduleSeek(final int progress) {
			if (delayedSeekTimer != null) {
				delayedSeekTimer.cancel();
			}
			
			delayedSeekTimer = new Timer();
			delayedSeekTimer.schedule(new TimerTask() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					Log.d(TAG, "Delayed Seek Timer run!");
					musicService.seek(progress);
					//updatePlayPanel(musicService.getCurrentVideo());
				}
				
				
			}, 170);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
//			updateCurrentTrackTask.pause();
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			Log.d(TAG, "Started tracking touch in Timeline Change!");
			//updateCurrentTrackTask.unPause();
			
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
	
	
	
}