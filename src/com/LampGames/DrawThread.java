package com.LampGames;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import mazeBig.Point;
import mazeBig.Rectangle;
import mazeBig.WallStuff;

import org.jivesoftware.smack.XMPPException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.Helper.MessageBox;
import com.MessageParseJunk.DrawGameRoomMessage;
import com.MessageParseJunk.SquarePlayer;
import com.Service.MultiRunner;

import drawBig.ChatMessage;
import drawBig.ClientDraw.DrawingInfo;
import drawBig.SquareGame;

class DrawView extends SurfaceView implements SurfaceHolder.Callback {

	Point lastMous = new Point(-1, -1);
	public Vibrator mVibrate;
	int mainAreaID = -100;
	public MultiRunner runner;

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (runner == null || runner.squareGame == null)
			return true;
		int be = event.getActionMasked();
		switch (be) {

		case MotionEvent.ACTION_DOWN:
			// mStatusText.setVisibility(View.VISIBLE);
			// mStatusText.setText("doing Mouse");
			startMouse = new Point((int) event.getX(0), (int) event.getY(0));
			lastMous = new Point(startMouse);
			thread.showChat = false;

			for (MessageBox box : runner.squareGame.Messages) {
				if (box.LeftButton(false).Collides(startMouse)) {
					box.ClickLeft();
					return true;
				}
				if (box.RightButton(false).Collides(startMouse)) {
					box.ClickRight();
					return true;
				}
				if (box.Rect().Collides(startMouse)) {
					return true;
				}
			}

			if (thread.otherPlayersRect.Collides(startMouse)) {
				currentMouseMove = MouseMoveState.Friending;

				if (runner.squareGame != null) {

					int u = thread.FriendsYOffset + 16 + thread.otherPlayersRect.Y + 6 + 16;
					for (SquarePlayer pv : runner.squareGame.Players) {

						if (startMouse.Y > u && startMouse.Y < u + 30)
							pv.ShowScore = true;
						else
							pv.ShowScore = false;

						u += 30;
					}

				}

			} else if (thread.otherGamesRect.Collides(startMouse)) {
				currentMouseMove = MouseMoveState.Gaming;
				if (runner.squareGame != null) {

					int u = thread.GameYOffset + 16 + thread.otherGamesRect.Y + 6;
					for (SquareGame pv : runner.squareGame.Games) {

						if (startMouse.Y > u && startMouse.Y < u + 30)
							pv.ShowGameData = true;
						else
							pv.ShowGameData = false;

						u += 30;
					}

				}

			} else if (thread.openChatBoxRect.Collides(startMouse)) {
				currentMouseMove = MouseMoveState.Friending;
				thread.myEditText.setVisibility(View.VISIBLE);
				thread.myEditText.bringToFront();
				thread.showChat = true;

			} else {
				thread.myEditText.setVisibility(View.INVISIBLE);
				currentMouseMove = MouseMoveState.Drawing;

				mainAreaID = event.getPointerId(0);

				curPND = startMouse;
				thread.setClickSpot(startMouse);
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			doingFriendsBox = false;
			// currentMouseMove = MouseMoveState.Mazing;

			mainAreaID = -100;
			curPND = null;
			newWait = null;
			startMouse = new Point(0, 0);
			startdn = 0;

			if (runner != null && runner.squareGame != null) {
				for (SquarePlayer pv : runner.squareGame.Players) {
					pv.ShowScore = false;
				}
				for (SquareGame pv : runner.squareGame.Games) {
					pv.ShowGameData = false;
				}

			}

			if (clickRunnable != null) {

				handler.removeCallbacks(clickRunnable);
				clickRunnable = null;
			}
			for (MessageBox box : runner.squareGame.Messages) {
				box.LeftPressing = false;
				box.RightPressing = false;
			}

			// mStatusText.setVisibility(View.INVISIBLE);
			break;
		case MotionEvent.ACTION_MOVE:
			if (event.getPointerCount() == 2) {
				currentMouseMove = MouseMoveState.Dragging;
			}
			Point bm = new Point((int) event.getX(0), (int) event.getY(0));
			addMouseLine(event, bm, lastMous);
			lastMous = bm;
			break;
		}

		return true; // indicate event was handled
	}

