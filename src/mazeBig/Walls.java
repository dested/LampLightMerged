package mazeBig;



public class Walls {
	public boolean South;
	public boolean North;
	public boolean East;
	public boolean West;

	public static Walls All() {
		Walls b = new Walls();
		b.South = true;
		b.North = true;
		b.East = true;
		b.West = true;
		return b;
	}

	public boolean StartingPosition() {
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
}