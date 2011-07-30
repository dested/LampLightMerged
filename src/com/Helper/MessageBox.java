package com.Helper;

import mazeBig.Rectangle;

public class MessageBox {
	public int x;
	public int y;
	public int width;
	public int height;
	public String Left;
	public String Right;
	public String Message;
	public boolean LeftPressing;
	public boolean RightPressing;

	public boolean LeftPressed;
	public boolean RightPressed;

	public void ClickLeft() {
		LeftPressing = true;
		LeftPressed = true;
	}

	public void ClickRight() {
		RightPressing = true;
		RightPressed = true;
	}

	public Rectangle LeftButton(boolean d) {
		return new Rectangle(x + 13 + (d ? 3 : 0), y + (height - 35) + (d ? 3 : 0), 50 - (d ? 6 : 0), 22 - (d ? 6 : 0));
	}

	public Rectangle RightButton(boolean d) {
		return new Rectangle((width / 6 + 50) + x + 13 + (d ? 3 : 0), y + (height - 35) + (d ? 3 : 0), 50 - (d ? 6 : 0), 22 - (d ? 6 : 0));
	}

	public Rectangle Rect() {
		return new Rectangle(x, y, width, height);
	}

}