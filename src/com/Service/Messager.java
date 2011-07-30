package com.Service;

public abstract class Messager {
	public enum MessagerType {
		PushStatusUpdate, AllowLogin, Login, WaitingRoomUserLoggedOut, WaitingRoomUserLoggedIn, WaitingRoomNewMessage, StartSquareGame, StartSudokuGame, FinishSudokuGame, StartMazeGame, FinishMazeGame
	}

	public abstract void SendUpdate(MessagerType mt, String d);
}
