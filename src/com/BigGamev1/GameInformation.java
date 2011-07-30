package com.BigGamev1;

public class GameInformation {
	public static String UserName;
	public static String Service = "gameservice";
	public static String HostName = "lamplightonline.com";
	public static String IP = "lamplightonline.com";

	public static String getXMPPInfo() {
		return Service + "." + HostName;
	}
}
