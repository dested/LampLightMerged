package com.LampGames;

import org.jivesoftware.smack.XMPPException;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.BigGamev1.GameInformation;
import com.Helper.Helping;
import com.LampGames.GrameView.GrameThread;
import com.LampLight.R;
import com.MessageParseJunk.GameRoomMessage;
import com.MessageParseJunk.GameRoomMessage.GameRoomMessageType;
import com.Service.Messager;
import com.Service.MultiRunner;

public class Grame extends Activity {

	/** A handle to the thread that's actually running the animation. */
	private GrameThread mLunarThread;
	/** A handle to the View in which the game is running. */
	private GrameView mLunarView;
	private static final String TAG = "HelloFormStuffActivity";
	public static String RoomToJoin;
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBoundService = ((MultiRunner.LocalBinder) service).getService();
			mLunarView.runner = mBoundService;
			mBoundService.Updater = new Messager() {
				@Override
				public void SendUpdate(MessagerType mt, String d) {
					switch (mt) {
					case StartMazeGame:
						mLunarThread.StartGame();
						break;
					case FinishMazeGame:
						finish();
						break;
					}
				}
			};
			mBoundService.JoinMazeGameRoom(RoomToJoin);

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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isFinishing()) {
			imm = null;
			doUnbindService();
			mBoundService.LeaveMazeGameRoom();
			setResult(RESULT_OK);
		}
	}

	EditText chatBox;

	InputMethodManager imm;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// tell system to use the layout defined in our XML file
		setContentView(R.layout.lunar_layout);

		// get handles to the LunarView from XML, and its LunarThread
		mLunarView = (GrameView) findViewById(R.id.grame);
		mLunarThread = mLunarView.getThread();

		if (imm == null)
			imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		mLunarThread.setChatBox(chatBox = (EditText) findViewById(R.id.chatBox));

		chatBox.setOnKeyListener(new TextView.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					if (event.getAction() != KeyEvent.ACTION_DOWN)
						return true;
					imm.hideSoftInputFromWindow(chatBox.getWindowToken(), 0);
					for (Pair<String, String> m : Helping.FixForChat(GameInformation.UserName, chatBox.getText().toString())) {
						GameRoomMessage vm = new GameRoomMessage(GameRoomMessageType.ChatMessage, m.first + ": " + m.second);
						try {
							mBoundService.mazeRoom.sendMessage(vm.GenerateMessage());
						} catch (XMPPException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					chatBox.setText("");

					return true;
				}
				return false;
			}
		});

		mLunarView.mVibrate = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		// mLunarThread.setState(GrameThread.STATE_INIT);

		doBindService();
	}

}