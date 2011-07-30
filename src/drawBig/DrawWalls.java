package drawBig;

import mazeBig.WallStuff;

public class DrawWalls {
	public boolean South;
	public boolean North;
	public boolean East;
	public boolean West;
	public int X;
	public int Y;
	public int FullOwner;
	public int NorthOwner;
	public int SouthOwner;
	public int EastOwner;
	public int WestOwner;

	public DrawWalls(int x, int y) {
		X = x;
		Y = y;
	}

	public DrawWalls() {
	}

	public DrawWalls(int x, int y, boolean south, boolean north, boolean west, boolean east) {
		X = x;
		Y = y;
		West = west;
		North = north;
		South = south;
		East = east;
	}

	public boolean Full() {
		return South && North && East && West;
	}

	public void remove(WallStuff direction) {
		switch (direction) {
		case North:
			North = false;
			break;
		case South:
			South = false;
			break;
		case East:
			East = false;
			break;
		case West:
			West = false;
			break;

		}

	}

	public boolean contains(WallStuff direction) {
		switch (direction) {
		case North:
			return North;
		case South:
			return South;
		case East:
			return East;
		case West:
			return West;
		}
		return false;
	}

	public void reset() {
		East = false;
		South = false;
		North = false;
		West = false;

	}

	public boolean Empty() {
		return !North && !South && !East && !West;
	}
}