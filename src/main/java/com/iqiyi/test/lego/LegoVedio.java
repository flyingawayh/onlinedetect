package com.iqiyi.test.lego;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;

import com.google.common.base.Joiner;
import com.iqiyi.test.mysql.DBImpl;
import com.iqiyi.test.utils.HttpMethod;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

public class LegoVedio {
	
	//通过登录获取cookie
	public HttpClient getHttpClient() throws HttpException, IOException, ClassNotFoundException, SQLException{
		HttpClient httpclient = new HttpClient();
		Login.login(httpclient);
		return httpclient;
	}
	
	//获取节目显示名称中含有待检测字符的奇谱id
	public List<VideoInfo> getTestVedio(HttpClient httpclient,String title) throws HttpException, IOException, ClassNotFoundException, SQLException{
		Login.login(httpclient);
		Cookie[] cookie = httpclient.getState().getCookies();
		//String str = HttpMethod.httpPost(httpclient, "http://lego.iqiyi.com/material/list", null, cookie);
		int perNum = 200;
		int count = 0;
		NameValuePair[] data = {	new NameValuePair("page.pageSize", String.valueOf(perNum)),
				new NameValuePair("page.pageNo","1"),
				new NameValuePair("page.orderBy","createTime"),
				new NameValuePair("page.order","desc"),
				//new NameValuePair("filter_EQL_qipuId","200055600"),
				new NameValuePair("filter_LIKES_displayName",title)};
		String json = HttpMethod.httpPost(httpclient, "http://lego.iqiyi.com/video/search", data, cookie);
		
		Object document =Configuration.defaultConfiguration().jsonProvider().parse(json);
		List<VideoInfo> infoList = new ArrayList<VideoInfo>();

		//判断所获取的document中是否含有totalPages节点。如无则表示查询结果返回异常
		if(document.toString().contains("totalPages")){
			Object size = JsonPath.read(document, "$.data.totalCount");	
			Object totalPages = JsonPath.read(document, "$.data.totalPages");	
			int totalCount = Integer.valueOf(size.toString());
			int pageNum = Integer.valueOf(totalPages.toString());
			//如果无对应查询结果
			if(pageNum<1){
				System.out.println("totalPages is :" + totalPages.toString());
			}else if(pageNum==1){
				//如果只有一页查询结果
				if(document.toString().contains("qipuId")){
					for(int i=0;i<totalCount;i++){
						Object qipuId = JsonPath.read(document, "$.data.result["+i+"].qipuId");
						Object displayName = JsonPath.read(document, "$.data.result["+i+"].displayName");
						Object freeType = JsonPath.read(document, "$.data.result["+i+"].freeType");
						Object entityId = JsonPath.read(document, "$.data.result["+i+"].entityId");
						Object createTime = JsonPath.read(document, "$.data.result["+i+"].createTime");
						Object updateTime = JsonPath.read(document, "$.data.result["+i+"].updateTime");
						Object createUser = JsonPath.read(document, "$.data.result["+i+"].createUser");

						//如果奇谱id为0说明还未编目，如果奇谱id为非00和01结尾，说明不是合法的奇谱id
						if((!(qipuId.toString().equals("0")))&&(qipuId.toString().endsWith("00")
								||qipuId.toString().endsWith("01"))&&(Integer.valueOf(qipuId.toString())>99)){
							VideoInfo info = new VideoInfo();
							info.setDisplayName(displayName.toString());
							info.setQipuId(Long.valueOf(qipuId.toString()));
							info.setEntityId(Long.valueOf(entityId.toString()));
							info.setFreeType(freeType.toString());
							info.setCreateTime(createTime.toString());
							info.setUpdateTime(updateTime.toString());
							info.setCreateUser(createUser.toString());
							if(JsonPath.read(document, "$.data.result["+i+"]").toString().contains("createUserName")){
								Object createUserName = JsonPath.read(document, "$.data.result["+i+"].createUserName");
								info.setCreateUserName(createUserName.toString());
							}else{
								info.setCreateUserName("");
							}							infoList.add(info);
							count++;
						}
						System.out.println("总第"+count+"个；详细为第1页，第"+i+"个节目，节目名称为"+displayName.toString()+"; 棋谱id: "+qipuId.toString());		
					}
				}
			}else{
				//抛去奇谱中不存在奇谱id的脏数据
				if(document.toString().contains("qipuId")){
					//如果存在多页查询结果
					for(int page=1;page<=pageNum;page++){
						//HttpClient moreHttpClient = new HttpClient();
						NameValuePair[] otherData = {	new NameValuePair("page.pageSize", String.valueOf(perNum)),
								new NameValuePair("page.pageNo",String.valueOf(page)),
								new NameValuePair("page.orderBy","createTime"),
								new NameValuePair("page.order","desc"),
								//new NameValuePair("filter_EQL_qipuId","200055600"),
								new NameValuePair("filter_LIKES_displayName",title)};
						String otherJson = HttpMethod.httpPost(httpclient, "http://lego.iqiyi.com/video/search", otherData, cookie);
						System.out.println("查询结果为："+otherJson);
						Object otherDocument =Configuration.defaultConfiguration().jsonProvider().parse(otherJson);
						//如果为非最后一页，那么每页的数目固定
						if(page<pageNum){
							for(int i=0;i<perNum;i++){
								Object qipuId = JsonPath.read(otherDocument, "$.data.result["+i+"].qipuId");
								Object displayName = JsonPath.read(otherDocument, "$.data.result["+i+"].displayName");
								Object freeType = JsonPath.read(otherDocument, "$.data.result["+i+"].freeType");
								Object entityId = JsonPath.read(otherDocument, "$.data.result["+i+"].entityId");
								Object createTime = JsonPath.read(otherDocument, "$.data.result["+i+"].createTime");
								Object updateTime = JsonPath.read(otherDocument, "$.data.result["+i+"].updateTime");
								Object createUser = JsonPath.read(otherDocument, "$.data.result["+i+"].createUser");

								//如果奇谱id为0说明还未编目，如果奇谱id为非00和01结尾，说明不是合法的奇谱id
								if((!(qipuId.toString().equals("0")))&&(qipuId.toString().endsWith("00")
										||qipuId.toString().endsWith("01"))&&(Integer.valueOf(qipuId.toString())>99)){
									if(qipuId.toString().equals("200075600")){
										System.out.println("1111111111");
									}
									VideoInfo info = new VideoInfo();
									info.setDisplayName(displayName.toString());
									info.setQipuId(Long.valueOf(qipuId.toString()));
									info.setEntityId(Long.valueOf(entityId.toString()));
									info.setFreeType(freeType.toString());
									info.setCreateTime(createTime.toString());
									info.setUpdateTime(updateTime.toString());
									info.setCreateUser(createUser.toString());

									if(JsonPath.read(otherDocument, "$.data.result["+i+"]").toString().contains("createUserName")){
										Object createUserName = JsonPath.read(otherDocument, "$.data.result["+i+"].createUserName");
										info.setCreateUserName(createUserName.toString());
									}else{
										info.setCreateUserName("");
									}
									infoList.add(info);
									count++;
								}					
								System.out.println("共"+totalCount+"个；总第"+count+"个；详细为第"+page+"页，第"+i+"个节目，节目名称为"+displayName.toString()+"; 棋谱id: "+qipuId.toString());		
							}
						}
						//如果是最后一页，那么此页数据数目为总数-前面所有页的数目之和
						if(page==pageNum){
							int lastPageNum = totalCount-((pageNum-1)*perNum);
							for(int i=0;i<lastPageNum;i++){
								Object qipuId = JsonPath.read(otherDocument, "$.data.result["+i+"].qipuId");
								Object displayName = JsonPath.read(otherDocument, "$.data.result["+i+"].displayName");
								Object freeType = JsonPath.read(otherDocument, "$.data.result["+i+"].freeType");
								Object entityId = JsonPath.read(otherDocument, "$.data.result["+i+"].entityId");
								Object createTime = JsonPath.read(otherDocument, "$.data.result["+i+"].createTime");
								Object updateTime = JsonPath.read(otherDocument, "$.data.result["+i+"].updateTime");
								Object createUser = JsonPath.read(otherDocument, "$.data.result["+i+"].createUser");

								//如果奇谱id为0说明还未编目，如果奇谱id为非00和01结尾，说明不是合法的奇谱id
								if((!(qipuId.toString().equals("0")))&&(qipuId.toString().endsWith("00")
										||qipuId.toString().endsWith("01"))&&(Integer.valueOf(qipuId.toString())>99)){
									if(qipuId.toString().equals("200075600")){
										System.out.println("1111111111");
									}
									VideoInfo info = new VideoInfo();
									info.setDisplayName(displayName.toString());
									info.setQipuId(Long.valueOf(qipuId.toString()));
									info.setEntityId(Long.valueOf(entityId.toString()));
									info.setFreeType(freeType.toString());
									info.setCreateTime(createTime.toString());
									info.setUpdateTime(updateTime.toString());
									info.setCreateUser(createUser.toString());

									if(JsonPath.read(otherDocument, "$.data.result["+i+"]").toString().contains("createUserName")){
										Object createUserName = JsonPath.read(otherDocument, "$.data.result["+i+"].createUserName");
										info.setCreateUserName(createUserName.toString());
									}else{
										info.setCreateUserName("");
									}									infoList.add(info);
									count++;
								}
								System.out.println("共"+totalCount+"个；总第"+count+"个；详细为第"+page+"页，第"+i+"个节目，节目名称为"+displayName.toString()+"; 棋谱id: "+qipuId.toString());		
							}
						}
					}
				}
			}
		}
	/*	for(int i=0;i<totalCount;i++){
			Object temp = JsonPath.read(document, "$.data.result["+i+"].qipuId");
			//如果奇谱id为0说明还未编目，如果奇谱id为非00和01结尾，说明不是合法的奇谱id
			if((!(temp.toString().equals("0")))&&(temp.toString().endsWith("00")
					||temp.toString().endsWith("01"))&&(!ids.contains(Long.valueOf(temp.toString())))){
				ids.add(Long.valueOf(temp.toString()));
			}
		}*/
		return infoList;
	}
	
