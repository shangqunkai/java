package com.tsdata.model.factor.vo;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class MyAuthenticator extends Authenticator {
	String userName;
	String password;

	public MyAuthenticator(String userName, String password) {
		this.userName = userName;
		this.password = password;
	}

	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(this.userName, this.password.toCharArray());
	}
}
