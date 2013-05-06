package com.iheanyiekechukwu.tubalr;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

//import com.koushikdutta.async.http.AsyncHttpClient;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Activities that
 * contain this fragment must implement the
 * {@link UserPlaylist.OnFragmentInteractionListener} interface to handle
 * interaction events. Use the {@link UserPlaylist#newInstance} factory method
 * to create an instance of this fragment.
 * 
 */
public class UserPlaylist extends SherlockFragment implements
		OnItemClickListener {
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String USER = UserHelper.userInfo[UserHelper.USER];
	private static final String TOKEN = UserHelper.userInfo[UserHelper.TOKEN];

	public static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";

	public static final String BUG_KEY = "b27d57ef";
	public static final String FLURRY_KEY = "4GF6RX8PZ7DP53V795RF";

	private ArrayAdapter<String> myAdapter;

	private ArrayList<String> userPlaylists;
	private ArrayList<Integer> playlistIDs;

	private OnFragmentInteractionListener mListener;

	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 * 
	 * @param param1
	 *            Parameter 1.
	 * @param param2
	 *            Parameter 2.
	 * @return A new instance of fragment PlaylistFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static UserPlaylist newInstance(String param1, String param2) {
		UserPlaylist fragment = new UserPlaylist();
		Bundle args = new Bundle();
		args.putString(USER, param1);
		args.putString(TOKEN, param2);
		fragment.setArguments(args);
		return fragment;
	}

	public UserPlaylist() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_playlist, container, false);

		final ListView playlistView = (ListView) v
				.findViewById(R.id.usersPlaylists);
		final ProgressBar userProgress = (ProgressBar) v
				.findViewById(R.id.playlistProgress);
		TextView loginText = (TextView) v.findViewById(R.id.loginText);

		userPlaylists = new ArrayList<String>();
		playlistIDs = new ArrayList<Integer>();

		String user = UserHelper.userInfo[UserHelper.USER];
		String token = UserHelper.userInfo[UserHelper.TOKEN];
		String url = "http://www.tubalr.com/api/user/info.json";

		// Log.d("INFO", user);
		// Log.d("INFO", token);

		if (UserHelper.userLoggedIn()) {
			loginText.setVisibility(View.GONE);
			playlistView.setVisibility(View.GONE);
			userProgress.setVisibility(View.VISIBLE);
			// Toast.makeText(getActivity(), "Fetching playlists for " + user +
			// " with token " + token, Toast.LENGTH_SHORT).show();
			AsyncHttpClient client = new AsyncHttpClient();
			RequestParams params = new RequestParams();
			params.put("auth_token", token);
			client.get(url, params, new JsonHttpResponseHandler() {
				@Override
				public void onSuccess(JSONObject result) {
					Log.d("USER",
							"Successfully succeeded in querying the page . . . ");
					JSONArray playlistArray;
					try {
						playlistArray = result.getJSONArray("playlists");
						for (int i = 0; i < playlistArray.length(); ++i) {
							JSONObject j = playlistArray.getJSONObject(i);
							Log.d("USER",
									"Adding: " + j.getString("playlist_name"));
							userPlaylists.add(j.getString("playlist_name"));
							playlistIDs.add(j.getInt("id"));
						}

						playlistView.setVisibility(View.VISIBLE);
						userProgress.setVisibility(View.GONE);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// JSONArray playlistArray = new JSONArray(result);

				}
			});

		} else {
			loginText.setVisibility(View.VISIBLE);
			playlistView.setVisibility(View.GONE);
			userProgress.setVisibility(View.GONE);
		}

		myAdapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, userPlaylists);
		playlistView.setAdapter(myAdapter);
		myAdapter.notifyDataSetChanged();

		playlistView.setOnItemClickListener(this);

		return v;
	}

	// TODO: Rename method, update argument and hook method into UI event
	public void onButtonPressed(Uri uri) {
		if (mListener != null) {
			mListener.onFragmentInteraction(uri);
		}
	}

	public String getUser() {

		return getArguments().getString(USER);
	}

	public String getToken() {
		return getArguments().getString(TOKEN);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnFragmentInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated to
	 * the activity and potentially other fragments contained in that activity.
	 * <p>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {
		// TODO: Update argument type and name
		public void onFragmentInteraction(Uri uri);
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View v, int pos, long id) {
		// TODO Auto-generated method stub

		Integer selected = playlistIDs.get(pos);

		String s_url = "";
		String s_type = "user";
		String s_artist = Integer.toString(selected);

		Intent i = new Intent(getActivity(), PlaylistActivity.class);
		i.putExtra("url", s_url);
		i.putExtra("type", s_type);
		i.putExtra("artist", s_artist);
		i.putExtra("new", true);
		getActivity().startActivityForResult(i, 1);

	}

}
