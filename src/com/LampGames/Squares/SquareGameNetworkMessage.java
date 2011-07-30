package com.LampGames.Squares;

import java.io.Serializable;

import mazeBig.Point;

public class SquareGameNetworkMessage implements Serializable {
	public enum SquareGameNetworkMessageType {
		AddPoint;
	}

	private static final long serialVersionUID = -667031454430706527L;
	public SquareGameNetworkMessageType Type;
	public Point Point;

	public SquareGameNetworkMessage(SquareGameNetworkMessageType t) {
		Type = t;
	}

	public SquareGameNetworkMessage(SquareGameNetworkMessageType t, Point p) {
		Type = t;
		Point = p;

	}

	public SquareGameNetworkMessage() {
	}

}
