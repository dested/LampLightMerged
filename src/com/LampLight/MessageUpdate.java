package com.LampLight;

import com.LampLight.LampMessager.LampMessagerType;

public class MessageUpdate {
	public LampMessagerType Status;
	public String StringToUpdate;
	public String To;
	public String From;

	public MessageUpdate(LampMessagerType mt, String st) {
		Status = mt;
		StringToUpdate = st;
	}

	public MessageUpdate(LampMessagerType mt, String t, String f, String st) {
		Status = mt;
		To = t;
		From = f;
		StringToUpdate = st;
	}
}
