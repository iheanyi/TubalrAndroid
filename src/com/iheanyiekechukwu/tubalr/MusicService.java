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
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
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
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

//import com.iheanyiekechukwu.tubalr.PlaylistActivity.YoutubeVideoTask;

public class MusicService extends Service implements OnPreparedListener,
		OnClickListener, OnSeekBarChangeListener, OnErrorListener,
		OnCompletionListener, OnBufferingUpdateListener {

	public static final String INTENT_BASE_NAME = "com.iheanyiekechukwu.tubalr.MusicService";
	public static final String UPDATE_PLAYLIST = INTENT_BASE_NAME
			+ ".PLAYLIST_UPDATED";
	public static final String QUEUE_TRACK = INTENT_BASE_NAME + ".QUEUE_TRACK";
	public static final String PLAY_TRACK = INTENT_BASE_NAME + ".PLAY_TRACK";
	public static final String NEXT_TRACK = INTENT_BASE_NAME + ".NEXT_TRACK";
	public static final String PREV_TRACK = INTENT_BASE_NAME + ".PREV_TRACK";
	public static final String PAUSE_TRACK = INTENT_BASE_NAME + ".PAUSE_TRACK";
	public static final String PLAY_SELECT = INTENT_BASE_NAME + ".PLAY_SELECT";
	public static final String NEW_SONGS = INTENT_BASE_NAME + ".NEW_SONGS";
	public static final String RESUME_TRACK = INTENT_BASE_NAME
			+ ".RESUME_TRACK";
	public static final String STOP_TRACK = INTENT_BASE_NAME + ".STOP_TRACK";

	private static final String YOUTUBE_VIDEO_URL = "https://youtube.com/watch?v=";

	private static MediaPlayer mMediaPlayer;
	private static ArrayList<VideoClass> videoList;
	private static boolean paused = false;
	private MusicServiceBroadcastReceiver broadcastReceiver = new MusicServiceBroadcastReceiver();

	private final static String TAG = "MusicService";
	private static int current = 0;
	private final Handler handler = new Handler();

	private static PlaylistActivity myActivity;
	private static MainActivity homeActivity;

	private static Context context;

	private NotificationManager mNotificationManager;
	private Notification notification;

	public boolean isVisible = true;

	public class MusicServiceBinder extends Binder {
		public MusicService getService() {
			Log.v("MusicService", "MusicServiceBinder: getService() called");
			return MusicService.this;
		}
	}

	public boolean notifStatus = false;

	@Override
	public void onCreate() {
		super.onCreate();

		Log.v(TAG, "MusicService: onCreate() called");
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(PLAY_TRACK);
		intentFilter.addAction(QUEUE_TRACK);
		intentFilter.addAction(NEXT_TRACK);
		intentFilter.addAction(PREV_TRACK);
		intentFilter.addAction(PAUSE_TRACK);
		intentFilter.addAction(RESUME_TRACK);
		intentFilter.addAction(PLAY_SELECT);
		intentFilter.addAction(NEW_SONGS);
		intentFilter.addAction(STOP_TRACK);
		registerReceiver(broadcastReceiver, intentFilter);

		context = getApplicationContext();

		/*
		 * mMediaPlayer = new MediaPlayer();
		 * mMediaPlayer.setOnBufferingUpdateListener(this);
		 * mMediaPlayer.setOnCompletionListener(this);
		 */
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		// TODO Auto-generated method stub

		if (myActivity != null) {
			final SeekBar songSeek = (SeekBar) myActivity
					.findViewById(R.id.songSeekBar);
			final TextView timeText = (TextView) myActivity
					.findViewById(R.id.timeText);

			if (mp.isPlaying()) {
				// songSeek.setProgress(mp.getCurrentPosition());
				// timeText.setText(getTimeString(songSeek.getProgress()));
			}
		}

	}

	/*
	 * private void showNotification() {
	 * 
	 * Intent intent = new Intent(this, PlaylistActivity.class); PendingIntent
	 * pIntent = PendingIntent.getActivity(this, 0, intent,
	 * Intent.FLAG_ACTIVITY_NEW_TASK); notification = new
	 * Notification.Builder(this) .setContentTitle("Tubalr")
	 * .setContentText(getCurrentVideo().getTitle())
	 * .setSmallIcon(R.drawable.tubalr_icon) .addAction(R.drawable.player_last,
	 * "Last Song", pIntent) .addAction(mMediaPlayer.isPlaying() ?
	 * R.drawable.player_pause : R.drawable.player_play,
	 * mMediaPlayer.isPlaying() ? "Pause" : "Play", pIntent)
	 * .addAction(R.drawable.player_first, "Next Song", pIntent).build();
	 * 
	 * //notificationManager = (NotificationManager)
	 * getSystemService(NOTIFICATION_SERVICE); notification.flags |=
	 * Notification.FLAG_INSISTENT; //notificationManager.notify(0,
	 * notification); }
	 */

	/*
	 * private void updateNotification() { Intent intent = new
	 * Intent(getBaseContext(), PlaylistActivity.class);
	 * intent.putExtra("videos", getVideos()); intent.putExtra("new", false);
	 * 
	 * PendingIntent pIntent = PendingIntent.getActivity(getBaseContext(), 0,
	 * intent, 0); PendingIntent prevIntent = PendingIntent.getBroadcast(this,
	 * 0, new Intent(PREV_TRACK), 0); PendingIntent nextIntent =
	 * PendingIntent.getBroadcast(this, 0, new Intent(NEXT_TRACK), 0);
	 * PendingIntent pauseIntent = PendingIntent.getBroadcast(this, 0, new
	 * Intent(PLAY_TRACK), 0); PendingIntent playIntent =
	 * PendingIntent.getBroadcast(this, 0, new Intent(PAUSE_TRACK), 0);
	 * Notification.Builder mBuilder = new Notification.Builder(this)
	 * .setSmallIcon(R.drawable.tubalr_icon) .setContentTitle("Tubalr")
	 * .setContentText(getCurrentVideo().getTitle()) .setContentIntent(pIntent)
	 * .addAction(R.drawable.player_prev, "Last Song", prevIntent)
	 * .addAction(mMediaPlayer.isPlaying() ? R.drawable.player_pause :
	 * R.drawable.player_play, mMediaPlayer.isPlaying() ? "Pause" : "Play",
	 * mMediaPlayer.isPlaying() ? pauseIntent : playIntent)
	 * .addAction(R.drawable.player_next, "Next Song", nextIntent);
	 * 
	 * Notification notif = mBuilder.build();
	 * 
	 * NotificationManager notificationManager = (NotificationManager)
	 * getSystemService (NOTIFICATION_SERVICE);
	 * 
	 * 
	 * notif.flags |= Notification.FLAG_ONGOING_EVENT |
	 * Notification.FLAG_AUTO_CANCEL; notificationManager.notify(0, notif); //
	 * notification.setLatestEventInfo(this, "Tubalr",
	 * videoList.get(current).getTitle(), pIntent);
	 * //notificationManager.notify(0, notification); }
	 */

	private static String getTimeString(long millis) {
		StringBuffer buf = new StringBuffer();

		int hours = (int) millis / (1000 * 60 * 60);
		int minutes = (int) (millis % (1000 * 60 * 60)) / (1000 * 60);
		int seconds = (int) ((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000;

		if (hours > 0) {
			buf.append(String.format("%02d", hours));
			buf.append(":");
		}
		// .append(String.format("%02d", hours))
		// .append(":")
		buf.append(String.format("%02d", minutes)).append(":")
				.append(String.format("%02d", seconds));

		return buf.toString();
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		return false;
	}

	public void startPlayProgressUpdater() {

		if (myActivity != null) {

			final SeekBar songSeek = (SeekBar) myActivity
					.findViewById(R.id.songSeekBar);

			if (mMediaPlayer != null) {
				songSeek.setProgress(mMediaPlayer.getCurrentPosition());

				if (mMediaPlayer.isPlaying()) {
					Runnable notification = new Runnable() {
						@Override
						public void run() {
							startPlayProgressUpdater();
						}
					};

					handler.postDelayed(notification, 1000);
				} else {
					pause();
					// songSeek.setProgress(0);
				}
			}

		}
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		// TODO Auto-generated method stub
		mp.start();

		// updateNotification();

		if (myActivity != null) {
			final ImageView imageView = (ImageView) myActivity
					.findViewById(R.id.videoImageView);

			UrlImageViewHelper.setUrlDrawable(imageView, getCurrentVideo()
					.getImageURL(), R.drawable.tubalr_icon);

			final SeekBar songSeek = (SeekBar) myActivity
					.findViewById(R.id.songSeekBar);
			final TextView timeText = (TextView) myActivity
					.findViewById(R.id.timeText);
			final TextView maxText = (TextView) myActivity
					.findViewById(R.id.maxText);

			songSeek.setMax(mp.getDuration());
			songSeek.setProgress(mp.getCurrentPosition());
			songSeek.setOnSeekBarChangeListener(this);
			timeText.setText(getTimeString(songSeek.getProgress()));
			maxText.setText(getTimeString(mp.getDuration()));

			final ImageButton playButton = (ImageButton) myActivity
					.findViewById(R.id.playButton);
			final ImageButton pauseButton = (ImageButton) myActivity
					.findViewById(R.id.pauseButton);

			playButton.setOnClickListener(this);
			pauseButton.setOnClickListener(this);

			if (isPlaying() || mMediaPlayer.isPlaying()) {
				playButton.setVisibility(View.GONE);
				pauseButton.setVisibility(View.VISIBLE);

				paused = false;
			}

			playlistUpdated();

			startPlayProgressUpdater();

		}

		if (homeActivity != null) {
			final ImageButton playButton = (ImageButton) homeActivity
					.findViewById(R.id.homePlayButton);
			final ImageButton pauseButton = (ImageButton) homeActivity
					.findViewById(R.id.homePauseButton);
			final ImageButton nextButton = (ImageButton) homeActivity
					.findViewById(R.id.homeNextButton);
			final ImageButton prevButton = (ImageButton) homeActivity
					.findViewById(R.id.homePrevButton);

			playButton.setOnClickListener(this);
			pauseButton.setOnClickListener(this);
			prevButton.setOnClickListener(this);
			nextButton.setOnClickListener(this);

			if (isPlaying() || mMediaPlayer.isPlaying()) {
				playButton.setVisibility(View.GONE);
				pauseButton.setVisibility(View.VISIBLE);

				paused = false;
			}
		}

	}

	public static void setMainActivity(PlaylistActivity playlistActivity) {

		Log.v(TAG, "SETMAINACTIVITY CALLED.");
		myActivity = playlistActivity;

		final ImageButton playButton = (ImageButton) myActivity
				.findViewById(R.id.playButton);
		// playButton.setOnClickListener(MusicService);

		final ImageButton pauseButton = (ImageButton) myActivity
				.findViewById(R.id.pauseButton);
		// pauseButton.setOnClickListener(this);

		final ImageButton nextButton = (ImageButton) myActivity
				.findViewById(R.id.nextButton);
		nextButton.setOnClickListener(myActivity);

		final ImageButton prevButton = (ImageButton) myActivity
				.findViewById(R.id.previousButton);
		prevButton.setOnClickListener(myActivity);

		final ListView songView = (ListView) myActivity
				.findViewById(R.id.playlistView);
		songView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View v, int i,
					long arg3) {
				// TODO Auto-generated method stub

			}

		});

		if (isPlaying()) {
			playButton.setVisibility(View.GONE);
			pauseButton.setVisibility(View.VISIBLE);

			playButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					if (mMediaPlayer != null) {
						if (!mMediaPlayer.isPlaying()) {
							mMediaPlayer.start();
							paused = false;
							pauseButton.setVisibility(View.VISIBLE);
							playButton.setVisibility(View.GONE);
						}
					}
				}

			});

			pauseButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					if (mMediaPlayer != null) {

						if (mMediaPlayer.isPlaying()) {
							// pause();
							mMediaPlayer.pause();
							paused = true;
							pauseButton.setVisibility(View.GONE);
							playButton.setVisibility(View.VISIBLE);
						}
					}
				}

			});

		}

	}

	public void updateTime() {
		// playlistUpdated();

		final ImageButton playButton = (ImageButton) myActivity
				.findViewById(R.id.playButton);
		final ImageButton pauseButton = (ImageButton) myActivity
				.findViewById(R.id.pauseButton);

		playButton.setOnClickListener(this);
		pauseButton.setOnClickListener(this);

		final TextView currentText = (TextView) myActivity
				.findViewById(R.id.currentTextView);
		currentText.setText(videoList.get(current).getTitle());
		final ImageView imageView = (ImageView) myActivity
				.findViewById(R.id.videoImageView);

		UrlImageViewHelper.setUrlDrawable(imageView, videoList.get(current)
				.getImageURL(), R.drawable.tubalr_icon);

		final SeekBar songSeek = (SeekBar) myActivity
				.findViewById(R.id.songSeekBar);
		final TextView timeText = (TextView) myActivity
				.findViewById(R.id.timeText);
		final TextView maxText = (TextView) myActivity
				.findViewById(R.id.maxText);
		if (mMediaPlayer != null) {

			songSeek.setMax(mMediaPlayer.getDuration());
			songSeek.setProgress(mMediaPlayer.getCurrentPosition());
			songSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					// TODO Auto-generated method stub

					final TextView timeText = (TextView) myActivity
							.findViewById(R.id.timeText);

					int secondaryPosition = seekBar.getSecondaryProgress();
					if (fromUser && mMediaPlayer != null) {
						if (progress > secondaryPosition) {
							mMediaPlayer.seekTo(progress);
							// String time = String.format("%d:%d",
							// TimeUnit.MILLISECONDS.toMinutes(player.getDuration()),
							// TimeUnit.MILLISECONDS.toSeconds(player.getDuration())
							// -
							// TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(player.getDuration())));
							timeText.setText(getTimeString(seekBar
									.getProgress()));
						}
					}

					else {
						// String time = String.format("%d:%d",
						// TimeUnit.MILLISECONDS.toMinutes(player.getDuration()),
						// TimeUnit.MILLISECONDS.toSeconds(player.getDuration())
						// -
						// TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(player.getDuration())));
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

		if (myActivity != null) {
			final ImageButton playButton = (ImageButton) myActivity
					.findViewById(R.id.playButton);
			// playButton.setOnClickListener(MusicService);

			final ImageButton pauseButton = (ImageButton) myActivity
					.findViewById(R.id.pauseButton);

			if (isPlaying()) {
				playButton.setVisibility(View.GONE);
				pauseButton.setVisibility(View.VISIBLE);

				playButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						if (mMediaPlayer != null && !(mMediaPlayer.isPlaying())) {
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
						if (mMediaPlayer.isPlaying() && mMediaPlayer != null) {
							mMediaPlayer.pause();
							paused = true;
							pauseButton.setVisibility(View.GONE);
							playButton.setVisibility(View.VISIBLE);
						}
					}

				});

			}
		}

		if (homeActivity != null) {
			final ImageButton playButton = (ImageButton) homeActivity
					.findViewById(R.id.homePlayButton);

			final ImageButton pauseButton = (ImageButton) homeActivity
					.findViewById(R.id.homePauseButton);

			final TextView currentText = (TextView) homeActivity
					.findViewById(R.id.artistNameText);
			currentText.setText(videoList.get(current).getTitle());

			if (isPlaying()) {
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
					if (mMediaPlayer != null && paused) {
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
					if (!paused && mMediaPlayer != null) {
						paused = true;
						mMediaPlayer.pause();
						pauseButton.setVisibility(View.GONE);
						playButton.setVisibility(View.VISIBLE);
					}
				}

			});
		}

	}

	public static void setMainActivity(MainActivity hActivity) {

		Log.v(TAG, "SETMAINACTIVITY CALLED FOR HOME ACTIVITY.");

		homeActivity = hActivity;
		myActivity = null;

		final ImageButton playButton = (ImageButton) homeActivity
				.findViewById(R.id.homePlayButton);
		// playButton.setOnClickListener(homeActivity);

		final ImageButton pauseButton = (ImageButton) homeActivity
				.findViewById(R.id.homePauseButton);
		// pauseButton.setOnClickListener(homeActivity);

		final ImageButton nextButton = (ImageButton) homeActivity
				.findViewById(R.id.homeNextButton);

		nextButton.setOnClickListener(homeActivity);

		final ImageButton prevButton = (ImageButton) homeActivity
				.findViewById(R.id.homePrevButton);
		prevButton.setOnClickListener(homeActivity);

		if (videoList != null) {
			final TextView currentText = (TextView) homeActivity
					.findViewById(R.id.artistNameText);
			currentText.setText(videoList.get(current).getTitle());
		}

		if (isPlaying()) {
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
				if (mMediaPlayer != null) {
					if (!mMediaPlayer.isPlaying()) {
						mMediaPlayer.start();
						paused = false;
						pauseButton.setVisibility(View.VISIBLE);
						playButton.setVisibility(View.GONE);
					}

				}
			}

		});

		pauseButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (mMediaPlayer != null) {

					if (mMediaPlayer.isPlaying()) {
						mMediaPlayer.pause();
						paused = true;
						pauseButton.setVisibility(View.GONE);
						playButton.setVisibility(View.VISIBLE);
					}

				}
			}

		});

	}

	public void decodeURL(String content) {
		// TODO Auto-generated method stub
		Pattern startPattern = Pattern
				.compile("url_encoded_fmt_stream_map\\\": \\\"");
		Pattern endPattern = Pattern.compile("\\\", \\\"");
		Matcher matcher = startPattern.matcher(content);

		String title = findVideoFilename(content);
		String id = findVideoID(content);

		// String id = this.tempID;
		if (matcher.find()) {
			String[] start = content.split(startPattern.toString());
			String[] end = start[1].split(endPattern.toString());

			// Other decoding stuff
			String contentDecoded = URLDecoder.decode(end[0]);
			contentDecoded = contentDecoded.replaceAll(", ", "-");
			contentDecoded = contentDecoded.replaceAll("sig=", "signature=");
			contentDecoded = contentDecoded.replaceAll("x-flv", "flv");
			contentDecoded = contentDecoded.replaceAll("\\\\u0026", "&");
			Log.d("DEBUG", "contentDecoded: " + contentDecoded);
			findCodecs(contentDecoded, title, id);

		}
	}
	
	public static String fixEncoding(String latin1) {
		  try {
		   byte[] bytes = latin1.getBytes("ISO-8859-1");
		   if (!validUTF8(bytes))
		    return latin1;   
		   return new String(bytes, "UTF-8");  
		  } catch (UnsupportedEncodingException e) {
		   // Impossible, throw unchecked
		   throw new IllegalStateException("No Latin1 or UTF-8: " + e.getMessage());
		  }

		 }

		 public static boolean validUTF8(byte[] input) {
		  int i = 0;
		  // Check for BOM
		  if (input.length >= 3 && (input[0] & 0xFF) == 0xEF
		    && (input[1] & 0xFF) == 0xBB & (input[2] & 0xFF) == 0xBF) {
		   i = 3;
		  }

		  int end;
		  for (int j = input.length; i < j; ++i) {
		   int octet = input[i];
		   if ((octet & 0x80) == 0) {
		    continue; // ASCII
		   }

		   // Check for UTF-8 leading byte
		   if ((octet & 0xE0) == 0xC0) {
		    end = i + 1;
		   } else if ((octet & 0xF0) == 0xE0) {
		    end = i + 2;
		   } else if ((octet & 0xF8) == 0xF0) {
		    end = i + 3;
		   } else {
		    // Java only supports BMP so 3 is max
		    return false;
		   }

		   while (i < end) {
		    i++;
		    octet = input[i];
		    if ((octet & 0xC0) != 0x80) {
		     // Not a valid trailing byte
		     return false;
		    }
		   }
		  }
		  return true;
		 }

	private final IBinder musicServiceBinder = new MusicServiceBinder();

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.v(TAG, "MusicService: onBind() called");

		if ((ArrayList<VideoClass>) intent.getSerializableExtra("videos") != null) {
			videoList = (ArrayList<VideoClass>) intent
					.getSerializableExtra("videos");
			release();
		}

		if (broadcastReceiver == null) {
			broadcastReceiver = new MusicServiceBroadcastReceiver();
		}

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(PLAY_TRACK);
		intentFilter.addAction(QUEUE_TRACK);
		intentFilter.addAction(NEXT_TRACK);
		intentFilter.addAction(PREV_TRACK);
		intentFilter.addAction(PAUSE_TRACK);
		intentFilter.addAction(RESUME_TRACK);
		intentFilter.addAction(PLAY_SELECT);
		intentFilter.addAction(NEW_SONGS);
		registerReceiver(broadcastReceiver, intentFilter);

		if (homeActivity != null) {

			if (videoList != null) {
				final TextView currentText = (TextView) homeActivity
						.findViewById(R.id.artistNameText);
				currentText.setText(videoList.get(current).getTitle());
			}

			final ImageButton pauseButton = (ImageButton) homeActivity
					.findViewById(R.id.homePauseButton);
			final ImageButton playButton = (ImageButton) homeActivity
					.findViewById(R.id.homePlayButton);

			if (mMediaPlayer.isPlaying()) {
				pauseButton.setVisibility(View.GONE);
				playButton.setVisibility(View.VISIBLE);
			}

		} else {

		}
		// Log.v(TAG, Integer.toString(videoList.size()));

		// playFirstSong();

		return musicServiceBinder;
	}

	public void playFirstSong() {
		// TODO Auto-generated method stub

		Log.v(TAG, "MusicService: playFirstSong() called.");

		String yt_video_url = YOUTUBE_VIDEO_URL
				+ videoList.get(current).getId();
		// YoutubeVideoTask myTask = new YoutubeVideoTask();
		// myTask.execute(yt_video_url);

	}

	public void playCurrentSong() {
		Log.v(TAG, "MusicService: playCurrentSong() called.");
		stop();

		String yt_video_url = YOUTUBE_VIDEO_URL
				+ videoList.get(current).getId();

		/*
		 * AsyncHttpClient.getDefaultInstance().get(yt_video_url, new
		 * AsyncHttpClient.StringCallback() { // Callback is invoked with any
		 * exceptions/errors, and the result, if available. public void
		 * onCompleted(Exception e, String result) {
		 * 
		 * System.out.println("I got a string: " + result); }
		 * 
		 * @Override public void onCompleted(Exception e, AsyncHttpResponse
		 * source, String result) { // TODO Auto-generated method stub if (e !=
		 * null) { e.printStackTrace(); return; }
		 * 
		 * System.out.println(result);
		 * 
		 * parseHTML(result);
		 * 
		 * } });
		 */

		YoutubeVideoTask myTask = new YoutubeVideoTask();
		myTask.execute(yt_video_url);

	}

	@Override
	public boolean onUnbind(Intent intent) {
		// All clients have unbound with unbindService()

		// release();

		return false;
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "MusicService: onDestroy() called");
		release();
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		
		String completedURL = "http://www.tubalr.com/api/analytics/report_watched_video";
		AsyncHttpClient client = new AsyncHttpClient();

		RequestParams params = new RequestParams();
		params.put("video_id", getCurrentVideo().getId().toString());
		params.put("video_title", getCurrentVideo().getTitle());
		if(UserHelper.userLoggedIn()) {
			params.put("user_id", UserHelper.userInfo[UserHelper.USER]);
		}
		params.put("user_agent", "android");
		
		client.post(completedURL, params, new JsonHttpResponseHandler() {
			
			@Override
			public void onSuccess(JSONObject result) {
				Log.d(TAG, "Result logged to database.");
				try {
					result.getBoolean("success");
					//Toast.makeText(getBaseContext(), "Successfully appended to the database.", Toast.LENGTH_SHORT).show();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		
		release();
		nextTrack();
	}

	private void release() {
		if (mMediaPlayer == null) {
			return;
		}

		if (mMediaPlayer.isPlaying()) {
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

		// videoList.clear();
		// videoList.add(video);
		// play();
	}

	public void setNotificationStatus(boolean status) {
		notifStatus = status;
	}

	public void play(int i) {

		Log.v(TAG, "Currently in playing a specific song . . .");
		current = i;

		// VideoClass newSong = videoList.get(current);

		String yt_video_url = YOUTUBE_VIDEO_URL
				+ videoList.get(current).getId();
		YoutubeVideoTask myTask = new YoutubeVideoTask();
		myTask.execute(yt_video_url);
		// play(newSong);
	}

	public void play() {
		Log.d(TAG, "Currently in play(), here's the stream URL: "
				+ videoList.get(current).getUrl());

		if (videoList.size() == 0) {
			return;
		}

		VideoClass video = videoList.get(current);

		// Notification notification = new Notification(R.drawable.tubalr_icon,
		// "Tubalr", video.getTitle());

		if (mMediaPlayer != null) {

			if (!isPlaying()) {
				mMediaPlayer.start();
				paused = false;
				if (notifStatus
						&& !ForegroundHelper.activityExistsInForeground()) {
					showNotification();
				}
				return;

			} else if (mMediaPlayer != null) {
				release();
			}

		}

		try {
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setOnPreparedListener(this);
			mMediaPlayer.setOnBufferingUpdateListener(this);
			mMediaPlayer.setDataSource(this, Uri.parse(video.getUrl()));
			mMediaPlayer.prepareAsync();

			mMediaPlayer.setOnCompletionListener(this);

			if (myActivity != null) {
				final TextView currentText = (TextView) myActivity
						.findViewById(R.id.currentTextView);
				currentText.setText(video.getTitle());
			}

			if (homeActivity != null) {
				final TextView currentText = (TextView) homeActivity
						.findViewById(R.id.artistNameText);
				currentText.setText(video.getTitle());
			}

			if (notifStatus && !ForegroundHelper.activityExistsInForeground()) {
				showNotification();
			}

		} catch (IOException ioe) {
			Log.e(TAG, "Error trying to play " + video.getTitle(), ioe);
			String message = "error trying to play track: " + video.getTitle()
					+ ".\nError: " + ioe.getMessage();
			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		}

	}

	private void playlistUpdated() {
		Intent updatePlaylistIntent = new Intent(UPDATE_PLAYLIST);
		this.sendBroadcast(updatePlaylistIntent);
	}

	private void nextTrack() {

		Log.v(TAG, "Currently in nextTrack()");
		// Toast.makeText(context, text, duration)
		current++;

		if (current >= videoList.size()) {
			current = 0;
		}

		String yt_video_url = YOUTUBE_VIDEO_URL
				+ videoList.get(current).getId();
		YoutubeVideoTask myTask = new YoutubeVideoTask();
		myTask.execute(yt_video_url);
		// play();
	}

	private void prevTrack() {

		Log.v(TAG, "Currently in prevTrack()");

		if (mMediaPlayer != null) {

			if (mMediaPlayer.isPlaying()
					&& mMediaPlayer.getCurrentPosition() > 5000) {
				seek(0);
			}

			else {
				current--;

				if (current < 0) {
					current = videoList.size() - 1;
				}

				String yt_video_url = YOUTUBE_VIDEO_URL
						+ videoList.get(current).getId();
				YoutubeVideoTask myTask = new YoutubeVideoTask();
				myTask.execute(yt_video_url);
			}
		}
	}

	public void stop() {
		release();
	}

	public static boolean isPlaying() {
		if (current > 40 || mMediaPlayer == null) {
			Log.v(TAG, "I'm guessing that mMediaPlayer is null");
			return false;
		}

		return mMediaPlayer.isPlaying();
	}

	public int getCurrentSong() {
		return current;
	}

	public void pause() {
		if (mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
			paused = true;

			if (myActivity != null) {
				final ImageButton pauseButton = (ImageButton) myActivity
						.findViewById(R.id.pauseButton);
				final ImageButton playButton = (ImageButton) myActivity
						.findViewById(R.id.playButton);

				pauseButton.setVisibility(View.GONE);
				playButton.setVisibility(View.VISIBLE);
			}

			if (homeActivity != null) {
				final ImageButton pauseButton = (ImageButton) homeActivity
						.findViewById(R.id.homePauseButton);
				final ImageButton playButton = (ImageButton) homeActivity
						.findViewById(R.id.homePlayButton);

				pauseButton.setVisibility(View.GONE);
				playButton.setVisibility(View.VISIBLE);
			}

			if (notifStatus && !ForegroundHelper.activityExistsInForeground()) {
				showNotification();
			}
		}
	}

	public VideoClass getCurrentVideo() {
		if (current > 40) {
			current = 0;
			// return null;
		}

		return videoList.get(current);
	}

	public ArrayList<VideoClass> getVideos() {

		return videoList;
	}

	public int elapsed() {
		if (mMediaPlayer == null) {
			return 0;
		} else {
			return mMediaPlayer.getCurrentPosition();
		}
	}

	public int maxDuration() {
		if (mMediaPlayer == null) {
			return 0;
		}

		return mMediaPlayer.getDuration();
	}

	public void seek(int timeInMillis) {
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.seekTo(timeInMillis);
		}
	}

	private class MusicServiceBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// long id = intent.getLongExtra("id", -1);
			// String url = intent.getStringExtra("artist");

			Log.d("MusicService",
					"Received intent for action " + intent.getAction());

			if (PLAY_TRACK.equals(action)) {
				playCurrentSong();
			} else if (RESUME_TRACK.equals(action)) {
				Log.v(TAG, "play() called.");
				play();
			} else if (STOP_TRACK.equals(action)) {
				Log.v(TAG, "stop() called");
				release();
			} else if (NEXT_TRACK.equals(action)) {
				Log.v(TAG, "nextTrack() called.");
				nextTrack();
			} else if (PREV_TRACK.equals(action)) {
				Log.v(TAG, "prevTrack() called.");
				prevTrack();
			} else if (PAUSE_TRACK.equals(action)) {
				Log.v(TAG, "pauseTrack() called.");
				pause();
			} else if (PLAY_SELECT.equals(action)) {
				int position = intent.getIntExtra("position", current);
				Log.d(TAG,
						"Received current number: "
								+ Integer.toString(position));

				Log.v(TAG, "Time to play a specific song!");
				// current = intent.getIntExtra("pos", current);
				play(position);

			} else if (NEW_SONGS.equals(action)) {
				current = 0;
				ArrayList<VideoClass> newSongs = (ArrayList<VideoClass>) intent
						.getSerializableExtra("videos");
				Log.v(TAG, "New Songs registered . . . ");
				Log.v(TAG,
						"New Playlist Size: "
								+ Integer.toString(newSongs.size()));
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

	private HttpClient createHttpsClient() {
		// Toast.makeText(context, "HTTP Client Created", Toast.LENGTH_SHORT);
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params,
				HTTP.DEFAULT_CONTENT_CHARSET);
		HttpProtocolParams.setUseExpectContinue(params, true);

		SchemeRegistry schReg = new SchemeRegistry();
		schReg.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		schReg.register(new Scheme("https",
				SSLSocketFactory.getSocketFactory(), 443));
		ClientConnectionManager conMgr = new ThreadSafeClientConnManager(
				params, schReg);

		return new DefaultHttpClient(conMgr, params);
	}

	private String findVideoFilename(String content) {
		// TODO Auto-generated method stub
		Pattern videoPattern = Pattern.compile("<title>(.*?)</title>");
		Matcher matcher = videoPattern.matcher(content);
		String title;
		String titleRaw;
		if (matcher.find()) {
			titleRaw = matcher.group().replaceAll("(<| - YouTube</)title>", "")
					.replaceAll("&quot;", "\"").replaceAll("&amp;", "&")
					.replaceAll("&#39;", "'");
			title = titleRaw;
		} else {
			title = "Youtube Video";
		}

		Log.d("DEBUG", "findVideoFilename: " + title);

		return title;
	}

	private String findVideoID(String content) {
		// Pattern videoPattern =
		// Pattern.compile("<input type=\\\"hidden\\\" name=\\\"video_id\\\" value=\\\"(.*?)\\\">");
		Pattern videoPattern = Pattern.compile("\\\"video_id\\\": (.*?) \\\"");
		// Pattern endPattern = Pattern.compile("\\\"");

		Matcher matcher = videoPattern.matcher(content);
		String id;

		if (matcher.find()) {
			id = matcher.group();
			id = id.replaceAll("\\\"video_id\\\": \\\"", "");
			id = id.replaceAll("\\\", \\\"", "").trim();
		}

		else {

			Toast.makeText(getBaseContext(),
					"Error parsing music . . . skipping track.",
					Toast.LENGTH_SHORT).show();
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
		if (matcher.find()) {
			String[] CQS = contentDecoded.split(trimPattern.toString());

			// Just go with first quality for now

			boolean found = false;
			System.out.println("CODINGS");
			for (String s : CQS) {
				if (s.contains("video/mp4")) {
					System.out.println(s + "" + title);
					linksComposer(s, 1, title, id);
					break;
				}
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
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity result = response.getEntity();
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
		if (urlMatcher.find()) {
			Pattern sigPattern = Pattern
					.compile("signature=[[0-9][A-Z]]{40}\\.[[0-9][A-Z]]{40}");
			Matcher sigMatcher = sigPattern.matcher(block);
			if (sigMatcher.find()) {
				String url = urlMatcher.group();
				url = url.replaceAll("&type=.*", "");
				url = url.replaceAll("&signature=.*", "");
				url = url.replaceAll("&quality=.*", "");
				url = url.replaceAll("&fallback_host=.*", "");
				Log.d("DEBUG", "url: " + url);
				String sig = sigMatcher.group();
				Log.d("DEBUG", "sig: " + sig);
				String linkToAdd = url + "&" + sig;
				// linkToAdd.replaceAll("&itag=[0-9][0-9]&signature",
				// "&signature");
				String testString = new String(linkToAdd.getBytes());
				Log.d("DEBUG", "link:" + testString);

				videoList.get(current).setUrl(testString);

				System.out.println(videoList.get(current).getUrl());
				// adapter.notifyDataSetChanged();

				Uri testUri = Uri.parse(testString);

				play();

			}
		} else {
			Toast.makeText(getBaseContext(),
					"Error playing track, next track called.",
					Toast.LENGTH_SHORT).show();
			nextTrack();
		}

	}
	
	private class StatsTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			
			String URL = params[0];
			String html = getPage(URL);
			return html;
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

		@Override
		protected void onPostExecute(String result) {

			String[] results = result.trim().split("var swf = ");

			results = results[results.length - 1].trim().split(
					"document.getElementById\\(");
			String html = results[0];
			// getURLs(html);
			decodeURL(result);
		}
	}

	public void parseHTML(String result) {
		String title = findVideoFilename(result);
		String id = findVideoID(result);
		String[] results = result.trim().split("var swf = ");

		results = results[results.length - 1].trim().split(
				"document.getElementById\\(");
		String html = results[0];

		getURLs(html, title, id);
	}

	public void getURLs(String result, String title, String id) {

		System.out.println(result);
		String url = URLDecoder.decode(result);
		url = URLDecoder.decode(url);
		url = URLDecoder.decode(url);
		url = url.replace("\\u0026", "&");

		String[] links = url.split("url_encoded_fmt_stream_map\":");

		String link = links[links.length - 1];
		String[] qualities = link.split("url=");
		ArrayList<String> qualitiesArray = new ArrayList<String>(
				Arrays.asList(qualities));
		qualitiesArray.remove(0);

		ArrayList<String> cleanURLs = new ArrayList<String>();
		for (String s : qualitiesArray) {

			String eachLink = s.split(";+codecs=")[0];

			String contentDecoded = "";
			Pattern sigPattern = Pattern
					.compile("signature=[[0-9][A-Z]]{40}\\.[[0-9][A-Z]]{40}");

			if (eachLink.contains("video/mp4")) {
				Matcher sigMatcher = sigPattern.matcher(eachLink);
				if (sigMatcher.find()) {
					String[] results = eachLink.split("&itag=");

					String lastTag = results[results.length - 1];
					String finalURL = eachLink.split("&itag=" + lastTag)[0];

					if (finalURL.contains("sig")) {
						Log.d(TAG, "Signature already present in: " + finalURL);
						contentDecoded = url;
					}

					contentDecoded = contentDecoded
							.replace("sig=", "signature");
					cleanURLs.add(contentDecoded);
				}
			}
		}

		for (String cleanLink : cleanURLs) {
			if (cleanLink.contains("fallback_host")
					&& cleanLink.contains("itag=18")) {
				videoList.get(current).setUrl(cleanLink);
				play();
				return;
			}
		}

		Toast.makeText(getBaseContext(),
				"No valid stream URL found, skipping to next track.",
				Toast.LENGTH_SHORT).show();
		nextTrack();
		/*
		 * try { String url = URLDecoder.decode(result, "UTF-8"); url =
		 * URLDecoder.decode(url, "UTF-8"); url = URLDecoder.decode(url,
		 * "UTF-8"); url = url.replace("\\\\u0026", "&");
		 * 
		 * String[] links = url.trim().split("url_encoded_fmt_stream_map\\\":");
		 * String link = links[links.length-1];
		 * 
		 * String[] qualities = link.trim().split("url="); ArrayList<String>
		 * qualitiesArray = new ArrayList<String>(Arrays.asList(qualities));
		 * qualitiesArray.remove(0);
		 * 
		 * ArrayList<String> cleanURLs = new ArrayList<String>();
		 * 
		 * for(String s : qualitiesArray) { String[] moreLinks =
		 * s.split(";+codecs="); String current = moreLinks[0];
		 * if(current.contains("video/mp4") || current.contains("video/3gpp")) {
		 * String[] tags = current.split("&itag="); String lastTag =
		 * tags[tags.length-1];
		 * 
		 * String[] moreURLs = current.split("&itag=" + lastTag); String
		 * cleanURL = moreURLs[moreURLs.length - 1]; cleanURLs.add(cleanURL);
		 * 
		 * } }
		 * 
		 * String contentDecoded = cleanURLs.get(0); contentDecoded =
		 * contentDecoded.replaceAll("sig=", "signature="); contentDecoded =
		 * contentDecoded.replaceAll("x-flv", "flv");
		 * 
		 * } catch (UnsupportedEncodingException e) { // TODO Auto-generated
		 * catch block e.printStackTrace(); }
		 */

	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub

		final TextView timeText = (TextView) myActivity
				.findViewById(R.id.timeText);

		int secondaryPosition = seekBar.getSecondaryProgress();
		if (fromUser && mMediaPlayer != null) {
			if (progress > secondaryPosition) {
				mMediaPlayer.seekTo(progress);
				// String time = String.format("%d:%d",
				// TimeUnit.MILLISECONDS.toMinutes(player.getDuration()),
				// TimeUnit.MILLISECONDS.toSeconds(player.getDuration()) -
				// TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(player.getDuration())));
				timeText.setText(getTimeString(seekBar.getProgress()));
			}
		}

		else {
			// String time = String.format("%d:%d",
			// TimeUnit.MILLISECONDS.toMinutes(player.getDuration()),
			// TimeUnit.MILLISECONDS.toSeconds(player.getDuration()) -
			// TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(player.getDuration())));
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
		if (myActivity != null) {
			final ImageButton pauseButton = (ImageButton) myActivity
					.findViewById(R.id.pauseButton);
			final ImageButton playButton = (ImageButton) myActivity
					.findViewById(R.id.playButton);
			switch (v.getId()) {

			case R.id.playButton:
				if (mMediaPlayer != null) {

					if (!(mMediaPlayer.isPlaying())) {
						mMediaPlayer.start();
						// paused = false;
						pauseButton.setVisibility(View.VISIBLE);
						playButton.setVisibility(View.GONE);

						startPlayProgressUpdater();
					}
				}
				break;

			case R.id.pauseButton:
				if (mMediaPlayer != null) {

					if (mMediaPlayer.isPlaying()) {
						pause();
						pauseButton.setVisibility(View.GONE);
						playButton.setVisibility(View.VISIBLE);
					}
				}
				break;

			}
		}

		if (homeActivity != null) {
			final ImageButton pauseButton = (ImageButton) homeActivity
					.findViewById(R.id.homePauseButton);
			final ImageButton playButton = (ImageButton) homeActivity
					.findViewById(R.id.homePlayButton);

			switch (v.getId()) {

			case R.id.homePlayButton:
				if (mMediaPlayer != null) {

					if (!(mMediaPlayer.isPlaying())) {
						mMediaPlayer.start();
						// paused = false;
						pauseButton.setVisibility(View.VISIBLE);
						playButton.setVisibility(View.GONE);
					}
				}
				break;

			case R.id.homePauseButton:

				if (mMediaPlayer != null) {

					if (mMediaPlayer.isPlaying()) {
						pause();
						pauseButton.setVisibility(View.GONE);
						playButton.setVisibility(View.VISIBLE);
					}

				}
				break;

			case R.id.homePrevButton:
				prevTrack();
				break;

			case R.id.nextButton:
				nextTrack();
				break;

			default:
				break;
			}
		}

		/*
		 * switch (v.getId()) {
		 * 
		 * case R.id.playButton: if(mMediaPlayer != null &&
		 * !(mMediaPlayer.isPlaying())) { mMediaPlayer.start(); //paused =
		 * false; pauseButton.setVisibility(View.VISIBLE);
		 * playButton.setVisibility(View.GONE); } break;
		 * 
		 * case R.id.pauseButton: if(mMediaPlayer.isPlaying() && mMediaPlayer !=
		 * null) { pause(); pauseButton.setVisibility(View.GONE);
		 * playButton.setVisibility(View.VISIBLE); } break;
		 * 
		 * case R.id.homePlayButton: Log.d(TAG, "HOME PLAY BUTTON CLICKED!");
		 * 
		 * if(mMediaPlayer != null && !(mMediaPlayer.isPlaying())) {
		 * mMediaPlayer.start(); //paused = false;
		 * pauseButton.setVisibility(View.VISIBLE);
		 * playButton.setVisibility(View.GONE); } break;
		 * 
		 * 
		 * case R.id.homePauseButton:
		 * 
		 * Log.d(TAG, "HOME PAUSE BUTTON CLICKED!"); if(mMediaPlayer.isPlaying()
		 * && mMediaPlayer != null) { pause();
		 * pauseButton.setVisibility(View.GONE);
		 * playButton.setVisibility(View.VISIBLE); } break;
		 * 
		 * case R.id.homeNextButton: nextTrack();
		 * 
		 * break;
		 * 
		 * case R.id.homePrevButton: prevTrack();
		 * 
		 * break;
		 * 
		 * 
		 * 
		 * 
		 * default: break;
		 */
	}

	@SuppressLint("NewApi")
	public void showNotification() {
		// TODO Auto-generated method stub

		Intent intent = null;
		if (homeActivity != null) {
			intent = new Intent(getApplicationContext(), MainActivity.class);
		}

		if (myActivity != null) {
			intent = new Intent(getApplicationContext(), PlaylistActivity.class);
			intent.putExtra("videos", getVideos());
			intent.putExtra("new", false);
		}

		intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			PendingIntent pIntent = PendingIntent.getActivity(
					getApplicationContext(), 0, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			PendingIntent prevIntent = PendingIntent.getBroadcast(this, 0,
					new Intent(PREV_TRACK), 0);
			PendingIntent nextIntent = PendingIntent.getBroadcast(this, 0,
					new Intent(NEXT_TRACK), 0);
			PendingIntent pauseIntent = PendingIntent.getBroadcast(this, 0,
					new Intent(RESUME_TRACK), 0);
			PendingIntent playIntent = PendingIntent.getBroadcast(this, 0,
					new Intent(PAUSE_TRACK), 0);
			PendingIntent stopIntent = PendingIntent.getBroadcast(this, 0,
					new Intent(STOP_TRACK), 0);
			Notification.Builder mBuilder = new Notification.Builder(this)
					.setSmallIcon(R.drawable.tubalr_icon)
					.setContentTitle("Tubalr")
					.setContentText(getCurrentVideo().getTitle())
					.setContentIntent(pIntent).setAutoCancel(false)
					.setDeleteIntent(stopIntent);

			if (Build.VERSION.SDK_INT >= 16) {
				mBuilder.addAction(R.drawable.player_prev, null, prevIntent)
						.addAction(
								isPlaying() ? R.drawable.player_pause
										: R.drawable.player_play,
								isPlaying() ? null : null,
								isPlaying() ? playIntent : pauseIntent)
						.addAction(R.drawable.player_next, null, nextIntent);
			}
			;

			Notification notif = mBuilder.build();

			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

			// notif.flags |= Notification.FLAG_NO_CLEAR |
			// Notification.FLAG_ONGOING_EVENT | Notification.FLAG_AUTO_CANCEL;
			notif.flags |= Notification.FLAG_NO_CLEAR;

			notificationManager.notify(0, notif);
		} else {
			PendingIntent pIntent = PendingIntent.getActivity(
					getApplicationContext(), 0, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			PendingIntent stopIntent = PendingIntent.getBroadcast(this, 0,
					new Intent(STOP_TRACK), 0);
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
					this).setSmallIcon(R.drawable.tubalr_icon)
					.setContentTitle("Tubalr")
					.setContentText(getCurrentVideo().getTitle())
					.setContentIntent(pIntent).setAutoCancel(false)
					.setDeleteIntent(stopIntent);

			Notification notif = mBuilder.build();

			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

			// notif.flags |= Notification.FLAG_NO_CLEAR |
			// Notification.FLAG_ONGOING_EVENT | Notification.FLAG_AUTO_CANCEL;
			notif.flags |= Notification.FLAG_NO_CLEAR;

			notificationManager.notify(0, notif);
		}

		// mNotificationManager.notify(0, mBuilder.build();

	}

	public void checkVisibility(boolean b) {
		// TODO Auto-generated method stub
		isVisible = b;
	}

	public void clearNotifications() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		notificationManager.cancelAll();
		notificationManager.cancel(0);
	}

}
