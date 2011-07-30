package com.LampGames;

import java.util.ArrayList;

import mazeBig.Point;
import mazeBig.Rectangle;

import org.jivesoftware.smack.XMPPException;

import sudokuBig.SudokuBuilder;
import sudokuBig.SudokuPoint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.BigGamev1.FriendsPlaying;
import com.LampLight.R;
import com.MessageParseJunk.SudokuGameRoomMessage;
import com.Service.MultiRunner;

/**
 * View that draws, takes keystrokes, etc. for a simple LunarLander game.
 * 
 * Has a mode which RUNNING, PAUSED, etc. Has a x, y, dx, dy, ... capturing the
 * current ship physics. All x/y etc. are measured with (0,0) at the lower left.
 * updatePhysics() advances the physics based on realtime. draw() renders the
 * ship, and does an invalidate() to prompt another draw() as soon as possible
 * by the system.
 */
class SudokuView extends SurfaceView implements SurfaceHolder.Callback {

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
					currentMouseMove = MouseMoveState.Sudoking;
					thread.mScrollingBoxPaint.setARGB(255, 255, 0, 50);
				} else {
					currentMouseMove = MouseMoveState.Dragging;
					secondID = event.getPointerId(0);
					thread.mScrollingBoxPaint.setARGB(255, 11, 123, 45);
				}
			} else if (thread.resizeBox.Collides((int) event.getX(), (int) event.getY())) {

				if (currentMouseMove == MouseMoveState.Resize) {
					secondID = -1;
					currentMouseMove = MouseMoveState.Sudoking;
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

				if (currentMouseMove == MouseMoveState.Sudoking) {

					int xPos = thread.MazePos.X + thread.mainRect.X;
					int yPos = thread.MazePos.Y + thread.mainRect.Y;

					SudokuPoint pm = new SudokuPoint(-1, new Point((startMouse.X - xPos) / thread.BlockSize, (startMouse.Y - yPos) / thread.BlockSize));

					if (pm.Position.X < 0 || pm.Position.Y < 0 || pm.Position.X > 8 || pm.Position.Y > 8)
						thread.CurrentMazePoint = null;
					else if (runner.sudokuGame.theIndexes[pm.Position.X][pm.Position.Y] == -1) {
						thread.CurrentMazePoint = pm;
						thread.SetDPadInfo(true, 100, startMouse);
					} else {
						thread.CurrentMazePoint = null;
					}

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

			thread.SetDPadInfo(false, 170, startMouse);
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
	Point LatestPoint;

	public enum MouseMoveState {
		Dragging, Resize, Sudoking
	}

	Runnable clickRunnable;
	MouseMoveState currentMouseMove = MouseMoveState.Sudoking;
	boolean doingFriendsBox = false;

	private void addMouseLine(Point lastMous2, final Point point) {
		Point p = Point.Difference(point, lastMous2);

		switch (currentMouseMove) {
		case Dragging:
			thread.CombineMazePos(p.Negative());
			break;
		case Resize:
			thread.SetBlockSize(p.Y);
			break;
		case Sudoking:
			if (doingFriendsBox) {
				thread.SetFriendsLoc(p.Y);
			} else {
				if (thread.CurrentMazePoint == null)
					return;
				Point pd = Point.Difference(startMouse, point);
				LatestPoint = new Point(point.X, point.Y);
				int dn = ((int) Math.sqrt(pd.X * pd.X + pd.Y * pd.Y));

				int degrees = (int) (Math.atan2(startMouse.Y - point.Y, point.X - startMouse.X) * 57.2957795);
				if (degrees < 0) {
					degrees += 360;
				}
				Integer toAdd = -1;
				if (degrees >= 0 && degrees < 40) {
					toAdd = 1;
				} else if (degrees >= 40 && degrees < 40 * 2) {
					toAdd = 2;
				} else if (degrees >= 40 * 2 && degrees < 40 * 3) {
					toAdd = 3;
				} else if (degrees >= 40 * 3 && degrees < 40 * 4) {
					toAdd = 4;
				} else if (degrees >= 40 * 4 && degrees < 40 * 5) {
					toAdd = 5;
				} else if (degrees >= 40 * 5 && degrees < 40 * 6) {
					toAdd = 6;
				} else if (degrees >= 40 * 6 && degrees < 40 * 7) {
					toAdd = 7;
				} else if (degrees >= 40 * 7 && degrees < 40 * 8) {
					toAdd = 8;
				} else if (degrees >= 40 * 8 && degrees < 40 * 9) {
					toAdd = 9;
				} else {
					toAdd = 0;
				}
				thread.CurrentMazePoint.Index = toAdd;

				if (dn > 100) {
					addMazePoint(thread.CurrentMazePoint);
					thread.CurrentMazePoint = null;
					thread.SetDPadInfo(false, 0, startMouse);

				}

				else
					thread.SetDPadInfo(true, dn + 150, startMouse);
			}

			break;
		}
	}

	public boolean addMazePoint(SudokuPoint g) {

		if (thread.AddMazePoint(g) != sudokuBig.SudokuBuilder.Status.Good) {
			return false;
		} else {

			mVibrate.vibrate(35);

			// directionAdjust.reduce(9);
		}
		try {

			runner.sudokuRoom.sendMessage(new SudokuGameRoomMessage(SudokuGameRoomMessage.GameRoomMessageType.SudokuMove, g).GenerateMessage());
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	Point directionAdjust = new Point(0, 0);

	class SudokuThread extends Thread {

		public SudokuPoint CurrentMazePoint;

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
		private Paint mWhitePaint2;
		private Paint mWhitePaint;
		private Paint mResizeBoxPaint;
		private Paint mScrollingBoxPaint;
		private Paint mLinePaint;
		private Paint mGrayPaint;
		private Paint mBackTrackPaint;
		private Paint mHeaderPointPaint;
		private Paint mBGPaint;
		private Paint mFriendsPaint;
		private Paint mDPadPaint;
		private Paint mDPadBackPaint;
		private Paint mGoodPaint;
		private Paint mGoodPaintSmall;

		Bitmap bg;

		public SudokuThread(SurfaceHolder surfaceHolder, Context context, Handler handler) {
			// get handles to some important objects
			mSurfaceHolder = surfaceHolder;
			mContext = context;

			mMazePaint = new Paint();
			mMazePaint.setARGB(255, 0, 0, 0);

			mWhitePaint = new Paint();
			mWhitePaint.setARGB(255, 255, 255, 255);

			mGrayPaint = new Paint();
			mGrayPaint.setStyle(Style.STROKE);
			mGrayPaint.setARGB(255, 180, 180, 180);
			mGrayPaint.setStrokeWidth(1);

			mWhitePaint2 = new Paint();
			mWhitePaint2.setARGB(255, 122, 255, 15);
			mWhitePaint2.setTextSize(7);

			mResizeBoxPaint = new Paint();
			mResizeBoxPaint.setARGB(255, 0, 115, 150);

			mDPadPaint = new Paint();
			mDPadPaint.setARGB(255, 0, 115, 150);

			mDPadBackPaint = new Paint();
			mDPadBackPaint.setARGB(255, 0, 15, 25);

			mScrollingBoxPaint = new Paint();
			mScrollingBoxPaint.setARGB(255, 255, 0, 50);

			mHeaderPointPaint = new Paint();
			mHeaderPointPaint.setARGB(255, 255, 0, 0);

			mLinePaint = new Paint();
			mLinePaint.setARGB(255, 0, 0, 0);
			mLinePaint.setStyle(Style.STROKE);
			mLinePaint.setStrokeWidth(4);

			mGoodPaint = new Paint();
			mGoodPaint.setARGB(255, 0, 0, 0);
			mGoodPaint.setTextSize(17);
			mGoodPaint.setTypeface(Typeface.DEFAULT_BOLD);

			mGoodPaintSmall = new Paint();
			mGoodPaintSmall.setARGB(255, 0, 0, 0);
			mGoodPaintSmall.setTextSize(7);
			mGoodPaintSmall.setTypeface(Typeface.DEFAULT_BOLD);

			mBackTrackPaint = new Paint();
			mBackTrackPaint.setARGB(255, 122, 175, 12);

			mBGPaint = new Paint();

			mFriendsPaint = new Paint();
			mFriendsPaint.setARGB(255, 222, 222, 222);

			bg = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.bg);

		}

		public sudokuBig.SudokuBuilder.Status AddMazePoint(SudokuPoint g) {
			sudokuBig.SudokuBuilder.Status d;
			if ((d = runner.sudokuGame.SudokuBuilder.AddPoint(g)) == sudokuBig.SudokuBuilder.Status.Good) {
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

		public int BlockSize = 29;
		public Point MazePos = new Point(0, 0);

		private void doDraw(Canvas canvas) {
			canvas.drawRect(mainRect.toRect(), mBGPaint);

			int xPos = MazePos.X + mainRect.X;
			int yPos = MazePos.Y + mainRect.Y;
			if (runner != null && runner.sudokuGame != null && runner.sudokuGame.mazeVisible && runner.sudokuGame.theIndexes != null) {

				// canvas.drawBitmap(bg, mainRect.X, mainRect.Y, mBGPaint);

				if (CurrentMazePoint != null) {
					canvas.drawCircle(xPos + CurrentMazePoint.Position.X * BlockSize + (BlockSize / 2), yPos + CurrentMazePoint.Position.Y * BlockSize + (BlockSize / 2),
							BlockSize / 2, mHeaderPointPaint);
					if (CurrentMazePoint.Index != -1) {
						for (int i = 0; i < 9; i++) {
							for (int a = 0; a < 9; a++) {
								if (runner.sudokuGame.theIndexes[i][a] == CurrentMazePoint.Index || runner.sudokuGame.SudokuBuilder.Contains(CurrentMazePoint.Index, i, a)) {
									canvas.drawCircle(xPos + i * BlockSize + (BlockSize / 2), yPos + a * BlockSize + (BlockSize / 2), BlockSize / 2, mBackTrackPaint);
								}
							}
						}
					}
				}
				for (int i = 0; i < 10; i++) {
					Paint d;
					if (i == 1 || i == 2 || i == 4 || i == 5 || i == 7 || i == 8) {
						d = mGrayPaint;
						canvas.drawLine(xPos + i * BlockSize, yPos + 0, xPos + (i * BlockSize), yPos + BlockSize * 9, d);
						canvas.drawLine(xPos + 0, yPos + i * BlockSize, xPos + BlockSize * 9, yPos + i * BlockSize, d);
					} else
						d = mLinePaint;
				}

				for (int i = 0; i < 10; i++) {
					Paint d;
					if (i == 1 || i == 2 || i == 4 || i == 5 || i == 7 || i == 8) {
						d = mGrayPaint;
					} else {
						d = mLinePaint;
						canvas.drawLine(xPos + i * BlockSize, yPos + 0, xPos + (i * BlockSize), yPos + BlockSize * 9, d);
						canvas.drawLine(xPos + 0, yPos + i * BlockSize, xPos + BlockSize * 9, yPos + i * BlockSize, d);
					}
				}

				for (int i = 0; i < 9; i++) {
					for (int a = 0; a < 9; a++) {
						if (runner.sudokuGame.theIndexes[i][a] == -1)
							continue;
						canvas.drawText(Integer.toString(runner.sudokuGame.theIndexes[i][a]), xPos + i * BlockSize + (BlockSize / 2), yPos + a * BlockSize + (BlockSize / 2)
								+ (BlockSize / 6), mGoodPaint);
					}
				}

				ArrayList<SudokuPoint> vf = runner.sudokuGame.SudokuBuilder.Magnify(BlockSize, new Point(mainRect.X + MazePos.X, mainRect.Y + MazePos.Y + (BlockSize / 4)));

				for (SudokuPoint m : vf) {

					canvas.drawText(Integer.toString(m.Index), m.Position.X, m.Position.Y, mGoodPaint);
				}
			}

			canvas.drawRect(scrollingBox.toRect(), mScrollingBoxPaint);
			canvas.drawRect(resizeBox.toRect(), mResizeBoxPaint);

			drawFriends(canvas);
			canvas.save();

			if (shouldDrawCursor) {

				int rad = 120;
				int ag = 1;
				canvas.drawCircle(startMouse.X, startMouse.Y, rad, mDPadBackPaint);
				for (int i = 360 - 1; i >= 0; i -= 40) {

					canvas.drawLine(startMouse.X, startMouse.Y, startMouse.X + (float) (rad * Math.cos(i * 0.0174532925)), startMouse.Y
							+ (float) (rad * Math.sin(i * 0.0174532925)), mDPadPaint);

					canvas.drawText(Integer.toString(ag), (startMouse.X) + (float) ((rad - 20) * Math.cos((i - 20) * 0.0174532925)),
							(startMouse.Y) + (float) ((rad - 20) * Math.sin((i - 20) * 0.0174532925)), mDPadPaint);
					ag++;

				}
				if (LatestPoint != null)
					canvas.drawLine(startMouse.X, startMouse.Y, LatestPoint.X, LatestPoint.Y, mDPadPaint);

			}
		}

		public void SetDPadInfo(boolean shouldDraw, int alpha, Point startMouse) {
			this.startMouse = startMouse;
			shouldDrawCursor = shouldDraw;
			if (alpha >= 255) {
				alpha = 255;
			}

			if (drawDPadAlpha != alpha) {
				mDPadBackPaint.setAlpha(alpha);
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
			float blockSize = (100f / 9);

			int j = 0;
			boolean right = false;
			int adjust = 0;
			for (FriendsPlaying fp : runner.sudokuGame.friends) {

				int xPos = friendsBox.X + (right ? 100 : 0);
				int yPos = friendsBox.Y + adjust;
				right = !right;
				if (!right) {
					adjust += 100;
				}

				canvas.drawRect(new Rect(xPos, yPos, xPos + 100, yPos + 100), mBGPaint);
				for (int i = 0; i < 10; i++) {
					Paint d1;
					if (i == 1 || i == 2 || i == 4 || i == 5 || i == 7 || i == 8) {
						d1 = mGrayPaint;
						canvas.drawLine(xPos + i * blockSize, yPos + 0, xPos + (i * blockSize), yPos + blockSize * 9, d1);
						canvas.drawLine(xPos + 0, yPos + i * blockSize, xPos + blockSize * 9, yPos + i * blockSize, d1);
					} else
						d1 = mLinePaint;
				}

				for (int i = 0; i < 10; i++) {
					Paint d1;
					if (i == 1 || i == 2 || i == 4 || i == 5 || i == 7 || i == 8) {
						d1 = mGrayPaint;
					} else {
						d1 = mLinePaint;
						canvas.drawLine(xPos + i * blockSize, yPos + 0, xPos + (i * blockSize), yPos + blockSize * 9, d1);
						canvas.drawLine(xPos + 0, yPos + i * blockSize, xPos + blockSize * 9, yPos + i * blockSize, d1);
					}
				}

				// canvas.drawRect(new Rect(xPos + 2, yPos + 2, xPos + 100 - 2,
				// yPos + 100 - 2), mBGPaint);

				for (int i = 0; i < 9; i++) {
					for (int a = 0; a < 9; a++) {
						if (runner.sudokuGame.theIndexes[i][a] == -1)
							continue;
						canvas.drawText(Integer.toString(runner.sudokuGame.theIndexes[i][a]), i * blockSize + (blockSize / 2), a * blockSize + (blockSize / 2), mGoodPaintSmall);
					}
				}
				if (fp.MovementPoints.size() > 0) {
					SudokuPoint sp = fp.MovementPoints.get(fp.MovementPoints.size() - 1);

					ArrayList<SudokuPoint> vf = SudokuBuilder.Magnify(fp.MovementPoints, blockSize, new Point(xPos, yPos));
					canvas.drawCircle(xPos + sp.Position.X * blockSize + (blockSize / 2), yPos + sp.Position.Y * blockSize + (blockSize / 2), blockSize / 2, mHeaderPointPaint);

					for (SudokuPoint m : vf) {

						canvas.drawText(Integer.toString(m.Index), m.Position.X, m.Position.Y, mGoodPaintSmall);
					}
				}

				canvas.drawRect(new Rect(xPos + 20, yPos + 15, xPos + 80, yPos + 30), mMazePaint);
				canvas.drawText(fp.Name.split("/")[fp.Name.split("/").length - 1], xPos + 26, yPos + 25, mWhitePaint);
				j++;
			}

		}

		private void updateEngine() {
			if (currentMouseMove == MouseMoveState.Dragging || runner.sudokuGame == null)
				return;

		}

		public void StartGame() {
			synchronized (mSurfaceHolder) {
				mBGPaint.setARGB(255, 255, 255, 255);
				MazePos = new Point(35, 6);
			}
		}
	}

	/** Handle to the application context, used to e.g. fetch Drawables. */
	private Context mContext;

	/** Pointer to the text view to display "Paused.." etc. */
	public TextView mStatusText;

	/** The thread that actually draws the animation */
	private SudokuThread thread;
	private Handler handler;
	protected MultiRunner runner;

	public SudokuView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// register our interest in hearing about changes to our surface
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);

		// create thread only; it's started in surfaceCreated()
		thread = new SudokuThread(holder, context, handler = new Handler() {
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
	public SudokuThread getThread() {
		return thread;
	}

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