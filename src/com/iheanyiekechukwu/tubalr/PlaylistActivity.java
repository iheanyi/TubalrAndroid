package com.iheanyiekechukwu.tubalr;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
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

import com.bugsense.trace.BugSenseHandler;
import com.bugsnag.android.Bugsnag;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
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
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class PlaylistActivity extends Activity implements OnClickListener, MyResultReceiver.Receiver, OnItemClickListener, OnSeekBarChangeListener, OnCompletionListener, OnBufferingUpdateListener, OnPreparedListener, Callback, OnAudioFocusChangeListener {

	private ListView playlistView;
	//private PlaylistAdapter playlistAdapter;
	
	private ArrayList<String> stringList;
	private ArrayAdapter<VideoClass> playlistStringAdapter;
	
	private PlaylistAdapter adapter;
	private Context context;
	private Intent intent;
	
	private ArrayList<VideoClass> videoList;
	
	private int selectedPosition = 0;
	
    private static final String YOUTUBE_VIDEO_URL = "https://youtube.com/watch?v=";

	private static final int FINISHED = 1;
    
    private MediaPlayer player;

    private SeekBar seek;
    
    private TextView timeText, maxText;
    
    private int current = 0;
    
    private SurfaceView vidV;
    private SurfaceHolder sh;
    
    private String artist;    
    
    public MyResultReceiver mReceiver;
    
    private TextView currentTextView;
    
	private ProgressDialog pd;
	
	private ImageButton playButton;
	private ImageButton pauseButton;
	private ImageButton nextButton;
	private ImageButton prevButton;
	
    // EchoNest URLs
    public static final String ECHONEST_SONG_URL = "http://developer.echonest.com/api/v4/artist/songs?api_key=OYJRQNQMCGIOZLFIW&name=";    
    public static final String ECHONEST_SIMILAR_URL = "http://developer.echonest.com/api/v4/artist/similar?api_key=OYJRQNQMCGIOZLFIW&name=";
    public static final String ECHONEST_RESULT_URL = "&format=json&callback=?&start=0&results=";

    public static final String BUG_KEY = "b27d57ef";
    
	private String s_url, s_artist, s_search, s_type = "";

    
    /*private BroadcastReceiver playlistReceiver = new BroadcastReceiver() {
    @Override
    	public void onReceive(Context context, Intent intent) {
    		Log.d("SHIT", "In Playlist Activity now!");
    		
    		if(videoList.size() > 0) {
    			videoList = new ArrayList<VideoClass>();
    		}
    		videoList = (ArrayList<VideoClass>) intent.getSerializableExtra("videos");
    		if(videoList.size() == 0) {
    			Toast.makeText(context, "ERROR BUILDING PLAYLIST", Toast.LENGTH_LONG).show();
    		}
    		
    		playlistView = (ListView) findViewById(R.id.playlistView);
    		adapter = new PlaylistAdapter(context, R.layout.basicitem, videoList);
    		playlistStringAdapter = new ArrayAdapter<VideoClass>(context, android.R.layout.simple_list_item_1, videoList);
    		playlistView.setAdapter(adapter);
    		
    		playlistView.setOnItemClickListener(PlaylistActivity.this);

    		
    		adapter.notifyDataSetChanged();
            String yt_video_url = YOUTUBE_VIDEO_URL + videoList.get(0).getId();
            YoutubeVideoTask myTask = new YoutubeVideoTask();
            myTask.execute(yt_video_url);
    		pd.dismiss();
    }
};*/
    
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_playlist);
		// Show the Up button in the action bar.
		
        BugSenseHandler.initAndStartSession(this, BUG_KEY);
        Bugsnag.register(this, "1d479c585e3d333a05943f37bef208cf");

		setupActionBar();
		
		
		// IMPLEMENT VISUALIZER!
		
		
		intent = this.getIntent();
		
		
		mReceiver = new MyResultReceiver(new Handler());
		mReceiver.setReceiver(this);
		
		String url = intent.getExtras().getString("url");
		String type = intent.getExtras().getString("type");
		String name = intent.getExtras().getString("artist");
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(PlaylistService.TRANSACTION_DONE);
		
		
		currentTextView = (TextView) findViewById(R.id.currentTextView);
		//registerReceiver(playlistReceiver, intentFilter);
		Intent i = new Intent(this, PlaylistService.class);
        i.putExtra("url", url);
        i.putExtra("type", type);
        i.putExtra("artist", name);
        i.putExtra("rec", mReceiver);
		startService(i);
		
		pd = ProgressDialog.show(this, "Building Playlist", "Finding Songs Relevant For Query: " + name);
		pd.setCancelable(true);

		vidV = (SurfaceView) findViewById(R.id.videoStream);
		sh = vidV.getHolder();
		
		sh.setFixedSize(640, 320);
		
		//Collections.shuffle(videoList);
		
		sh.addCallback(this);
		//ArrayList<VideoClass> tempList = (ArrayList<VideoClass>) i.getSerializableExtra("playlistExtra");
		//videoList = (ArrayList<VideoClass>) i.getSerializableExtra("playlistExtra");
		//Iterator<VideoClass> it = videoList.iterator();
		
		if(player != null) {
			player.stop();
		}
		
		
		else {
			player = new MediaPlayer();
		}
		
		timeText = (TextView) findViewById(R.id.timeText);
		maxText = (TextView) findViewById(R.id.maxText);
		context = this.getApplicationContext();
		stringList = new ArrayList<String>();
		
		playButton = (ImageButton) findViewById(R.id.playButton);
		pauseButton = (ImageButton) findViewById(R.id.pauseButton);
		nextButton = (ImageButton) findViewById(R.id.nextButton);
		prevButton = (ImageButton) findViewById(R.id.previousButton);
		
		playButton.setOnClickListener(this);
		pauseButton.setOnClickListener(this);
		nextButton.setOnClickListener(this);
		prevButton.setOnClickListener(this);
		/*playlistView = (ListView) findViewById(R.id.playlistView);
		adapter = new PlaylistAdapter(this, R.layout.basicitem, videoList);
		playlistStringAdapter = new ArrayAdapter<VideoClass>(context, android.R.layout.simple_list_item_1, videoList);
		playlistView.setAdapter(adapter);
		
		playlistView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);*/
		
		seek = (SeekBar) findViewById(R.id.songSeekBar);
		
		seek.setOnSeekBarChangeListener(this);

		//playlistView.setOnItemClickListener(this);
		
        Drawable d = getResources().getDrawable(R.drawable.navbar);
        
        getActionBar().setBackgroundDrawable(d);
        
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowTitleEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(false);
		
	    int actionBarTitleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
	    
	    TextView actionBarTextView = (TextView) findViewById(actionBarTitleId);
	    actionBarTextView.setTextColor(Color.WHITE);
	    
	    Typeface bookman = Typeface.createFromAsset(getAssets(), "fonts/bookos.ttf");
	    actionBarTextView.setTypeface(bookman);
		getActionBar().setTitle("tubalr");
		
		// Adapter has been loaded properly . . . Sooo, yeah, play the first song.
		

        
        player.setOnCompletionListener(this);
        player.setOnBufferingUpdateListener(this);
        player.setOnPreparedListener(this);
        
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

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.playlist, menu);
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
			NavUtils.navigateUpFromSameTask(this);
			return true;
			
    	case R.id.menu_search: 		
    		showSearchDialog();
    		return true;
    		
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
		// TODO Auto-generated method stub
		
		selectedPosition = pos;
		current = pos;
		
		playlistView.setSelection(pos);
		currentTextView.setText(videoList.get(pos).getTitle());
		
		//VideoClass selectedVideo = videoList.get(pos);
		
		//String id = selectedVideo.getId();
		
        String yt_video_url = YOUTUBE_VIDEO_URL + videoList.get(pos).getId();
        YoutubeVideoTask myTask = new YoutubeVideoTask();
        myTask.execute(yt_video_url);
		
		
		
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
		        player.reset();
		        try {
		            player.setDataSource(this, testUri);
		            player.setDisplay(sh);
		            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		            player.prepareAsync(); // buffer it asynchronously
		            
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
    }
    private class PlaylistAdapter extends ArrayAdapter<VideoClass> {
    	
    	private LayoutInflater inflater;
    	private ArrayList<VideoClass> data;
    	private PlaylistItem item;
    	
    	
    	
    	public PlaylistAdapter(Context context, int resource, ArrayList<VideoClass> data) {
    		super(context, resource, data);
    		PlaylistItem item;
    	}
    	
    	public View getView(int position, View convertView, ViewGroup parent) {
    		
    		String myText = getItem(position).toString();
    		if(convertView == null) {
    			LayoutInflater inflater = LayoutInflater.from(this.getContext());
    			convertView = inflater.inflate(R.layout.basicitem, parent, false);
    			item = new PlaylistItem();
    			item.songTextView = (TextView) convertView.findViewById(R.id.text1);
    			
    			convertView.setTag(item);
    			
    		} else {
    			item = (PlaylistItem) convertView.getTag();
    		}
    		
    		Typeface bolded = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Regular.ttf");

    		item.songTextView.setText(myText);
        	item.songTextView.setTextColor(Color.WHITE);
        	item.songTextView.setTypeface(bolded);
        	item.songTextView.setTextSize(14);
        	
        	return convertView;

    	}
    	
    	
    	
    }
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		
		if(fromUser) {
			player.seekTo(progress);
            //String time = String.format("%d:%d", TimeUnit.MILLISECONDS.toMinutes(player.getDuration()), TimeUnit.MILLISECONDS.toSeconds(player.getDuration()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(player.getDuration())));
			timeText.setText(getTimeString(seekBar.getProgress()));
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
		
		Log.i("Completion Listener", "Song Complete");
		mp.stop();
		mp.reset();
		
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

	    buf
        //.append(String.format("%02d", hours))
        //.append(":")
        .append(String.format("%02d", minutes))
        .append(":")
        .append(String.format("%02d", seconds));
	    
	    return buf.toString();
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		// TODO Auto-generated method stub
		
		if(mp.isPlaying() && seek != null) {
			seek.setProgress(mp.getCurrentPosition());
			timeText.setText(getTimeString(seek.getProgress()));
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
        seek.setMax(mp.getDuration());
        maxText.setText(getTimeString(mp.getDuration()));	
        player.start();
        
        if(player.isPlaying()) {
        	playButton.setVisibility(View.GONE);
        	pauseButton.setVisibility(View.VISIBLE);
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
		
		player.setDisplay(holder);
		
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
	            adapter = new PlaylistAdapter(context, R.layout.basicitem, videoList);
	    		playlistStringAdapter = new ArrayAdapter<VideoClass>(context, android.R.layout.simple_list_item_1, videoList);
	    		playlistView.setAdapter(adapter);
	    		
	    		playlistView.setOnItemClickListener(this);
	    		pd.dismiss();
	    		adapter.notifyDataSetChanged();

	    		if(player != null) {
	    			player.stop();
	    			player.reset();
	    		}
	    		
	    		
	            
	    		
	    		// Play the first song
	            String yt_video_url = YOUTUBE_VIDEO_URL + videoList.get(0).getId();
	            currentTextView.setText(videoList.get(0).getTitle());
	            YoutubeVideoTask myTask = new YoutubeVideoTask();
	            myTask.execute(yt_video_url);
	    		
	    		
	            // do something interesting
	            // hide progress
	            break;

	    }
	}
	  @Override
	  public void onPause() {
	    super.onPause();
	    if(mReceiver != null) {
			mReceiver.setReceiver(null);
	    }
	    //unregisterReceiver(playlistReceiver);
	    //playlistReceiver = null;
	    
	  }
	  
	  @Override
	  public void onResume() {
		  super.onResume();
		  IntentFilter intentFilter = new IntentFilter();
		  intentFilter.addAction(PlaylistService.TRANSACTION_DONE);
		  //registerReceiver(playlistReceiver, intentFilter);
	  }
	  
	 @Override
	 protected void onDestroy() {
		 super.onDestroy();
	  	//unregisterReceiver(playlistReceiver);
	 }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		switch (v.getId()) {
		
		case R.id.playButton:
			if(!player.isPlaying()) {
				player.start();
				pauseButton.setVisibility(View.VISIBLE);
				playButton.setVisibility(View.GONE);
				
			}
			break;
			
		case R.id.pauseButton:
			if(player.isPlaying()) {
				player.pause();
				pauseButton.setVisibility(View.GONE);
				playButton.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.nextButton:			
			player.stop();
			player.reset();
			
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
	        
			break;
			
		case R.id.previousButton:

			player.stop();
			player.reset();
			
			
			// Loop around if you reach the end of the playlist
			if(current == 0) {
				current = 0;
			}
			
			else {
				current--;
			}
			
			playlistView.setSelection(current);
			
			currentTextView.setText(videoList.get(current).getTitle());
	        String yt_url = YOUTUBE_VIDEO_URL + videoList.get(current).getId();
	        YoutubeVideoTask newTask = new YoutubeVideoTask();
	        newTask.execute(yt_url);
	        
			break;
			
		default: 
			break;
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
	

	
}