	Point startMouse;
	Point LatestPoint;

	public enum MouseMoveState {
		Dragging, Resize, Drawing, Friending, Gaming
	}

	int startdn = 0;
	Runnable clickRunnable;
	MouseMoveState currentMouseMove = MouseMoveState.Drawing;
	boolean doingFriendsBox = false;
	private WallStuff newWait;

	private void addMouseLine(MotionEvent evt, Point lastMous2, final Point point) {
		Point p = Point.Difference(point, lastMous2);
		Point pd;
		int dn;
		switch (currentMouseMove) {
		case Dragging:

			if (evt.getPointerCount() == 2) {
				Point p1 = new Point((int) evt.getX(0), (int) evt.getY(0));
				Point p2 = new Point((int) evt.getX(1), (int) evt.getY(1));

				pd = Point.Difference(p1, p2);
				newWait = null;

				dn = ((int) Math.sqrt(pd.X * pd.X + pd.Y * pd.Y));
				if (startdn == 0) {
					startdn = dn;
				} else {

					if (((startdn - dn) / 3f) == 0)

						thread.CombineOffsetPos(p.Negative().Magnify(3.6f));
					else
						thread.SetBlockSize((startdn - dn) / 3f, new Point((p1.X + p2.X) / 2, (p1.Y + p2.Y) / 2));

					startdn = dn;
				}
			} else {

				thread.CombineOffsetPos(p.Negative().Magnify(2.6f));

			}
			break;

		case Friending:

			pd = Point.Difference(startMouse, point);
			thread.setFriendOffset(pd.Y / 5);
			break;
		case Gaming:

			pd = Point.Difference(startMouse, point);
			thread.setGameOffset(pd.Y / 5);
			break;
		case Resize:
			// thread.SetBlockSize(p.Y);
			break;
		case Drawing:
			if (curPND == null)
				return;
			pd = Point.Difference(startMouse, point);
			dn = ((int) Math.sqrt(pd.X * pd.X + pd.Y * pd.Y));
			curPND = point;

			WallStuff ws = null;

			int degrees = (int) (Math.atan2(startMouse.Y - point.Y, point.X - startMouse.X) * 57.2957795);

			degrees -= 45;
			if (degrees < 0) {
				degrees += 360;
			}
			if (degrees >= 0 && degrees < 90) {
				ws = WallStuff.North;
			} else if (degrees >= 90 && degrees < 90 * 2) {
				ws = WallStuff.West;
			} else if (degrees >= 90 * 2 && degrees < 90 * 3) {
				ws = WallStuff.South;
			} else if (degrees >= 90 * 3 && degrees < 90 * 4) {
				ws = WallStuff.East;
			}
			newWait = ws;

			if (dn > 50) {
				if (ws != null) {
					pumpMessage(ws);
					curPND = null;
					thread.newPoint = null;
					newWait = null;

					startMouse = new Point(0, 0);
					mVibrate.vibrate(35);
				}
			}

			break;
		}
	}

	Point curPND;

	private void pumpMessage(WallStuff ws) {

		try {
			runner.drawGameRoom.sendMessage(new DrawGameRoomMessage(DrawGameRoomMessage.GameRoomMessageType.AppendLineServer, thread.newPoint, ws, runner.squareGame.PlayerID)
					.GenerateMessage());

		} catch (XMPPException e) {
			e.printStackTrace();
		} finally {

		}

	}

	class DrawThread extends Thread {

		private int mCanvasHeight = 1;
		private int mCanvasWidth = 1;

		Rectangle mainRect;

		Rectangle otherPlayersRect;
		Rectangle otherGamesRect;

		Rectangle chatRect;
		Rectangle openChatBoxRect;

		/** Indicate whether the surface has been created & is ready to draw */
		private boolean mRun = false;

		/** Handle to the surface manager object we interact with */
		private SurfaceHolder mSurfaceHolder;
		private Paint mLinePaint;
		private Paint mDefaultPaint;
		private Paint mGrayPaint;
		private Paint mSelectedPaint;

		private Paint mMessageBoxOuterPaint;
		private Paint mMessageBoxInnerPaint;
		private Paint mMessageBoxTextPaint;
		private Paint mMessageBoxButtonOuterPaint;
		private Paint mMessageBoxButtonInnerPaint;
		private Paint mMessageBoxButtonInnerHighlightPaint;

