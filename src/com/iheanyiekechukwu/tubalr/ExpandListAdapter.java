package com.iheanyiekechukwu.tubalr;

import java.util.ArrayList;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class ExpandListAdapter extends BaseExpandableListAdapter {

	private Context context;
	private ArrayList<ListGroup> groups;
	
	private LayoutInflater inflater;
	
	public ExpandListAdapter(Context context, ArrayList<ListGroup> groups) {
		inflater = LayoutInflater.from(context);
		this.groups = groups;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		
		//ArrayList<String> chList = groups.get(groupPosition).getArrayChildren().get(childPosition);

		return groups.get(groupPosition).getArrayChildren().get(childPosition);
			
		//return chList.get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		
		return childPosition;
	}
	
	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view,
            ViewGroup parent) {
		// TODO Auto-generated method stub
		
//		ListChild child = (ListChild) getChild(groupPosition, childPosition);
		
		if(view == null) {
			view = inflater.inflate(R.layout.expand_list_child_item, parent, false);	
		}
		
		TextView tv = (TextView) view.findViewById(R.id.tvChild);
		
		tv.setText(groups.get(groupPosition).getArrayChildren().get(childPosition));
		//tv.setTag(child.getTag());
		return view;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
      //  ArrayList<ListChild> chList = groups.get(groupPosition).getItems();

        return groups.get(groupPosition).getArrayChildren().size();
       // return chList.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return groups.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return groups.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}
	
	public void addItem(ListChild item, ListGroup group) {
		if(!groups.contains(group)) {
			groups.add(group);
		}
		
		int index = groups.indexOf(group);
		/*ArrayList<ListChild> ch = groups.get(index).getItems();
		ch.add(item);
		groups.get(index).setItems(ch)*/;
	}
	

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View view, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		ListGroup group = (ListGroup) getGroup(groupPosition);
		
		if(view == null) {
			view = inflater.inflate(R.layout.expand_list_group_item, parent, false);
		}
		
		TextView tv = (TextView) view.findViewById(R.id.tvGroup);
		tv.setText(group.getTitle());
		return view;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
	}
	
	 @Override
	    public void registerDataSetObserver(DataSetObserver observer) {
	        /* used to make the notifyDataSetChanged() method work */
	        super.registerDataSetObserver(observer);
	    }
	
}
