package com.MessageParseJunk;

import java.util.ArrayList;

import mazeBig.Point;
import mazeBig.WallStuff;
import drawBig.DColor;
import drawBig.DrawWalls;
import drawBig.SquareGame;

public class DrawGameRoomMessage {
	public enum GameRoomMessageType {
		Ping, SendStartData, AppendLine, Leave, UpdatePerimeter, CreateNewGame, Chat, UpdatePlayersInfo, AskJoinGame, JoinGame, AppendLineServer, UpdateCurrentPlayer, UpdateGamePlayerInfo
	}

	public int PlayerID = 0;

	public static int FULLSIZE = 100;
	public GameRoomMessageType Type;
	public String ChatMessage;
	public DrawWalls[][] Blocks;

	public Point UpdatePoint;
	public String WhoTo;
	public WallStuff UpdateLine;

	String drawData;
	public ArrayList<DrawWalls> Perimeter;
	public String GameName;

	public DColor GameColor;
	public ArrayList<SquarePlayer> PlayersInGame;

	public String CurrentPlayer;

	public ArrayList<Integer> PlayerIDs;

	public static DrawGameRoomMessage Parse(String st) {

		DrawGameRoomMessage t = new DrawGameRoomMessage();
		String[] d = st.split("\\|");
		int de = Integer.valueOf(d[0]);
		switch (de) {
		case 0:
			t.Type = GameRoomMessageType.Ping;

			break;
		case 1:
			t.Type = GameRoomMessageType.SendStartData;
			t.WhoTo = d[1];// 2 is the size (1000)
			t.PlayerID = Integer.parseInt(d[2]);
			t.Blocks = parseMazeData(d[4]);
			break;
		case 2:
			t.Type = GameRoomMessageType.AppendLine;

			switch (Integer.parseInt(d[1])) {

			case 0:
				t.UpdateLine = WallStuff.East;
				break;
			case 1:
				t.UpdateLine = WallStuff.West;
				break;
			case 2:
				t.UpdateLine = WallStuff.North;
				break;
			case 3:
				t.UpdateLine = WallStuff.South;
				break;
			}

			t.UpdatePoint = new Point(Integer.parseInt(d[2]), Integer.parseInt(d[3]));
			t.PlayerID = Integer.parseInt(d[4]);
			break;
		case 3:
			t.Type = GameRoomMessageType.Leave;
			break;
		case 4:
			t.Type = GameRoomMessageType.UpdatePerimeter;
			t.Perimeter = new ArrayList<DrawWalls>();
			t.GameName = d[1];
			String[] dc = d[2].split("\\.");
			for (int g = 0; g < dc.length; g++) {
				DrawWalls c;
				t.Perimeter.add(c = new DrawWalls());

				String[] mo = dc[g].split("-");
				c.X = Integer.parseInt(mo[0]);
				c.Y = Integer.parseInt(mo[1]);

				int f = mo[2].toCharArray()[0] - 'a';

				if ((f & E) == E) {
					c.East = true;
				}
				if ((f & W) == W) {
					c.West = true;
				}
				if ((f & N) == N) {
					c.North = true;
				}
				if ((f & S) == S) {
					c.South = true;
				}

			}

			break;

		case 5:
			t.Type = GameRoomMessageType.CreateNewGame;
			t.GameColor = DColor.Parse(d[1]);
			t.GameName = (d[2]);

			break;

		case 6:
			t.Type = GameRoomMessageType.Chat;
			t.ChatMessage = d[1];

			break;
		case 7:
			t.Type = GameRoomMessageType.UpdatePlayersInfo;
			t.PlayersInGame = new ArrayList<SquarePlayer>();
			for (int i = 1; i < d.length; i++) {
				String[] ffc = d[i].split("\\^");
				SquarePlayer x = new SquarePlayer(ffc[1], DColor.Parse(ffc[3]), Integer.parseInt(ffc[4]));
				x.Active = Boolean.parseBoolean(ffc[0]);
				x.Score = Integer.parseInt(ffc[2]);
				t.PlayersInGame.add(x);
			}
			break;
		case 8:
			t.Type = GameRoomMessageType.AskJoinGame;
			t.WhoTo = d[2];
			t.GameName = d[1];
			break;
		case 9:
			t.Type = GameRoomMessageType.JoinGame;
			t.WhoTo = d[2];
			t.GameName = d[1];
			break;
		case 10:
			t.Type = GameRoomMessageType.AppendLineServer;

			switch (Integer.parseInt(d[1])) {

			case 0:
				t.UpdateLine = WallStuff.East;
				break;
			case 1:
				t.UpdateLine = WallStuff.West;
				break;
			case 2:
				t.UpdateLine = WallStuff.North;
				break;
			case 3:
				t.UpdateLine = WallStuff.South;
				break;
			}

			t.UpdatePoint = new Point(Integer.parseInt(d[2]), Integer.parseInt(d[3]));
			t.PlayerID = Integer.parseInt(d[4]);
			break;

		case 11:
			t.Type = GameRoomMessageType.UpdateCurrentPlayer;
			t.CurrentPlayer = d[1];
			t.UpdatePoint = new Point(Integer.parseInt(d[2]), Integer.parseInt(d[3]));
			t.GameName = d[4];
			break;

		case 12:
			t.Type = GameRoomMessageType.UpdateGamePlayerInfo;
			t.GameName = d[1];
			t.PlayerIDs = new ArrayList<Integer>();
			for (int i = 2; i < d.length; i++) {
				t.PlayerIDs.add(Integer.parseInt(d[i]));
			}
			break;
		}

		return t;

	}

