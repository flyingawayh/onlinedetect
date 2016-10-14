/**
 * 
 */
package com.iqiyi.test.mail;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.iqiyi.test.lego.VideoInfo;
import com.iqiyi.test.login.UserImpl;
import com.iqiyi.test.mysql.DBImpl;
/**
 * @author huhonghui
 * 
 */
public class Mail {
	
	// 配置发送邮件的环境属性
	public final Properties setProperties(String userName,String passWord){
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", "smtp.qiyi.com");
        // 发件人的账号
        props.put("mail.user", userName);
        // 访问SMTP服务时需要提供的密码
        props.put("mail.password", passWord);
		return props;
	}
	
	//构建授权信息，用于进行SMTP进行身份验证
	public Authenticator getAuthenticator(final Properties props){
		 Authenticator authenticator = new Authenticator() {
	            @Override
	            protected PasswordAuthentication getPasswordAuthentication() {
	                // 用户名、密码
	                String userName = props.getProperty("mail.user");
	                String password = props.getProperty("mail.password");
	                return new PasswordAuthentication(userName, password);
	            }
	        };
	     return authenticator;
	}
	
	//创建邮件消息
	public MimeMessage getMimeMessage(final Properties props,Authenticator authenticator){
		// 使用环境属性和授权信息，创建邮件会话
        Session mailSession = Session.getInstance(props, authenticator);
        // 创建邮件消息
        MimeMessage message = new MimeMessage(mailSession);
        return message;
	}
	
	//获取邮件收件人列表
	public InternetAddress[] getRecvAddress() 
			throws SQLException, ClassNotFoundException, AddressException{
		List<String> list = new ArrayList<String>();
		Connection conn = DBImpl.getLegoConnection();
		String  selectSql = "SELECT mail FROM maillist where isRecv=1;"; 
		ResultSet rs = DBImpl.getSelect(conn, selectSql);
		while(rs.next()){
			list.add(rs.getString(1));
		}
		InternetAddress[] address = new InternetAddress[list.size()];
		for(int i=0;i<list.size();i++){
			address[i] = new InternetAddress(list.get(i));
		}
		DBImpl.releaseConn(conn);
		return address;
	}
	
	//获取邮件抄送人列表
	public InternetAddress[] getCcAddress() 
			throws SQLException, ClassNotFoundException, AddressException{
		List<String> list = new ArrayList<String>();
		Connection conn = DBImpl.getLegoConnection();
		String  selectSql = "SELECT mail FROM maillist where isCc=1;"; 
		ResultSet rs = DBImpl.getSelect(conn, selectSql);
		while(rs.next()){
			list.add(rs.getString(1));
		}
		InternetAddress[] address = new InternetAddress[list.size()];
		for(int i=0;i<list.size();i++){
			address[i] = new InternetAddress(list.get(i));
		}
		DBImpl.releaseConn(conn);
		return address;
	}
	
	//设置邮件收件人和抄送人
	public void setMailRecvs(final Properties props,MimeMessage message,
			InternetAddress[] recs,InternetAddress[] ccs) throws MessagingException{
		// 设置发件人
        InternetAddress form = new InternetAddress(props.getProperty("mail.user"));
        message.setFrom(form);
        
        //设置邮件收件人
        message.setRecipients(Message.RecipientType.TO, recs);
        
        //设置邮件抄送人
        message.setRecipients(Message.RecipientType.CC, ccs);
	}
	
