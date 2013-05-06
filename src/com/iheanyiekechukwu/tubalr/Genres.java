package com.iheanyiekechukwu.tubalr;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.bugsense.trace.BugSenseHandler;
import com.bugsnag.android.Bugsnag;
import com.flurry.android.FlurryAgent;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Activities that
 * contain this fragment must implement the
 * {@link Genres.OnFragmentInteractionListener} interface to handle interaction
 * events. Use the {@link Genres#newInstance} factory method to create an
 * instance of this fragment.
 * 
 */


public class Genres extends SherlockFragment implements OnClickListener, OnItemClickListener {
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";
	public static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";

    public static final String BUG_KEY = "b27d57ef";
    public static final String FLURRY_KEY = "4GF6RX8PZ7DP53V795RF";
    
	private ArrayAdapter<String> allAdapter;

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;
	
    ArrayList<String> groupItem = new ArrayList<String>();
    ArrayList<Object> childItem = new ArrayList<Object>();

    private ArrayList<String> allGenresList;
    private static final String[] topGenres = new String[]{"rock",
		"electronic",
		"hip hop",
		"pop rock",
		"indie rock",
		"jazz",
		"techno",
		"hard rock",
		"electro",
		"disco",
		"heavy metal",
		"trance",
		"alternative rock",
		"pop",
		"downtempo",
		"new wave",
		"reggae",
		"folk rock",
		"funk",
		"dance pop",
		"soft rock",
		"classic rock",
		"dub",
		"tech house",
		"deep house",
		"progressive house",
		"rap",
		"progressive trance",
		"art rock",
		"breakbeat",
		"singer-songwriter",
		"indie pop",
		"easy listening",
		"experimental",
		"country rock",
		"blues",
		"gangster rap",
		"punk",
		"pop rap",
		"blues-rock"};
	
	private String[] allGenres = new String[] {"a cappella",
			"acid house",
			"acid jazz",
			"acoustic blues",
			"afrobeat",
			"album rock",
			"alternative country",
			"alternative dance",
			"alternative hip hop",
			"alternative metal",
			"alternative rock",
			"ambient",
			"anti-folk",
			"art rock",
			"atmospheric black metal",
			"australian hip hop",
			"avant-garde",
			"avant-garde jazz",
			"avantgarde metal",
			"azonto",
			"bachata",
			"baile funk",
			"banda",
			"bass music",
			"bebop",
			"bhangra",
			"big band",
			"big beat",
			"black metal",
			"blue-eyed soul",
			"bluegrass",
			"blues",
			"blues-rock",
			"bolero",
			"boogaloo",
			"boogie-woogie",
			"bossa nova",
			"brass band",
			"brazilian pop music",
			"breakbeat",
			"breakcore",
			"brill building pop",
			"british blues",
			"british folk",
			"british invasion",
			"british pop",
			"broken beat",
			"brutal death metal",
			"bubblegum dance",
			"bubblegum pop",
			"cabaret",
			"calypso",
			"canterbury scene",
			"ccm",
			"celtic",
			"celtic rock",
			"chamber pop",
			"chanson",
			"chicago blues",
			"chicago house",
			"chicago soul",
			"children's music",
			"chill-out",
			"chillwave",
			"chiptune",
			"choral",
			"choro",
			"christian alternative rock",
			"christian hardcore",
			"christian hip hop",
			"christian metal",
			"christian music",
			"christian punk",
			"christian rock",
			"classic funk rock",
			"classic rock",
			"classical",
			"comedy",
			"contemporary country",
			"cool jazz",
			"country",
			"country blues",
			"country gospel",
			"country rock",
			"cowpunk",
			"crossover thrash",
			"crunk",
			"crust punk",
			"cumbia",
			"dance pop",
			"dance rock",
			"dance-punk",
			"dancehall",
			"dark ambient",
			"dark wave",
			"death core",
			"death metal",
			"deathgrind",
			"deep house",
			"delta blues",
			"desi",
			"detroit techno",
			"digital hardcore",
			"dirty south rap",
			"disco",
			"disco house",
			"djent",
			"doo-wop",
			"doom metal",
			"downtempo",
			"dream pop",
			"drone",
			"drum and bass",
			"dub",
			"dub techno",
			"dubstep",
			"dutch rock",
			"early music",
			"east coast hip hop",
			"easy listening",
			"ebm",
			"electric blues",
			"electro",
			"electro-industrial",
			"electroclash",
			"electronic",
			"emo",
			"eurobeat",
			"eurodance",
			"europop",
			"exotica",
			"experimental",
			"experimental rock",
			"fado",
			"filmi",
			"flamenco",
			"folk",
			"folk metal",
			"folk punk",
			"folk rock",
			"folk-pop",
			"freak folk",
			"freakbeat",
			"free improvisation",
			"free jazz",
			"freestyle",
			"funeral doom",
			"funk",
			"funk metal",
			"funk rock",
			"futurepop",
			"g funk",
			"gabba",
			"game",
			"gangster rap",
			"garage rock",
			"german pop",
			"glam metal",
			"glam rock",
			"glitch",
			"goregrind",
			"gospel",
			"gothic metal",
			"gothic rock",
			"gothic symphonic metal",
			"grave wave",
			"grime",
			"grindcore",
			"groove metal",
			"grunge",
			"gypsy jazz",
			"happy hardcore",
			"hard bop",
			"hard house",
			"hard rock",
			"hard trance",
			"hardcore",
			"hardcore hip hop",
			"hardcore techno",
			"hardstyle",
			"harmonica blues",
			"hi nrg",
			"highlife",
			"hip hop",
			"hip house",
			"horror punk",
			"house",
			"hyphy",
			"icelandic pop",
			"illbient",
			"indian classical",
			"indie folk",
			"indie pop",
			"indie rock",
			"indietronica",
			"industrial",
			"industrial metal",
			"industrial rock",
			"intelligent dance music",
			"irish folk",
			"italian disco",
			"j pop",
			"j rock",
			"jam band",
			"jangle pop",
			"japanoise",
			"jazz",
			"jazz blues",
			"jazz funk",
			"jazz fusion",
			"judaica",
			"jug band",
			"juggalo",
			"jump blues",
			"jungle music",
			"k pop",
			"kiwi rock",
			"klezmer",
			"kompa",
			"kraut rock",
			"kwaito",
			"laiko",
			"latin",
			"latin alternative",
			"latin jazz",
			"latin pop",
			"lo-fi",
			"louisiana blues",
			"lounge",
			"lovers rock",
			"madchester",
			"mambo",
			"mariachi",
			"martial industrial",
			"math rock",
			"mathcore",
			"mbalax",
			"medieval",
			"mellow gold",
			"melodic death metal",
			"melodic hardcore",
			"melodic metalcore",
			"memphis blues",
			"memphis soul",
			"merengue",
			"merseybeat",
			"metal",
			"metalcore",
			"minimal",
			"modern blues",
			"modern classical",
			"motown",
			"mpb",
			"musique concrete",
			"nashville sound",
			"native american",
			"neo classical metal",
			"neo soul",
			"neo-progressive",
			"neoclassical",
			"neofolk",
			"neue deutsche harte",
			"new age",
			"new beat",
			"new jack swing",
			"new orleans blues",
			"new orleans jazz",
			"new rave",
			"new romantic",
			"new wave",
			"new weird america",
			"ninja",
			"no wave",
			"noise pop",
			"noise rock",
			"northern soul",
			"nu jazz",
			"nu metal",
			"nu skool breaks",
			"nwobhm",
			"oi",
			"old school hip hop",
			"opera",
			"opm",
			"oratory",
			"outlaw country",
			"pagan black metal",
			"piano blues",
			"piano rock",
			"piedmont blues",
			"polka",
			"pop",
			"pop punk",
			"pop rap",
			"pop rock",
			"portuguese rock",
			"post rock",
			"post-grunge",
			"post-hardcore",
			"post-metal",
			"post-punk",
			"power electronics",
			"power metal",
			"power noise",
			"power pop",
			"power violence",
			"progressive bluegrass",
			"progressive house",
			"progressive metal",
			"progressive rock",
			"progressive trance",
			"protopunk",
			"psychedelic rock",
			"psychedelic trance",
			"psychobilly",
			"punk",
			"punk blues",
			"quiet storm",
			"r&b",
			"ragtime",
			"rai",
			"ranchera",
			"rap",
			"rap metal",
			"rap rock",
			"reggae",
			"reggaeton",
			"renaissance",
			"rock",
			"rock 'n roll",
			"rock en espanol",
			"rock steady",
			"rockabilly",
			"roots reggae",
			"roots rock",
			"rumba",
			"salsa",
			"samba",
			"screamo",
			"sexy",
			"shibuya-kei",
			"shoegaze",
			"show tunes",
			"singer-songwriter",
			"ska",
			"ska punk",
			"skate punk",
			"skiffle",
			"slovenian rock",
			"slow core",
			"sludge metal",
			"smooth jazz",
			"soca",
			"soft rock",
			"soukous",
			"soul",
			"soul blues",
			"soul jazz",
			"soundtrack",
			"southern gospel",
			"southern hip hop",
			"southern rock",
			"southern soul",
			"space rock",
			"speed garage",
			"speed metal",
			"speedcore",
			"stoner metal",
			"stoner rock",
			"straight edge",
			"stride",
			"suomi rock",
			"surf music",
			"swamp blues",
			"swedish indie pop",
			"swing",
			"symphonic black metal",
			"symphonic metal",
			"symphonic rock",
			"synthpop",
			"tango",
			"tech house",
			"technical death metal",
			"techno",
			"teen pop",
			"tejano",
			"texas blues",
			"texas country",
			"thai pop",
			"thrash core",
			"thrash metal",
			"traditional blues",
			"traditional country",
			"traditional folk",
			"trance",
			"trap music",
			"trapstep",
			"tribal house",
			"trip hop",
			"turbo folk",
			"turntablism",
			"twee pop",
			"uk garage",
			"uk post-punk",
			"underground hip hop",
			"uplifting trance",
			"urban contemporary",
			"vallenato",
			"video game music",
			"viking metal",
			"visual kei",
			"vocal house",
			"vocal jazz",
			"west coast rap",
			"western swing",
			"world",
			"worship",
			"zouk",
			"zydeco"};


	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 * 
	 * @param param1
	 *            Parameter 1.
	 * @param param2
	 *            Parameter 2.
	 * @return A new instance of fragment Genres.
	 */
	// TODO: Rename and change types and number of parameters
	public static Genres newInstance(String param1, String param2) {
		Genres fragment = new Genres();
		Bundle args = new Bundle(1);
		args.putString(EXTRA_MESSAGE, param1);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}

	public Genres() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
;
		
		setGroupData();
		setChildData();
		
		
		/*if (getArguments() != null) {
			mParam1 = getArguments().getString(ARG_PARAM1);
			mParam2 = getArguments().getString(ARG_PARAM2);
		}*/
	}
	
	@Override
	public void onSaveInstanceState(Bundle saveInstanceState) {	
		saveInstanceState.putString("BUG_FIX", "BUG_FIX");
		super.onSaveInstanceState(saveInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_genres, container, false);
		
		ListView topList = (ListView) v.findViewById(R.id.topGenreList);
		
		//ViewGroup header = (ViewGroup) inflater.inflate(R.layout.topheader, topList, false);
		//topList.addHeaderView(header, null, false);
		
		allGenresList = new ArrayList<String>(Arrays.asList(allGenres));
		java.util.Collections.sort(allGenresList);
		
		//ArrayAdapter<String> topAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, topGenres);
		//topList.setAdapter(topAdapter);
		
		ListView allList = (ListView) v.findViewById(R.id.allGenreList);
		
		ViewGroup bottomHeader = (ViewGroup) inflater.inflate(R.layout.allheader, allList, false);
		//allList.addHeaderView(bottomHeader, null, false);
		
		allAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, allGenresList);
		allList.setAdapter(allAdapter);
		
		allList.setOnItemClickListener(this);
		
		BugSenseHandler.initAndStartSession(getActivity(), BUG_KEY);
        Bugsnag.register(getActivity(), "1d479c585e3d333a05943f37bef208cf");
        FlurryAgent.onStartSession(getActivity(), FLURRY_KEY);

		
		
		return v;
	}
	
	public void setGroupData() {
		groupItem.add("Top Genres");
		groupItem.add("All Genres");
	}
	
	public void setChildData() {

		childItem.add(topGenres);
		childItem.add(allGenres);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
				
	}

	@Override
	public void onItemClick(AdapterView<?> adapt, View v, int pos, long arg3) {
		// TODO Auto-generated method stub
		String selected = allGenresList.get(pos);
		String s_url = "";
		String s_type ="genre";
		String s_artist = selected;
		
		Intent i = new Intent(getActivity(), PlaylistActivity.class);
        i.putExtra("url", s_url);
        i.putExtra("type", s_type);
        i.putExtra("artist", s_artist);
        i.putExtra("new", true);
        getActivity().startActivityForResult(i, 1);
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
}
