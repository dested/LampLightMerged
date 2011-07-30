package com.Helper;

import java.lang.reflect.Array;
import java.util.ArrayList;

import mazeBig.Point;
import android.graphics.Rect;
import android.util.Pair;

public class Helping {
	public static String GetNameFromLongName(String d) {
		String[] vf = d.split("/");
		return vf[vf.length - 1];
	}

	public static ArrayList<Pair<Point, Rect>> toRects(Point[] vf) {
		ArrayList<Pair<Point, Rect>> lst = new ArrayList<Pair<Point, Rect>>();

		for (int index = 0; index < vf.length - 1; index++) {
			Point point = vf[index];
			Point point2 = vf[index + 1];

			int left, right, top, bottom;

			Rect cur;
			if (point2.X > point.X) {
				left = point.X - 1;
				right = point2.X + 1;
			} else {
				left = point2.X - 1;
				right = point.X + 1;
			}
			if (point2.Y > point.Y) {
				top = point.Y - 1;
				bottom = point2.Y + 1;
			} else {
				top = point2.Y - 1;
				bottom = point.Y + 1;
			}

			cur = new Rect(left, top, right, bottom);

			lst.add(new Pair<Point, Rect>(point, cur));
		}
		return lst;
	}

	public static Pair<String, String>[] FixForChat(String uName, String string) {

		ArrayList<StringBuilder> sbs = new ArrayList<StringBuilder>();
		int count = 0;
		StringBuilder last;
		sbs.add(last = new StringBuilder());
		for (char cc : string.toCharArray()) {
			if (count > 35) {
				count = 0;
				sbs.add(last = new StringBuilder());
			}
			last.append(cc);
			count++;
		}
		ArrayList<Pair<String, String>> myarr = new ArrayList<Pair<String, String>>(sbs.size());

		count = 0;
		for (StringBuilder sb : sbs) {
			myarr.add(new Pair<String, String>(uName, sb.toString()));
			uName = "";
		}
		Pair<String, String>[] fb = (Pair<String, String>[]) Array.newInstance(Pair.class, sbs.size());

		myarr.toArray(fb);
		return fb;
	}

	public static String getNameFromShortName(String user) {
		String[] vf = user.split("@");
		return vf[0];
	}
}
