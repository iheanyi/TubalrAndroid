package com.iheanyiekechukwu.tubalr;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

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
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.bugsense.trace.BugSenseHandler;
import com.bugsnag.android.Bugsnag;
import com.flurry.android.FlurryAgent;
import com.iheanyiekechukwu.tubalr.UserPlaylist.OnFragmentInteractionListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.viewpagerindicator.TitlePageIndicator;

public class MainActivity extends SherlockFragmentActivity implements OnClickListener, OnFragmentInteractionListener {

	private static final String[] CONTENT = new String[] {"Genres", "Subreddits", "My Playlists"};
	
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	TabsAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	
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
    
    private static Integer userID = -1;
    
    private static String userToken = "";
    
    
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
    
    boolean artistClicked = false;
    
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

	MyPageAdapter pageAdapter;
	
	boolean runUnregister = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		BugSenseHandler.initAndStartSession(this, BUG_KEY);
        Bugsnag.register(this, "1d479c585e3d333a05943f37bef208cf");
        FlurryAgent.onStartSession(this, FLURRY_KEY);
		
		List<Fragment> fragments = getFragments();

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.

		ActionBar bar = getSupportActionBar();
		
		mSectionsPagerAdapter = new TabsAdapter(getSupportFragmentManager());
		
		//pageAdapter = new MyPageAdapter(getSupportFragmentManager(), fragments);
		
		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		//mViewPager.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
		mViewPager.setOffscreenPageLimit(mSectionsPagerAdapter.getCount() - 1);
		
		
		//bar.addTab(bar.newTab().setText("Genres"), Genres.class, null);

		
		Tab genreTab = bar.newTab();
		//genreTab.setText("Genres");
		//genreTab.setTabListener(this);
		Tab redditTab = bar.newTab();
		Tab userTab = bar.newTab();
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		
		getSupportActionBar().setTitle("tubalr");
        
        Drawable d = getResources().getDrawable(R.drawable.navbar);
        
        getSupportActionBar().setBackgroundDrawable(d);
        
        context = getBaseContext();
        
		if((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
			runUnregister = false;
			finish();
			return;
		}
		
		//redditTab.setText("Subreddits");
		//redditTab.setTabListener(this);
		
		mSectionsPagerAdapter.addTab(genreTab, Genres.class, null);
		mSectionsPagerAdapter.addTab(redditTab, Subreddit.class, null);
		mSectionsPagerAdapter.addTab(userTab, UserPlaylist.class, null);
		mSectionsPagerAdapter.notifyDataSetChanged();
		
		TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
		indicator.setViewPager(mViewPager);
		//indicator.setViewPager(view)
		indicator.setBackgroundColor(Color.DKGRAY);
		
		currentArtist = (TextView) findViewById(R.id.artistNameText);
		currentArtist.setOnClickListener(this);
		
		controlLayout = (LinearLayout) findViewById(R.id.controlLayout);
		
		if(!isMyServiceRunning()) {
			controlLayout.setVisibility(View.GONE);
		} else {
			controlLayout.setVisibility(View.VISIBLE);
		}
		
		prevButton = (ImageButton) findViewById(R.id.homePrevButton);
		nextButton = (ImageButton) findViewById(R.id.homeNextButton);
		
		homePlayButton = (ImageButton) findViewById(R.id.homePlayButton);
		homePauseButton = (ImageButton) findViewById(R.id.homePrevButton);
		
		prevButton.setOnClickListener(this);
		nextButton.setOnClickListener(this);
		
        context = getBaseContext();
        
		Typeface bolded = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Regular.ttf");
		Typeface light = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
		
        currentArtist.setTypeface(bolded);
        artistSongList = new ArrayList<String>();

		
		//mViewPager.setOnPageChangeListener(this);
		//bar.addTab(genreTab);
		//bar.addTab(redditTab);
		//mSectionsPagerAdapter.addTab(bar.newTab().setText("Genres"), Genres.class, null);
		//mSectionsPagerAdapter.addTab(bar.newTab().setText("Reddit"), Subreddit.class, null);
		
	}
	
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
    	
		Log.d(TAG, "On activity result!");
		
