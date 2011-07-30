package com.LampLight;

import java.io.Serializable;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.Helper.WSHelper;

public abstract class LampView extends SurfaceView implements SurfaceHolder.Callback {

	public Vibrator mVibrate;
	public LampService runner;

	public LampPlayer getMyPlayer() {
		return runner.getLampPlayer();
	}

	public void setStateObject(StateObject so) {
		runner.StateObject = so;
	}

	public StateObject getStateObject() {
		return runner.StateObject;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int be = event.getActionMasked();
		switch (be) {

		case MotionEvent.ACTION_DOWN:

			return onTouchDown(event);

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			return onTouchUp(event);

		case MotionEvent.ACTION_MOVE:
			return onTouchMoved(event);

		}

		return true; // indicate event was handled
	}

	public abstract boolean onTouchMoved(MotionEvent event);

	public abstract boolean onTouchUp(MotionEvent event);

	public abstract boolean onTouchDown(MotionEvent event);

	public abstract class LampDrawer {

		public PaintBucket Bucket = new PaintBucket();

		public abstract void RecieveMessage(Object serializedMessage);

		public abstract void onDrawing(Canvas canvas);

		public abstract boolean onEngineTick();

		public abstract void onResize(int width, int height);
	}

	class LampThread extends Thread {

		private int mCanvasHeight = 1;
		private int mCanvasWidth = 1;

		private SurfaceHolder mSurfaceHolder;

		private Paint mWhitePaint;

		public LampThread(SurfaceHolder surfaceHolder, Context context, Handler handler) {
			// get handles to some important objects
			mSurfaceHolder = surfaceHolder;
			mContext = context;

			mWhitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mWhitePaint.setARGB(255, 255, 255, 255);

		}

		boolean mRun;

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

		public void setRunning(boolean b) {
			mRun = b;

		}

		public void setSurfaceSize(int width, int height) {

			synchronized (mSurfaceHolder) {
				mCanvasWidth = width;
				mCanvasHeight = height;

				myDrawer.onResize(width, height);
				// readjustSizes();
			}
		}

		private void doDraw(Canvas canvas) {
			myDrawer.onDrawing(canvas);
			canvas.save();

		}

		private void updateEngine() {
			myDrawer.onEngineTick();
		}

		public void StartGame() {
			synchronized (mSurfaceHolder) {

			}
		}

		public void PumpMessage(Object serializedMessage) {
			synchronized (mSurfaceHolder) {
				myDrawer.RecieveMessage(serializedMessage);
			}
		}

	}

	/** Handle to the application context, used to e.g. fetch Drawables. */
	private Context mContext;

	/** The thread that actually draws the animation */
	private LampThread thread;
	private Handler handler;

	protected LampDrawer myDrawer;

	private LampDrawer drawGuy;

	public LampView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// register our interest in hearing about changes to our surface
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);

		// create thread only; it's started in surfaceCreated()
		thread = new LampThread(holder, context, handler = new Handler() {
		});

		setFocusable(true); // make sure we get key events
	}

	/**
	 * Fetches the animation thread corresponding to this LunarView.
	 * 
	 * @return the animation thread
	 */
	public LampThread getThread() {
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
			thread = new LampThread(holder, mContext, handler = new Handler() {

			});
			SurfaceHolder holder1 = getHolder();
			holder1.addCallback(this);
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

	public void PumpMessage(Object serializedMessage) {
		thread.PumpMessage(serializedMessage);

	}

	public boolean SendNetworkMessage(LampPlayer whoTo, Serializable serializedMessage) {

		Message msg = new Message(runner.gameRoom.getRoom(), Message.Type.groupchat);
		if (whoTo != null)
			msg.setTo(whoTo.FullName);
		msg.setFrom(getMyPlayer().FullName);
		String stg = WSHelper.OToS(serializedMessage);
		msg.setBody(stg);

		try {
			if (!runner.mBoundService.xmpp.isConnected()) {
				System.out.println("SS");
				return false;
			}
			runner.gameRoom.sendMessage(msg);
			if (runner.mBoundService.xmpp.isConnected()) {
				System.out.println("SS");
			}
		} catch (XMPPException e) {
			// TODO Auto-generated catch block e.printStackTrace();

			return true;
		}

		return true;

	}

	public abstract void RecieveNetworkMessage(LampPlayer whoTo, LampPlayer whoFrom, Object serializedMessage);

	public abstract void onConnectionEstablished();

	public abstract void onUserLogin(LampPlayer lampPlayer);

	public abstract void onUserLogout(LampPlayer lampPlayer);
}