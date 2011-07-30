package drawBig;

import java.util.ArrayList;

import mazeBig.Point;
import mazeBig.WallStuff;
import android.graphics.Paint;
import android.util.Pair;

import com.Helper.MessageBox;
import com.MessageParseJunk.SquarePlayer;

public class ClientDraw {

	public ArrayList<ChatMessage> ChatMessages;
	public DrawWalls[][] theWalls;
	public ArrayList<SquarePlayer> Players;
	public Point CurrentViewPoint;
	public ArrayList<SquareGame> Games;
	public int PlayerID;

	public ArrayList<MessageBox> Messages;
	public DrawingInfo[][] Drawer;
	public String TryJoinGameName;

	public void AddMessageBox(String message, String left, String right) {
		MessageBox box;
		Messages.add(box = new MessageBox());
		box.x = 160;
		box.y = 111;
		box.width = 190;
		box.height = 100;
		box.Message = message;
		box.Left = left;
		box.Right = right;

	}

	public ClientDraw(DrawWalls[][] wz, int playerIndex) {
		theWalls = wz;
		PlayerID = playerIndex;
		CurrentViewPoint = new Point(40, 40);
		Players = new ArrayList<SquarePlayer>();
		Games = new ArrayList<SquareGame>();
		ChatMessages = new ArrayList<ChatMessage>();
		Messages = new ArrayList<MessageBox>();
	}

	public void initMaze() {

	}

	public SquarePlayer GetPlayerByID(int fullOwner) {
		for (SquarePlayer sp : Players) {

			if (sp.PlayerID == fullOwner) {
				return sp;
			}
		}
		return null;
	}

	public boolean Redraw = false;

	public void RebuildDrawingInfo(boolean redraw) {
		Redraw = redraw;
		DrawingInfo[][] drawer = new DrawingInfo[theWalls.length][theWalls.length];

		for (int y = 0; y < theWalls.length; y++) {
			for (int x = 0; x < theWalls.length; x++) {
				DrawWalls d = theWalls[x][y];

				Paint n = null, s = null, e = null, w = null, f = null;
				DColor c;
				if (d.East) {
					e = PaintBuilder.GetFromColor(GetPlayerByID(d.EastOwner).Color);
				}
				if (d.West) {
					w = PaintBuilder.GetFromColor(GetPlayerByID(d.WestOwner).Color);
				}
				if (d.North) {
					n = PaintBuilder.GetFromColor(GetPlayerByID(d.NorthOwner).Color);
				}
				if (d.South) {
					s = PaintBuilder.GetFromColor(GetPlayerByID(d.SouthOwner).Color);
				}
				if (d.FullOwner > 0) {
					DColor fc = GetPlayerByID(d.FullOwner).Color;

					f = PaintBuilder.GetFromColor(new DColor(fc.A - 120, fc.R, fc.G, fc.B));
				}
				drawer[x][y] = new DrawingInfo(e, w, n, s, f);
			}
		}

		for (SquareGame game : Games) {
			Paint scp = PaintBuilder.GetFromColor(new DColor(game.StartingColor.A - 130, game.StartingColor.R, game.StartingColor.G, game.StartingColor.B));

			for (DrawWalls wall : game.Perimeter) {

				if (wall.contains(WallStuff.West) && drawer[wall.X][wall.Y].WestPaint == null) {
					drawer[wall.X][wall.Y].WestPaint = scp;
				}
				if (wall.contains(WallStuff.East) && drawer[wall.X][wall.Y].EastPaint == null) {
					drawer[wall.X][wall.Y].EastPaint = scp;
				}
				if (wall.contains(WallStuff.North) && drawer[wall.X][wall.Y].NorthPaint == null) {
					drawer[wall.X][wall.Y].NorthPaint = scp;
				}
				if (wall.contains(WallStuff.South) && drawer[wall.X][wall.Y].SouthPaint == null) {
					drawer[wall.X][wall.Y].SouthPaint = scp;
				}

			}
		}
		Drawer = drawer;
	}

	public static class PaintBuilder {
		static ArrayList<Pair<String, Paint>> pfs = new ArrayList<Pair<String, Paint>>();

		public static Paint GetFromColor(DColor col) {
			String fd = col.toString();

			for (Pair<String, Paint> df : pfs) {
				if (df.first.equals(fd)) {
					return df.second;
				}
			}
			Paint p = new Paint();
			p.setARGB(col.A, col.R, col.G, col.B);
			pfs.add(new Pair<String, Paint>(fd, p));
			return p;
		}
	}

	public class DrawingInfo {
		public Paint EastPaint;
		public Paint WestPaint;
		public Paint NorthPaint;
		public Paint SouthPaint;
		public Paint FullPaint;

		public DrawingInfo(Paint e, Paint w, Paint n, Paint s, Paint f) {
			EastPaint = e;
			WestPaint = w;
			NorthPaint = n;
			SouthPaint = s;
			FullPaint = f;
		}
	}

}