		private Paint mBGPaint;
		private Paint mChatBoxButtonPaint;
		private Paint mScrapPaint;
		private Paint mBlackPaint;
		private Paint mChatPaint;
		private Paint mWhitePaint;

		private Paint mAnotherPaint;

		private Paint mFriendsPaint;
		private Paint mGamesPaint;
		private Paint mFriendsItemPaint;

		public DrawThread(SurfaceHolder surfaceHolder, Context context, Handler handler) {
			// get handles to some important objects
			mSurfaceHolder = surfaceHolder;
			mContext = context;

			mAnotherPaint = new Paint();

			mMessageBoxOuterPaint = new Paint();
			mMessageBoxOuterPaint.setARGB(150, 47, 73, 91);

			mMessageBoxInnerPaint = new Paint();
			mMessageBoxInnerPaint.setARGB(200, 103, 159, 198);
			mMessageBoxTextPaint = new Paint();
			mMessageBoxTextPaint.setARGB(255, 237, 239, 255);
			mMessageBoxButtonOuterPaint = new Paint();
			mMessageBoxButtonOuterPaint.setARGB(255, 158, 111, 55);
			mMessageBoxButtonInnerPaint = new Paint();
			mMessageBoxButtonInnerPaint.setARGB(255, 158, 158, 55);
			mMessageBoxButtonInnerHighlightPaint = new Paint();
			mMessageBoxButtonInnerHighlightPaint.setARGB(255, 243, 255, 119);

			mScrapPaint = new Paint();

			mWhitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mWhitePaint.setARGB(255, 255, 255, 255);

			mChatBoxButtonPaint = new Paint();
			mChatBoxButtonPaint.setARGB(90, 56, 19, 20);

			mFriendsItemPaint = new Paint();
			mFriendsItemPaint.setARGB(220, 133, 86, 66);

			mFriendsPaint = new Paint();
			mFriendsPaint.setARGB(118, 94, 49, 45);

			mGamesPaint = new Paint();
			mGamesPaint.setARGB(118, 94, 49, 45);

			mChatPaint = new Paint();
			mChatPaint.setARGB(170, 230, 230, 230);

			mGrayPaint = new Paint();
			mGrayPaint.setARGB(255, 15, 0, 53);
			mGrayPaint.setStrokeWidth(2);

			mSelectedPaint = new Paint();
			mSelectedPaint.setARGB(255, 255, 185, 99);

			mLinePaint = new Paint();
			mLinePaint.setARGB(255, 76, 118, 255);

			mBlackPaint = new Paint();
			mBlackPaint.setARGB(255, 0, 0, 0);

			mDefaultPaint = new Paint();
			mDefaultPaint.setARGB(255, 255, 255, 255);

			mBGPaint = new Paint();
			mBGPaint.setARGB(255, 255, 255, 255);

		}

		public void setFriendOffset(int dn) {
			FriendsYOffset -= dn;
			if (FriendsYOffset < -((runner.squareGame.Players.size() - 1) * 30))
				FriendsYOffset = -((runner.squareGame.Players.size() - 1) * 30);

			if (FriendsYOffset > 0)
				FriendsYOffset = 0;

		}

		public void setGameOffset(int dn) {
			GameYOffset -= dn;
			if (GameYOffset < -((runner.squareGame.Games.size() - 1) * 30))
				GameYOffset = -((runner.squareGame.Games.size() - 1) * 30);

			if (GameYOffset > 0)
				GameYOffset = 0;
			runner.squareGame.Redraw = true;
		}

		public void SetBlockSize(float i, Point center) {
			synchronized (mSurfaceHolder) {

				BlockSize -= i;
				if (BlockSize < 5) {
					BlockSize = 5;
					return;
				}

				if (BlockSize > 50) {
					BlockSize = 50;
					return;
				}

				// thread.CombineOffsetPos(new Point((int) (i * (center.X /
				// 30)), (int) (i * (center.Y / 20))));

				runner.squareGame.Redraw = true;
			}
		}

