package com.LampGames.Squares;

import java.io.Serializable;

import mazeBig.Point;

import com.LampLight.LampPlayer;

public class SquareGameMessage implements Serializable {
	public enum SquareGameMessageType {
		AddPoint, AddUser, RemoveUser, AddBluePoint;
	}

	private static final long serialVersionUID = -6670314524430706527L;
	public SquareGameMessageType Type;
	public Point Point;
	public LampPlayer LampPlayer;

	public SquareGameMessage(SquareGameMessageType t) {
		Type = t;
	}

	public SquareGameMessage() {
	}

	public SquareGameMessage(SquareGameMessageType t, LampPlayer pl, Point p) {
		Type = t;
		Point = p;
		LampPlayer = pl;

	}

	public SquareGameMessage(SquareGameMessageType t, LampPlayer pl) {
		Type = t;
		LampPlayer = pl;

	}
}
