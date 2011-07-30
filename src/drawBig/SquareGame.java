package drawBig;

import java.util.ArrayList;

import mazeBig.Point;

import com.MessageParseJunk.SquarePlayer;

public class SquareGame {
	public ArrayList<SquarePlayer> PlayersInGame;
	public ArrayList<DrawWalls> Perimeter;
	public DColor StartingColor;
	public String Name;

	public SquarePlayer CurrentPlayer;

	public SquareGame(DColor startingColor) {
		PlayersInGame = new ArrayList<SquarePlayer>();
		Perimeter = new ArrayList<DrawWalls>();
		StartingColor = startingColor;
	}

	public Point MidPoint() {
		int fLeft = 200;
		int fRight = 0;

		int fTop = 200;
		int fBottom = 0;
		for (DrawWalls w : Perimeter) {
			int x = w.X;
			int y = w.Y;

			if (x < fLeft) {
				fLeft = x;
			}
			if (x > fRight) {
				fRight = x;
			}
			if (y < fTop) {
				fTop = y;
			}
			if (y > fBottom) {
				fBottom = y;
			}

		}

		return new Point(fLeft + (fRight - fLeft) / 2, fTop + (fBottom - fTop) / 2);

	}

	public Point CurPlayerPoint;
	public String CurPlayerName;
	public boolean ShowGameData;

	public void setCurrentPlayer(String currentPlayer2, Point updatePoint) {
		CurPlayerName = currentPlayer2;
		CurPlayerPoint = updatePoint;
	}
}