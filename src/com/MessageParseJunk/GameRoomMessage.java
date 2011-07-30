package com.MessageParseJunk;

import mazeBig.Point;
import mazeBig.Walls;

public class GameRoomMessage {
	public enum GameRoomMessageType {
		MazeData, MazeMove, GameStarted, GameFinish, Leave, ChatMessage, GameStarting
	}

	public Point point;
	public int MazeWidth;
	public Walls[][] MazeData;
	public GameRoomMessageType Type;
	public String Message;
	public int Seconds;

	public static GameRoomMessage Parse(String st) {

		GameRoomMessage t = new GameRoomMessage();
		String[] d = st.split("\\|");
		int de = Integer.valueOf(d[0]);
		switch (de) {
		case 0:
			t.Type = GameRoomMessageType.MazeData;
			t.MazeWidth = Integer.parseInt(d[1]);
			t.MazeData = parseMazeData(d[2], t.MazeWidth);
			break;
		case 1:
			t.Type = GameRoomMessageType.MazeMove;
			t.point = new Point(Integer.parseInt(d[1]), Integer.parseInt(d[2]));
			break;
		case 2:
			t.Type = GameRoomMessageType.GameStarted;
			break;
		case 3:
			t.Type = GameRoomMessageType.GameFinish;
			break;
		case 4:
			t.Type = GameRoomMessageType.Leave;
			break;
		case 5:
			t.Type = GameRoomMessageType.ChatMessage;
			t.Message = d[1];

			break;
		case 6:
			t.Type = GameRoomMessageType.GameStarting;
			t.Seconds = Integer.parseInt(d[1]);
			break;
		}

		return t;

	}

	public String GenerateMessage() {
		String d = "";
		switch (Type) {
		case MazeData:
			d = "0|" + makeMazeData(MazeData, MazeWidth);
			break;
		case MazeMove:
			d = "1|" + point.X + "|" + point.Y;
			break;
		case GameStarted:
			d = "2|";
			break;
		case GameFinish:
			d = "3|";
			break;
		case Leave:
			d = "4|";
			break;
		case ChatMessage:
			d = "5|" + Message;
			break;
		case GameStarting:
			d = "6|" + Seconds;
			break;

		}

		return d;// + "|" + Argument;
	}

	private static Walls[][] parseMazeData(String string, int width) {
		Walls[][] fc = new Walls[width][];
		int cur = 0;
		int lCur = 0;
		Walls[] curItem = null;
		char[] cd = string.toCharArray();
		for (int i = 0; i < cd.length; i++) {

			if (cur == 0) {
				curItem = new Walls[width];
			}
			Walls wal = new Walls();

			if (cd[i++] != '0')
				wal.East = true;
			if (cd[i++] != '0')
				wal.West = true;
			if (cd[i++] != '0')
				wal.North = true;
			if (cd[i] != '0')
				wal.South = true;

			curItem[cur] = wal;
			cur++;
			if (cur == width) {
				fc[lCur] = curItem;
				lCur++;
				cur = 0;
			}
		}
		return fc;
	}

	private static String makeMazeData(Walls[][] walls, int width) {
		StringBuilder sb = new StringBuilder();

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < width; y++) {
				if (walls[x][y].East)
					sb.append(1);
				else
					sb.append(0);
				if (walls[x][y].West)
					sb.append(2);
				else
					sb.append(0);
				if (walls[x][y].North)
					sb.append(3);
				else
					sb.append(0);
				if (walls[x][y].South)
					sb.append(4);
				else
					sb.append(0);

			}
		}
		return width + "|" + sb.toString();
	}

	public GameRoomMessage(GameRoomMessageType t, Point p) {
		Type = t;
		point = p;
	}

	public GameRoomMessage(GameRoomMessageType t) {
		Type = t;
	}

	public GameRoomMessage(GameRoomMessageType t, String message) {
		Type = t;
		Message = message;
	}

	public GameRoomMessage(GameRoomMessageType t, int seconds) {
		Type = t;
		Seconds = seconds;
	}

	public GameRoomMessage(GameRoomMessageType t, Walls[][] p, int mazeSize) {
		Type = t;
		MazeData = p;
		MazeWidth = mazeSize;
	}

	private GameRoomMessage() {
	}

}
