/**
 * 
 */
package com.iqiyi.test.quartz;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.httpclient.HttpException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.iqiyi.test.lego.LegoVedio;
import com.iqiyi.test.lego.VideoInfo;
import com.iqiyi.test.login.UserImpl;
import com.iqiyi.test.mail.Mail;
import com.iqiyi.test.qipu.QipuFusionService;
import com.qiyi.knowledge.client.DataCenter;

/**
 * @author Administrator
 *
 */
public class JobDetect implements Job{
	@Override  
    //把要执行的操作，写在execute方法中  
    public void execute(JobExecutionContext arg0) throws JobExecutionException {  
       
		List<Long> ensureIds = null;
		String[] titles = {"test","ceshi","测试"};
		//String[] titles = {"ceshi"};
		//获取乐高节目库中显示名称含有待检测字符串的所有节目奇谱id
		try {
			try {
				ensureIds = getWaitEnsureIds(titles);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//获取所监控节目中存在端上线的所有节目奇谱id
		List<VideoInfo> onlineIds = getOnlineDetectIds(ensureIds);
		//如果不存在上线的测试数据，则不给监控人发邮件
		if(onlineIds.size()!=0){
			//将存在端上线的节目奇谱id发邮件告知相关方
			try {
				sendEmail(onlineIds);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}else{
			try {
				sendEmailToQA(onlineIds);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
    }  

	/*//废弃，使用ReadService的时候使用。
	//获取所有节目显示名称中含有测试、test、testing、ceshi等监控字段且属于被监控范围内的的节目奇谱id
	public List<VideoInfo> getWaitEnsureIds(String[] titles) 
			throws HttpException, IOException, ClassNotFoundException, SQLException{
		List<VideoInfo> ensureVideos = new ArrayList<VideoInfo>();
		List<VideoInfo> singleids = new ArrayList<VideoInfo>();
		List<VideoInfo> idsForQipu = new ArrayList<VideoInfo>();

		LegoVedio vedio = new LegoVedio();
		ensureVideos = vedio.getAllTestVedioIds(titles);
		singleids = vedio.getSingelList(ensureVideos);
		vedio.delVideoInfo();
		vedio.updateDbVdInfo(singleids);
		idsForQipu = vedio.getListForReadS();
		return idsForQipu;
	}*/
	
	//获取所有节目显示名称中含有测试、test、testing、ceshi等监控字段且属于被监控范围内的的节目奇谱id
	public List<Long> getWaitEnsureIds(String[] titles) 
			throws HttpException, IOException, ClassNotFoundException, SQLException{
		List<VideoInfo> ensureVideos = new ArrayList<VideoInfo>();
		List<VideoInfo> singleids = new ArrayList<VideoInfo>();
		List<Long> idsForQipu = new ArrayList<Long>();

		LegoVedio vedio = new LegoVedio();
		ensureVideos = vedio.getAllTestVedioIds(titles);
		singleids = vedio.getSingelList(ensureVideos);
		vedio.delVideoInfo();
		vedio.updateDbVdInfo(singleids);
		idsForQipu = vedio.getListForFusionService();
		return idsForQipu;
	}
	
	//废弃，使用ReadService的时候使用
	//从ReadService中判断任何一端均存在上线状态的节目的奇谱id
	/*public List<VideoInfo> getOnlineDetectIds(List<VideoInfo> ensureIds){
		List<VideoInfo> onlineIds = new ArrayList<VideoInfo>();
		QipuService service = new QipuService();
		
		onlineIds = service.getOnlineQipuIds(ensureIds);
		return onlineIds;
	}*/
	
	//从FusionService中中判断任何一端均存在上线状态的节目列表
	public List<VideoInfo> getOnlineDetectIds(List<Long> ensureIds){
		List<VideoInfo> onlineIds = new ArrayList<VideoInfo>();
		QipuFusionService service = new QipuFusionService();
		
		//offline
		//onlineIds = service.getAllOnlineQipuIds(DataCenter.TEST, ensureIds);
		//online
		onlineIds = service.getAllOnlineQipuIds(DataCenter.BJDXT, ensureIds);
		
		return onlineIds;
	}
		
	//发邮件
	public void sendEmail(List<VideoInfo> onlineIds) throws ClassNotFoundException, SQLException{
		System.out.println("测试Quartz"+new Date());  
        
        Mail mail = new Mail();
        String userName = UserImpl.getUserInfo().getUsername()+"@qiyi.com";
		String passWord = UserImpl.getUserInfo().getPassword();
		//配置发送邮件的环境属性
		final Properties props = mail.setProperties(userName, passWord);
		//构建授权信息，用于进行SMTP进行身份验证
		Authenticator authenticator = mail.getAuthenticator(props);
		//创建邮件消息
		MimeMessage message = mail.getMimeMessage(props, authenticator);
		//设置邮件收件人和抄送人
        try {
        	InternetAddress[] recs = mail.getRecvAddress();
    		InternetAddress[] ccs = mail.getCcAddress();
			message.setRecipients(RecipientType.TO, recs);
  	        message.setRecipients(RecipientType.CC, ccs);
        
        	/*InternetAddress[] recs = new InternetAddress[2];
    		InternetAddress[] ccs = new InternetAddress[1];
    		recs[0] = new InternetAddress("huhonghui@qiyi.com");
    		recs[1] = new InternetAddress("huhonghui@qiyi.com");
    		ccs[0] = new InternetAddress("huhonghui@qiyi.com");*/
    		
	        mail.setMailRecvs(props, message, recs, ccs);      
			//设置邮件内容
			mail.setMailContent(message, onlineIds);
        } catch (AddressException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		//发送邮件
		mail.sendMail(message);
        System.out.println("send over");
	}
	
	//发邮件
	public void sendEmailToQA(List<VideoInfo> onlineIds) throws ClassNotFoundException, SQLException{
		System.out.println("测试Quartz"+new Date());  
        
        Mail mail = new Mail();
        String userName = UserImpl.getUserInfo().getUsername()+"@qiyi.com";
		String passWord = UserImpl.getUserInfo().getPassword();
		//配置发送邮件的环境属性
		final Properties props = mail.setProperties(userName, passWord);
		//构建授权信息，用于进行SMTP进行身份验证
		Authenticator authenticator = mail.getAuthenticator(props);
		//创建邮件消息
		MimeMessage message = mail.getMimeMessage(props, authenticator);
		//设置邮件收件人和抄送人
        try {
        	InternetAddress[] recs = new InternetAddress[2];
    		InternetAddress[] ccs = new InternetAddress[1];
    		recs[0] = new InternetAddress("huhonghui@qiyi.com");
    		recs[1] = new InternetAddress("zhangyaping@qiyi.com");
    		ccs[0] = new InternetAddress("pinghong@qiyi.com");

			message.setRecipients(RecipientType.TO, recs);
  	        message.setRecipients(RecipientType.CC, ccs);
        
	        mail.setMailRecvs(props, message, recs, ccs);      
			//设置邮件内容
			mail.setMailContent(message, onlineIds);
        } catch (AddressException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		//发送邮件
		mail.sendMail(message);
        System.out.println("send over");
	}
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		List<VideoInfo> onlineIds = new ArrayList<VideoInfo>();
		VideoInfo info = new VideoInfo();
		info.setQipuId(734852100L);
		info.setEntityId(210012346L);
		info.setCreateTime("2016-03-21 15:47:35	");
		info.setCreateUserName("王琳");
		info.setDisplayName("预览测试");
		onlineIds.add(info);
		JobDetect mail = new JobDetect();
		mail.sendEmail(onlineIds);
	}
}