	//从乐高中获取节目显示名称中含有所有待检测字符的奇谱id
	public List<VideoInfo> getAllTestVedioIds(String[] titles) throws HttpException, IOException, ClassNotFoundException, SQLException{
		List<VideoInfo> allInfo = new ArrayList<VideoInfo>();
		//登录获取cookie
		HttpClient httpClient = getHttpClient();
		for(int i=0;i<titles.length;i++){
			List<VideoInfo> infoList = getTestVedio(httpClient,titles[i]);
			allInfo.addAll(infoList);
		}
		return allInfo;
	}
	
	//从乐高获取的节目可能存在重复数据，例如同一个节目显示名称中同时含有测试、test、ceshi
	//对这部分数据虑重，保证存到数据库videoinfo的数据每个奇谱id只存一次，同时通过设置videoinfo表qipuId字段为主键来保证
	public List<VideoInfo> getSingelList(List<VideoInfo> allList){
		List<VideoInfo> singelList = new ArrayList<VideoInfo>();
		List<Long> ids = new ArrayList<Long>();
		Iterator<VideoInfo> iter = allList.iterator();
		while(iter.hasNext()){
			VideoInfo info = iter.next();
			if(singelList.size()==0){
				singelList.add(info);
				ids.add(info.getQipuId());
				continue;
			}
			if(!ids.contains(info.getQipuId())){
				singelList.add(info);
				ids.add(info.getQipuId());
			}
		}
		return singelList;
	}
	
	
	//获取数据库中VideoInfo表的所有数据
	public List<VideoInfo> getInfo() throws ClassNotFoundException, SQLException{
		List<VideoInfo> list = new ArrayList<VideoInfo>();
		Connection conn = DBImpl.getLegoConnection();
		String selectSql = "select * from videoinfo";
		ResultSet rs = DBImpl.getSelect(conn, selectSql);
		while(rs.next()){
			VideoInfo info = new VideoInfo();
			info.setQipuId(rs.getLong(1));
			info.setEntityId(rs.getLong(2));
			info.setDisplayName(rs.getString(3));
			info.setFreeType(rs.getString(4));
			info.setCreateTime(rs.getString(5));
			info.setUpdateTime(rs.getString(6));
			info.setCreateUserName(rs.getString(7));
			info.setCreateUser(rs.getString(8));
			list.add(info);
		}
		DBImpl.releaseConn(conn);
		return list;
	}
	
