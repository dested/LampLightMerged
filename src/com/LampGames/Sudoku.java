package com.LampGames;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;

import com.LampGames.SudokuView.SudokuThread;
import com.LampLight.R;
import com.Service.Messager;
import com.Service.MultiRunner;

public class Sudoku extends Activity {

	private SudokuThread mLunarThread;

	/** A handle to the View in which the game is running. */
	private SudokuView mLunarView;
	private static final String TAG = "HelloFormStuffActivity";

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBoundService = ((MultiRunner.LocalBinder) service).getService();
			mLunarView.runner = mBoundService;
			mBoundService.Updater = new Messager() {
				@Override
				public void SendUpdate(MessagerType mt, String d) {
					switch (mt) {
					case StartSudokuGame:
						mLunarThread.StartGame();
						break;
					case FinishSudokuGame:
						finish();
						break;
					}
				}
			};
			mBoundService.JoinSudokuGameRoom(RoomToJoin);

		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBoundService = null;
		}
	};
	private MultiRunner mBoundService;
	boolean mIsBound;

	void doBindService() {
		// Establish a connection with the service. We use an explicit
		// class name because we want a specific service implementation that
		// we know will be running in our own process (and thus won't be
		// supporting component replacement by other applications).
		bindService(new Intent(this, MultiRunner.class), mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	void doUnbindService() {
		if (mIsBound) {
			// Detach our existing connection.
			unbindService(mConnection);
			mIsBound = false;
		}
	}

	public static String RoomToJoin;

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isFinishing()) {
			mBoundService.LeaveSudokuGameRoom();
			setResult(RESULT_OK);
			doUnbindService();

		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// tell system to use the layout defined in our XML file
		setContentView(R.layout.sudokulayout);

		// get handles to the LunarView from XML, and its LunarThread
		mLunarView = (SudokuView) findViewById(R.id.sudoku);
		mLunarThread = mLunarView.getThread();

		mLunarView.mVibrate = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		// mLunarThread.setState(GrameThread.STATE_INIT);

		doBindService();

	}

}