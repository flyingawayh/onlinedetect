package com.iqiyi.test.login;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.iqiyi.test.mysql.DBImpl;


public class UserImpl {

	public static UserInfo getUserInfo() throws ClassNotFoundException, SQLException{
		UserInfo info = new UserInfo();
		Connection conn = DBImpl.getLoginConnection();
		String selectSql = "select * from login";
		ResultSet rs = DBImpl.getSelect(conn, selectSql);
		while(rs.next()){
			info.setUsername(rs.getString(1));
			info.setPassword(rs.getString(2));
		}
		return info;
	}
}