	//获取数据库中VideoInfo表的所有奇谱id
	public List<Long> getAllIds() throws ClassNotFoundException, SQLException{
		List<Long> list = new ArrayList<Long>();
		Connection conn = DBImpl.getLegoConnection();
		String selectSql = "select * from videoinfo";
		ResultSet rs = DBImpl.getSelect(conn, selectSql);
		while(rs.next()){
			list.add(rs.getLong(1));
		}
		DBImpl.releaseConn(conn);
		return list;
	}
	
	//获取数据库中VideoInfo表中指定奇谱id的数据
	public List<VideoInfo> getInfo(List<Long> ids) throws ClassNotFoundException, SQLException{
		List<VideoInfo> list = new ArrayList<VideoInfo>();
		Connection conn = DBImpl.getLegoConnection();
		String idStr = Joiner.on(",").join(ids);
		String selectSql = "select * from videoinfo where qipuId in ("+idStr+")";
		ResultSet rs = DBImpl.getSelect(conn, selectSql);
		while(rs.next()){
			VideoInfo info = new VideoInfo();
			info.setQipuId(rs.getLong(1));
			info.setEntityId(rs.getLong(2));
			info.setDisplayName(rs.getString(3));
			info.setFreeType(rs.getString(4));
			info.setCreateTime(rs.getString(5));
			info.setUpdateTime(rs.getString(6));
			info.setCreateUserName(rs.getString(7));
			info.setCreateUser(rs.getString(8));
			list.add(info);
		}
		DBImpl.releaseConn(conn);
		return list;
	}	
	
