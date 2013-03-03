package com.iheanyiekechukwu.tubalr;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class PlaylistActivity extends Activity implements OnItemClickListener {

	private ListView playlistView;
	//private PlaylistAdapter playlistAdapter;
	
	private ArrayList<String> stringList;
	private ArrayAdapter<VideoClass> playlistStringAdapter;
	
	private PlaylistAdapter adapter;
	private Context context;
	private Intent i;
	
	private ArrayList<VideoClass> videoList;
	
	private int selectedPosition = 0;
	
    private static final String YOUTUBE_VIDEO_URL = "https://youtube.com/watch?v=";
    
    private MediaPlayer player;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_playlist);
		// Show the Up button in the action bar.
		setupActionBar();
		
		
		i = this.getIntent();
		
		
		//ArrayList<VideoClass> tempList = (ArrayList<VideoClass>) i.getSerializableExtra("playlistExtra");
		videoList = (ArrayList<VideoClass>) i.getSerializableExtra("playlistExtra");
		Iterator<VideoClass> it = videoList.iterator();
		
		context = this.getBaseContext();
		stringList = new ArrayList<String>();
		playlistView = (ListView) findViewById(R.id.playlistView);
		adapter = new PlaylistAdapter(this, R.layout.basicitem, videoList);
		playlistStringAdapter = new ArrayAdapter<VideoClass>(context, android.R.layout.simple_list_item_1, videoList);
		playlistView.setAdapter(adapter);

		playlistView.setOnItemClickListener(this);
		
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
		
        String yt_video_url = YOUTUBE_VIDEO_URL + videoList.get(0).getId();
        YoutubeVideoTask myTask = new YoutubeVideoTask();
        myTask.execute(yt_video_url);
		
		
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
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
		// TODO Auto-generated method stub
		
		selectedPosition = pos;
		
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
				
		        MediaPlayer sdrPlayer = new MediaPlayer();

		        Uri testUri = Uri.parse(testString);
		        try {
		            sdrPlayer.setDataSource(this, testUri);
		            sdrPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		            sdrPlayer.prepare(); //don't use prepareAsync for mp3 playback
		        } catch (IOException e) {
		            // TODO Auto-generated catch block
		            e.printStackTrace();
		        }

		        sdrPlayer.start();
		        
				// You probably want to play back what was selected.
				
				
				
				//playlist.add(new VideoClass(id, title, testString));
				
				//playlistStringArray.add(title);
				
				//playlistViewAdapter.add(title);
				//playlistViewAdapter.notifyDataSetChanged();
		        /*MediaPlayer sdrPlayer = new MediaPlayer();

		        Uri testUri = Uri.parse(testString);
		        try {
		            sdrPlayer.setDataSource(this, testUri);
		            sdrPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		            sdrPlayer.prepare(); //don't use prepareAsync for mp3 playback
		        } catch (IOException e) {
		            // TODO Auto-generated catch block
		            e.printStackTrace();
		        }

		        sdrPlayer.start();
				Log.d("PLAY", Integer.toString(playlist.size()));*/
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
}