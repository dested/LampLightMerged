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
import com.LampGames.DrawView.DrawThread;
import com.LampLight.R;
import com.MessageParseJunk.DrawGameRoomMessage;
import com.MessageParseJunk.DrawGameRoomMessage.GameRoomMessageType;
import com.Service.Messager;
import com.Service.MultiRunner;

public class Draw extends Activity {

	private DrawThread mLunarThread;

	/** A handle to the View in which the game is running. */
	private DrawView mLunarView;
	private static final String TAG = "HelloFormStuffAct0ivity";

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
					case StartSquareGame:
						mLunarThread.StartGame();
						break;
					}
				}
			};
			mBoundService.JoinDrawGameRoom(RoomToJoin);

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
			mBoundService.LeaveDrawGameRoom();
			setResult(RESULT_OK);
			doUnbindService();

		}
	}

	InputMethodManager imm;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		if (imm == null)
			imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		setContentView(R.layout.drawlayout);

		// get handles to the LunarView from XML, and its LunarThread
		mLunarView = (DrawView) findViewById(R.id.draw);
		mLunarThread = mLunarView.getThread();
		final EditText tv;
		mLunarThread.setChatBox(tv = (EditText) findViewById(R.id.chatBox));

		tv.setOnKeyListener(new TextView.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					if (event.getAction() != KeyEvent.ACTION_DOWN)
						return true;
					imm.hideSoftInputFromWindow(tv.getWindowToken(), 0);
					for (Pair<String, String> m : Helping.FixForChat(GameInformation.UserName, tv.getText().toString())) {
						DrawGameRoomMessage vm = new DrawGameRoomMessage(GameRoomMessageType.Chat, m.first, m.second);
						try {
							mBoundService.drawGameRoom.sendMessage(vm.GenerateMessage());
						} catch (XMPPException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					tv.setText("");
					tv.setVisibility(View.INVISIBLE);

					return true;
				}
				return false;
			}
		});

		if (mLunarView.mVibrate == null)
			mLunarView.mVibrate = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		doBindService();
	}
}