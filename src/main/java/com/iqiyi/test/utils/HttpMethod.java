package com.iqiyi.test.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

import com.iqiyi.test.lego.Login;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

public class HttpMethod {
	private static HttpClient httpclient2 = new HttpClient();
	//private static DefaultHttpClient  client = new DefaultHttpClient ();
	
	
	public static HttpClient getHttpclient() {
		return httpclient2;
	}


	public static void setHttpclient(HttpClient httpclient) {
		HttpMethod.httpclient2 = httpclient;
	}


	//
	 /**
	 * 
	 *
	 * @param url      
	 */
	public static String httpGet(HttpClient httpclient,String url) throws HttpException, IOException{
		String result = "";
		GetMethod getMethod = new GetMethod(url);
		httpclient.executeMethod(getMethod);
		InputStream responseBody = getMethod.getResponseBodyAsStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(responseBody,"utf-8"));
		String line = reader.readLine();
		while(line != null){
			//System.out.println(new String(line.getBytes()));
			result = result + line;
			line = reader.readLine();
		}
		return result;
	}
	
	
	 /**
     *http post
     * @param httpclient      httpclient对象
	 * @param url             访问的域名
	 * @param pairs           传入的参数
	 * @param cook            cookie
     */
	public static String httpPost(HttpClient httpclient,String url,NameValuePair[] pairs,Cookie[] cook) throws HttpException, IOException{
		String result = "";

		PostMethod postMethod = new UTF8PostMethod(url);
		postMethod.setRequestHeader("Content-Type","application/x-www-form-urlencoded;charset=gbk");

		if(pairs==null||pairs.length==0){
			
		}else{
			postMethod.setRequestBody(pairs);
		}

		try{
			httpclient.getState().addCookies(cook);
			int code = httpclient.executeMethod(postMethod);
			//System.out.println("Status_Code : "+ code);
			//Cookie[] cook2 = httpclient.getState().getCookies();

			InputStream responseBody = postMethod.getResponseBodyAsStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(responseBody,"utf-8"));
			String line = reader.readLine();
			while(line != null){
				//System.out.println(new String(line.getBytes()));
				result = result + line;
				line = reader.readLine();
			}
			reader.close();
		}
		catch (HttpException e) {
			// TODO: handle exception
			System.out.println("Please check your provided http address!");
			e.printStackTrace();
		}catch (IOException e) {
			// TODO: handle exception
			System.out.println("the line is wrong!");
			e.printStackTrace();
		}finally{
			postMethod.releaseConnection();
		   // ((SimpleHttpConnectionManager)httpclient.getHttpConnectionManager()).shutdown();  
		}
		return result;
	}
	
	
	
	public static void main(String[] args) throws HttpException, IOException, ClassNotFoundException, SQLException {
		// TODO Auto-generated method stub

		HttpClient httpclient = new HttpClient();
		Login.login(httpclient);
		Cookie[] cookie = httpclient.getState().getCookies();
		//String str = HttpMethod.httpPost(httpclient, "http://lego.iqiyi.com/material/list", null, cookie);
		int perNum = 200;
		int count = 0;
		NameValuePair[] data = {	new NameValuePair("page.pageSize", String.valueOf(perNum)),
				new NameValuePair("page.pageNo","1"),
				new NameValuePair("page.orderBy","createTime"),
				new NameValuePair("page.order","desc"),
				new NameValuePair("filter_LIKES_displayName","测试")};
		String json = HttpMethod.httpPost(httpclient, "http://lego.iqiyi.com/video/search", data, cookie);
		
		Object document =Configuration.defaultConfiguration().jsonProvider().parse(json);
		List<Long> list = new ArrayList<Long>();

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
						Object temp = JsonPath.read(document, "$.data.result["+i+"].qipuId");
						list.add(Long.valueOf(temp.toString()));
						count++;
						System.out.println("总第"+count+"个；详细为第1页，第"+i+"个棋谱id: "+temp.toString());		
					}
				}
			}else{
				//抛去奇谱中不存在奇谱id的脏数据
				if(document.toString().contains("qipuId")){
					List<HttpClient> client = new ArrayList<HttpClient>();
					for(int page=1;page<=pageNum;page++){
						client.add(new HttpClient());
					}
					//如果存在多页查询结果
					for(int page=1;page<=pageNum;page++){
						
						NameValuePair[] otherData = {	new NameValuePair("page.pageSize", String.valueOf(perNum)),
								new NameValuePair("page.pageNo",String.valueOf(page)),
								new NameValuePair("page.orderBy","createTime"),
								new NameValuePair("page.order","desc"),
								new NameValuePair("filter_LIKES_displayName","测试")};
						String otherJson = HttpMethod.httpPost(client.get(page-1), "http://lego.iqiyi.com/video/search", otherData, cookie);
						Object otherDocument =Configuration.defaultConfiguration().jsonProvider().parse(otherJson);
						//如果为非最后一页，那么每页的数目固定
						if(page<pageNum){
							for(int i=0;i<perNum;i++){
								Object temp = JsonPath.read(otherDocument, "$.data.result["+i+"].qipuId");
								list.add(Long.valueOf(temp.toString()));
								count++;
								System.out.println("共"+totalCount+"个；总第"+count+"个；详细为第"+page+"页，第"+i+"个棋谱id: "+temp.toString());		
							}
						}
						//如果是最后一页，那么此页数据数目为总数-前面所有页的数目之和
						if(page==pageNum){
							int lastPageNum = totalCount-((pageNum-1)*perNum);
							for(int i=0;i<lastPageNum;i++){
								Object temp = JsonPath.read(otherDocument, "$.data.result["+i+"].qipuId");
								list.add(Long.valueOf(temp.toString()));
								count++;
								System.out.println("共"+totalCount+"个；总第"+count+"个；详细为第"+page+"页，第"+i+"个棋谱id: "+temp.toString());		
							}
						}
						
						httpclient2 = null;
					}
				}
			}
			/*System.out.println("totalCount is :" + size.toString());
			int totalCount = Integer.valueOf(size.toString());
	
			Object temp = JsonPath.read(document, "$.data.result.qipuId");
			list.add(Long.valueOf(temp.toString()));
			System.out.println("第"+i+"个棋谱id: "+temp.toString());		

			for(int i=0;i<totalCount;i++){
				Object temp = JsonPath.read(document, "$.data.result["+i+"].qipuId");
				list.add(Long.valueOf(temp.toString()));
				System.out.println("第"+i+"个棋谱id: "+temp.toString());		
			}*/
		}
		/*QipuReadService qipu = new QipuReadService();
		qipu.getContent(list, DataCenter.TEST);
*/
	}
	
	//Inner class for UTF-8 support  
	public static class UTF8PostMethod extends PostMethod{  
		public UTF8PostMethod(String url){  
			super(url);  
		}  
		@Override  
		public String getRequestCharSet() {  
			//return super.getRequestCharSet();  
			return "UTF-8";  
		}
	}

}
