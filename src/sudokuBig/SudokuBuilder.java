package sudokuBig;

import java.util.ArrayList;

import mazeBig.Point;

public class SudokuBuilder {
	private int[][] theWalls;
	public ArrayList<SudokuPoint> Points;

	public SudokuBuilder(int[][] walls) {
		theWalls = walls;
		Points = new ArrayList<SudokuPoint>();
	}

	public enum Status {
		Good(), Bad(), Same();
		Status() {
		}
	}

	public Status AddPoint(SudokuPoint p) {

		for (SudokuPoint pm : Points) {
			if (pm.Position.X == p.Position.X && pm.Position.Y == p.Position.Y) {
				Points.remove(pm);
				break;
			}
		}

		Points.add(p);
		return Status.Good;
	}

	public ArrayList<SudokuPoint> Magnify(int blockSize, Point offset) {
		ArrayList<SudokuPoint> ps = new ArrayList<SudokuPoint>();
		for (SudokuPoint point : Points) {
			ps.add(new SudokuPoint(point.Index, new Point((point.Position.X * blockSize) + (blockSize / 2) + offset.X, (point.Position.Y * blockSize) + (blockSize / 2) + offset.Y)));
		}
		return ps;
	}

	public static ArrayList<SudokuPoint> Magnify(ArrayList<SudokuPoint> points, float blockSize, Point offset) {
		ArrayList<SudokuPoint> ps = new ArrayList<SudokuPoint>();
		for (SudokuPoint point : points) {
			ps.add(new SudokuPoint(point.Index, new Point((int) (point.Position.X * blockSize) + (int) (blockSize / 2) + offset.X, (int) (point.Position.Y * blockSize)
					+ (int) (blockSize / 2) + offset.Y)));
		}
		return ps;
	}

	public boolean Contains(Integer index) {
		for (SudokuPoint jj : Points) {
			if (jj.Index == index) {
				return true;
			}
		}

		return false;
	}

	public boolean Contains(Integer index, int i, int a) {
		if (index.equals(-1))
			return false;
		for (SudokuPoint jj : Points) {
			if (jj.Index == index && jj.Position.X == i && jj.Position.Y == a) {
				return true;
			}
		}

		return false;
	}
}