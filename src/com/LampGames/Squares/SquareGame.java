package com.LampGames.Squares;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import mazeBig.Point;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.LampGames.Squares.SquareGameMessage.SquareGameMessageType;
import com.LampGames.Squares.SquareGameNetworkMessage.SquareGameNetworkMessageType;
import com.LampLight.LampPlayer;
import com.LampLight.LampView;

public class SquareGame extends LampView {

	public SquareGame(Context context, AttributeSet attrs) {
		super(context, attrs);
		myDrawer = new SquareDrawer();
	}

	@Override
	public void onConnectionEstablished() {
		setStateObject(new SquareGameState());
	}

	@Override
	public boolean onTouchDown(MotionEvent event) {
		PumpMessage(new SquareGameMessage(SquareGameMessageType.AddBluePoint, getMyPlayer(), new Point((int) event.getX(), (int) event.getY())));
		SendNetworkMessage(null, new SquareGameNetworkMessage(SquareGameNetworkMessageType.AddPoint, new Point((int) event.getX(), (int) event.getY())));
		return true;
	}

	@Override
	public boolean onTouchMoved(MotionEvent event) {
		PumpMessage(new SquareGameMessage(SquareGameMessageType.AddBluePoint, getMyPlayer(), new Point((int) event.getX(), (int) event.getY())));
		SendNetworkMessage(null, new SquareGameNetworkMessage(SquareGameNetworkMessageType.AddPoint, new Point((int) event.getX(), (int) event.getY())));
		return true;
	}

	@Override
	public boolean onTouchUp(MotionEvent event) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void RecieveNetworkMessage(LampPlayer whoTo, LampPlayer whoFrom, Object content) {
		SquareGameNetworkMessage sn = (SquareGameNetworkMessage) content;
		switch (sn.Type) {
		case AddPoint:
			PumpMessage(new SquareGameMessage(SquareGameMessageType.AddPoint, whoFrom, sn.Point));
			break;
		}
	}

	class SquareDrawer extends LampDrawer {
		SquareDrawer() {
			Paint p = Bucket.AddPaint("linePaint");
			p.setARGB(255, 255, 14, 156);

			p = Bucket.AddPaint("blu");
			p.setARGB(255, 0, 0, 255);
		}

		@Override
		public boolean onEngineTick() {

			return true;
		}

		HashMap<LampPlayer, ArrayList<Point>> PRZ = new HashMap<LampPlayer, ArrayList<Point>>();
		ArrayList<Point> blu = new ArrayList<Point>();

		@Override
		public void onDrawing(Canvas canvas) {
			canvas.drawColor(Color.argb(255, 19, 210, 59));
			Set<Entry<LampPlayer, ArrayList<Point>>> c = PRZ.entrySet();
			// obtain an Iterator for Collection
			Iterator<Entry<LampPlayer, ArrayList<Point>>> itr = c.iterator();

			if (blu.size() > 0) {
				Point last = blu.get(0);
				for (Point pt : blu) {
					canvas.drawLine(last.X, last.Y, pt.X, pt.Y, Bucket.GetPaint("blu"));
					last = pt;
				}
			}
			int yz = 25;
			while (itr.hasNext()) {
				Entry<LampPlayer, ArrayList<Point>> vals = itr.next();
				canvas.drawText(vals.getKey().Name, 15, yz, Bucket.GetPaint("linePaint"));
				yz += 30;
				if (vals.getValue().size() > 0) {
					Point last = vals.getValue().get(0);
					for (Point pn : vals.getValue()) {
						canvas.drawLine(last.X, last.Y, pn.X, pn.Y, Bucket.GetPaint("linePaint"));
						last = pn;
					}
				}
			}
		}

		@Override
		public void RecieveMessage(Object content) {
			SquareGameMessage msg = (SquareGameMessage) content;

			switch (msg.Type) {
			case AddPoint:
				PRZ.get(msg.LampPlayer).add(msg.Point);
				break;
			case AddBluePoint:
				blu.add(msg.Point);
				break;
			case AddUser:
				PRZ.put(msg.LampPlayer, new ArrayList<Point>());
				break;
			case RemoveUser:
				PRZ.remove(msg.LampPlayer);
				break;
			}

		}

		@Override
		public void onResize(int width, int height) {
		}
	}

	@Override
	public void onUserLogin(LampPlayer lampPlayer) {
		PumpMessage(new SquareGameMessage(SquareGameMessageType.AddUser, lampPlayer));
	}

	@Override
	public void onUserLogout(LampPlayer lampPlayer) {
		PumpMessage(new SquareGameMessage(SquareGameMessageType.RemoveUser, lampPlayer));
	}

}