		switch(requestCode) {
			case 1:
	    		if(resultCode == RESULT_FIRST_USER) {
	    			
	    			Log.d(TAG, "Activity for Result! RESULT OK!");	
	    			videos = (ArrayList<VideoClass>) data.getSerializableExtra("videos");
		    		
	    			//serviceConnection = new MusicServiceConnection();
	    			if(isMyServiceRunning()) {
	    				controlLayout.setVisibility(View.VISIBLE);
	    			}
	    			
	    			MusicService.setMainActivity(MainActivity.this);

		    		if(musicServiceIntent == null) {
		    			musicServiceIntent = new Intent(this, MusicService.class);

		    		}
		    		
		    		bindService(musicServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

		    		
	    			
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
			    	
			    	mViewPager.getAdapter().notifyDataSetChanged();
	    	}
	    		
			case 2:
				if(resultCode == RESULT_OK) {
					userID = data.getIntExtra("userid", -1);
					userToken = data.getStringExtra("token");
					
					UserHelper.userInfo[UserHelper.USER] = Integer.toString(userID);
					UserHelper.userInfo[UserHelper.TOKEN] = userToken; 
					
					Toast.makeText(context, "Successfully logged in. . . Token: " + Integer.toString(userID) + " " + userToken, Toast.LENGTH_SHORT).show();
					//UserPlaylist.newInstance(UserHelper.userInfo[UserHelper.USER], UserHelper.userInfo[UserHelper.TOKEN]);
					mViewPager.getAdapter().notifyDataSetChanged();					
				}
				
	    		
	    		break;
		}

}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private List<Fragment> getFragments(){
		  List<Fragment> fList = new ArrayList<Fragment>();
		 
		  fList.add(Genres.newInstance("Genres", "Test"));
		  fList.add(Subreddit.newInstance("Subreddit", "Test")); 
		  fList.add(UserPlaylist.newInstance(Integer.toString(userID), userToken));
		  
		  //}
		  //fList.add(MyFragment.newInstance("Fragment 3"));
		 
		  return fList;
	}
	
	@Override
	public void onSaveInstanceState(Bundle saveInstanceState) {
		
		saveInstanceState.putString("BUG_FIX", "BUG_FIX");
		super.onSaveInstanceState(saveInstanceState);
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
			MusicService.setMainActivity(MainActivity.this);
			
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
    			
    			//updatePlayPanel(musicService.getCurrentVideo());

    			
    			currentArtist.setText(musicService.getCurrentVideo().getTitle());
    		}
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
			        
