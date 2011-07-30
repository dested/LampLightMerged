package com.BigGamev1;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

public class Horiz extends HorizontalScrollView {

	public Horiz(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public Horiz(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public Horiz(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {

		super.onLayout(changed, l, t, r, b);

		if (changed)
			scrollTo(301, 0);
	}

}
