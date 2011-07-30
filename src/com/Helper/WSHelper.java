package com.Helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.util.Base64;

public class WSHelper {

	static public String OToS(Object obj) {
		long start = System.currentTimeMillis();
		String out = null;
		if (obj != null) {
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);
				oos.writeObject(obj);
				out = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("Encode:" + (end - start));
		return out;
	}

	static public Object SToO(String str) {
		long start = System.currentTimeMillis();
		Object out = null;
		if (str != null) {
			try {

				ByteArrayInputStream bios = new ByteArrayInputStream(Base64.decode(str, Base64.DEFAULT));
				ObjectInputStream ois = new ObjectInputStream(bios);
				out = ois.readObject();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("Decode:" + (end - start));
		return out;
	}
}