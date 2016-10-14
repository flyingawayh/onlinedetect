/**
 * 
 */
package com.iqiyi.test.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author huhonghui
 * 数据库操作函数
 */
public class DBImpl {

    //private static final String url = "jdbc:mysql://10.1.194.136:3306/lego";
    private static final String url = "jdbc:mysql://10.15.230.76:3306/lego";

    private static final String username = "admin";
    private static final String password = "admin";
    
	//获取数据库连接
	public static Connection getLegoConnection() throws ClassNotFoundException, SQLException{
		String driver = "com.mysql.jdbc.Driver";
		//String url = "jdbc:mysql://10.1.194.136:3306/lego";
		String url = "jdbc:mysql://10.15.230.76:3306/lego";
		String username = "admin";
		String password = "admin";
		Connection conn = null;
		Class.forName(driver);
		conn = DriverManager.getConnection(url, username, password);
		return conn;
	}
	
	//获取测试机数据库连接
	public static Connection getLoginConnection() throws ClassNotFoundException, SQLException{
		String driver = "com.mysql.jdbc.Driver";
		Connection conn = null;
		Class.forName(driver);
		conn = DriverManager.getConnection(url, username, password);
		return conn;
	}
			
	//获取数据库连接
	public static Connection getConnection(String driver,String url,String username,
			String password) throws ClassNotFoundException, SQLException{
		Connection conn = null;
		Class.forName(driver);
		conn = DriverManager.getConnection(url, username, password);
		return conn;
	}
		
	//获取查询结果
	public static ResultSet getSelect(Connection conn,String selectSql){
		ResultSet rs = null;
		try {
			Statement state = conn.createStatement();
			rs = state.executeQuery(selectSql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}
		
	//执行插入操作
	public static int excuteUpdate(Connection conn,String updateSql){
		int row = 0;
		try {
			Statement state = conn.createStatement();
			row = state.executeUpdate(updateSql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return row;
	}
	
	//获取定时任务的表达式
	public static String getExpression(String type){
		String expression = "";
		String sql = "select cronexpression from `async_task` where type=?";
		Connection conn;
		try {
			conn = getLegoConnection();
			PreparedStatement pre = conn.prepareStatement(sql);
			pre.setString(1, type);
			ResultSet set = pre.executeQuery();
			while(set.next()){
				expression = set.getString(1);
			}
			DBImpl.releaseConn(conn);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return expression;
	}
	//释放数据库连接
	public static void releaseConn(Connection conn) throws SQLException{
		conn.close();
	}
	
		
	/**
	 * @param args
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		String driver = "com.mysql.jdbc.Driver";
		String url = "jdbc:mysql://10.1.194.136:3306/lego";
		String username = "root";
		String password = "admin";
		//Connection conn = DBImpl.getConnection(driver, url, username, password);
		
		String insertSql = "INSERT INTO test(mail,isRecv,isCc) "
				+ "VALUES(\"zhangzz@qiyi.com\",false,false)";
		//int row = DBImpl.excuteUpdate(conn, insertSql);
		//System.out.println("执行插入操作，结果为："+row);
		
		/*String selectSql = "select * from test";
		ResultSet rs = DBImpl.getSelect(conn, selectSql);
		while(rs.next()){
			System.out.println("mail is: "+rs.getString(2));
		}
		DBImpl.releaseConn(conn);*/
		getExpression("1");
	}

}