		public void CombineOffsetPos(Point negative) {
			synchronized (mSurfaceHolder) {
				if (negative.X < -40)
					negative.X = -40;
				if (negative.Y < -40)
					negative.Y = -40;

				if (negative.X > 40)
					negative.X = 40;
				if (negative.Y > 40)
					negative.Y = 40;

				// synchronized (mSurfaceHolder) {
				OffsetPos.Offset(negative.X, negative.Y);
				// }
				runner.squareGame.Redraw = true;
			}
		}

		public boolean setClickSpot(Point startMouse) {

			boolean b = false;

			int xPos = OffsetPos.X;
			int yPos = OffsetPos.Y;

			int jx = (int) ((startMouse.X - mainRect.X - xPos) / BlockSize);
			int jy = (int) ((startMouse.Y - mainRect.Y - yPos) / BlockSize);

			if (newPoint != null && newPoint.equals(new Point(jx, jy)))
				b = true;

			newPoint = new Point(jx, jy);

			runner.squareGame.Redraw = true;
			return b;
		}

		Point newPoint;

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
			mainRect = new Rectangle(148, 16, mCanvasWidth - 148 - 16, mCanvasHeight - 32);
			openChatBoxRect = new Rectangle(16, mCanvasHeight - 48, 115, 32);

			otherPlayersRect = new Rectangle(16, 16, 115, (mCanvasHeight - 64) / 2);

			otherGamesRect = new Rectangle(16, 16 + (mCanvasHeight - 64) / 2, 115, (mCanvasHeight - 64) / 2);

			chatRect = new Rectangle(mCanvasWidth - ((mCanvasWidth / 4) * 2), mCanvasHeight - (mCanvasHeight / 2), (mCanvasWidth / 4) * 2 - 16, mCanvasHeight / 2 - 16);

		}

		public Point OffsetPos = new Point(32, 32);
		public float BlockSize = 40;
		private int FriendsYOffset = 0;
		private int GameYOffset = 0;
		Bitmap latestBitmap;

		GradientDrawable mainGradient;

		GradientDrawable mainLGradient;
		GradientDrawable mainRGradient;
		GradientDrawable mainTGradient;
		GradientDrawable mainBGradient;

