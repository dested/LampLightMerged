package com.LampLight;

import org.jivesoftware.smack.XMPPException;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.BigGamev1.GameInformation;
import com.Service.Messager;
import com.Service.MultiRunner;

public class StartingRoom extends Activity {
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBoundService = ((MultiRunner.LocalBinder) service).getService();

			mStatusText.setText(mBoundService.GetStatus());
			assignUpdater();

		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBoundService = null;
		}
	};

	private void assignUpdater() {

		mBoundService.Updater = new Messager() {

			@Override
			public void SendUpdate(MessagerType mt, String d) {
				switch (mt) {
				case PushStatusUpdate:
					mStatusText.setText(d);
					break;
				case Login:
					startItUp();
					break;
				case AllowLogin:
					chatSendButton.setClickable(true);
					break;

				}

			}

		};
	}

	boolean setClickable = false;
	TextView mStatusText;

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isFinishing()) {
			// Debug.stopMethodTracing();
			doUnbindService();
		}
	}

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

	Button chatSendButton;

	private MultiRunner mBoundService;

	Thread tr;
	EditText uIn;

	InputMethodManager imm;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Debug.startMethodTracing("myapp");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startingroom);

		if (imm == null)
			imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		final EditText createName = (EditText) findViewById(R.id.createUsername);

		final EditText createPassword = (EditText) findViewById(R.id.createPassword);
		uIn = (EditText) findViewById(R.id.userName);
		chatSendButton = (Button) findViewById(R.id.button);
		final Button crat = (Button) findViewById(R.id.createButton);
		mStatusText = (TextView) findViewById(R.id.text);
		mStatusText.setText("Waiting To Join");

		uIn.setOnKeyListener(new TextView.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					if (event.getAction() != KeyEvent.ACTION_DOWN)
						return true;
					imm.hideSoftInputFromWindow(uIn.getWindowToken(), 0);
					startGameUp();
					return true;
				}
				return false;
			}
		});
		chatSendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				startGameUp();
				imm.hideSoftInputFromWindow(chatSendButton.getWindowToken(), 0);

			}

		});

		crat.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String userName = createName.getText().toString();
				String passWord = createPassword.getText().toString();
				imm.hideSoftInputFromWindow(crat.getWindowToken(), 0);
				try {
					mBoundService.xmpp.getAccountManager().createAccount(userName, passWord);
				} catch (XMPPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mStatusText.setText("User Created");
				uIn.setText(userName);

			}
		});
		doBindService();
	}

	private void startGameUp() {
		GameInformation.UserName = uIn.getText().toString();

		mBoundService.Login(GameInformation.UserName, "d");

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		try {
			assignUpdater();
			mStatusText.setText(mBoundService.GetStatus());
			chatSendButton.setClickable(true);
		} catch (Exception e) {
		}
	}

	private void startItUp() {
		GameRoom.WaitingRoomIndex = 1;// randomly generated

		Intent myIntent = new Intent();
		myIntent.setClassName("com.LampLight", "com.LampLight.GameRoom");
		startActivityForResult(myIntent, 0);
	}

}