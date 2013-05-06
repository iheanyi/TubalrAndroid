package com.iheanyiekechukwu.tubalr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bugsense.trace.BugSenseHandler;
import com.bugsnag.android.Bugsnag;
import com.flurry.android.FlurryAgent;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpResponse;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Activities that
 * contain this fragment must implement the
 * {@link Subreddit.OnFragmentInteractionListener} interface to handle
 * interaction events. Use the {@link Subreddit#newInstance} factory method to
 * create an instance of this fragment.
 * 
 */
public class Subreddit extends Fragment implements OnClickListener, OnChildClickListener {
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;
	private ProgressBar progress;
	
	private  ExpandListAdapter expAdapter;
	private ArrayList<ListGroup> listItems;
	private ExpandableListView expListView;
	
    public static final String BUG_KEY = "b27d57ef";
    public static final String FLURRY_KEY = "4GF6RX8PZ7DP53V795RF";
    
	
	TextView testText;

	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 * 
	 * @param param1
	 *            Parameter 1.
	 * @param param2
	 *            Parameter 2.
	 * @return A new instance of fragment Subreddit.
	 */
	// TODO: Rename and change types and number of parameters
	public static Subreddit newInstance(String param1, String param2) {
		Subreddit fragment = new Subreddit();
		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, param1);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}

	public Subreddit() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mParam1 = getArguments().getString(ARG_PARAM1);
			mParam2 = getArguments().getString(ARG_PARAM2);
		}
	}
	
   /* public ArrayList<ListGroup> SetStandardGroups() {
    	/*ArrayList<ListGroup> list = new ArrayList<ListGroup>();
    	ArrayList<ListChild> list2 = new ArrayList<ListChild>();
        ListGroup gru1 = new ListGroup();
        gru1.setName("Comedy");
        ListChild ch1_1 = new ListChild();
        ch1_1.setName("A movie");
        ch1_1.setTag(null);
        list2.add(ch1_1);
        ListChild ch1_2 = new ListChild();
        ch1_2.setName("An other movie");
        ch1_2.setTag(null);
        list2.add(ch1_2);
        ListChild ch1_3 = new ListChild();
        ch1_3.setName("And an other movie");
        ch1_3.setTag(null);
        list2.add(ch1_3);
        gru1.setItems(list2);
        list2 = new ArrayList<ListChild>();
        
        ListGroup gru2 = new ListGroup();
        gru2.setName("Action");
        ListChild ch2_1 = new ListChild();
        ch2_1.setName("A movie");
        ch2_1.setTag(null);
        list2.add(ch2_1);
        ListChild ch2_2 = new ListChild();
        ch2_2.setName("An other movie");
        ch2_2.setTag(null);
        list2.add(ch2_2);
        ListChild ch2_3 = new ListChild();
        ch2_3.setName("And an other movie");
        ch2_3.setTag(null);
        list2.add(ch2_3);
        gru2.setItems(list2);
        list.add(gru1);
        list.add(gru2);
        
        return list;
    }*/

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		BugSenseHandler.initAndStartSession(getActivity(), BUG_KEY);
        Bugsnag.register(getActivity(), "1d479c585e3d333a05943f37bef208cf");
        FlurryAgent.onStartSession(getActivity(), FLURRY_KEY);
        
		View v = inflater.inflate(R.layout.fragment_subreddit, container, false);
		
		listItems = new ArrayList<ListGroup>();
		ArrayList<String> children = new ArrayList<String>();

		progress = (ProgressBar) v.findViewById(R.id.redditProgressBar);


		
		expListView = (ExpandableListView) v.findViewById(R.id.expandable_list);
		expListView.setOnChildClickListener(this);
		
		expListView.setVisibility(View.GONE);
		
		
		AsyncHttpClient.getDefaultInstance().get("http://www.tubalr.com/api/library.json", new AsyncHttpClient.StringCallback() {

			@Override
			public void onCompleted(Exception e, AsyncHttpResponse source,
					String result) {
				// TODO Auto-generated method stub
				if(e != null) {
		            e.printStackTrace();
		            return;
				}
				
				processTubalrJSON(result);
				progress.setVisibility(View.GONE);
				expListView.setVisibility(View.VISIBLE);
				
			}
			
			
		});
		
		for(int i = 0; i < 10; ++i) {

			
			for(int j = 0; j < 10; ++j) {
				children.add("Child " + j);
			}
			//parent.setArrayChildren(children);
			
			//groups.add(parent);
		}
		
		//expAdapter = new ExpandListAdapter(getActivity(), groups);
		//expListView.setAdapter(new ExpandListAdapter(getActivity(), groups));

        
        
		
		/*AsyncHttpClient.getDefaultInstance().get("http://www.tubalr.com/api/library.json", new AsyncHttpClient.StringCallback() {
		    // Callback is invoked with any exceptions/errors, and the result, if available.
 
			@Override
			public void onCompleted(Exception e, AsyncHttpResponse source,
					String result) {
				// TODO Auto-generated method stub
		        if (e != null) {
		            e.printStackTrace();
		            return;
		        }
		        System.out.println("I got a string: " + result);
			}
		});*/
		
		
		return v;
	}
	
	public void processTubalrJSON(String result) {
		ArrayList<ListGroup> groups = new ArrayList<ListGroup>();

		try {
			JSONObject object = new JSONObject(result);
			JSONArray reddit = object.getJSONArray("reddit");
			
			for(int i = 0; i < reddit.length(); ++i) {
				JSONObject category = reddit.getJSONObject(i);
				JSONArray categoryArray = category.getJSONArray("subreddits");
				
				ListGroup parent = new ListGroup();
				parent.setTitle(category.getString("category"));
				ArrayList<String> children = new ArrayList<String>();
				for(int j = 0; j < categoryArray.length(); ++j) {
					String name = categoryArray.getString(j);
					children.add(name);
					
				}
				parent.setArrayChildren(children);
				
				listItems.add(parent);
			}
			
			Collections.sort(listItems, new CustomComparator());
			
			expListView.setAdapter(new ExpandListAdapter(getActivity(), listItems));
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle saveInstanceState) {
		
		saveInstanceState.putString("BUG_FIX", "BUG_FIX");
		super.onSaveInstanceState(saveInstanceState);
	}

	// TODO: Rename method, update argument and hook method into UI event

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated to
	 * the activity and potentially other fragments contained in that activity.
	 * <p>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	
	public class CustomComparator implements Comparator<ListGroup> {
	    @Override
	    public int compare(ListGroup o1, ListGroup o2) {
	        return o1.getTitle().compareTo(o2.getTitle());
	    }
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		switch(v.getId()) {
		case R.id.list_item:
			
			break;
		}
		
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		// TODO Auto-generated method stub
		
		String selected = listItems.get(groupPosition).getArrayChildren().get(childPosition);
		String s_url = "";
		String s_type = "reddit";
		String s_artist = selected;
		
		Intent i = new Intent(getActivity(), PlaylistActivity.class);
        i.putExtra("url", s_url);
        i.putExtra("type", s_type);
        i.putExtra("artist", s_artist);
        i.putExtra("new", true);
        getActivity().startActivityForResult(i, 1);
		return true;
	}
}
