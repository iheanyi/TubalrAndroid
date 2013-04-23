package com.iheanyiekechukwu.tubalr;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
//import com.iheanyiekechukwu.tubalr.PlaylistActivity.YoutubeVideoTask;

public class MusicService extends Service implements OnPreparedListener, OnClickListener, OnSeekBarChangeListener, OnErrorListener, OnCompletionListener, OnBufferingUpdateListener {

	public static final String INTENT_BASE_NAME = "com.iheanyiekechukwu.tubalr.MusicService";
	public static final String UPDATE_PLAYLIST = INTENT_BASE_NAME + ".PLAYLIST_UPDATED";
	public static final String QUEUE_TRACK = INTENT_BASE_NAME + ".QUEUE_TRACK";
	public static final String PLAY_TRACK = INTENT_BASE_NAME + ".PLAY_TRACK";
	public static final String NEXT_TRACK = INTENT_BASE_NAME + ".NEXT_TRACK";
	public static final String PREV_TRACK = INTENT_BASE_NAME + ".PREV_TRACK";
	public static final String PAUSE_TRACK = INTENT_BASE_NAME + ".PAUSE_TRACK";
	public static final String PLAY_SELECT = INTENT_BASE_NAME + ".PLAY_SELECT";
	public static final String NEW_SONGS = INTENT_BASE_NAME + ".NEW_SONGS";

    private static final String YOUTUBE_VIDEO_URL = "https://youtube.com/watch?v=";

	private static MediaPlayer mMediaPlayer;
	private static ArrayList<VideoClass> videoList;
	private static boolean paused = false;
	private MusicServiceBroadcastReceiver broadcastReceiver = new MusicServiceBroadcastReceiver();
	
    private final static String TAG = "MusicService";
    private static int current = 0;
    private final Handler handler = new Handler();

    private static PlaylistActivity myActivity;
    private static HomeActivity homeActivity;
    
    private static Context context;
    
    private NotificationManager mNotificationManager;
    private Notification notification;

	public class MusicServiceBinder extends Binder {
		public MusicService getService() {
			Log.v("MusicService", "MusicServiceBinder: getService() called");
			return MusicService.this;
		}
	}
	
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		
        Log.v(TAG, "MusicService: onCreate() called");
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(PLAY_TRACK);
		intentFilter.addAction(QUEUE_TRACK);
		intentFilter.addAction(NEXT_TRACK);
		intentFilter.addAction(PREV_TRACK);
		intentFilter.addAction(PLAY_SELECT);
		intentFilter.addAction(NEW_SONGS);
		registerReceiver(broadcastReceiver, intentFilter);
		
		context = getApplicationContext();
		
