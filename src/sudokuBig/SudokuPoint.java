package sudokuBig;

import mazeBig.Point;

public class SudokuPoint {
	public Integer Index;
	public Point Position;

	public SudokuPoint(Integer d, Point de) {
		Position = de;
		Index = d;
	}
}