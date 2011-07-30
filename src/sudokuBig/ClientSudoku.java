package sudokuBig;

import java.util.ArrayList;

import com.BigGamev1.FriendsPlaying;
import com.Helper.MessageBox;

public class ClientSudoku {

	public int[][] theIndexes;
	public SudokuBuilder SudokuBuilder;
	public ArrayList<FriendsPlaying> friends = new ArrayList<FriendsPlaying>();
	public boolean mazeVisible;
	public ArrayList<MessageBox> Messages;

	public ClientSudoku(int[][] wz) {
		theIndexes = wz;
		SudokuBuilder = new SudokuBuilder(theIndexes);
		Messages = new ArrayList<MessageBox>();
	}

	public void AddMessageBox(String message, String left, String right) {
		MessageBox box;
		Messages.add(box = new MessageBox());
		box.x = 160;
		box.y = 111;
		box.width = 160;
		box.height = 100;
		box.Message = message;
		box.Left = left;
		box.Right = right;

	}

	public ClientSudoku() {
	}

	public void initMaze() {

	}

}