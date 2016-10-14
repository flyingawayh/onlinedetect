package com.iqiyi.test.lego;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;

import com.iqiyi.test.login.UserImpl;
import com.iqiyi.test.utils.HttpMethod;


public class Login {
	
	public static String login(org.apache.commons.httpclient.HttpClient httpclient) 
			throws HttpException, IOException, ClassNotFoundException, SQLException{
		String result = "";
		NameValuePair[] data = { new NameValuePair("username", UserImpl.getUserInfo().getUsername()),
										new NameValuePair("password",UserImpl.getUserInfo().getPassword())};
		result = HttpMethod.httpPost(httpclient,"http://lego.iqiyi.com/authenticate", data,null);
		return result;
	}
	
	
	public static void main(String[] args) throws IOException {
		//DefaultHttpClient httpclient = new DefaultHttpClient(new PoolingClientConnectionManager());
		String str = null;
	}
}
