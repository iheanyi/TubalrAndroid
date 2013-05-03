package com.iheanyiekechukwu.tubalr;

public class UserHelper {

	public static final String[] userInfo= {"", ""};
	
	public static final int USER = 0;
	public static final int TOKEN = 1;
	
	public static final boolean userLoggedIn() {
		if(userInfo[USER].equals("") || userInfo[USER] == "-1") {
			return false;
		}
		
		return true;
	}
}
