package com.LampLight;

import com.Helper.Helping;

public class LampPlayer {
	public final String FullName;
	public String Name;

	public LampPlayer(String fn) {
		FullName = fn;
		Name = Helping.GetNameFromLongName(fn);
	}

	public LampPlayer() {
		FullName = "";
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof LampPlayer))
			return false;

		return FullName.equals(((LampPlayer) obj).FullName);
	}

	public int hashCode() {
		return FullName.hashCode();
	}

	public String toString() {
		return FullName;
	}
}
