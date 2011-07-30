package com.MessageParseJunk;

import mazeBig.Point;
import sudokuBig.SudokuPoint;

public class SudokuGameRoomMessage {
	public enum GameRoomMessageType {
		SudokuData, SudokuMove, GameStarted, GameFinish, Leave
	}

	public SudokuPoint point;
	public int[][] SudokuData;
	public GameRoomMessageType Type;

	public static SudokuGameRoomMessage Parse(String st) {

		SudokuGameRoomMessage t = new SudokuGameRoomMessage();
		String[] d = st.split("\\|");
		int de = Integer.valueOf(d[0]);
		switch (de) {
		case 0:
			t.Type = GameRoomMessageType.SudokuData;
			t.SudokuData = parseSudokuData(d[1]);
			break;
		case 1:
			t.Type = GameRoomMessageType.SudokuMove;
			t.point = new SudokuPoint(Integer.valueOf(d[1]), new Point(Integer.parseInt(d[2]), Integer.parseInt(d[3])));
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
		}

		return t;

	}

	public String GenerateMessage() {
		String d = "";
		switch (Type) {
		case SudokuData:
			d = "0|" + makeSudokuData(SudokuData);
			break;
		case SudokuMove:
			d = "1|" + point.Index + "|" + point.Position.X + "|" + point.Position.Y;
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

		}

		return d;// + "|" + Argument;
	}

	private static int[][] parseSudokuData(String string) {
		int[][] fc = new int[9][];
		int cur = 0;
		int lCur = 0;
		int[] curItem = null;
		char[] cd = string.toCharArray();
		for (int i = 0; i < cd.length; i++) {

			if (cur == 0) {
				curItem = new int[9];
			}

			if (cd[i] == '-') {
				curItem[cur] = -1;
				i++;
			} else
				curItem[cur] = Integer.parseInt(Character.toString(cd[i]));
			cur++;
			if (cur == 9) {
				fc[lCur] = curItem;
				lCur++;
				cur = 0;
			}
		}
		return fc;
	}

	private static String makeSudokuData(int[][] walls) {
		StringBuilder sb = new StringBuilder();

		for (int x = 0; x < 9; x++) {
			for (int y = 0; y < 9; y++) {
				sb.append(walls[x][y]);

			}
		}
		return sb.toString();
	}

	public SudokuGameRoomMessage(GameRoomMessageType t, SudokuPoint p) {
		Type = t;
		point = p;
	}

	public SudokuGameRoomMessage(GameRoomMessageType t) {
		Type = t;
	}

	public SudokuGameRoomMessage(GameRoomMessageType t, int[][] p) {
		Type = t;
		SudokuData = p;
	}

	private SudokuGameRoomMessage() {
	}

}
