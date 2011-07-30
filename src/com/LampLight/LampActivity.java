package com.LampLight;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.view.inputmethod.InputMethodManager;

import com.Helper.WSHelper;
import com.LampLight.LampView.LampThread;

public class LampActivity extends Activity {

	private LampThread mLampThread;

	/** A handle to the View in which the game is running. */
	private LampView mLampView;
	private static final String TAG = "HelloFormStuffAct0ivity";

	public static String RoomToJoin;
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBoundService = ((LampService.LocalBinder) service).getService();
			mLampView.runner = mBoundService;
			mBoundService.Updater = new LampMessager() {
				@Override
				public void SendUpdate(MessageUpdate mu) {
					switch (mu.Status) {
					case NewMessage:
						mLampView.RecieveNetworkMessage(new LampPlayer(mu.To), new LampPlayer(mu.From), WSHelper.SToO(mu.StringToUpdate));
						break;
					case UserLoggedIn:
						mLampView.onUserLogin(new LampPlayer(mu.StringToUpdate));
						break;
					case UserLoggedOut:
						mLampView.onUserLogout(new LampPlayer(mu.StringToUpdate));
						break;
					}

				}
			};
			mBoundService.JoinGameRoom(1, new Runnable() {

				@Override
				public void run() {
					mLampView.onConnectionEstablished();

				}
			});
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBoundService = null;
		}
	};
	private LampService mBoundService;
	boolean mIsBound;

	void doBindService() {
		bindService(new Intent(this, LampService.class), mConnection, Context.BIND_AUTO_CREATE);
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

		Bundle extras = getIntent().getExtras();

		setContentView(extras.getInt("ID"));

		// get handles to the LunarView from XML, and its LunarThread
		mLampView = (LampView) findViewById(R.id.lamp);
		mLampThread = mLampView.getThread();

		if (mLampView.mVibrate == null)
			mLampView.mVibrate = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		doBindService();
	}
}