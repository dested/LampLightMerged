package com.LampLight;

import java.util.HashMap;

import android.graphics.Paint;

public class PaintBucket {
	private HashMap<String, Paint> hs = new HashMap<String, Paint>();

	public Paint GetPaint(String n) {
		return hs.get(n);
	}

	public void AddPaint(String n, Paint p) {
		hs.put(n, p);
	}

	public Paint AddPaint(String n) {
		Paint ps;

		hs.put(n, ps = new Paint());
		return ps;
	}
}