		private void doDraw(Canvas canvas) {
			// canvas.drawRect(mainRect.toRect(), mBGPaint);

			if (mainGradient == null) {
				mainGradient = new GradientDrawable(Orientation.TL_BR, new int[] { Color.argb(255, 112, 159, 74), Color.argb(255, 188, 157, 154) });
				mainGradient.setBounds(canvas.getClipBounds());
			}

			mainGradient.draw(canvas);

			int xPos = OffsetPos.X;
			int yPos = OffsetPos.Y;
			int d = 2;

			if (latestBitmap == null) {
				latestBitmap = Bitmap.createBitmap(mainRect.Width, mainRect.Height, Config.RGB_565);

				runner.squareGame.Redraw = true;

			}
			if (true || runner.squareGame.Redraw) {
				runner.squareGame.Redraw = false;
				Canvas curCanvas = new Canvas(latestBitmap);
				curCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
				curCanvas.drawColor(Color.argb(255, 210, 210, 210));

				if (newWait != null && newPoint != null) {
					int i = newPoint.X;
					int a = newPoint.Y;
					if (newWait == (WallStuff.West)) {
						curCanvas.drawRect((i) * BlockSize + xPos - d, (a) * BlockSize + yPos, (i) * BlockSize + xPos, (a + 1) * BlockSize + yPos, mGrayPaint);
					}
					if (newWait == (WallStuff.East)) {
						curCanvas.drawRect((i + 1) * BlockSize + xPos, (a) * BlockSize + yPos, (i + 1) * BlockSize + d + xPos, (a + 1) * BlockSize + yPos, mGrayPaint);
					}
					if (newWait == (WallStuff.North)) {
						curCanvas.drawRect((i) * BlockSize + xPos, (a) * BlockSize + yPos, (i + 1) * BlockSize + xPos, (a) * BlockSize + yPos + d, mGrayPaint);

					}
					if (newWait == (WallStuff.South)) {
						curCanvas.drawRect((i) * BlockSize + xPos, (a + 1) * BlockSize + yPos - d, (i + 1) * BlockSize + xPos, (a + 1) * BlockSize + yPos, mGrayPaint);

					}
					curCanvas.drawRect((i) * BlockSize + xPos + d, (a) * BlockSize + yPos + d, (i + 1) * BlockSize + xPos - d, (a + 1) * BlockSize + yPos - d, mSelectedPaint);
				}

				if (runner.squareGame != null && runner.squareGame.Drawer != null) {
					DrawingInfo[][] dif = runner.squareGame.Drawer;

					int yStart = -((int) Math.floor((yPos / BlockSize))) - 1;
					int yMax = yStart + (int) (mainRect.Height / BlockSize) + 2;
					int xStart = -((int) Math.floor((xPos / BlockSize))) - 1;
					int xMax = xStart + (int) (mainRect.Width / BlockSize) + 2;

					if (yMax >= DrawGameRoomMessage.FULLSIZE)
						yMax = DrawGameRoomMessage.FULLSIZE - 1;
					if (xMax >= DrawGameRoomMessage.FULLSIZE)
						xMax = DrawGameRoomMessage.FULLSIZE - 1;

					if (xStart < 0)
						xStart = 0;
					if (yStart < 0)
						yStart = 0;

					for (int a = yStart; a < yMax; a++) {
						for (int i = xStart; i < xMax; i++) {

							DrawingInfo df = dif[i][a];
							if (df.FullPaint != null) {
								curCanvas.drawRect((i) * BlockSize + xPos + d, (a) * BlockSize + yPos + d, (i + 1) * BlockSize + xPos - d, (a + 1) * BlockSize + yPos - d,
										df.FullPaint);
							}
							if (df.WestPaint != null) {
								curCanvas.drawRect((i) * BlockSize + xPos - d, (a) * BlockSize + yPos, (i) * BlockSize + xPos, (a + 1) * BlockSize + yPos, df.WestPaint);
							}
							if (df.EastPaint != null) {
								curCanvas.drawRect((i + 1) * BlockSize + xPos, (a) * BlockSize + yPos, (i + 1) * BlockSize + d + xPos, (a + 1) * BlockSize + yPos, df.EastPaint);
							}
							if (df.NorthPaint != null) {
								curCanvas.drawRect((i) * BlockSize + xPos, (a) * BlockSize + yPos, (i + 1) * BlockSize + xPos, (a) * BlockSize + yPos + d, df.NorthPaint);
							}
							if (df.SouthPaint != null) {
								curCanvas.drawRect((i) * BlockSize + xPos, (a + 1) * BlockSize + yPos - d, (i + 1) * BlockSize + xPos, (a + 1) * BlockSize + yPos, df.SouthPaint);
							}

							if (BlockSize > 45) {
								curCanvas.drawCircle((i) * BlockSize + xPos, (a) * BlockSize + yPos, 2.3f, mGrayPaint);
							} else if (BlockSize > 30) {
								curCanvas.drawCircle((i) * BlockSize + xPos, (a) * BlockSize + yPos, 2.1f, mGrayPaint);
							} else if (BlockSize > 25) {
								if (i % 2 == 0 && a % 2 == 0)
									curCanvas.drawCircle((i) * BlockSize + xPos, (a) * BlockSize + yPos, 1.8f, mGrayPaint);
							} else if (BlockSize > 20) {
								if (i % 2 == 0 && a % 2 == 0)
									curCanvas.drawCircle((i) * BlockSize + xPos, (a) * BlockSize + yPos, 1.5f, mGrayPaint);
							} else if (BlockSize > 15) {
								if (i % 3 == 0 && a % 3 == 0)
									curCanvas.drawCircle((i) * BlockSize + xPos, (a) * BlockSize + yPos, 1.2f, mGrayPaint);
							} else if (BlockSize > 10) {
								if (i % 4 == 0 && a % 4 == 0)
									curCanvas.drawCircle((i) * BlockSize + xPos, (a) * BlockSize + yPos, 1.0f, mGrayPaint);
							} else if (BlockSize >= 5) {
								if (i % 5 == 0 && a % 5 == 0)
									curCanvas.drawCircle((i) * BlockSize + xPos, (a) * BlockSize + yPos, 0.6f, mGrayPaint);
							}

						}
					}
				}
				if (curPND != null) {
					curCanvas.drawLine(startMouse.X - mainRect.X, startMouse.Y - mainRect.Y, curPND.X - mainRect.X, curPND.Y - mainRect.Y, mGrayPaint);
				}
				if (runner.squareGame != null) {
					for (SquareGame gm : runner.squareGame.Games) {
						if (gm.CurPlayerName != null) {
							// curCanvas.drawRect(new RectF(gm.CurPlayerPoint.X
							// *
							// BlockSize + xPos - 20, gm.CurPlayerPoint.Y *
							// BlockSize + yPos - 20, gm.CurPlayerPoint.X *
							// BlockSize + xPos+ 20, gm.CurPlayerPoint.Y *
							// BlockSize
							// + yPos + 20), mBlackPaint);

							for (SquarePlayer pl : runner.squareGame.Players) {
								if (pl.FullName.equals(gm.CurPlayerName)) {
									int amt = (int) (60 - BlockSize);
									if (amt < 7)
										amt = 7;
									if (amt > 18)
										amt = 18;
									curCanvas.drawRect(new RectF(gm.CurPlayerPoint.X * BlockSize + xPos - amt, gm.CurPlayerPoint.Y * BlockSize + yPos - amt, gm.CurPlayerPoint.X
											* BlockSize + xPos + amt, gm.CurPlayerPoint.Y * BlockSize + yPos + amt), pl.GetPaint());

									break;
								}
							}

						}
					}
				}

				curCanvas.save();
			}
			canvas.drawBitmap(latestBitmap, null, mainRect.toRect(), mWhitePaint);

			int gRad = 18;
			if (mainLGradient == null) {
				mainLGradient = new GradientDrawable(Orientation.LEFT_RIGHT, new int[] { gradLight, gradDark });
				mainLGradient.setBounds(new Rect(mainRect.X, mainRect.Y, mainRect.X + gRad, mainRect.Bottom()));
			}
			if (mainRGradient == null) {
				mainRGradient = new GradientDrawable(Orientation.RIGHT_LEFT, new int[] { gradLight, gradDark });
				mainRGradient.setBounds(new Rect(mainRect.Right() - gRad, mainRect.Y, mainRect.Right(), mainRect.Bottom()));
			}
			if (mainTGradient == null) {
				mainTGradient = new GradientDrawable(Orientation.TOP_BOTTOM, new int[] { gradLight, gradDark });
				mainTGradient.setBounds(new Rect(mainRect.X, mainRect.Y, mainRect.Right(), mainRect.Y + gRad));
			}
			if (mainBGradient == null) {
				mainBGradient = new GradientDrawable(Orientation.BOTTOM_TOP, new int[] { gradLight, gradDark });
				mainBGradient.setBounds(new Rect(mainRect.X, mainRect.Bottom() - gRad, mainRect.Right(), mainRect.Bottom()));
			}

			mainBGradient.draw(canvas);
			mainTGradient.draw(canvas);
			mainLGradient.draw(canvas);
			mainRGradient.draw(canvas);

			canvas.drawRect(otherPlayersRect.toRect(), mFriendsPaint);

			canvas.drawRect(otherGamesRect.toRect(), mGamesPaint);

			canvas.drawRect(openChatBoxRect.toRect(), mChatBoxButtonPaint);
			canvas.drawText("Chat", openChatBoxRect.X + 39, openChatBoxRect.Y + 18, mWhitePaint);
			if (runner.squareGame != null) {

				Bitmap friendsBitmap = Bitmap.createBitmap(otherPlayersRect.Width, otherPlayersRect.Height - 16, Config.ARGB_8888);
				Canvas friendsCanvas = new Canvas(friendsBitmap);
				int u = FriendsYOffset + 6;
				canvas.drawText("Players", otherPlayersRect.X + 27, otherPlayersRect.Y + 12, mWhitePaint);

				for (SquarePlayer pv : runner.squareGame.Players) {

					friendsCanvas.drawRect(new Rect(5, u, otherPlayersRect.Width - 5, u + 25), mFriendsItemPaint);

					mScrapPaint.setARGB(pv.Color.A, pv.Color.R, pv.Color.G, pv.Color.B);
					if (pv.Active) {
						mScrapPaint.setStyle(Style.FILL);
					} else {
						mScrapPaint.setStyle(Style.STROKE);
					}
					friendsCanvas.drawRect(new Rect(10, u + 3, 30, u + 19), mScrapPaint);

					friendsCanvas.drawRect(new Rect(32, u + 2, 33, u + 20), mBlackPaint);

					if (pv.ShowScore) {
						canvas.drawRoundRect(new RectF(otherPlayersRect.Right() + 15, otherPlayersRect.Y + u + 5, otherPlayersRect.Right() + 80, otherPlayersRect.Y + u + 25), 3f,
								3f, mBlackPaint);
						canvas.drawText("Score: " + (pv.Score), otherPlayersRect.Right() + 25, otherPlayersRect.Y + u + 18, mWhitePaint);
					}

					friendsCanvas.drawText(pv.Name, 35, u + 13, mWhitePaint);
					mScrapPaint.setStyle(Style.FILL);
					u += 30;
				}
				friendsCanvas.save();
				canvas.drawBitmap(friendsBitmap, new Rect(0, 0, otherPlayersRect.Width, otherPlayersRect.Height - 16), new Rect(otherPlayersRect.X, otherPlayersRect.Y + 16,
						otherPlayersRect.Right(), otherPlayersRect.Bottom()), new Paint(Paint.FILTER_BITMAP_FLAG));

				Bitmap gamesBitmap = Bitmap.createBitmap(otherGamesRect.Width, otherGamesRect.Height - 16, Config.ARGB_8888);
				Canvas gamesCanvas = new Canvas(gamesBitmap);
				u = GameYOffset + 6;
				canvas.drawText("Games", otherGamesRect.X + 27, otherGamesRect.Y + 12, mWhitePaint);

				for (SquareGame gm : runner.squareGame.Games) {
					Paint curPlayerCol = null;
					if (gm.CurPlayerName != null) {
						for (SquarePlayer pl : runner.squareGame.Players) {
							if (pl.FullName.equals(gm.CurPlayerName)) {
								curPlayerCol = pl.GetPaint();
								break;
							}
						}
					}
					if (curPlayerCol != null) {
						gamesCanvas.drawRect(new Rect(3, u + 3, 19, u + 19), curPlayerCol);
						gamesCanvas.drawText("Started By", 22, u + 18, mWhitePaint);

						mScrapPaint.setARGB(gm.StartingColor.A, gm.StartingColor.R, gm.StartingColor.G, gm.StartingColor.B);
						mScrapPaint.setStyle(Style.FILL);
						gamesCanvas.drawRect(new Rect(otherGamesRect.Width - 23, u + 3, otherGamesRect.Width - 7, u + 19), mScrapPaint);

						if (gm.ShowGameData) {
							canvas.drawRoundRect(new RectF(otherPlayersRect.Right() + 15, otherGamesRect.Y, otherPlayersRect.Right() + 110, otherGamesRect.Bottom() - 20), 3f, 3f,
									mBlackPaint);
							int u2 = otherGamesRect.Y + 6;
							for (SquarePlayer pl : gm.PlayersInGame) {

								canvas.drawText(Integer.toString(pl.Score), otherPlayersRect.Right() + 7, otherPlayersRect.Y + u2 + 18, mWhitePaint);
								canvas.drawText(pl.Name, otherPlayersRect.Right() + 23, u2 + 13, mWhitePaint);
							}
						}

					}
					u += 30;
				}

				gamesCanvas.save();
				canvas.drawBitmap(gamesBitmap, new Rect(0, 0, otherGamesRect.Width, otherGamesRect.Height - 16),
						new Rect(otherGamesRect.X, otherGamesRect.Y + 16, otherGamesRect.Right(), otherGamesRect.Bottom()), new Paint(Paint.FILTER_BITMAP_FLAG));

			}

			ChatMessage[] mst = new ChatMessage[runner.squareGame.ChatMessages.size()];
			runner.squareGame.ChatMessages.toArray(mst);
			boolean haventDrawn = true;
			int yChatOffset = 0;

			Calendar now = new GregorianCalendar();
			now.setTime(new Date());
			int maxLines = (chatRect.Height / 20) - 1;
			int lines = 0;
			for (int i = mst.length - 1; i >= 0; i--) {
				Calendar c = mst[i].Time;

				if (showChat || c.after(now)) {
					if (haventDrawn) {
						canvas.drawRect(chatRect.toRect(), mChatPaint);
						haventDrawn = false;
					}
					yChatOffset += 20;
					canvas.drawText(mst[i].Message, chatRect.X + 15, chatRect.Bottom() - yChatOffset, mBlackPaint);

					lines++;
					if (lines > maxLines) {
						break;
					}
				} else {

				}
			}

			canvas.drawText(MultiRunner.charsReiceved + " ", mCanvasWidth - 75, 50, mBlackPaint);

			drawBoxes(canvas);

			canvas.save();

		}