			        if(mBound && serviceConnection != null && videos.size() != 0) {
			        	//musicService.stop();
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
			        
			        
			        if(mBound && serviceConnection != null) {
			        	//musicService.stop();
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

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public static class TabsAdapter extends FragmentPagerAdapter implements TabListener {
		/*private final Context mContext;
        private final ActionBar mActionBar;
        private final ViewPager mViewPager;*/
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
        
	    static final class TabInfo {
            private final Class<?> clss;
            private final Bundle args;

            TabInfo(Class<?> _class, Bundle _args) {
                clss = _class;
                args = _args;
            }
	    }
        public TabsAdapter(FragmentManager fm) {
        	
           super(fm);
        }
        
        @Override
        public CharSequence getPageTitle(int position) {
            return CONTENT[position % CONTENT.length].toUpperCase();
        }
        
        public void addTab(Tab tab, Class<?> clss, Bundle args) {
            TabInfo info = new TabInfo(clss, args);
            tab.setTag(info);
            tab.setTabListener(this);
            //mTabs.add(info);
            //mActionBar.addTab(tab);
            //notifyDataSetChanged();
        }
        
        public int getCount() {
        	return 3;
            //return mTabs.size();
        }
		


		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			//TabInfo info = mTabs.get(position);
			
			if(position == 0) {
				return Genres.newInstance("Swag", "Swoop");
			} else if(position == 1) {
				return Subreddit.newInstance("Swag", "Swoop");
			} else {
				return UserPlaylist.newInstance(UserHelper.userInfo[UserHelper.USER], UserHelper.userInfo[UserHelper.TOKEN]);
			}
		}
		
		@Override	
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			// TODO Auto-generated method stub
			
		}


	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";

		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main_dummy,
					container, false);
			/*TextView dummyTextView = (TextView) rootView
					.findViewById(R.id.section_label);*/
			/*dummyTextView.setText(Integer.toString(getArguments().getInt(
					ARG_SECTION_NUMBER)));*/
			return rootView;
		}
	}
	
    public void showToast() {
    	Toast.makeText(this, "Working on this feature still, try searching for something! Sorry!", Toast.LENGTH_SHORT).show();
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
	    	case R.id.menu_search:
	    		
	    		showSearchDialog();
	    		return true;
	    		
	    	case R.id.action_login:
	    		
	    		Intent loginIntent = new Intent(this, LoginActivity.class);
	    		
	    		startActivityForResult(loginIntent, 2);
	    		return true;
	    		
	    	case R.id.menu_add:
	    		
	    		if(UserHelper.userLoggedIn()) {
		    		showAddDialog();
		    		

	    		} else {
	    			Toast.makeText(this, "Please log in first.", Toast.LENGTH_SHORT).show();
	    		}
	    		return true;
	    		
	    	case R.id.menu_refresh:
	    		if(UserHelper.userLoggedIn()) {
					mViewPager.getAdapter().notifyDataSetChanged();
	    			//UserPlaylist.newInstance(UserHelper.userInfo[UserHelper.USER], UserHelper.userInfo[UserHelper.TOKEN]);
	    		} else {
	    			Toast.makeText(this, "Please login first.", Toast.LENGTH_SHORT).show();
	    		}
	    		
	    		return true;
	    		
	    	
	    	default:
	    		showToast();
	    		return super.onOptionsItemSelected(item);
    	}
    }
    
    private void showAddDialog() {
		// TODO Auto-generated method stub
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
		
		//alert.setTitle("");
		alert.setMessage("Create a New Playlist:");
		final EditText input = new EditText(this);
		//String artist = "";
		alert.setView(input);
		alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				String playlistName = input.getEditableText().toString().trim();
				
        		dialog.dismiss();

				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append("http://www.tubalr.com/api/playlist/create?playlist_name=");
				stringBuilder.append(URLEncoder.encode(playlistName));
				String createURL = stringBuilder.toString();
	            
				AsyncHttpClient client = new AsyncHttpClient();
	          
				
				RequestParams params = new RequestParams();
				params.put("auth_token", UserHelper.userInfo[UserHelper.TOKEN]);
	            
				client.post(createURL, params, new JsonHttpResponseHandler() {
	            	public void onSuccess(JSONObject result) {
	            		
	            		String name = "";
						try {
							name = result.getString("name");

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							
							Toast.makeText(getBaseContext(), "Error creating a new playlist!", Toast.LENGTH_SHORT).show();
							e.printStackTrace();
						}
	            		
	            		Toast.makeText(getBaseContext(), "Playlist \"" + name + "\" successfully created", Toast.LENGTH_SHORT).show();


	            	}

	            });
	            



			}
		});
		
		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
}
		});
    
		AlertDialog showAlert = alert.create();
		showAlert.show();
	}

	@Override
    public void onPause() {
	    //super.onPause();
	    super.onPause();
	   
	    
	    ForegroundHelper.activityStates[ForegroundHelper.MAINACT] = false;

	    if(mBound && !ForegroundHelper.activityExistsInForeground() && !artistClicked){
		    musicService.showNotification();
		    musicService.setNotificationStatus(true);
	    }

		 if(musicServiceBroadcastReceiver != null && runUnregister) {
			  unregisterReceiver(musicServiceBroadcastReceiver);
			  musicServiceBroadcastReceiver = null;
		 }
		
		if(mBound) {
			 // unbindService(serviceConnection);
		}

	    
	    
	    //unregisterReceiver(playlistReceiver);
	    //playlistReceiver = null;
	    
	  }
    
    @Override
    public void onDestroy() {
	    //super.onPause();
	    super.onDestroy();

	    
		  if(musicServiceBroadcastReceiver != null && runUnregister) {
			  unregisterReceiver(musicServiceBroadcastReceiver);
			  musicServiceBroadcastReceiver = null;
		  }
		
/*		  if(mBound && serviceConnection != null) {
			 unbindService(serviceConnection);
		  }*/

	    
	    
	    //unregisterReceiver(playlistReceiver);
	    //playlistReceiver = null;
	    
	  }
    
    @Override
	public void onResume() {
		  super.onResume();
		  
		  ForegroundHelper.activityStates[ForegroundHelper.MAINACT] = true;

		  
		  if(musicService != null) {
			  musicService.updateButtons();
			  musicService.clearNotifications();

		  }
		  
		  artistClicked = false;
		  
		  controlLayout = (LinearLayout) findViewById(R.id.controlLayout);

		  	if(mBound) {
				
				if(!isMyServiceRunning()) {
					controlLayout.setVisibility(View.GONE);
				} else {
					controlLayout.setVisibility(View.VISIBLE);
				}
				
				prevButton = (ImageButton) findViewById(R.id.homePrevButton);
				nextButton = (ImageButton) findViewById(R.id.homeNextButton);
				
				
				
				homePlayButton = (ImageButton) findViewById(R.id.homePlayButton);
				homePauseButton = (ImageButton) findViewById(R.id.homePrevButton);
				
				prevButton.setOnClickListener(this);
				nextButton.setOnClickListener(this);
				
		        context = getBaseContext();
		        
				currentArtist = (TextView) findViewById(R.id.artistNameText);
				currentArtist.setOnClickListener(this);
				
				currentArtist.setText(musicService.getCurrentVideo().getTitle());
				MusicService.setMainActivity(MainActivity.this);
		    	musicService.setNotificationStatus(false);


		  	}

    }
    

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
        switch (v.getId()) {
        
  		case R.id.artistNameText:
  			
  			if(mBound) {
  				
  				artistClicked = true;
  				Intent playlistIntent = new Intent(this, PlaylistActivity.class);
  				playlistIntent.putExtra("videos", musicService.getVideos());
  				playlistIntent.putExtra("new", false);
  				startActivityForResult(playlistIntent, 1);
  				
  			}
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

	@Override
	public void onFragmentInteraction(Uri uri) {
		// TODO Auto-generated method stub
		
	}

}
