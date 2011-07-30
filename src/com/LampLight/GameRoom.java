package com.LampLight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jivesoftware.smack.XMPPException;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.BigGamev1.GameInformation;
import com.BigGamev1.Horiz;
import com.LampGames.Draw;
import com.LampGames.Grame;
import com.LampGames.Sudoku;
import com.MessageParseJunk.WaitingRoomMessage;
import com.Service.MessageType;
import com.Service.Messager;
import com.Service.MultiRunner;

public class GameRoom extends Activity {
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBoundService = ((MultiRunner.LocalBinder) service).getService();

			assignUpdater();

			mBoundService.JoinWaitingRoom(WaitingRoomIndex);
			for (Pair<String, String> vm : mBoundService.waitChat) {
				SendMessage(vm.first, vm.second);
			}

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
				case WaitingRoomUserLoggedOut:
					removeUserFromDisplay(d);
					break;
				case WaitingRoomUserLoggedIn:
					addUserToDisplay(d);
					break;
				case WaitingRoomNewMessage:
					doThings(WaitingRoomMessage.Parse(d));
					break;
				}
			}
		};
	}

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
			mBoundService.LeaveWaitingRoom();
			setResult(RESULT_OK);
			doUnbindService();
		}

	}

	static int WaitingRoomIndex;

	// UI Widgets
	ListView chatList;
	ListView chatUserList;
	SimpleAdapter chatAdapter;
	SimpleAdapter chatUsersAdapter;
	Button chatSendButton;
	EditText chatInput;

	ArrayList<HashMap<String, Object>> chatListItems = new ArrayList<HashMap<String, Object>>();
	ArrayList<HashMap<String, Object>> chatUserListItems = new ArrayList<HashMap<String, Object>>();

	final Runnable mUpdateFriendsList = new Runnable() {
		public void run() {
			chatUsersAdapter.notifyDataSetChanged();
		}
	};

	Button mazeSend;
	Button sudokuSend;
	Button lampSend;
	Button drawSend;

	Horiz horzView;

	InputMethodManager imm;

	GridView grid_main;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		int display_mode = getResources().getConfiguration().orientation;

		if (display_mode == 1) {
			setContentView(R.layout.gameroom_layoutport);
		} else {
			setContentView(R.layout.gameroom_layout);
		}

		if (imm == null)
			imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		loadGames();

		horzView = (Horiz) findViewById(R.id.horzView);

		grid_main = (GridView) findViewById(R.id.GridView01);
		grid_main.setAdapter(new ImageAdapter(this));
		grid_main.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

			}
		});

		chatAdapter = new SimpleAdapter(this, chatListItems, R.layout.chat_list_item, new String[] { "chatAuthor", "chatText" }, new int[] { R.id.chatAuthor, R.id.chatText });
		chatUsersAdapter = new SimpleAdapter(this, chatUserListItems, R.layout.chat_user_list_item, new String[] { "chatUser" }, new int[] { R.id.chatUser });

		chatList = (ListView) findViewById(R.id.chatList);
		chatList.setTextFilterEnabled(true);
		chatList.setAdapter(chatAdapter);

		chatUserList = (ListView) findViewById(R.id.chatUserList);
		chatUserList.setTextFilterEnabled(true);
		chatUserList.setAdapter(chatUsersAdapter);

		chatInput = (EditText) findViewById(R.id.chatInput);

		chatInput.setOnKeyListener(new TextView.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					if (event.getAction() != KeyEvent.ACTION_DOWN)
						return true;
					WaitingRoomMessage vm = new WaitingRoomMessage(MessageType.Chat, GameInformation.UserName, chatInput.getText().toString());
					try {
						mBoundService.waitingRoom.sendMessage(vm.GenerateMessage());
						imm.hideSoftInputFromWindow(chatInput.getWindowToken(), 0);
					} catch (XMPPException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					chatInput.setText("");

					return true;
				}
				return false;
			}
		});

		chatSendButton = (Button) findViewById(R.id.chatSend);
		chatSendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {

				WaitingRoomMessage vm = new WaitingRoomMessage(MessageType.Chat, GameInformation.UserName, chatInput.getText().toString());
				try {
					mBoundService.waitingRoom.sendMessage(vm.GenerateMessage());
					imm.hideSoftInputFromWindow(chatSendButton.getWindowToken(), 0);
				} catch (XMPPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				chatInput.setText("");
			}
		});

		doBindService();
	}

	private void loadGames() {
		List<String> gswede = getInstalledComponentList();
		System.out.print(gswede);

	}

	private List<String> getInstalledComponentList() {
		List<ApplicationInfo> packages = getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
		List<String> componentList = new ArrayList<String>();
		for (ApplicationInfo ri : packages) {
			if (ri.packageName != null) {
				componentList.add(ri.packageName + " " + ri.name);// +
																	// ri.activityInfo.name);
				Log.d("123", "Found installed: " + componentList.get(componentList.size() - 1));
			}
		}
		return componentList;
	}

	public class ImageAdapter extends BaseAdapter {
		Context mContext;
		public static final int ACTIVITY_CREATE = 10;

		public ImageAdapter(Context c) {
			mContext = c;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 29;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View v;
			if (convertView == null) {
				LayoutInflater li = getLayoutInflater();
				v = li.inflate(R.layout.icon, null);
				TextView tv = (TextView) v.findViewById(R.id.icon_text);
				tv.setText("Profile " + position);
				ImageView iv = (ImageView) v.findViewById(R.id.icon_image);
				iv.setImageResource(R.drawable.anicon);

			} else {
				v = convertView;
			}
			return v;
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}
	}

	private void SendMessage(String author, String st) {

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("chatAuthor", author);
		map.put("chatText", st);
		chatListItems.add(map);
		chatAdapter.notifyDataSetChanged();
		chatList.setSelection(chatList.getCount() - 1);
	}

	final Handler mHandler = new Handler();

	private void addUserToDisplay(String username) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("chatUser", username);
		chatUserListItems.add(map);
		mHandler.post(mUpdateFriendsList);
	}

	private boolean removeUserFromDisplay(String username) {
		for (HashMap<String, Object> user : chatUserListItems) {
			if (user.get("chatUser").toString().equalsIgnoreCase(username)) {
				chatUserListItems.remove(user);
				chatUsersAdapter.notifyDataSetChanged();
				return true;
			}
		}

		return false;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		horzView.scrollTo(201, 0);
		if (mBoundService != null) {
			chatUserListItems.clear();
			chatUsersAdapter.notifyDataSetChanged();

			assignUpdater();
			mBoundService.JoinWaitingRoom(WaitingRoomIndex);
		}
	}

	private void doThings(WaitingRoomMessage lastMessage) {
		switch (lastMessage.Type) {
		case Chat:
			// write the entire lastMessage.Argument
			mBoundService.waitChat.add(new Pair<String, String>(lastMessage.Argument, lastMessage.Argument2));
			SendMessage(lastMessage.Argument, lastMessage.Argument2);
			break;
		case JoinSudokuRoom:
			if (GameInformation.UserName.equals(lastMessage.Argument)) {
				// join new activity, argument will be the roomname
				Sudoku.RoomToJoin = lastMessage.Argument2;
				Intent myIntent = new Intent();
				myIntent.setClassName("com.BigGamev1", "com.BigGamev1.Sudoku");
				SendMessage("*", GameInformation.UserName + " Has Left");

				startActivityForResult(myIntent, 0);

				mBoundService.LeaveWaitingRoom();
				sudokuSend.setClickable(true);
				// SendMessage(StartingRoom.xmpp.getUser().split("@")[0],chatInput.getText().toString());
				sudokuSend.setText("Ready To Sudoku!");
			}

			break;
		case JoinMazeRoom:
			if (GameInformation.UserName.equals(lastMessage.Argument)) {
				// join new activity, argument will be the roomname
				Grame.RoomToJoin = lastMessage.Argument2;
				Intent myIntent = new Intent();
				myIntent.setClassName("com.BigGamev1", "com.BigGamev1.Grame");
				SendMessage("*", GameInformation.UserName + " Has Left");

				startActivityForResult(myIntent, 0);

				mBoundService.LeaveWaitingRoom();
				mazeSend.setClickable(true);
				// SendMessage(StartingRoom.xmpp.getUser().split("@")[0],chatInput.getText().toString());
				mazeSend.setText("Ready To Maze!");
			}

			break;

		case JoinDrawRoom:
			if (GameInformation.UserName.equals(lastMessage.Argument)) {
				// join new activity, argument will be the roomname
				Draw.RoomToJoin = lastMessage.Argument2;
				Intent myIntent = new Intent();
				myIntent.setClassName("com.BigGamev1", "com.BigGamev1.Draw");
				SendMessage("*", GameInformation.UserName + " Has Left");

				startActivityForResult(myIntent, 0);

				mBoundService.LeaveWaitingRoom();

				drawSend.setClickable(true);
				// SendMessage(StartingRoom.xmpp.getUser().split("@")[0],chatInput.getText().toString());
				drawSend.setText("Ready To Draw!!");

			}

			break;
		}
	}
}
