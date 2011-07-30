package com.LampGames;

import java.util.ArrayList;

import mazeBig.Builder;
import mazeBig.Builder.Status;
import mazeBig.Point;
import mazeBig.Rectangle;
import mazeBig.WallStuff;

import org.jivesoftware.smack.XMPPException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.EditText;
import android.widget.TextView;

import com.BigGamev1.FriendsPlaying;
import com.Helper.Helping;
import com.LampLight.R;
import com.MessageParseJunk.GameRoomMessage;
import com.MessageParseJunk.GameRoomMessage.GameRoomMessageType;
import com.Service.MultiRunner;

import drawBig.ChatMessage;

/**
 * View that draws, takes keystrokes, etc. for a simple LunarLander game.
 * 
 * Has a mode which RUNNING, PAUSED, etc. Has a x, y, dx, dy, ... capturing the
 * current ship physics. All x/y etc. are measured with (0,0) at the lower left.
 * updatePhysics() advances the physics based on realtime. draw() renders the
 * ship, and does an invalidate() to prompt another draw() as soon as possible
 * by the system.
 */
class GrameView extends SurfaceView implements SurfaceHolder.Callback {

	Point lastMous = new Point(-1, -1);
	public Vibrator mVibrate;
	int secondID = -100;
	int mainAreaID = -100;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Dump touch event to log

		int be = event.getActionMasked();
		switch (be) {

		case MotionEvent.ACTION_DOWN:
			// mStatusText.setVisibility(View.VISIBLE);
			// mStatusText.setText("doing Mouse");

			if (thread.friendsBox.Collides((int) event.getX(), (int) event.getY())) {
				doingFriendsBox = true;
				mainAreaID = event.getPointerId(0);
				lastMous = new Point((int) event.getX(), (int) event.getY());
			} else if (thread.scrollingBox.Collides((int) event.getX(), (int) event.getY())) {

				if (currentMouseMove == MouseMoveState.Dragging) {
					secondID = -1;
					currentMouseMove = MouseMoveState.Mazing;
					thread.mScrollingBoxPaint.setARGB(255, 255, 0, 50);
				} else {
					currentMouseMove = MouseMoveState.Dragging;
					secondID = event.getPointerId(0);
					thread.mScrollingBoxPaint.setARGB(255, 11, 123, 45);
				}
			} else if (thread.resizeBox.Collides((int) event.getX(), (int) event.getY())) {

				if (currentMouseMove == MouseMoveState.Resize) {
					secondID = -1;
					currentMouseMove = MouseMoveState.Mazing;
					thread.mResizeBoxPaint.setARGB(255, 0, 115, 150);
				} else {
					currentMouseMove = MouseMoveState.Resize;
					secondID = event.getPointerId(0);
					thread.mResizeBoxPaint.setARGB(255, 11, 123, 45);
				}
			} else {

				mainAreaID = event.getPointerId(0);
				lastMous = new Point((int) event.getX(), (int) event.getY());

				startMouse = new Point((int) event.getX(), (int) event.getY());

				if (currentMouseMove == MouseMoveState.Mazing) {
					thread.SetDPadInfo(true, 50, startMouse);
					poinst = startMouse;
					clickRunnable = new Runnable() {

						public void run() {
							switch (currentMouseMove) {

							case Mazing:
								int waitBeforeAdd = 1000;

								Point pd = Point.Difference(startMouse, poinst);

								int dn = 50 + ((int) Math.sqrt(pd.X * pd.X + pd.Y * pd.Y) * 3);
								if (dn > 55 && dn < 180) {
									waitBeforeAdd = 300;
								} else if (dn >= 180 && dn < 250) {
									waitBeforeAdd = 150;
								} else if (dn >= 250) {
									waitBeforeAdd = 150;
								} else {
									handler.postDelayed(this, 35);
									return;
								}

								boolean b = false;
								switch (getDirection(pd)) {
								case North:
									b = addMazePoint(new Point(thread.CurrentMazePoint.X, thread.CurrentMazePoint.Y - 1));
									break;
								case South:
									b = addMazePoint(new Point(thread.CurrentMazePoint.X, thread.CurrentMazePoint.Y + 1));
									break;
								case West:
									b = addMazePoint(new Point(thread.CurrentMazePoint.X - 1, thread.CurrentMazePoint.Y));
									break;
								case East:
									b = addMazePoint(new Point(thread.CurrentMazePoint.X + 1, thread.CurrentMazePoint.Y));
									break;
								}

								if (b == false) {
									startMouse.X = poinst.X;
									startMouse.Y = poinst.Y;
									thread.SetDPadInfo(false, dn, startMouse);
								} else {
									thread.SetDPadInfo(true, dn, startMouse);
								}

								handler.postDelayed(this, waitBeforeAdd);

								break;
							}
						}
					};

					clickRunnable.run();

				}
			}

			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			doingFriendsBox = false;
			// currentMouseMove = MouseMoveState.Mazing;
			if (secondID == event.getPointerId(0)) {
				// thread.mResizeBoxPaint.setARGB(255, 0, 115, 150);
				// thread.mScrollingBoxPaint.setARGB(255, 255, 0, 50);
				secondID = -100;
			} else {
				mainAreaID = -100;

				startMouse = new Point(0, 0);
			}
			if (clickRunnable != null) {

				handler.removeCallbacks(clickRunnable);
				clickRunnable = null;
			}

			thread.SetDPadInfo(false, 127, startMouse);
			// mStatusText.setVisibility(View.INVISIBLE);
			break;
		case MotionEvent.ACTION_MOVE:
			if ((event.getPointerId(0) == mainAreaID)) {

				Point bm = new Point((int) event.getX(), (int) event.getY());
				addMouseLine(lastMous, bm);
				lastMous = bm;
			} else if ((event.getPointerId(0) == secondID)) {

			}
			break;
		}