	//设置邮件内容
	public void setMailContent(MimeMessage message,List<VideoInfo> onlineIds) throws MessagingException{
		message.setSubject("乐高线上测试数据监控告警");
		//线上存在被监控的数据处于端上线状态
		if(onlineIds.size()!=0){
	        StringBuffer str = new StringBuffer();
	        str = str.append("<table width=\"750\">")
	        		.append("<tr>")
	        		.append("<td width=\"50\">序号</td>")
	        		.append("<td width=\"200\">节目显示名称</td>")
	        		.append("<td width=\"200\">奇谱id</td>")
	        		.append("<td width=\"200\">创建时间</td>")
	        		.append("<td width=\"100\">创建者</td>")
	        		.append("</tr>");
	        for(int i=0;i<onlineIds.size();i++){
	        	VideoInfo info = new VideoInfo();
	        	info = onlineIds.get(i);
	        	str.append("<tr>")
	        		.append("<td width=\"50\">"+String.valueOf(i+1)+"</td>")
	        		.append("<td width=\"200\">"+info.getDisplayName()+"</td>")
	        		.append("<td width=\"200\">"+info.getQipuId()+"</td>")
	        		.append("<td width=\"200\">"+info.getCreateTime()+"</td>")
	        		.append("<td width=\"100\">"+info.getCreateUserName()+"</td>")
	        		.append("</tr>");
	        }
	        str = str.append("</table>");
	        // 设置邮件的内容体
	        String content = "<p>总共存在："+onlineIds.size()+" 个所监视的节目或者专辑存在端上线情况，详情如下：</p><br>"+
	        		"<p>"+str.toString()+"</p><br>"+"<p>此部分测试节目名称含有测试等字样，且存在端上线情况，请查看</p>"+"<br>"+
	        		"<a href='http://lego.iqiyi.com/'>点击此处跳转到乐高系统</a>";
	        message.setContent(content, "text/html;charset=UTF-8");
		}else{
			String content = "不存在被监控的显示名称含有测试、ceshi、test等字段的节目存在端上线情况";
	        message.setContent(content, "text/html;charset=UTF-8");
		}
	}
	
	//发送邮件
	public boolean sendMail(MimeMessage message){
        try {
			Transport.send(message);
			return true;
		} catch (MessagingException e) {
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * @param args
	 * @throws MessagingException 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws MessagingException, ClassNotFoundException, SQLException {
		
		Mail mail = new Mail();
		String userName = UserImpl.getUserInfo().getUsername()+"@qiyi.com";
		String passWord = UserImpl.getUserInfo().getUsername();
		//配置发送邮件的环境属性
		final Properties props = mail.setProperties(userName, passWord);
		//构建授权信息，用于进行SMTP进行身份验证
		Authenticator authenticator = mail.getAuthenticator(props);
		//创建邮件消息
		MimeMessage message = mail.getMimeMessage(props, authenticator);
		//设置邮件收件人和抄送人
	
		InternetAddress[] recs = new InternetAddress[2];
		InternetAddress[] ccs = new InternetAddress[1];
		recs[0] = new InternetAddress("huhonghui@qiyi.com");
		recs[1] = new InternetAddress("huhonghui@qiyi.com");
		ccs[0] = new InternetAddress("huhonghui@qiyi.com");

        message.setRecipients(RecipientType.TO, recs);

        message.setRecipients(RecipientType.CC, ccs);
        
		mail.setMailRecvs(props, message, recs, ccs);
		//设置邮件内容
		List<VideoInfo> onlineIds = new ArrayList<VideoInfo>();
		VideoInfo info1 = new VideoInfo();
		info1.setDisplayName("测试数据1");
		info1.setQipuId(111111111111L);
		info1.setCreateTime("2016-01-21 13:54:23");
		info1.setUpdateTime("2016-01-22 13:54:23");
		info1.setEntityId(111111111111111L);
		info1.setCreateUserName("test1");
		
		VideoInfo info2 = new VideoInfo();
		info2.setDisplayName("测试数据2");
		info2.setQipuId(111111111112L);
		info2.setCreateTime("2016-01-21 13:54:23");
		info2.setUpdateTime("2016-01-22 13:54:23");
		info2.setEntityId(111111111111112L);
		info2.setCreateUserName("test2");
		
		onlineIds.add(info1);
		onlineIds.add(info2);
		mail.setMailContent(message, onlineIds);
		
		//发送邮件
		mail.sendMail(message);
        System.out.println("send over");
	}

}
