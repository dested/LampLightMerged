package com.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import mazeBig.ClientMaze;
import mazeBig.Point;
import mazeBig.WallStuff;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;

import sudokuBig.ClientSudoku;
import sudokuBig.SudokuBuilder;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Pair;
import android.widget.Toast;

import com.BigGamev1.FriendsPlaying;
import com.BigGamev1.GameInformation;
import com.Helper.Helping;
import com.MessageParseJunk.DrawGameRoomMessage;
import com.MessageParseJunk.GameRoomMessage;
import com.MessageParseJunk.SquarePlayer;
import com.MessageParseJunk.SudokuGameRoomMessage;
import com.MessageParseJunk.WaitingRoomMessage;
import com.Service.Messager.MessagerType;

import drawBig.ChatMessage;
import drawBig.ClientDraw;
import drawBig.DrawWalls;
import drawBig.SquareGame;

public class MultiRunner extends Service {
	private NotificationManager mNM;

	// Unique Identification Number for the Notification.
	// We use it on Notification start, and to cancel it.
	private int NOTIFICATION = 546874650;

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		public MultiRunner getService() {
			return MultiRunner.this;
		}
	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			if (Updater != null) {
				Updater.SendUpdate(((MessageUpdate) msg.obj).Status, ((MessageUpdate) msg.obj).StringToUpdate);
			}
		}
	};

	public class MessageUpdate {
		public MessagerType Status;
		public String StringToUpdate;

		public MessageUpdate(MessagerType mt, String st) {
			Status = mt;
			StringToUpdate = st;
		}
	}

	public XMPPConnection xmpp;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		// Cancel the persistent notification.
		mNM.cancel(NOTIFICATION);

		try {
			if (xmpp.isConnected()) {
				xmpp.disconnect(new Presence(Presence.Type.unavailable));
			}
			// Tell the user we stopped.
			Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();

		} catch (Exception gs) {
			Toast.makeText(this, "Disconnected failed", Toast.LENGTH_SHORT).show();

		} finally {
			// setResult(RESULT_OK);
		}

	}

	@Override
	public void onCreate() {
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		ConnectionConfiguration hara = new ConnectionConfiguration(GameInformation.IP, 5222, GameInformation.getXMPPInfo());
		SmackConfiguration.setPacketReplyTimeout(10000);

		hara.setCompressionEnabled(true);
		xmpp = new XMPPConnection(hara);

		Thread tr = new Thread() {
			public void update(String d) {
				android.os.Message m = new android.os.Message();
				m.obj = new MessageUpdate(MessagerType.PushStatusUpdate, d);
				mHandler.sendMessage(m);
			}

			public void setAllowLogin() {
				android.os.Message m = new android.os.Message();
				m.obj = new MessageUpdate(MessagerType.AllowLogin, "");
				mHandler.sendMessage(m);
			}

			public void run() {
				update("Connecting");
				try {
					if (!xmpp.isConnected())
						xmpp.connect();
					update("Connected.");
					setAllowLogin();
				} catch (XMPPException e) {
					update(e.toString());
				}

			}

		};
		tr.start();
	}

	public void Login(final String username, final String password) {
		if (!xmpp.isConnected())
			return;

		Thread t = new Thread() {

			public void update(String d) {

				android.os.Message m = new android.os.Message();
				m.obj = new MessageUpdate(MessagerType.PushStatusUpdate, d);
				mHandler.sendMessage(m);
			}

			public void setLogin() {
				android.os.Message m = new android.os.Message();
				m.obj = new MessageUpdate(MessagerType.Login, "");
				mHandler.sendMessage(m);
			}

			public void run() {
				try {

					if (xmpp.isAuthenticated()) {
						xmpp.disconnect(new Presence(Presence.Type.unavailable, "", 1, Presence.Mode.away));

						update("Connecting");
						xmpp.connect();
						update("Connected");
					}
					update("Logging In");
					xmpp.login(username, password);
					update("Logged In");
					setLogin();
				} catch (XMPPException e) {
					update(e.toString());

					e.printStackTrace();
				}
			}
		};
		t.start();

	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new LocalBinder();

	public Messager Updater;
	public MultiUserChat waitingRoom;
	public MultiUserChat drawGameRoom;
	public MultiUserChat sudokuRoom;
	public MultiUserChat mazeRoom;

	public ClientDraw squareGame;
	public ClientSudoku sudokuGame;
	public ClientMaze mazeGame;
	public ArrayList<Pair<String, String>> waitChat = new ArrayList<Pair<String, String>>();

	public void LeaveWaitingRoom() {
		try {

			waitChat.add(new Pair<String, String>("*", GameInformation.UserName + " Has Left"));
			if (xmpp != null && xmpp.isConnected() && waitingRoom.isJoined())
				waitingRoom.leave();

			if (waitingRoomPing != null)
				waitingRoomPing.cancel();

			waitingRoom.removeParticipantListener(waitPartList);
			waitPartList = null;
			waitingRoom.removeMessageListener(waitMessageList);
			waitMessageList = null;
			waitingRoom = null;
		} catch (Exception gs) {

		}
	}

	PacketListener waitPartList;
	PacketListener waitMessageList;
	Timer waitingRoomPing;

	public void JoinWaitingRoom(final int waitingRoomIndex) {

		final String nam = "waitingroom" + waitingRoomIndex + "@" + GameInformation.getXMPPInfo();
		new Thread(new Runnable() {
			private void update(WaitingRoomMessage rw) {
				android.os.Message m = new android.os.Message();
				m.obj = new MessageUpdate(MessagerType.WaitingRoomNewMessage, rw.GenerateMessage());
				mHandler.sendMessage(m);
			}

			private void UserJoined(String name) {
				android.os.Message m = new android.os.Message();
				m.obj = new MessageUpdate(MessagerType.WaitingRoomUserLoggedIn, name);
				mHandler.sendMessage(m);

			}

			private void UserLeft(String name) {
				android.os.Message m = new android.os.Message();
				m.obj = new MessageUpdate(MessagerType.WaitingRoomUserLoggedOut, name);
				mHandler.sendMessage(m);

			}

			public void run() {
				try {

					if (xmpp == null) {
						return;
					}

					if (waitingRoom == null) {
						waitingRoom = new MultiUserChat(xmpp, nam);

						waitingRoom.join(GameInformation.UserName);
					}

					waitingRoomPing = new Timer();
					waitingRoomPing.scheduleAtFixedRate(new TimerTask() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							try {
								waitingRoom.sendMessage(new WaitingRoomMessage(MessageType.Ping).GenerateMessage());
							} catch (XMPPException e) {
								e.printStackTrace();
							}

						}
					}, 100, 50 * 1000);

					boolean hasWatcher = false;
					for (Iterator<String> it = waitingRoom.getOccupants(); it.hasNext();) {
						String vf = it.next();
						if (vf.toLowerCase().endsWith("sudoker")) {
							hasWatcher = true;
							continue;

						}
						UserJoined(Helping.GetNameFromLongName(vf));
					}

					if (!hasWatcher) {
						GregorianCalendar gr = new GregorianCalendar();
						gr.setTime(new Date());
						waitChat.add(new Pair<String, String>("*", "Watcher is not available"));
					}
					if (waitPartList != null) {
						waitingRoom.removeParticipantListener(waitPartList);
					}
					waitingRoom.addParticipantListener(waitPartList = new PacketListener() {
						@Override
						public void processPacket(Packet arg0) {
							if (arg0.getFrom().toLowerCase().endsWith("sudoker")) {
								return;
							}
							Presence pre = (Presence) arg0;

							switch (pre.getType()) {
							case available:
								UserJoined(Helping.GetNameFromLongName(arg0.getFrom()));
								break;
							case unavailable:
								UserLeft(Helping.GetNameFromLongName(arg0.getFrom()));
								break;
							}
						}
					});
					if (waitMessageList != null) {
						waitingRoom.removeMessageListener(waitMessageList);
					}
					waitingRoom.addMessageListener(waitMessageList = new PacketListener() {

						@Override
						public void processPacket(Packet message) {
							String d = ((Message) message).getBody();
							// charsReiceved += d.length();
							update(WaitingRoomMessage.Parse(d));
						}
					});
				} catch (XMPPException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (Exception er) {

				}
			}

		}).start();

	}

	public String GetStatus() {
		// TODO Auto-generated method stub
		if (xmpp == null) {
			return "Waiting To Connect";
		}
		if (xmpp.isConnected()) {
			return "Connected";
		}
		return "Not Connected";
	}

	PacketListener drawMessageListener;
	PacketListener drawPartListener;
	public static int charsReiceved;

	public void JoinDrawGameRoom(final String roomToJoin) {

		new Thread(new Runnable() {

			private void startGame() {
				android.os.Message m = new android.os.Message();
				m.obj = new MessageUpdate(MessagerType.StartSquareGame, "");
				mHandler.sendMessage(m);
			}

			public void run() {
				if (xmpp == null)
					return;
				try {

					if (drawGameRoom == null) {
						drawGameRoom = new MultiUserChat(xmpp, roomToJoin + "@" + GameInformation.getXMPPInfo());
						drawGameRoom.join(GameInformation.UserName);
					} else {
						startGame();
					}

					if (drawMessageListener != null)
						drawGameRoom.removeMessageListener(drawMessageListener);
					drawGameRoom.addMessageListener(drawMessageListener = new PacketListener() {
						@Override
						public void processPacket(Packet message) {

							String d = ((Message) message).getBody();
							// charsReiceved += d.length();
							DrawGameRoomMessage gm = DrawGameRoomMessage.Parse(d);
							boolean first = true;
							switch (gm.Type) {
							case SendStartData:
								if (gm.WhoTo.endsWith(GameInformation.UserName)) {
									squareGame = new ClientDraw(gm.Blocks, gm.PlayerID);

									startGame();
									squareGame.RebuildDrawingInfo(true);
								}
								break;
							case CreateNewGame:

								SquareGame g = new SquareGame(gm.GameColor);
								g.Name = gm.GameName;

								for (SquarePlayer sp : squareGame.Players) {
									if (sp.Name.equals(GameInformation.UserName)) {
										g.PlayersInGame.add(sp);
										break;
									}
								}

								squareGame.Games.add(g);

								squareGame.RebuildDrawingInfo(true);
								break;

							case UpdatePlayersInfo:
								squareGame.Players = gm.PlayersInGame;
								if (first) {
									first = false;
									squareGame.RebuildDrawingInfo(false);
								}
								break;
							case Chat:

								Date df = new Date();
								GregorianCalendar gdc = new GregorianCalendar();
								gdc.setTime(df);
								gdc.add(Calendar.SECOND, 9);
								squareGame.ChatMessages.add(new ChatMessage(gm.ChatMessage, gdc));

								break;
							case UpdatePerimeter:

								SquareGame gdm = null;

								for (SquareGame fd : squareGame.Games) {
									if (fd.Name.equals(gm.GameName)) {
										gdm = fd;
										break;
									}
								}

								gdm.Perimeter.clear();
								for (DrawWalls prm : gm.Perimeter) {
									gdm.Perimeter.add(prm);
								}

								squareGame.RebuildDrawingInfo(true);
								break;
							case AskJoinGame:
								if (gm.WhoTo.endsWith(GameInformation.UserName)) {
									squareGame.TryJoinGameName = gm.GameName;
									squareGame.AddMessageBox("Would you like to join this game?", "Yes", "No");
								}
								break;

							case UpdateCurrentPlayer:

								for (SquareGame gam : squareGame.Games) {
									if (gm.GameName.equals(gam.Name)) {
										gam.setCurrentPlayer(gm.CurrentPlayer, gm.UpdatePoint);
									}

								}

								break;
							case UpdateGamePlayerInfo:

								for (SquareGame gam : squareGame.Games) {
									if (gm.GameName.equals(gam.Name)) {

										gam.PlayersInGame.clear();

										for (Integer ind : gm.PlayerIDs) {
											for (SquarePlayer pl : squareGame.Players) {
												if (pl.PlayerID == ind) {
													gam.PlayersInGame.add(pl);
												}
											}
										}

										gam.setCurrentPlayer(gm.CurrentPlayer, gm.UpdatePoint);
									}

								}

								break;
							case AppendLine:

								boolean bad = false;
								switch (gm.UpdateLine) {
								case North:
									bad = squareGame.theWalls[gm.UpdatePoint.X][gm.UpdatePoint.Y].North;
									break;
								case South:
									bad = squareGame.theWalls[gm.UpdatePoint.X][gm.UpdatePoint.Y].South;
									break;
								case East:
									bad = squareGame.theWalls[gm.UpdatePoint.X][gm.UpdatePoint.Y].East;
									break;
								case West:
									bad = squareGame.theWalls[gm.UpdatePoint.X][gm.UpdatePoint.Y].West;
									break;
								}
								if (bad) {

									return;
								}

								switch (gm.UpdateLine) {
								case North:
									squareGame.theWalls[gm.UpdatePoint.X][gm.UpdatePoint.Y].North = true;
									squareGame.theWalls[gm.UpdatePoint.X][gm.UpdatePoint.Y].NorthOwner = gm.PlayerID;
									break;
								case South:
									squareGame.theWalls[gm.UpdatePoint.X][gm.UpdatePoint.Y].South = true;
									squareGame.theWalls[gm.UpdatePoint.X][gm.UpdatePoint.Y].SouthOwner = gm.PlayerID;
									break;
								case East:
									squareGame.theWalls[gm.UpdatePoint.X][gm.UpdatePoint.Y].East = true;
									squareGame.theWalls[gm.UpdatePoint.X][gm.UpdatePoint.Y].EastOwner = gm.PlayerID;
									break;
								case West:
									squareGame.theWalls[gm.UpdatePoint.X][gm.UpdatePoint.Y].West = true;
									squareGame.theWalls[gm.UpdatePoint.X][gm.UpdatePoint.Y].WestOwner = gm.PlayerID;
									break;
								}

								WallStuff gc = getOpposite(gm.UpdateLine);
								int dx = gm.UpdatePoint.X + getDX(gm.UpdateLine),
								dy = gm.UpdatePoint.Y + getDY(gm.UpdateLine);

								switch (gc) {
								case North:
									if (!(dy < 0 || dy > DrawGameRoomMessage.FULLSIZE)) {
										squareGame.theWalls[dx][dy].North = true;
										squareGame.theWalls[dx][dy].NorthOwner = gm.PlayerID;
									}
									break;
								case South:
									if (!(dy < 0 || dy > DrawGameRoomMessage.FULLSIZE)) {
										squareGame.theWalls[dx][dy].South = true;
										squareGame.theWalls[dx][dy].SouthOwner = gm.PlayerID;
									}
									break;
								case East:
									if (!(dx < 0 || dx > DrawGameRoomMessage.FULLSIZE)) {
										squareGame.theWalls[dx][dy].East = true;
										squareGame.theWalls[dx][dy].EastOwner = gm.PlayerID;
									}
									break;
								case West:
									if (!(dx < 0 || dx > DrawGameRoomMessage.FULLSIZE)) {
										squareGame.theWalls[dx][dy].West = true;
										squareGame.theWalls[dx][dy].WestOwner = gm.PlayerID;
									}
									break;
								}

								if (squareGame.theWalls[gm.UpdatePoint.X][gm.UpdatePoint.Y].Full()) {
									squareGame.theWalls[gm.UpdatePoint.X][gm.UpdatePoint.Y].FullOwner = gm.PlayerID;
								}

								if (!(dx < 0 || dx > DrawGameRoomMessage.FULLSIZE) && !(dy < 0 || dy > DrawGameRoomMessage.FULLSIZE)) {
									if (squareGame.theWalls[dx][dy].Full()) {
										squareGame.theWalls[dx][dy].FullOwner = gm.PlayerID;
									}
								}
								// mLunarThread.addPointToFriend(friend,
								// gm.point);
								squareGame.RebuildDrawingInfo(true);
								break;
							}

						}
					});

				} catch (XMPPException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}).start();
	}

	private WallStuff getOpposite(WallStuff wallse) {
		switch (wallse) {
		case North:
			return WallStuff.South;
		case South:
			return WallStuff.North;
		case East:
			return WallStuff.West;
		case West:
			return WallStuff.East;
		}
		return WallStuff.East;// never hit
	}

	private int getDX(WallStuff wallse) {

		switch (wallse) {
		case North:
		case South:
			return 0;
		case East:
			return 1;
		case West:
			return -1;
		}
		return 0;
	}

	private int getDY(WallStuff wallse) {

		switch (wallse) {
		case North:
			return -1;
		case South:
			return 1;
		case East:
		case West:
			return 0;
		}
		return 0;
	}

	public void LeaveDrawGameRoom() {
		try {
			if (xmpp == null || !xmpp.isConnected()) {
				return;
			}
			if (drawGameRoom != null)
				drawGameRoom.leave();
			drawGameRoom = null;
			squareGame = null;
		} catch (Exception gs) {

		} finally {
		}

	}

	public void JoinSudokuGameRoom(final String roomToJoin) {

		sudokuGame = new ClientSudoku();
		new Thread(new Runnable() {
			private void startGame() {
				android.os.Message m = new android.os.Message();
				m.obj = new MessageUpdate(MessagerType.StartSudokuGame, "");
				mHandler.sendMessage(m);
			}

			private void finish() {
				android.os.Message m = new android.os.Message();
				m.obj = new MessageUpdate(MessagerType.FinishSudokuGame, "");
				mHandler.sendMessage(m);
			}

			public void run() {

				sudokuRoom = new MultiUserChat(xmpp, roomToJoin + "@" + GameInformation.getXMPPInfo());

				try {
					sudokuRoom.join(GameInformation.UserName);
					for (Iterator<String> it = sudokuRoom.getOccupants(); it.hasNext();) {
						String vf = it.next();
						if (vf.endsWith(GameInformation.UserName) || vf.toLowerCase().endsWith("sudoker")) {
							continue;
						}
						FriendsPlaying fp = new FriendsPlaying();
						fp.Name = vf;
						sudokuGame.friends.add(fp);
					}
				} catch (XMPPException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				sudokuRoom.addParticipantListener(sudokuPartListener = new PacketListener() {
					@Override
					public void processPacket(Packet arg0) {
						if (arg0.getFrom().endsWith(GameInformation.UserName) || arg0.getFrom().toLowerCase().endsWith("sudoker")) {
							return;
						}

						Presence pre = (Presence) arg0;

						switch (pre.getType()) {
						case available:

							for (FriendsPlaying f : sudokuGame.friends) {
								if (f.Name.equals(arg0.getFrom()))
									return;
							}
							FriendsPlaying fp = new FriendsPlaying();
							fp.Name = arg0.getFrom();
							sudokuGame.friends.add(fp);
							break;
						case unavailable:

							for (FriendsPlaying f : sudokuGame.friends) {
								if (f.Name.equals(arg0.getFrom())) {
									sudokuGame.friends.remove(f);
									break;
								}
							}

							break;
						}
					}
				});

				sudokuRoom.addMessageListener(sudokuMessageListener = new PacketListener() {
					@Override
					public void processPacket(Packet message) {

						String d = ((Message) message).getBody();

						SudokuGameRoomMessage gm = SudokuGameRoomMessage.Parse(d);

						switch (gm.Type) {
						case SudokuData:
							sudokuGame.theIndexes = gm.SudokuData;
							sudokuGame.SudokuBuilder = new SudokuBuilder(sudokuGame.theIndexes);

							break;
						case SudokuMove:
							for (FriendsPlaying friend : sudokuGame.friends) {
								if (friend.Name.equals(message.getFrom())) {

									friend.MovementPoints.add(gm.point);
								}
							}
							break;
						case GameStarted:
							sudokuGame.mazeVisible = true;
							startGame();

							break;
						case GameFinish:
							sudokuGame.mazeVisible = false;

							finish();
							break;

						}

					}
				});
			}

		}).start();

	}

	PacketListener sudokuMessageListener;
	PacketListener sudokuPartListener;
	ParticipantStatusListener sudokuPartStatusListener;

	public void LeaveSudokuGameRoom() {
		if (sudokuRoom != null)
			sudokuRoom.leave();

		if (sudokuRoom != null && sudokuMessageListener != null)
			sudokuRoom.removeMessageListener(sudokuMessageListener);
		if (sudokuRoom != null && sudokuPartListener != null)
			sudokuRoom.removeParticipantListener(sudokuPartListener);
		if (sudokuRoom != null && sudokuPartStatusListener != null)
			sudokuRoom.removeParticipantStatusListener(sudokuPartStatusListener);
		sudokuRoom = null;

	}

	PacketListener mazeMessageListener;
	PacketListener mazePartListener;
	ParticipantStatusListener mazePartStatusListener;

	public void JoinMazeGameRoom(final String roomToJoin) {
		mazeGame = new ClientMaze();
		new Thread(new Runnable() {
			private void startGame() {
				android.os.Message m = new android.os.Message();
				m.obj = new MessageUpdate(MessagerType.StartMazeGame, "");
				mHandler.sendMessage(m);
			}

			private void finish() {
				android.os.Message m = new android.os.Message();
				m.obj = new MessageUpdate(MessagerType.FinishMazeGame, "");
				mHandler.sendMessage(m);
			}

			public void run() {

				mazeRoom = new MultiUserChat(xmpp, roomToJoin + "@" + GameInformation.getXMPPInfo());

				try {
					mazeRoom.join(GameInformation.UserName);
					for (Iterator<String> it = mazeRoom.getOccupants(); it.hasNext();) {
						String vf = it.next();
						if (vf.toLowerCase().endsWith("sudoker")) {
							continue;
						}
						FriendsPlaying fp = new FriendsPlaying();
						fp.Name = vf;
						mazeGame.PlayersInWaitingRoom.add(fp);
					}
				} catch (XMPPException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				mazeRoom.addParticipantListener(mazePartListener = new PacketListener() {
					@Override
					public void processPacket(Packet arg0) {
						if (arg0.getFrom().toLowerCase().endsWith("sudoker")) {
							return;
						}

						Presence pre = (Presence) arg0;

						switch (pre.getType()) {
						case available:

							for (FriendsPlaying f : mazeGame.PlayersInWaitingRoom) {
								if (f.Name.equals(arg0.getFrom()))
									return;
							}
							FriendsPlaying fp = new FriendsPlaying();
							fp.Name = arg0.getFrom();
							mazeGame.PlayersInWaitingRoom.add(fp);
							break;
						case unavailable:

							for (FriendsPlaying f : mazeGame.PlayersInWaitingRoom) {
								if (f.Name.equals(arg0.getFrom())) {
									mazeGame.PlayersInWaitingRoom.remove(f);
									break;
								}
							}
							for (FriendsPlaying f : mazeGame.PlayersInGame) {
								if (f.Name.equals(arg0.getFrom())) {
									mazeGame.PlayersInGame.remove(f);
									break;
								}
							}

							break;
						}
					}
				});

				mazeRoom.addMessageListener(mazeMessageListener = new PacketListener() {
					@Override
					public void processPacket(Packet message) {

						String d = ((Message) message).getBody();

						GameRoomMessage gm = GameRoomMessage.Parse(d);

						switch (gm.Type) {
						case MazeData:
							mazeGame.Start(gm.MazeData, gm.MazeWidth);
							break;
						case MazeMove:
							for (FriendsPlaying friend : mazeGame.PlayersInGame) {
								if (friend.Name.equals(message.getFrom())) {
									friend.MovePoints.add(gm.point);
									break;
								}
							}
							break;
						case GameStarted:
							mazeGame.GameStartingIn = null;
							mazeGame.InWaitingRoom = false;
							for (FriendsPlaying pl : mazeGame.PlayersInWaitingRoom) {
								mazeGame.PlayersInGame.add(pl);
							}
							mazeGame.PlayersInWaitingRoom.clear();
							startGame();
							mazeGame.MazeBuilder.AddPoint(new Point(0, 0), false);
							break;
						case GameStarting:
							GregorianCalendar cal = new GregorianCalendar();
							cal.setTime(new Date());
							cal.add(Calendar.SECOND, gm.Seconds);
							mazeGame.GameStartingIn = cal;
							break;
						case GameFinish:
							mazeGame.InWaitingRoom = true;
							break;
						case ChatMessage:
							mazeGame.ChatMessages.add(gm.Message);
							break;
						}

					}
				});
			}

		}).start();

	}

	public void LeaveMazeGameRoom() {
		if (mazeRoom != null)
			mazeRoom.leave();

		if (mazeRoom != null && mazeMessageListener != null)
			mazeRoom.removeMessageListener(mazeMessageListener);
		if (mazeRoom != null && mazePartListener != null)
			mazeRoom.removeParticipantListener(mazePartListener);
		if (mazeRoom != null && mazePartStatusListener != null)
			mazeRoom.removeParticipantStatusListener(mazePartStatusListener);
		mazeRoom = null;
	}
}