	//每次添加前，清空videoinfo表
	public void delVideoInfo() throws ClassNotFoundException, SQLException{
		Connection conn = DBImpl.getLegoConnection();
		String deleteSql = "delete from videoinfo";
		DBImpl.excuteUpdate(conn, deleteSql);
		DBImpl.releaseConn(conn);
	}
	
	//暂时废弃该函数
	//把所有含有待检测字符的数据入库
	public void updateDbVdInfo(List<VideoInfo> fromLego,List<VideoInfo> fromDb) 
			throws ClassNotFoundException, SQLException{
		Connection conn = DBImpl.getLegoConnection();
		Iterator<VideoInfo> iter = fromLego.iterator();
		while(iter.hasNext()){
			VideoInfo info = iter.next();
			String insertSql = "INSERT INTO videoinfo VALUES('"
					+ String.valueOf(info.getQipuId())+"','"
					+ String.valueOf(info.getEntityId())+"','"
					+ String.valueOf(info.getDisplayName())+"','"
					+ String.valueOf(info.getFreeType())+"','"
					+ String.valueOf(info.getCreateTime())+"','"
					+ String.valueOf(info.getUpdateTime())+"','"
					+ String.valueOf(info.getCreateUserName())+"','"
					+ String.valueOf(info.getCreateUser())+"');";
			//第一次执行，videoinfo表为空
			if(fromDb.size()==0){
				DBImpl.excuteUpdate(conn, insertSql);
			}
			for(int i=0;i<fromDb.size();i++){
				
				if(String.valueOf(fromDb.get(i).getQipuId()).equals(String.valueOf(info.getQipuId()))){
					if(String.valueOf(fromDb.get(i).getUpdateTime()).equals(String.valueOf(info.getUpdateTime()))){
						//如果奇谱id相同，且更新时间一致，则不更新数据库
					}else{
						String updateSql = "UPDATE videoinfo SET "
								+ "displayName= '"+info.getDisplayName()+
								"' , freeType= '"+info.getFreeType()+
								"', updateTime= '"+info.getUpdateTime()+
								"'  WHERE qipuId= '"+info.getQipuId()+"'";
						DBImpl.excuteUpdate(conn, updateSql);
					}
				}else{
					if(i==fromDb.size()-1){
						DBImpl.excuteUpdate(conn, insertSql);
					}
				}
			}
		}
		DBImpl.releaseConn(conn);
	}
	
	//把所有含有待检测字符的数据入库
	public void updateDbVdInfo(List<VideoInfo> fromLego) 
			throws ClassNotFoundException, SQLException{
		Connection conn = DBImpl.getLegoConnection();
		Iterator<VideoInfo> iter = fromLego.iterator();
		String strsql = "INSERT INTO videoinfo VALUES(?,?,?,?,?,?,?,?);";
	    
		while(iter.hasNext()){
			VideoInfo info = iter.next();
			//防sql注入
			PreparedStatement pstmt = conn.prepareStatement(strsql);
			pstmt.setString(1, strToutf8(String.valueOf(info.getQipuId())));
			pstmt.setString(2, strToutf8(String.valueOf(info.getEntityId())));
			pstmt.setString(3, strToutf8(String.valueOf(info.getDisplayName())));
			pstmt.setString(4, strToutf8(String.valueOf(info.getFreeType())));
			pstmt.setString(5, strToutf8(String.valueOf(info.getCreateTime())));
			pstmt.setString(6, strToutf8(String.valueOf(info.getUpdateTime())));
			pstmt.setString(7, strToutf8(String.valueOf(info.getCreateUserName())));
			pstmt.setString(8, strToutf8(String.valueOf(info.getCreateUser())));
			System.out.println(111);
			pstmt.executeUpdate();
		}
		DBImpl.releaseConn(conn);
	}
	
