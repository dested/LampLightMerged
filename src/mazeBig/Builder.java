package mazeBig;

import java.util.ArrayList;

public class Builder {
	private Walls[][] theWalls;
	public ArrayList<Point> Points;

	public Builder(Walls[][] walls) {
		theWalls = walls;
		NumHits = new boolean[walls.length][];
		for (int j = 0; j < walls.length; j++) {
			NumHits[j] = new boolean[walls.length];
		}
		NumHits[0][0] = true;
		Points = new ArrayList<Point>();
	}

	public enum Status {
		Good(), Bad(), Same();
		Status() {
		}
	}

	public Status AddPoint(Point p, Boolean wasBad) {

		if (p.X < 0 || p.X >= theWalls.length || p.Y < 0 || p.Y >= theWalls.length)
			return Status.Bad;

		Point pr;
		if (Points.size() > 0) {
			pr = Points.get(Points.size() - 1);
			if (pr.X == p.X && pr.Y == p.Y) {
				return Status.Same;
			}
		} else {
			Points.add(p);
			return Status.Good;
		}

		if (pr.X + 1 == p.X) {
			if (theWalls[p.X][p.Y].contains(WallStuff.East))
				return Status.Bad;
		} else if (pr.X - 1 == p.X) {
			if (theWalls[p.X][p.Y].contains(WallStuff.West))
				return Status.Bad;
		} else if (pr.Y + 1 == p.Y) {
			if (theWalls[p.X][p.Y].contains(WallStuff.North))
				return Status.Bad;
		} else if (pr.Y - 1 == p.Y) {
			if (theWalls[p.X][p.Y].contains(WallStuff.South))
				return Status.Bad;
		}

		if (Points.size() > 0 && wasBad) {
			return Points.contains(p) ? Status.Good : Status.Bad;
		}

		int inj = Points.size();
		if (inj > 3) {
			ArrayList<Point> vf = Points;
			if (vf.get(inj - 2).X == p.X && vf.get(inj - 2).Y == p.Y) {
				NumHits[vf.get(inj - 1).X][vf.get(inj - 1).Y] = !NumHits[vf.get(inj - 1).X][vf.get(inj - 1).Y];
			}
		}

		NumHits[p.X][p.Y] = !NumHits[p.X][p.Y];

		Points.add(p);
		return Status.Good;
	}

	public boolean[][] NumHits;

	public ArrayList<Point> Magnify(int blockSize, Point offset) {
		ArrayList<Point> ps = new ArrayList<Point>();
		for (Point point : Points) {

			ps.add(new Point((point.X * blockSize) + (blockSize / 2) + offset.X, (point.Y * blockSize) + (blockSize / 2) + offset.Y));

		}

		return ps;
	}

	public static ArrayList<Point> Magnify(ArrayList<Point> points, float blockSize, Point offset) {
		ArrayList<Point> ps = new ArrayList<Point>();
		for (Point point : points) {

			ps.add(new Point((int) (point.X * blockSize) + (int) (blockSize / 2) + offset.X, (int) (point.Y * blockSize) + (int) (blockSize / 2) + offset.Y));

		}

		return ps;
	}
}