package com.LampLight;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.MultiUserChat;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import com.BigGamev1.GameInformation;
import com.LampLight.LampMessager.LampMessagerType;
import com.Service.MultiRunner;

public class LampService extends Service {
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			if (Updater != null) {
				Updater.SendUpdate((MessageUpdate) msg.obj);
			}
		}
	};
	public LampMessager Updater;

	public void LeaveGameRoom() {
		try {
			if (mBoundService.xmpp != null && mBoundService.xmpp.isConnected() && gameRoom.isJoined())
				gameRoom.leave();

			if (pingTimer != null)
				pingTimer.cancel();
			gameRoom.removeParticipantListener(gamePartList);
			gamePartList = null;
			gameRoom.removeMessageListener(gameMessageList);
			gameMessageList = null;
			gameRoom = null;
		} catch (Exception gs) {

		}
	}

	PacketListener gamePartList;
	PacketListener gameMessageList;

	public MultiUserChat gameRoom;
	Timer pingTimer;

	public void JoinGameRoom(final int gameRoomIndex, final Runnable runnable) {

		final String nam = "squaregame" + gameRoomIndex + "@" + GameInformation.getXMPPInfo();
		new Thread(new Runnable() {
			private void update(String to, String from, String mg) {
				android.os.Message m = new android.os.Message();
				m.obj = new MessageUpdate(LampMessagerType.NewMessage, to, from, mg);
				mHandler.sendMessage(m);
			}

			private void UserJoined(String name) {
				android.os.Message m = new android.os.Message();
				m.obj = new MessageUpdate(LampMessagerType.UserLoggedIn, name);
				mHandler.sendMessage(m);

			}

			private void UserLeft(String name) {
				android.os.Message m = new android.os.Message();
				m.obj = new MessageUpdate(LampMessagerType.UserLoggedOut, name);
				mHandler.sendMessage(m);

			}

			public void run() {
				try {

					if (mBoundService.xmpp == null) {
						return;
					}

					if (gameRoom == null) {
						gameRoom = new MultiUserChat(mBoundService.xmpp, nam);

						gameRoom.join(GameInformation.UserName);
					}

					pingTimer = new Timer();
					pingTimer.scheduleAtFixedRate(new TimerTask() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							try {
								gameRoom.sendMessage("---");
							} catch (XMPPException e) {
								e.printStackTrace();
							}

						}
					}, 100, 50 * 1000);

					boolean hasWatcher = false;
					for (Iterator<String> it = gameRoom.getOccupants(); it.hasNext();) {
						String vf = it.next();
						if (vf.toLowerCase().endsWith("squarewatcher")) {
							hasWatcher = true;
							continue;

						}
						if (vf.endsWith("/" + GameInformation.UserName)) {
							curLamp = new LampPlayer(vf);
						}
						UserJoined(vf);
					}

					if (!hasWatcher) {

					}
					if (gamePartList != null) {
						gameRoom.removeParticipantListener(gamePartList);
					}
					gameRoom.addParticipantListener(gamePartList = new PacketListener() {
						@Override
						public void processPacket(Packet arg0) {
							if (arg0.getFrom().toLowerCase().endsWith("squarewatcher")) {
								return;
							}
							Presence pre = (Presence) arg0;

							switch (pre.getType()) {
							case available:
								UserJoined(arg0.getFrom());
								break;
							case unavailable:
								UserLeft(arg0.getFrom());
								break;
							}
						}
					});
					if (gameMessageList != null) {
						gameRoom.removeMessageListener(gameMessageList);
					}
					gameRoom.addMessageListener(gameMessageList = new PacketListener() {

						@Override
						public void processPacket(Packet message) {
							Message mg = ((Message) message);
							String d = mg.getBody();
							if (d.equals("---"))
								return;
							// charsReiceved += d.length();
							String to = mg.getTo();
							if (mg.getTo().equals(mBoundService.xmpp.getUser())) {
								to = getLampPlayer().FullName;
							}

							update(to, mg.getFrom(), d);
						}
					});
					runnable.run();
				} catch (XMPPException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (Exception er) {

				}
			}

		}).start();

	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new LocalBinder();

	public class LocalBinder extends Binder {
		public LampService getService() {
			return LampService.this;
		}
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBoundService = ((MultiRunner.LocalBinder) service).getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBoundService = null;
		}
	};

	MultiRunner mBoundService;
	boolean mIsBound;
	public StateObject StateObject;

	public LampPlayer getLampPlayer() {
		return curLamp;
	}

	private LampPlayer curLamp;

	void doBindService() {
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
	public int onStartCommand(Intent intent, int flags, int startId) {
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	private NotificationManager mNM;
	private int NOTIFICATION = 546174650;

	@Override
	public void onDestroy() {
		// Cancel the persistent notification.
		mNM.cancel(NOTIFICATION);

		Toast.makeText(this, "Game Left", Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onCreate() {
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		doBindService();
	}

}