		final int gradDark = Color.argb(0, 111, 111, 111);
		final int gradLight = Color.argb(190, 140, 140, 140);

		private void drawBoxes(Canvas canvas) {

			for (int l = runner.squareGame.Messages.size() - 1; l >= 0; l--) {
				MessageBox box = runner.squareGame.Messages.get(l);

				int roundSize = 3;

				canvas.drawRoundRect(new Rectangle(box.x, box.y, box.width, box.height).toRectF(), roundSize, roundSize, mMessageBoxOuterPaint);
				canvas.drawRect(new Rectangle(box.x + roundSize, box.y + roundSize, box.width - roundSize * 2, box.height - roundSize * 2).toRectF(), mMessageBoxInnerPaint);
				canvas.drawText(box.Message, box.x + 12, box.y + 22, mMessageBoxTextPaint);

				canvas.drawRoundRect(box.LeftButton(false).toRectF(), roundSize, roundSize, mMessageBoxButtonOuterPaint);

				Paint col;
				if (box.LeftPressing)
					col = mMessageBoxButtonInnerHighlightPaint;
				else
					col = mMessageBoxButtonInnerPaint;
				canvas.drawRect(box.LeftButton(true).toRectF(), col);
				canvas.drawText(box.Left, box.LeftButton(true).X + +roundSize * 3, box.LeftButton(true).Y + roundSize + 8, mMessageBoxTextPaint);

				canvas.drawRoundRect(box.RightButton(false).toRectF(), roundSize, roundSize, mMessageBoxButtonOuterPaint);
				if (box.RightPressing)
					col = mMessageBoxButtonInnerHighlightPaint;
				else
					col = mMessageBoxButtonInnerPaint;
				canvas.drawRect(box.RightButton(true).toRectF(), col);
				canvas.drawText(box.Right, box.RightButton(true).X + +roundSize * 3, box.RightButton(true).Y + roundSize + 8, mMessageBoxTextPaint);

				if (box.LeftPressed && !box.LeftPressing) {
					if (runner.squareGame.TryJoinGameName != null) {

						try {
							runner.drawGameRoom
									.sendMessage(new DrawGameRoomMessage(DrawGameRoomMessage.GameRoomMessageType.JoinGame, true, runner.squareGame.TryJoinGameName, "  ")
											.GenerateMessage());
						} catch (XMPPException e) {
							e.printStackTrace();
						} finally {

						}

						runner.squareGame.TryJoinGameName = null;
					}

					box.LeftPressed = false;
					runner.squareGame.Messages.remove(box);
				}
				if (box.RightPressed && !box.RightPressing) {
					box.RightPressed = false;
					runner.squareGame.Messages.remove(box);
				}
			}

		}

		public boolean showChat = false;

		private void updateEngine() {

			if (runner.squareGame != null && runner.squareGame.Drawer != null) {

			}
		}

		public void StartGame() {
			// synchronized (mSurfaceHolder) {
			// mBGPaint.setARGB(255, 255, 251, 229);

			// }
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
	private DrawThread thread;
	private Handler handler;

	public DrawView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// register our interest in hearing about changes to our surface
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);

		// create thread only; it's started in surfaceCreated()
		thread = new DrawThread(holder, context, handler = new Handler() {
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
	public DrawThread getThread() {
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

		if (thread == null) {
			thread = new DrawThread(holder, mContext, handler = new Handler() {
				@Override
				public void handleMessage(Message m) {

					mStatusText.setVisibility(m.getData().getInt("viz"));
					mStatusText.setText(m.getData().getString("text"));

				}

			});
		}

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
				thread = null;
			} catch (InterruptedException e) {
			}
		}
	}
}