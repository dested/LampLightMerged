package mazeBig;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import com.BigGamev1.FriendsPlaying;

public class ClientMaze {
	public int MazeSize;
	public Walls[][] theWalls;
	public Builder MazeBuilder;
	public boolean InWaitingRoom = true;
	public ArrayList<FriendsPlaying> PlayersInGame = new ArrayList<FriendsPlaying>();
	public ArrayList<FriendsPlaying> PlayersInWaitingRoom = new ArrayList<FriendsPlaying>();
	public ArrayList<String> ChatMessages = new ArrayList<String>();
	public GregorianCalendar GameStartingIn = null;

	public ClientMaze(Walls[][] wz, int mazeSize) {
		MazeSize = mazeSize;
		theWalls = wz;
		MazeBuilder = new Builder(theWalls);
	}

	public ClientMaze() {
	}

	public void initMaze() {
		MazeBuilder.AddPoint(new Point(0, 0), true);
	}

	public void Start(Walls[][] wz, int mazeSize) {
		MazeSize = mazeSize;
		theWalls = wz;
		MazeBuilder = new Builder(theWalls);
	}

}