	private static DrawWalls[][] parseMazeData(String string) {
		int width = FULLSIZE;
		DrawWalls[][] fc = new DrawWalls[width][];
		int cur = 0;
		int lCur = 0;
		DrawWalls[] curItem = null;

		byte N = 1;
		byte S = 2;
		byte E = 4;
		byte W = 8;

		char[] cx = string.toCharArray();

		for (int a = 0; a < cx.length; a++) {
			int f = cx[a] - 'a';

			if (cur == 0) {
				curItem = new DrawWalls[width];
			}
			DrawWalls wal = new DrawWalls();
			if ((f & E) == E) {
				wal.East = true;
				a++;
				int fdc = (int) (cx[a] - 'a');
				wal.EastOwner = fdc;

			}
			if ((f & W) == W) {
				wal.West = true;
				a++;
				int fdc = (int) (cx[a] - 'a');
				wal.WestOwner = fdc;
			}
			if ((f & N) == N) {
				wal.North = true;
				a++;

				int fdc = (int) (cx[a] - 'a');
				wal.NorthOwner = fdc;

			}
			if ((f & S) == S) {
				wal.South = true;
				a++;
				int fdc = (int) (cx[a] - 'a');
				wal.SouthOwner = fdc;
			}
			a++;

			int fdc = (int) (cx[a] - 'a');
			wal.FullOwner = fdc;

			curItem[cur] = wal;
			cur++;
			if (cur == width) {
				cur = 0;
				fc[lCur] = curItem;
				lCur++;
			}
		}
		return fc;
	}

	static byte N = 1;
	static byte S = 2;
	static byte E = 4;
	static byte W = 8;