		/*mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setOnBufferingUpdateListener(this);
		mMediaPlayer.setOnCompletionListener(this);*/
	}
	
	
	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		// TODO Auto-generated method stub

			if(myActivity != null) {
				final SeekBar songSeek = (SeekBar) myActivity.findViewById(R.id.songSeekBar);
				final TextView timeText = (TextView) myActivity.findViewById(R.id.timeText);
				
				if(mp.isPlaying()) {
					//songSeek.setProgress(mp.getCurrentPosition());
					//timeText.setText(getTimeString(songSeek.getProgress()));	
				}	
			}


	}
	
	/*private void showNotification() {
		
		Intent intent = new Intent(this, PlaylistActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
		notification = new Notification.Builder(this)
			.setContentTitle("Tubalr")
			.setContentText(getCurrentVideo().getTitle())
			.setSmallIcon(R.drawable.tubalr_icon)
			.addAction(R.drawable.player_last, "Last Song", pIntent)
			.addAction(mMediaPlayer.isPlaying() ? R.drawable.player_pause : R.drawable.player_play, mMediaPlayer.isPlaying() ? "Pause" : "Play", pIntent)
			.addAction(R.drawable.player_first, "Next Song", pIntent).build();
		
		//notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notification.flags |= Notification.FLAG_INSISTENT;
		//notificationManager.notify(0, notification);
	}*/
	
	private void updateNotification() {
		
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, new Intent(this, PlaylistActivity.class), Intent.FLAG_ACTIVITY_NEW_TASK);
//		notification.setLatestEventInfo(this, "Tubalr", videoList.get(current).getTitle(), pIntent);
		//notificationManager.notify(0, notification);
	}
	
	private static String getTimeString(long millis) {
		StringBuffer buf = new StringBuffer();
		
		int hours =(int) millis/(1000*60*60);
	    int minutes = (int) ( millis % (1000*60*60) ) / (1000*60);
	    int seconds = (int) (( millis % (1000*60*60) ) % (1000*60) ) / 1000;

	    
	    if(hours > 0) {
	    	buf.append(String.format("%02d", hours));
	    	buf.append(":");
	    }
        //.append(String.format("%02d", hours))
        //.append(":")
        buf.append(String.format("%02d", minutes))
        .append(":")
        .append(String.format("%02d", seconds));
	    
	    return buf.toString();
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		return false;
	}
	
    public void startPlayProgressUpdater() {
    	
    	if(myActivity != null) {
    		
    	
    	final SeekBar songSeek = (SeekBar) myActivity.findViewById(R.id.songSeekBar);

    	
    	if(mMediaPlayer != null) {
        	songSeek.setProgress(mMediaPlayer.getCurrentPosition());

            if (mMediaPlayer.isPlaying()) {
                Runnable notification = new Runnable() {
                    public void run() {
                        startPlayProgressUpdater();
                    }
                };
                
                handler.postDelayed(notification,1000);
            } else {
                pause();
                //songSeek.setProgress(0);
            }
    	}


    	}
    }

	@Override
	public void onPrepared(MediaPlayer mp) {
		// TODO Auto-generated method stub
		mp.start();
		
		//updateNotification();
		
		if(myActivity != null) {
			final ImageView imageView = (ImageView) myActivity.findViewById(R.id.videoImageView);
			
			UrlImageViewHelper.setUrlDrawable(imageView, getCurrentVideo().getImageURL(), R.drawable.tubalr_icon);

			final SeekBar songSeek = (SeekBar) myActivity.findViewById(R.id.songSeekBar);
			final TextView timeText = (TextView) myActivity.findViewById(R.id.timeText);
			final TextView maxText = (TextView) myActivity.findViewById(R.id.maxText);
			
			songSeek.setMax(mp.getDuration());
			songSeek.setProgress(mp.getCurrentPosition());
			songSeek.setOnSeekBarChangeListener(this);
			timeText.setText(getTimeString(songSeek.getProgress()));
			maxText.setText(getTimeString(mp.getDuration()));
			
			
			final ImageButton playButton = (ImageButton) myActivity.findViewById(R.id.playButton);
			final ImageButton pauseButton = (ImageButton) myActivity.findViewById(R.id.pauseButton);
			
			playButton.setOnClickListener(this);
			pauseButton.setOnClickListener(this);
			
		    if(isPlaying() || mMediaPlayer.isPlaying()) {
		    	playButton.setVisibility(View.GONE);
		        pauseButton.setVisibility(View.VISIBLE);
		        
		        paused = false;
		    }

			playlistUpdated();
			
			
	        startPlayProgressUpdater();
					
		}
		
		if(homeActivity != null) {
			final ImageButton playButton = (ImageButton) homeActivity.findViewById(R.id.homePlayButton);
			final ImageButton pauseButton = (ImageButton) homeActivity.findViewById(R.id.homePauseButton);
		
			playButton.setOnClickListener(this);
			pauseButton.setOnClickListener(this);
			
		    if(isPlaying() || mMediaPlayer.isPlaying()) {
		    	playButton.setVisibility(View.GONE);
		        pauseButton.setVisibility(View.VISIBLE);
		        
		        paused = false;
		    }
		}

		


		
	}
	
	public static void  setMainActivity(PlaylistActivity playlistActivity) {
		
		Log.v(TAG, "SETMAINACTIVITY CALLED.");
		myActivity = playlistActivity;
		
		final ImageButton playButton = (ImageButton) myActivity.findViewById(R.id.playButton);
		//playButton.setOnClickListener(MusicService);
		
		final ImageButton pauseButton = (ImageButton) myActivity.findViewById(R.id.pauseButton);
		//pauseButton.setOnClickListener(this);
		
		final ImageButton nextButton = (ImageButton) myActivity.findViewById(R.id.nextButton);
		nextButton.setOnClickListener(myActivity);
		
		final ImageButton prevButton = (ImageButton) myActivity.findViewById(R.id.previousButton);
		prevButton.setOnClickListener(myActivity);
		
		
		
		if(isPlaying()) {
			playButton.setVisibility(View.GONE);
			pauseButton.setVisibility(View.VISIBLE);
			
			playButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					if(mMediaPlayer != null && !(mMediaPlayer.isPlaying())) {
						mMediaPlayer.start();
						paused = false;
						pauseButton.setVisibility(View.VISIBLE);
						playButton.setVisibility(View.GONE);
						
					}
				}
				
			});
			
			pauseButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					if(mMediaPlayer.isPlaying() && mMediaPlayer != null) {
						pause();
						pauseButton.setVisibility(View.GONE);
						playButton.setVisibility(View.VISIBLE);
					}
				}
				
			});		
			
		}
		
		
		
	}
	
	public void updateTime() {
		//playlistUpdated();
		
		
		
		final ImageButton playButton = (ImageButton) myActivity.findViewById(R.id.playButton);
		final ImageButton pauseButton = (ImageButton) myActivity.findViewById(R.id.pauseButton);
		
		playButton.setOnClickListener(this);
		pauseButton.setOnClickListener(this);
		
		final TextView currentText = (TextView) myActivity.findViewById(R.id.currentTextView);
		currentText.setText(videoList.get(current).getTitle());
		final ImageView imageView = (ImageView) myActivity.findViewById(R.id.videoImageView);
		
		UrlImageViewHelper.setUrlDrawable(imageView, videoList.get(current).getImageURL(), R.drawable.tubalr_icon);
		

		final SeekBar songSeek = (SeekBar) myActivity.findViewById(R.id.songSeekBar);
		final TextView timeText = (TextView) myActivity.findViewById(R.id.timeText);
		final TextView maxText = (TextView) myActivity.findViewById(R.id.maxText);
		if(mMediaPlayer != null) {
			
		
		songSeek.setMax(mMediaPlayer.getDuration());
		songSeek.setProgress(mMediaPlayer.getCurrentPosition());
		songSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				
				
				final TextView timeText = (TextView) myActivity.findViewById(R.id.timeText);

				int secondaryPosition = seekBar.getSecondaryProgress();
				if(fromUser && mMediaPlayer != null) {
					if(progress > secondaryPosition) {
						mMediaPlayer.seekTo(progress);
			            //String time = String.format("%d:%d", TimeUnit.MILLISECONDS.toMinutes(player.getDuration()), TimeUnit.MILLISECONDS.toSeconds(player.getDuration()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(player.getDuration())));
						timeText.setText(getTimeString(seekBar.getProgress()));
					}
				}

				else {
		            //String time = String.format("%d:%d", TimeUnit.MILLISECONDS.toMinutes(player.getDuration()), TimeUnit.MILLISECONDS.toSeconds(player.getDuration()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(player.getDuration())));
					seekBar.setProgress(progress);
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
			
		});
		timeText.setText(getTimeString(songSeek.getProgress()));
		maxText.setText(getTimeString(mMediaPlayer.getDuration()));
		
		

		playlistUpdated();
		startPlayProgressUpdater();
		}
	}
	
	public static void updateButtons() {
		
		
		if(myActivity != null) {
			final ImageButton playButton = (ImageButton) myActivity.findViewById(R.id.playButton);
			//playButton.setOnClickListener(MusicService);
			
			final ImageButton pauseButton = (ImageButton) myActivity.findViewById(R.id.pauseButton);
			
			if(isPlaying()) {
				playButton.setVisibility(View.GONE);
				pauseButton.setVisibility(View.VISIBLE);
				
				playButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						if(mMediaPlayer != null && !(mMediaPlayer.isPlaying())) {
							mMediaPlayer.start();
							paused = false;
							pauseButton.setVisibility(View.VISIBLE);
							playButton.setVisibility(View.GONE);
						}
					}
					
				});
				
				pauseButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						if(mMediaPlayer.isPlaying() && mMediaPlayer != null) {
							pause();
							pauseButton.setVisibility(View.GONE);
							playButton.setVisibility(View.VISIBLE);
						}
					}
					
				});		
				
			}
		}
		
		if(homeActivity != null) {
			final ImageButton playButton = (ImageButton) homeActivity.findViewById(R.id.homePlayButton);
			
			final ImageButton pauseButton = (ImageButton) homeActivity.findViewById(R.id.homePauseButton);
			
			final TextView currentText = (TextView) homeActivity.findViewById(R.id.artistNameText);
			currentText.setText(videoList.get(current).getTitle());
			
			if(isPlaying()) {
				playButton.setVisibility(View.GONE);
				pauseButton.setVisibility(View.VISIBLE);
						
				
			} else {
				pauseButton.setVisibility(View.GONE);
				playButton.setVisibility(View.VISIBLE);
				
			}
			
			playButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					if(mMediaPlayer != null && paused) {
						mMediaPlayer.start();
						paused = false;
						pauseButton.setVisibility(View.VISIBLE);
						playButton.setVisibility(View.GONE);
					}
				}
				
			});
			
			pauseButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					if(!paused && mMediaPlayer != null) {
						pause();
						pauseButton.setVisibility(View.GONE);
						playButton.setVisibility(View.VISIBLE);
					}
				}
				
			});
		}

	}
	
	public static void setMainActivity(HomeActivity hActivity) {
		
		Log.v(TAG, "SETMAINACTIVITY CALLED FOR HOME ACTIVITY.");

		homeActivity = hActivity;
		myActivity = null;
		
		final ImageButton playButton = (ImageButton) homeActivity.findViewById(R.id.homePlayButton);
		//playButton.setOnClickListener(homeActivity);
		
		final ImageButton pauseButton = (ImageButton) homeActivity.findViewById(R.id.homePauseButton);
		//pauseButton.setOnClickListener(homeActivity);
		
		final ImageButton nextButton = (ImageButton) homeActivity.findViewById(R.id.homeNextButton);
		nextButton.setOnClickListener(homeActivity);
		
		final ImageButton prevButton = (ImageButton) homeActivity.findViewById(R.id.homePrevButton);
		prevButton.setOnClickListener(homeActivity);

		if(videoList != null) {
			final TextView currentText = (TextView) homeActivity.findViewById(R.id.artistNameText);
			currentText.setText(videoList.get(current).getTitle());
		}

		
		if(isPlaying()) {
			playButton.setVisibility(View.GONE);
			pauseButton.setVisibility(View.VISIBLE);
					
			
		} else {
			pauseButton.setVisibility(View.GONE);
			playButton.setVisibility(View.VISIBLE);
			
		}
		
		playButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(mMediaPlayer != null && paused) {
					mMediaPlayer.start();
					paused = false;
					pauseButton.setVisibility(View.VISIBLE);
					playButton.setVisibility(View.GONE);
				}
			}
			
		});
		
		pauseButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(!paused && mMediaPlayer != null) {
					pause();
					pauseButton.setVisibility(View.GONE);
					playButton.setVisibility(View.VISIBLE);
				}
			}
			
		});

		
		
	
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

	private final IBinder musicServiceBinder = new MusicServiceBinder();
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
        Log.v(TAG, "MusicService: onBind() called");

        if((ArrayList<VideoClass>) intent.getSerializableExtra("videos") != null) {
            videoList = (ArrayList<VideoClass>) intent.getSerializableExtra("videos");
            release();
        }
        
        if(broadcastReceiver == null) {
            broadcastReceiver = new MusicServiceBroadcastReceiver();
        }
        
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(PLAY_TRACK);
		intentFilter.addAction(QUEUE_TRACK);
		intentFilter.addAction(NEXT_TRACK);
		intentFilter.addAction(PREV_TRACK);
		intentFilter.addAction(PLAY_SELECT);
		intentFilter.addAction(NEW_SONGS);
		registerReceiver(broadcastReceiver, intentFilter);
		
		
		if(homeActivity != null) {
			
			if(videoList != null) {
				final TextView currentText = (TextView) homeActivity.findViewById(R.id.artistNameText);
				currentText.setText(videoList.get(current).getTitle());
			}
			
			
			final ImageButton pauseButton = (ImageButton) homeActivity.findViewById(R.id.homePauseButton);
			final ImageButton playButton = (ImageButton) homeActivity.findViewById(R.id.homePlayButton);
			
			if(mMediaPlayer.isPlaying()) {
				pauseButton.setVisibility(View.GONE);
				playButton.setVisibility(View.VISIBLE);
			}

			
		} else {
			
		}
        //Log.v(TAG, Integer.toString(videoList.size()));
        
       // playFirstSong();
        
		return musicServiceBinder;
	}
	
	public void playFirstSong() {
		// TODO Auto-generated method stub
		
		Log.v(TAG, "MusicService: playFirstSong() called.");
		
        String yt_video_url = YOUTUBE_VIDEO_URL + videoList.get(current).getId();
        YoutubeVideoTask myTask = new YoutubeVideoTask();
        myTask.execute(yt_video_url);
		
		
	}
	
	public void playCurrentSong() {
		Log.v(TAG, "MusicService: playCurrentSong() called.");
		stop();
		
        String yt_video_url = YOUTUBE_VIDEO_URL + videoList.get(current).getId();
        YoutubeVideoTask myTask = new YoutubeVideoTask();
        myTask.execute(yt_video_url);	
        
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
	    // All clients have unbound with unbindService()

	    //release();

	    return false;
	}

	public void onDestroy() {
		Log.i(TAG, "MusicService: onDestroy() called");
		release();
	}
	
	public void onCompletion(MediaPlayer mp) {
		release();
		nextTrack();
	}
	
	private void release() {
		if(mMediaPlayer == null) {
			return;
		}
		
		if(mMediaPlayer.isPlaying()) {
			mMediaPlayer.stop();
		}
		
		mMediaPlayer.reset();
		mMediaPlayer.release();
		mMediaPlayer = null;
	}
	
	public void play(VideoClass video) {
		stop();
		
		Log.v(TAG, "We playing a specific video now?");
        String yt_video_url = YOUTUBE_VIDEO_URL + video.getId();
        YoutubeVideoTask myTask = new YoutubeVideoTask();
        myTask.execute(yt_video_url);
        
		//videoList.clear();
		//videoList.add(video);
		//play();
	}
	
	public void play(int i) {
		
		Log.v(TAG, "Currently in playing a specific song . . .");
		current = i;
		
		//VideoClass newSong = videoList.get(current);
		
		
        String yt_video_url = YOUTUBE_VIDEO_URL + videoList.get(current).getId();
        YoutubeVideoTask myTask = new YoutubeVideoTask();
        myTask.execute(yt_video_url);
		//play(newSong);
	}
	
	public void play() {
		Log.d(TAG, "Currently in play(), here's the stream URL: " + videoList.get(current).getUrl());
		
		if(videoList.size() == 0) {
			return;
		}
		
		VideoClass video = videoList.get(current);
		
		//Notification notification = new Notification(R.drawable.tubalr_icon, "Tubalr", video.getTitle());
		
		if(mMediaPlayer != null && paused) {
			mMediaPlayer.start();
			paused = false;
			return;
		} else if(mMediaPlayer != null) {
			release();
		}
		
		try {
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setOnPreparedListener(this);
			mMediaPlayer.setOnBufferingUpdateListener(this);
			mMediaPlayer.setDataSource(this, Uri.parse(video.getUrl()));
			mMediaPlayer.prepareAsync();
			
			mMediaPlayer.setOnCompletionListener(this);
			
			if(myActivity != null) {
				final TextView currentText = (TextView) myActivity.findViewById(R.id.currentTextView);
				currentText.setText(video.getTitle());	
			}
			
			if(homeActivity != null) {
				final TextView currentText = (TextView) homeActivity.findViewById(R.id.artistNameText);
				currentText.setText(video.getTitle());
			}

	
			

			

		} catch (IOException ioe){
			Log.e(TAG, "Error trying to play " + video.getTitle(), ioe);
            String message = "error trying to play track: " + video.getTitle() + ".\nError: " + ioe.getMessage();
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		}
		
	}
	
	private void playlistUpdated() {
		Intent updatePlaylistIntent = new Intent(UPDATE_PLAYLIST);
		this.sendBroadcast(updatePlaylistIntent);
	}
	
	
	private void nextTrack() {
		
		Log.v(TAG, "Currently in nextTrack()");
		current++;
		
		if(current >= videoList.size()) {
			current = 0;
		}
		
		String yt_video_url = YOUTUBE_VIDEO_URL + videoList.get(current).getId();
        YoutubeVideoTask myTask = new YoutubeVideoTask();
        myTask.execute(yt_video_url);
		//play();
	}
	
	private void prevTrack() {
		
		Log.v(TAG, "Currently in prevTrack()");
		
		if(mMediaPlayer != null) {
			
		if(mMediaPlayer.isPlaying() && mMediaPlayer.getCurrentPosition() > 5000) {
			seek(0);
		}
				
		else {
			current--;
			
			if(current < 0) {
				current = videoList.size() - 1;
			}
			
			String yt_video_url = YOUTUBE_VIDEO_URL + videoList.get(current).getId();
	        YoutubeVideoTask myTask = new YoutubeVideoTask();
	        myTask.execute(yt_video_url);
		}
		}
	}
	
	public void stop() {
		release();
	}
	
	public static boolean isPlaying() {
		if(current > 40 || mMediaPlayer == null) {
			Log.v(TAG, "I'm guessing that mMediaPlayer is null");
			return false;
		}
		
		return mMediaPlayer.isPlaying();
	}
	
	public int getCurrentSong() {
		return current;
	}
	
	public static void pause() {
		if(mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
			paused = true;
			
			if(myActivity != null) {
				final ImageButton pauseButton = (ImageButton) myActivity.findViewById(R.id.pauseButton);
				final ImageButton playButton = (ImageButton) myActivity.findViewById(R.id.playButton);
				
				pauseButton.setVisibility(View.GONE);
				playButton.setVisibility(View.VISIBLE);
			}
			
			if(homeActivity != null) {
				final ImageButton pauseButton = (ImageButton) homeActivity.findViewById(R.id.homePauseButton);
				final ImageButton playButton = (ImageButton) homeActivity.findViewById(R.id.homePlayButton);
				
				pauseButton.setVisibility(View.GONE);
				playButton.setVisibility(View.VISIBLE);
			}
			
		}
	}
	
	public VideoClass getCurrentVideo() {
		if(current > 40) {
			current = 0;
			//return null;
		}
		
		return videoList.get(current);
	}
	
	public ArrayList<VideoClass> getVideos() {
		
		return videoList;
	}
	
	public int elapsed() {
		if(mMediaPlayer == null) {
			return 0;
		}
		else {
			return mMediaPlayer.getCurrentPosition();
		}
	}
	
	public int maxDuration() {
		if(mMediaPlayer == null) {
			return 0;
		}
		
		return mMediaPlayer.getDuration();
	}
	
	public void seek(int timeInMillis) {
		if(mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.seekTo(timeInMillis);
		}
	}
	
	
	private class MusicServiceBroadcastReceiver extends BroadcastReceiver {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			//long id = intent.getLongExtra("id", -1);
			//String url = intent.getStringExtra("artist");
			
			Log.d("MusicService", "Received intent for action " + intent.getAction());
			
			if(PLAY_TRACK.equals(action)) {
				playCurrentSong();
			} else if(NEXT_TRACK.equals(action)) {
				Log.v(TAG, "nextTrack() called.");
				nextTrack();
			} else if(PREV_TRACK.equals(action)) {
				Log.v(TAG, "prevTrack() called.");
				prevTrack();
			} else if(PAUSE_TRACK.equals(action)) {
				Log.v(TAG, "pauseTrack() called.");
				pause();
			} else if(PLAY_SELECT.equals(action)) {
				int position = intent.getIntExtra("position", current);
				Log.d(TAG, "Received current number: " + Integer.toString(position));

				Log.v(TAG, "Time to play a specific song!");
				//current = intent.getIntExtra("pos", current);
				play(position);
				
			} else if(NEW_SONGS.equals(action)) {
				current = 0;
				ArrayList<VideoClass> newSongs = (ArrayList<VideoClass>) intent.getSerializableExtra("videos");
				Log.v(TAG, "New Songs registered . . . ");
				Log.v(TAG, "New Playlist Size: "  + Integer.toString(newSongs.size()));
				newPlaylist(newSongs);
				
			} else {

				Log.e(TAG, "ACTION NOT RECOGNIZED.");
			}
		}
	}
	
	private void nextSongPlayed() {
		Intent nextSongIntent = new Intent(NEXT_TRACK);
		this.sendBroadcast(nextSongIntent);
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
			
			Toast.makeText(getBaseContext(), "Error parsing music . . . skipping track.", Toast.LENGTH_SHORT).show();
			nextTrack();
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

	private void linksComposer(String block, int i, String title, String id) {
		// TODO Auto-generated method stub
		
		Log.v(TAG, "MusicService: currently in linksComposer()");
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
				
				videoList.get(current).setUrl(testString);
				
				System.out.println(videoList.get(current).getUrl());
				//adapter.notifyDataSetChanged();

		        Uri testUri = Uri.parse(testString);

		        play();
	
			}
		} else {
			Toast.makeText(getBaseContext(), "Error playing track, next track called.", Toast.LENGTH_SHORT).show();
			nextTrack();
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
			
			String[] results = result.trim().split("var swf = ");
			
			results = results[results.length - 1].trim().split("document.getElementById\\(");
			String html = results[0];
			//getURLs(html);
			decodeURL(result);
		}
    }

    public void getURLs(String result) {
    	try {
			String url = URLDecoder.decode(result, "UTF-8");
			url = URLDecoder.decode(url, "UTF-8");
			url = URLDecoder.decode(url, "UTF-8");
			url = url.replace("\\\\u0026", "&");
			
			String[] links = url.trim().split("url_encoded_fmt_stream_map\\\":");
			String link = links[links.length-1];
			
			String[] qualities = link.trim().split("url=");
			ArrayList<String> qualitiesArray = new ArrayList<String>(Arrays.asList(qualities));
			qualitiesArray.remove(0);
			
			ArrayList<String> cleanURLs = new ArrayList<String>();
			
			for(String s : qualitiesArray) {
				String[] moreLinks = s.split(";+codecs=");
				String current = moreLinks[0];
				if(current.contains("video/mp4") || current.contains("video/3gpp")) {
					String[] tags = current.split("&itag=");
					String lastTag = tags[tags.length-1];
					
					String[] moreURLs = current.split("&itag=" + lastTag);
					String cleanURL = moreURLs[moreURLs.length - 1];
					cleanURLs.add(cleanURL);
					
				}
			}
			
			String contentDecoded = cleanURLs.get(0);
			contentDecoded = contentDecoded.replaceAll("sig=", "signature=");
			contentDecoded = contentDecoded.replaceAll("x-flv", "flv");			
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    


	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		
		final TextView timeText = (TextView) myActivity.findViewById(R.id.timeText);

		int secondaryPosition = seekBar.getSecondaryProgress();
		if(fromUser && mMediaPlayer != null) {
			if(progress > secondaryPosition) {
				mMediaPlayer.seekTo(progress);
	            //String time = String.format("%d:%d", TimeUnit.MILLISECONDS.toMinutes(player.getDuration()), TimeUnit.MILLISECONDS.toSeconds(player.getDuration()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(player.getDuration())));
				timeText.setText(getTimeString(seekBar.getProgress()));
			}
		}

		else {
            //String time = String.format("%d:%d", TimeUnit.MILLISECONDS.toMinutes(player.getDuration()), TimeUnit.MILLISECONDS.toSeconds(player.getDuration()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(player.getDuration())));
			seekBar.setProgress(progress);
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
	
	public void newPlaylist(ArrayList<VideoClass> newPlaylist) {
		videoList = newPlaylist;
		current = 0;
		playCurrentSong();
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub	
		if(myActivity != null) {
			final ImageButton pauseButton = (ImageButton) myActivity.findViewById(R.id.pauseButton);
			final ImageButton playButton = (ImageButton) myActivity.findViewById(R.id.playButton);
			switch (v.getId()) {

			case R.id.playButton:
				if(mMediaPlayer != null) {
					
				
					if(!(mMediaPlayer.isPlaying())) {
						mMediaPlayer.start();
						//paused = false;
						pauseButton.setVisibility(View.VISIBLE);
						playButton.setVisibility(View.GONE);
						
						startPlayProgressUpdater();
					} 
				}
				break;

			case R.id.pauseButton:
				if(mMediaPlayer != null) {
					
				
					if(mMediaPlayer.isPlaying()) {
						pause();
						pauseButton.setVisibility(View.GONE);
						playButton.setVisibility(View.VISIBLE);
					}
				}
				break;

		} 
			}
		
		if(homeActivity != null) {
			final ImageButton pauseButton = (ImageButton) homeActivity.findViewById(R.id.homePauseButton);
			final ImageButton playButton = (ImageButton) homeActivity.findViewById(R.id.homePlayButton);
			
			switch (v.getId()) {

			case R.id.homePlayButton:
				if(mMediaPlayer != null) {
					
				
					if(!(mMediaPlayer.isPlaying())) {
						mMediaPlayer.start();
						//paused = false;
						pauseButton.setVisibility(View.VISIBLE);
						playButton.setVisibility(View.GONE);
					}
				}
				break;

			case R.id.homePauseButton:
				
				if(mMediaPlayer != null) {
					
				
					if(mMediaPlayer.isPlaying()) {
						pause();
						pauseButton.setVisibility(View.GONE);
						playButton.setVisibility(View.VISIBLE);
					}
				
				}
				break;
		} 
			}
		
		
		/*switch (v.getId()) {

		case R.id.playButton:
			if(mMediaPlayer != null && !(mMediaPlayer.isPlaying())) {
				mMediaPlayer.start();
				//paused = false;
				pauseButton.setVisibility(View.VISIBLE);
				playButton.setVisibility(View.GONE);
			}
			break;

		case R.id.pauseButton:
			if(mMediaPlayer.isPlaying() && mMediaPlayer != null) {
				pause();
				pauseButton.setVisibility(View.GONE);
				playButton.setVisibility(View.VISIBLE);
			}
			break;
			
		case R.id.homePlayButton:
			Log.d(TAG, "HOME PLAY BUTTON CLICKED!");

			if(mMediaPlayer != null && !(mMediaPlayer.isPlaying())) {
				mMediaPlayer.start();
				//paused = false;
				pauseButton.setVisibility(View.VISIBLE);
				playButton.setVisibility(View.GONE);
			}
			break;

			
		case R.id.homePauseButton:
			
			Log.d(TAG, "HOME PAUSE BUTTON CLICKED!");
			if(mMediaPlayer.isPlaying() && mMediaPlayer != null) {
				pause();
				pauseButton.setVisibility(View.GONE);
				playButton.setVisibility(View.VISIBLE);
			}
			break;
			
		case R.id.homeNextButton:
			nextTrack();
			
			break;
		
		case R.id.homePrevButton:
			prevTrack();
			
			break;
			



		default: 
			break;*/
	}
    
	
}