		return true; // indicate event was handled
	}

	Point startMouse;

	public enum MouseMoveState {
		Dragging, Resize, Mazing
	}

	Runnable clickRunnable;
	MouseMoveState currentMouseMove = MouseMoveState.Mazing;
	boolean doingFriendsBox = false;
	Point poinst;

	private void addMouseLine(Point lastMous2, final Point point) {
		Point p = Point.Difference(point, lastMous2);

		switch (currentMouseMove) {
		case Dragging:
			thread.CombineMazePos(p.Negative());
			break;
		case Resize:
			thread.SetBlockSize(p.Y);
			break;
		case Mazing:
			if (doingFriendsBox) {
				thread.SetFriendsLoc(p.Y);
			} else {

				poinst = point;
				int waitBeforeAdd = 1000;

				Point pd = Point.Difference(startMouse, point);

				int dn = 50 + ((int) Math.sqrt(pd.X * pd.X + pd.Y * pd.Y) * 3);
				if (dn > 55 && dn < 180) {
					return;
				} else if (dn >= 180 && dn < 250) {
					waitBeforeAdd = 250;
				} else if (dn >= 250) {
					waitBeforeAdd = 150;
				} else
					return;

				if (clickRunnable != null) {

					handler.removeCallbacks(clickRunnable);
				}

				boolean b = false;
				switch (getDirection(pd)) {
				case North:
					b = addMazePoint(new Point(thread.CurrentMazePoint.X, thread.CurrentMazePoint.Y - 1));
					break;
				case South:
					b = addMazePoint(new Point(thread.CurrentMazePoint.X, thread.CurrentMazePoint.Y + 1));
					break;
				case West:
					b = addMazePoint(new Point(thread.CurrentMazePoint.X - 1, thread.CurrentMazePoint.Y));
					break;
				case East:
					b = addMazePoint(new Point(thread.CurrentMazePoint.X + 1, thread.CurrentMazePoint.Y));
					break;
				}

				if (b == false) {
					startMouse.X = point.X;
					startMouse.Y = point.Y;
					thread.SetDPadInfo(false, dn, startMouse);
				} else {
					thread.SetDPadInfo(true, dn, startMouse);
					handler.postDelayed(clickRunnable, waitBeforeAdd);
				}

			}

			break;
		}

	}

	private WallStuff getDirection(Point pd) {
		if (pd.X < 0) {
			if (Math.abs(pd.X) > Math.abs(pd.Y))
				return WallStuff.East;
		}
		if (pd.X >= 0) {
			if (Math.abs(pd.X) > Math.abs(pd.Y))
				return WallStuff.West;
		}
		if (pd.Y > 0) {
			return WallStuff.North;
		}
		if (pd.Y <= 0) {
			return WallStuff.South;
		}
		return WallStuff.East;
	}

	public boolean addMazePoint(Point g) {

		if (thread.AddMazePoint(g) != Status.Good) {
			return false;
		} else {

			mVibrate.vibrate(35);

			// directionAdjust.reduce(9);
		}
		try {

			runner.mazeRoom.sendMessage(new GameRoomMessage(GameRoomMessageType.MazeMove, g).GenerateMessage());
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	Point directionAdjust = new Point(0, 0);

	class GrameThread extends Thread {

		public Point CurrentMazePoint = new Point(0, 0);

		private int mCanvasHeight = 1;
		private int mCanvasWidth = 1;

		Rectangle resizeBox;
		Rectangle mainRect;
		Rectangle scrollingBox;
		Rectangle friendsBox;

		/** Indicate whether the surface has been created & is ready to draw */
		private boolean mRun = false;

		/** Handle to the surface manager object we interact with */
		private SurfaceHolder mSurfaceHolder;
		private Paint mMazePaint;
		private Paint mWhitePaint;
		private Paint mResizeBoxPaint;
		private Paint mScrollingBoxPaint;
		private Paint mLinePaint;
		private Paint mBackTrackPaint;
		private Paint mHeaderPointPaint;
		private Paint mBGPaint;

		private Paint mChatPaint;
		private Paint mChatFriendsPaint;
		private Paint mChatFriendsFontPaint;
		private Paint mChatBoxPaint;

		private Paint mFriendsPaint;
		private Paint mDPadPaint;

		Bitmap mDpad;
		Bitmap bg;

		public GrameThread(SurfaceHolder surfaceHolder, Context context, Handler handler) {
			// get handles to some important objects
			mSurfaceHolder = surfaceHolder;
			mContext = context;

			mMazePaint = new Paint();
			mMazePaint.setARGB(255, 0, 0, 0);

			mChatPaint = new Paint();
			mChatPaint.setARGB(255, 135, 90, 58);
			mChatFriendsPaint = new Paint();
			mChatFriendsPaint.setARGB(255, 112, 59, 45);
			mChatFriendsFontPaint = new Paint();
			mChatFriendsFontPaint.setARGB(255, 255, 255, 255);
			mChatBoxPaint = new Paint();
			mChatBoxPaint.setARGB(255, 112, 112, 112);

			mWhitePaint = new Paint();
			mWhitePaint.setARGB(255, 255, 255, 255);

			mResizeBoxPaint = new Paint();
			mResizeBoxPaint.setARGB(255, 0, 115, 150);

			mDPadPaint = new Paint();

			mScrollingBoxPaint = new Paint();
			mScrollingBoxPaint.setARGB(255, 255, 0, 50);

			mHeaderPointPaint = new Paint();
			mHeaderPointPaint.setARGB(255, 255, 0, 0);

			mLinePaint = new Paint();
			mLinePaint.setARGB(255, 255, 255, 0);

			mBackTrackPaint = new Paint();
			mBackTrackPaint.setARGB(255, 122, 175, 12);

			mBGPaint = new Paint();

			mFriendsPaint = new Paint();
			mFriendsPaint.setARGB(255, 222, 222, 222);

			bg = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.bg);
			mDpad = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.dpad);

		}

		public Status AddMazePoint(Point g) {
			Status d;
			if ((d = runner.mazeGame.MazeBuilder.AddPoint(g, false)) == Status.Good) {
				CurrentMazePoint = g;
			}
			return d;

		}

		public void SetFriendsLoc(int x) {
			friendsLoc += x;

		}

		public void CombineMazePos(Point negative) {
			MazePos.Combine(negative);

		}

		public void SetBlockSize(int y) {
			BlockSize += y;
		}

		@Override
		public void run() {
			while (mRun) {
				Canvas c = null;
				try {
					c = mSurfaceHolder.lockCanvas(null);
					synchronized (mSurfaceHolder) {
						updateEngine();
						doDraw(c);
					}
				} catch (Exception ee) {
					Log.d(ee.toString(), "");

				} finally {
					// do this in a finally so that if an exception is thrown
					// during the above, we don't leave the Surface in an
					// inconsistent state
					if (c != null) {
						mSurfaceHolder.unlockCanvasAndPost(c);
					}
				}
			}
		}

		/**
		 * Used to signal the thread whether it should be running or not.
		 * Passing true allows the thread to run; passing false will shut it
		 * down if it's already running. Calling start() after this was most
		 * recently called with false will result in an immediate shutdown.
		 * 
		 * @param b
		 *            true to run, false to shut down
		 */
		public void setRunning(boolean b) {
			mRun = b;
		}

		/* Callback invoked when the surface dimensions change. */
		public void setSurfaceSize(int width, int height) {
			// synchronized to make sure these all change atomically
			synchronized (mSurfaceHolder) {
				mCanvasWidth = width;
				mCanvasHeight = height;

				readjustSizes();
			}
		}

		private void readjustSizes() {
			mainRect = new Rectangle(200, 0, mCanvasWidth - 200, mCanvasHeight);
			friendsBox = new Rectangle(0, 0, 200, mCanvasHeight - 40);
			scrollingBox = new Rectangle(friendsBox.X, mCanvasHeight - 40, 100, 40);
			resizeBox = new Rectangle(friendsBox.X + 100, mCanvasHeight - 40, 100, 40);
			Bitmap.createScaledBitmap(bg, mainRect.Width, mainRect.Height, true);
		}

		public int BlockSize = 30;
		public Point MazePos = new Point(0, 0);

		private void doDraw(Canvas canvas) {
			canvas.drawRect(mainRect.toRect(), mBGPaint);

			int xPos = MazePos.X + mainRect.X;
			int yPos = MazePos.Y + mainRect.Y;
			int d = 2;// must be even

			if (!runner.mazeGame.InWaitingRoom) {
				if (runner.mazeGame.theWalls != null) {
					canvas.drawBitmap(bg, mainRect.X, mainRect.Y, mBGPaint);

					for (int i = 0; i < runner.mazeGame.MazeSize; i++) {
						for (int a = 0; a < runner.mazeGame.MazeSize; a++) {
							if (((i) * BlockSize) + MazePos.X > mainRect.Width || (a) * BlockSize - d + MazePos.Y > mainRect.Height || ((i + 1) * BlockSize) + MazePos.X < 0
									|| (a + 1) * BlockSize - d + MazePos.Y < 0) {
								continue;
							}
							if (runner.mazeGame.theWalls[i][a].contains(WallStuff.West)) {
								canvas.drawRect((i + 1) * BlockSize + xPos, (a) * BlockSize + yPos, (i + 1) * BlockSize + d + xPos, (a + 1) * BlockSize + yPos, mWhitePaint);

							}
							if (runner.mazeGame.theWalls[i][a].contains(WallStuff.East)) {
								canvas.drawRect((i) * BlockSize - d + xPos, (a) * BlockSize + yPos, (i) * BlockSize + xPos, (a + 1) * BlockSize + yPos, mWhitePaint);

							}
							if (runner.mazeGame.theWalls[i][a].contains(WallStuff.North)) {
								canvas.drawRect((i) * BlockSize + xPos, (a) * BlockSize + yPos, (i + 1) * BlockSize + xPos, (a) * BlockSize + d + yPos, mWhitePaint);

							}
							if (runner.mazeGame.theWalls[i][a].contains(WallStuff.South)) {
								canvas.drawRect((i) * BlockSize + xPos, (a + 1) * BlockSize - d + yPos, (i + 1) * BlockSize + xPos, (a + 1) * BlockSize + yPos, mWhitePaint);

							}
						}

						ArrayList<Point> vf = runner.mazeGame.MazeBuilder.Magnify(BlockSize, new Point(mainRect.X + MazePos.X, mainRect.Y + MazePos.Y));

						int inj = vf.size();
						if (inj > 1) {

							Point[] pts = new Point[vf.size()];
							runner.mazeGame.MazeBuilder.Points.toArray(pts);

							Point[] strArray = new Point[vf.size()];
							vf.toArray(strArray);
							int fj = 0;
							for (Pair<Point, Rect> m : Helping.toRects(strArray)) {
								fj++;
								Point pt = pts[fj];
								if (pt.X * BlockSize - mainRect.X < 0 || pt.Y * BlockSize - mainRect.Y < 0 || (pt.X + 1) * BlockSize - mainRect.X > mainRect.Width
										|| (pt.Y + 1) * BlockSize - mainRect.Y > mainRect.Height) {
									continue;
								}
								if (runner.mazeGame.MazeBuilder.NumHits[pt.X][pt.Y]) {
									canvas.drawRect(m.second, mBackTrackPaint);
								} else {
									canvas.drawRect(m.second, mLinePaint);
								}
							}
							canvas.drawCircle(xPos + CurrentMazePoint.X * BlockSize + (BlockSize / 2), yPos + CurrentMazePoint.Y * BlockSize + (BlockSize / 2), d,
									mHeaderPointPaint);

						} else if (vf.size() == 1) {
							canvas.drawCircle(xPos + CurrentMazePoint.X * BlockSize + (BlockSize / 2), yPos + CurrentMazePoint.Y * BlockSize + (BlockSize / 2), d,
									mHeaderPointPaint);

						} else if (vf.size() == 0) {
						}
					}
				}
			} else {
				canvas.drawRect(mainRect.toRect(), mChatBoxPaint);
				canvas.drawRect(new Rect(mainRect.X, mainRect.Y, mainRect.X + 100, mainRect.Bottom()), mChatFriendsPaint);
				if (runner.mazeGame.GameStartingIn != null) {

					canvas.drawCircle(mainRect.Right() - 40, mainRect.Y + 40, 30, mBackTrackPaint);
					canvas.drawText(runner.mazeGame.GameStartingIn.toString(), mainRect.Right() - 40, mainRect.Y + 40, mBGPaint);
				}
				int xp = mainRect.X + 15;
				int yp = mainRect.Y + 50;
				for (FriendsPlaying fp : runner.mazeGame.PlayersInWaitingRoom) {
					canvas.drawText(Helping.GetNameFromLongName(fp.Name), xp, yp, mChatFriendsFontPaint);
					yp += 24;
				}

				ChatMessage[] mst = new ChatMessage[runner.mazeGame.ChatMessages.size()];
				runner.mazeGame.ChatMessages.toArray(mst);
				xp = mainRect.X + 15 + 100;
				yp = mainRect.Bottom() - 100;
				int maxLines = ((mainRect.Height - 100) / 20) - 1;
				int lines = 0;
				for (int i = mst.length - 1; i >= 0; i--) {
					canvas.drawText(mst[i].Message, xp, yp, mChatFriendsFontPaint);
					yp -= 20;
					lines++;
					if (lines > maxLines) {
						break;
					}

				}
				// canvas.drawRect(new Rect(mainRect.X + 105, mainRect.Bottom()
				// - 50, mainRect.Right() - 20, mainRect.Bottom() - 10),
				// mWhitePaint);
			}

			canvas.drawRect(scrollingBox.toRect(), mScrollingBoxPaint);
			canvas.drawRect(resizeBox.toRect(), mResizeBoxPaint);

			drawFriends(canvas);
			canvas.save();

			if (shouldDrawCursor) {
				// canvas.drawBitmap(mDpad, startMouse.X - mDpad.getWidth() /
				// 2,startMouse.Y - mDpad.getHeight() / 2, mDPadPaint);

			}
		}

		public void SetDPadInfo(boolean shouldDraw, int alpha, Point startMouse) {
			this.startMouse = startMouse;
			shouldDrawCursor = shouldDraw;
			if (alpha >= 255) {
				alpha = 255;
			}

			if (drawDPadAlpha != alpha) {
				mDPadPaint.setAlpha(alpha);
			}
			drawDPadAlpha = alpha;

		}

		public Point startMouse;
		public boolean shouldDrawCursor;
		public int drawDPadAlpha;// 0-255

		public int friendsLoc = 0;

		private void drawFriends(Canvas canvas) {
			canvas.drawRect(friendsBox.toRect(), mFriendsPaint);

			int d = 1;
			float blockSize = (100f / runner.mazeGame.MazeSize);

			int j = 0;
			boolean right = false;
			int adjust = 0;
			for (FriendsPlaying fp : runner.mazeGame.PlayersInGame) {

				int xPos = friendsBox.X + (right ? 100 : 0);
				int yPos = friendsBox.Y + adjust;
				right = !right;
				if (!right) {
					adjust += 100;
				}

				canvas.drawRect(new Rect(xPos, yPos, xPos + 100, yPos + 100), mMazePaint);
				canvas.drawRect(new Rect(xPos + 2, yPos + 2, xPos + 100 - 2, yPos + 100 - 2), mWhitePaint);

				for (int i = 0; i < runner.mazeGame.MazeSize; i++) {
					for (int a = 0; a < runner.mazeGame.MazeSize; a++) {
						if (((i) * blockSize - d + xPos) > mCanvasWidth || (a) * blockSize - d + yPos > mCanvasHeight) {
							continue;
						}

						if (runner.mazeGame.theWalls[i][a].contains(WallStuff.West)) {
							canvas.drawRect(new Rect((int) ((i + 1) * blockSize - d + xPos), (int) ((a) * blockSize + yPos), (int) ((i + 1) * blockSize + d + xPos), (int) ((a + 1)
									* blockSize + yPos)), mMazePaint);
						}
						if (runner.mazeGame.theWalls[i][a].contains(WallStuff.East)) {
							canvas.drawRect(new Rect((int) ((i) * blockSize - d + xPos), (int) ((a) * blockSize + yPos), (int) ((i) * blockSize + d + xPos), (int) ((a + 1)
									* blockSize + yPos)), mMazePaint);
						}
						if (runner.mazeGame.theWalls[i][a].contains(WallStuff.North)) {
							canvas.drawRect(new Rect((int) ((i) * blockSize + xPos), (int) ((a) * blockSize - d + yPos), (int) ((i + 1) * blockSize + xPos), (int) ((a) * blockSize
									+ d + yPos)), mMazePaint);
						}
						if (runner.mazeGame.theWalls[i][a].contains(WallStuff.South)) {
							canvas.drawRect(new Rect((int) ((i) * blockSize + xPos), (int) ((a + 1) * blockSize - d + yPos), (int) ((i + 1) * blockSize + xPos), (int) ((a + 1)
									* blockSize + d + yPos)), mMazePaint);
						}
					}
				}
				ArrayList<Pair<Rect, Paint>> rectso = new ArrayList<Pair<Rect, Paint>>(fp.MovePoints.size());

				ArrayList<Point> vf = Builder.Magnify(fp.MovePoints, blockSize, new Point(xPos, yPos));
				int inj = vf.size();
				if (inj > 1) {

					if (inj > 3) {
						if (vf.get(inj - 3).X == vf.get(inj - 1).X && vf.get(inj - 3).Y == vf.get(inj - 1).Y) {
							vf.add(inj - 1, vf.get(inj - 2));
						}
					}

					Point[] strArray = new Point[vf.size()];
					vf.toArray(strArray);
					ArrayList<Pair<Point, Rect>> jim = Helping.toRects(strArray);

					for (int jj = 0; jj < jim.size(); jj++) {
						Pair<Point, Rect> m = jim.get(jj);
						boolean blue = true;
						for (int ind = strArray.length - 1; ind >= 0; ind--) {
							Point ps = strArray[ind];

							if (ps.X == m.first.X && ps.Y == m.first.Y) {
								blue = !blue;
							}
						}

						if (blue) {
							canvas.drawRect(m.second, mBackTrackPaint);
						} else {
							canvas.drawRect(m.second, mLinePaint);
						}
					}

					Point cc = fp.MovePoints.get(fp.MovePoints.size() - 1);
					canvas.drawRect(xPos + cc.X * blockSize + (blockSize / 2) - 3, yPos + cc.Y * blockSize + (blockSize / 2) - 3, xPos + cc.X * blockSize + (blockSize / 2) + 3,
							yPos + cc.Y * blockSize + (blockSize / 2) + 3, mHeaderPointPaint);
				}

				canvas.drawRect(new Rect(xPos + 20, yPos + 15, xPos + 80, yPos + 30), mMazePaint);
				canvas.drawText(fp.Name.split("/")[fp.Name.split("/").length - 1], xPos + 26, yPos + 25, mWhitePaint);
				j++;
			}

		}

		private void updateEngine() {
			if (currentMouseMove == MouseMoveState.Dragging || runner.mazeGame == null || runner.mazeGame.MazeBuilder == null)
				return;
			int gm = runner.mazeGame.MazeBuilder.Points.size();
			if (gm != 0) {
				Point p = runner.mazeGame.MazeBuilder.Points.get(gm - 1);
				int limit = 90;
				if (MazePos.X + p.X * BlockSize > (mainRect.Width) - limit) {
					MazePos.X -= ((MazePos.X + p.X * BlockSize) - ((mainRect.Width) - limit));
				} else if (MazePos.X + p.X * BlockSize < limit) {
					MazePos.X += ((limit) - (MazePos.X + p.X * BlockSize));
				}

				if (MazePos.Y + p.Y * BlockSize > (mainRect.Height) - limit) {
					MazePos.Y -= ((MazePos.Y + p.Y * BlockSize) - ((mainRect.Height) - limit));
				} else if (MazePos.Y + p.Y * BlockSize < limit) {
					MazePos.Y += ((limit) - (MazePos.Y + p.Y * BlockSize));
				}

			}
		}

		public void StartGame() {
			synchronized (mSurfaceHolder) {
				mBGPaint.setARGB(255, 12, 45, 78);
			}
		}

		EditText myEditText;

		public void setChatBox(EditText findViewById) {
			myEditText = findViewById;

		}
	}

	/** Handle to the application context, used to e.g. fetch Drawables. */
	private Context mContext;

	/** Pointer to the text view to display "Paused.." etc. */
	public TextView mStatusText;

	/** The thread that actually draws the animation */
	private GrameThread thread;
	private Handler handler;
	protected MultiRunner runner;

	public GrameView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// register our interest in hearing about changes to our surface
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);

		// create thread only; it's started in surfaceCreated()
		thread = new GrameThread(holder, context, handler = new Handler() {
			@Override
			public void handleMessage(Message m) {

				mStatusText.setVisibility(m.getData().getInt("viz"));
				mStatusText.setText(m.getData().getString("text"));

			}

		});

		setFocusable(true); // make sure we get key events
	}

	/**
	 * Fetches the animation thread corresponding to this LunarView.
	 * 
	 * @return the animation thread
	 */
	public GrameThread getThread() {
		return thread;
	}

	/**
	 * Installs a pointer to the text view used for messages.
	 */

	/* Callback invoked when the surface dimensions change. */
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		thread.setSurfaceSize(width, height);
	}

	/*
	 * Callback invoked when the Surface has been created and is ready to be
	 * used.
	 */
	public void surfaceCreated(SurfaceHolder holder) {
		// start the thread here so that we don't busy-wait in run()
		// waiting for the surface to be created
		thread.setRunning(true);
		thread.start();
	}

	/*
	 * Callback invoked when the Surface has been destroyed and must no longer
	 * be touched. WARNING: after this method returns, the Surface/Canvas must
	 * never be touched again!
	 */
	public void surfaceDestroyed(SurfaceHolder holder) {
		// we have to tell thread to shut down & wait for it to finish, or else
		// it might touch the Surface after we return and explode
		boolean retry = true;
		thread.setRunning(false);
		while (retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {
			}
		}
	}
}