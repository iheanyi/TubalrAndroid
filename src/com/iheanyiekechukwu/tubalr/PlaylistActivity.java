package com.iheanyiekechukwu.tubalr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class PlaylistActivity extends Activity {

	private ListView playlistView;
	//private PlaylistAdapter playlistAdapter;
	
	private ArrayList<String> stringList;
	private ArrayAdapter<VideoClass> playlistStringAdapter;
	private Context context;
	private Intent i;
	
	private ArrayList<VideoClass> videoList;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_playlist);
		// Show the Up button in the action bar.
		setupActionBar();
		
		
		i = this.getIntent();
		
		
		ArrayList<VideoClass> tempList = (ArrayList<VideoClass>) i.getSerializableExtra("playlistExtra");
		videoList = (ArrayList<VideoClass>) i.getSerializableExtra("playlistExtra");
		Iterator<VideoClass> it = videoList.iterator();
		
		context = this.getBaseContext();
		stringList = new ArrayList<String>();
		playlistView = (ListView) findViewById(R.id.playlistView);
		playlistStringAdapter = new ArrayAdapter<VideoClass>(context, android.R.layout.simple_list_item_1, videoList);
		playlistView.setAdapter(playlistStringAdapter);

		
		/*while(it.hasNext()) {
			VideoClass currentVideo = it.next();
			
			String info = currentVideo.getTitle();
			stringList.add(info);
			playlistStringAdapter.add(info);
			playlistStringAdapter.notifyDataSetChanged();
			Log.d("ADD", "Added " + info + " to list.");
		}*/
		

	
		

		
		
		//Log.d("LENGTH", Integer.toString(stringList.size());
		

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
	
/*	private class VideoAdapter extends ArrayAdapter<T> {
		private LayoutInflater inflater;
		private ArrayList<String> data;
		
		public VideoAdapter(Context context, int resource, ArrayList<T> objects) {
			super(context, resource, objects);
			
			
			// TODO Auto-generated constructor stub
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			
		}

		
	} */
	
	/*public class PlaylistAdapter extends ArrayAdapter<String> {
		super(context, resource, data);
	} */

}