	//废弃，不用ReadService接口，改用FusionService
	//获取数据库中需要在ReadService中进行端上线查询的节目
	public List<VideoInfo> getListForReadS() throws ClassNotFoundException, SQLException{
		List<VideoInfo> list = new ArrayList<VideoInfo>();
		Connection conn = DBImpl.getLegoConnection();
		String sql = "SELECT a.* FROM `videoinfo` a "
						+" WHERE (a.`userId` IN (SELECT uid FROM monitormail) "
						+" OR(a.`userId` NOT IN (SELECT uid FROM monitormail ) AND a.`createUserName`=\"\")"
						+" OR(a.`userId`=\"\" AND a.`createUserName` IN (SELECT username FROM monitormail)))"
						+" AND( a.`qipuId` NOT IN(SELECT qipuid FROM whitelist));";
		ResultSet rs = DBImpl.getSelect(conn, sql);
		while(rs.next()){
			VideoInfo info = new VideoInfo();
			info.setQipuId(rs.getLong(1));
			info.setEntityId(rs.getLong(2));
			info.setDisplayName(rs.getString(3));
			info.setFreeType(rs.getString(4));
			info.setCreateTime(rs.getString(5));
			info.setUpdateTime(rs.getString(6));
			info.setCreateUserName(rs.getString(7));
			info.setCreateUser(rs.getString(8));
			list.add(info);
		}
		DBImpl.releaseConn(conn);
		return list;
	}
	
	//获取数据库中需要在FusionService中进行端上线查询的节目
	public List<Long> getListForFusionService() throws ClassNotFoundException, SQLException{
		List<Long> list = new ArrayList<Long>();
		Connection conn = DBImpl.getLegoConnection();
		String sql = "SELECT a.* FROM `videoinfo` a "
						+" WHERE (a.`userId` IN (SELECT uid FROM monitormail) "
						+" OR(a.`userId` NOT IN (SELECT uid FROM monitormail ) AND a.`createUserName`=\"\")"
						+" OR(a.`userId`=\"\" AND a.`createUserName` IN (SELECT username FROM monitormail)))"
						+" AND( a.`qipuId` NOT IN(SELECT qipuid FROM whitelist)) "
						+" ORDER BY createTime;";
		ResultSet rs = DBImpl.getSelect(conn, sql);
		while(rs.next()){
			list.add(rs.getLong(1));
		}
		DBImpl.releaseConn(conn);
		return list;
	}
	
	//utf8编码
	public String strToutf8(String str){
		String utf8Str = "";
		try {
			utf8Str = new String(str.getBytes("utf8"),"utf8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return utf8Str;
	}
	public static void main(String[] args) throws HttpException, IOException, ClassNotFoundException, SQLException {
		List<VideoInfo> allInfo = new ArrayList<VideoInfo>();
		List<VideoInfo> fromLego = new ArrayList<VideoInfo>();
		List<VideoInfo> fromDb = new ArrayList<VideoInfo>();
		List<Long> ids = new ArrayList<Long>();
		ids.add(735363200L);
		ids.add(735368300L);
		ids.add(735368500L);
		ids.add(735376800L);


		LegoVedio vedio = new LegoVedio();
	//	vedio.delVideoInfo();
		String[] titles = {"test","ceshi","测试"};
		//String[] titles = {"ceshi","速度与激情7"};
		//String[] titles = {"test"};

//		allInfo = vedio.getAllTestVedioIds(titles);
//		fromLego = vedio.getSingelList(allInfo);
//		vedio.updateDbVdInfo(fromLego);
/*		fromDb = vedio.getListForReadS();
		System.out.println("虑重前含有测试等字样的节目个数为："+allInfo.size());
		System.out.println("虑重后含有测试等字样的节目个数为："+fromLego.size());
		System.out.println("需要发往奇谱确认端是否上线的节目个数为："+fromDb.size());	*/	
		
		vedio.getInfo(ids);
		vedio.getListForFusionService();
		

	}
}
