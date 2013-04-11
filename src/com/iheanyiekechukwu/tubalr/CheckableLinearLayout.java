package com.iheanyiekechukwu.tubalr;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.LinearLayout;

public class CheckableLinearLayout extends LinearLayout implements Checkable {
	private boolean checked = false;

	public CheckableLinearLayout(Context context) {
	    super(context, null);
	}

	public CheckableLinearLayout(Context context, AttributeSet attrs) {
	    super(context, attrs);       
	}

	private final int[] CheckedStateSet = {
	    android.R.attr.state_checked
	};

	public void setChecked(boolean b) {
	    checked = b;
	}

	private final List<Checkable> mCheckableViews = new ArrayList<Checkable>();

	@Override
	protected void onFinishInflate() {
	    super.onFinishInflate();
	    final int childCount = getChildCount();
	    findCheckableChildren(this);
	}

	private void findCheckableChildren(View v) {
	    if (v instanceof Checkable && v instanceof ViewGroup) {
	        mCheckableViews.add((Checkable) v);
	    }
	    if (v instanceof ViewGroup) {
	        final ViewGroup vg = (ViewGroup) v;
	        final int childCount = vg.getChildCount();
	        for (int i = 0; i < childCount; ++i) {
	            findCheckableChildren(vg.getChildAt(i));
	        }
	    }
	}


	public void toggle() {
	    checked = !checked;
	}

	@Override
	protected int[] onCreateDrawableState(int extraSpace) {
	    final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
	    if (isChecked()) {
	        mergeDrawableStates(drawableState, CheckedStateSet);
	    }
	    return drawableState;
	}

	@Override
	public boolean performClick() {
	    toggle();
	    return super.performClick();
	}

	@Override
	public boolean isChecked() {
		// TODO Auto-generated method stub
		return checked;
	}

}