	private static String makeMazeData(DrawWalls[][] walls) {
		StringBuilder sb = new StringBuilder();
		int width = FULLSIZE;

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < width; y++) {
				int jf = 0;

				StringBuilder cl = new StringBuilder();

				if (walls[x][y].East) {
					jf = jf | E;
					cl.append((char) (walls[x][y].EastOwner + 'a'));
				}
				if (walls[x][y].West) {
					jf = jf | W;
					cl.append((char) (walls[x][y].WestOwner + 'a'));
				}
				if (walls[x][y].North) {
					jf = jf | N;
					cl.append((char) (walls[x][y].NorthOwner + 'a'));
				}
				if (walls[x][y].South) {
					jf = jf | S;
					cl.append((char) (walls[x][y].SouthOwner + 'a'));
				}
				cl.append((char) (walls[x][y].FullOwner + 'a'));
				sb.append((char) (jf + 'a') + cl.toString());
			}
		}
		return width + "|" + sb.toString();
	}

	public String GenerateMessage() {
		String d = "";
		switch (Type) {
		case Ping:
			d = "0|";
			break;
		case SendStartData:
			d = "1|" + WhoTo + "|" + PlayerID + "|" + makeMazeData(Blocks);
			break;
		case AppendLine:
			String g = "";

			switch (UpdateLine) {

			case East:
				g = "0";
				break;
			case West:
				g = "1";
				break;
			case North:
				g = "2";
				break;
			case South:
				g = "3";
				break;
			}

			d = "2|" + g + "|" + UpdatePoint.X + "|" + UpdatePoint.Y + "|" + PlayerID;
			break;
		case Leave:
			d = "3|";
			break;

		case UpdatePerimeter:
			StringBuilder sb = new StringBuilder();
			for (DrawWalls dc : Perimeter) {
				if (!dc.Empty()) {
					sb.append(dc.X + "-" + dc.Y + "-");
					int jf = 0;

					if (dc.East)
						jf = jf | E;
					if (dc.West)
						jf = jf | W;
					if (dc.North)
						jf = jf | N;
					if (dc.South)
						jf = jf | S;
					sb.append(((char) (jf + 'a')) + ".");

				}
			}

			d = "4|" + GameName + "|" + sb.toString();
			break;

		case CreateNewGame:
			d = "5|" + GameColor.toString() + "|" + GameName;
			break;
		case Chat:
			d = "6|" + ChatMessage;
			break;
		case UpdatePlayersInfo:
			d = "7|";
			StringBuilder sbc = new StringBuilder();

			for (SquarePlayer sp : PlayersInGame) {
				sbc.append(sp.Active + "^" + sp.FullName + "^" + sp.Score + "^" + sp.Color.toString() + "^" + sp.PlayerID + "|");
			}
			d += sbc.toString();
			break;
		case AskJoinGame:
			d = "8|";
			d += GameName + "|" + WhoTo;
			break;
		case JoinGame:
			d = "9|";
			d += GameName;
			d += "|" + WhoTo;
			break;
		case AppendLineServer:
			String g1 = "";

			switch (UpdateLine) {

			case East:
				g1 = "0";
				break;
			case West:
				g1 = "1";
				break;
			case North:
				g1 = "2";
				break;
			case South:
				g1 = "3";
				break;
			}

			d = "10|" + g1 + "|" + UpdatePoint.X + "|" + UpdatePoint.Y + "|" + PlayerID;
			break;
		case UpdateCurrentPlayer:
			d = "11|";
			d += CurrentPlayer;
			d += "|" + UpdatePoint.X + "|" + UpdatePoint.Y;
			d += "|" + GameName;
			break;

		case UpdateGamePlayerInfo:
			d = "12|";
			d += GameName + "|";

			for (Integer p : PlayerIDs) {
				d += p + "|";
			}

			break;
		}

		return d;
	}

	public DrawGameRoomMessage(GameRoomMessageType t, Point p, WallStuff f, int playerinde) {
		Type = t;
		UpdateLine = f;
		UpdatePoint = p;
		PlayerID = playerinde;
	}

	public DrawGameRoomMessage(GameRoomMessageType t, boolean b, String gamename, String whoTo) {
		Type = t;
		GameName = gamename;
		WhoTo = whoTo;
	}

	public DrawGameRoomMessage(GameRoomMessageType t, String whoto, DrawWalls[][] itm, int playerin) {
		Type = t;
		WhoTo = whoto;
		Blocks = itm;
		PlayerID = playerin;
	}

	public DrawGameRoomMessage(GameRoomMessageType t) {
		Type = t;
	}

	public DrawGameRoomMessage(GameRoomMessageType t, DColor c, String name) {
		Type = t;
		GameColor = c;
		GameName = name;
	}

	public DrawGameRoomMessage(GameRoomMessageType t, String n, String n2) {
		Type = t;
		ChatMessage = n + " : " + n2;
	}

	private DrawGameRoomMessage() {
	}

	public DrawGameRoomMessage(GameRoomMessageType t, DrawWalls[][] perimeter, String gameName) {
		Type = t;
		Perimeter = new ArrayList<DrawWalls>();

		for (DrawWalls[] g : perimeter) {
			for (DrawWalls s : g) {
				if (s != null)
					Perimeter.add(s);
			}
		}
		GameName = gameName;
	}

	public DrawGameRoomMessage(GameRoomMessageType updateplayersinfo, ArrayList<SquarePlayer> playersInGame) {
		Type = updateplayersinfo;
		PlayersInGame = playersInGame;
	}

	public DrawGameRoomMessage(GameRoomMessageType updateplayersinfo, String gamename, ArrayList<Integer> pIds) {
		Type = updateplayersinfo;
		PlayerIDs = pIds;
		GameName = gamename;
	}

	public DrawGameRoomMessage(GameRoomMessageType updatecurrentplayer, SquareGame exists) {
		Type = updatecurrentplayer;
		CurrentPlayer = exists.CurrentPlayer.FullName;
		UpdatePoint = exists.MidPoint();
		GameName = exists.Name;

	}

}
