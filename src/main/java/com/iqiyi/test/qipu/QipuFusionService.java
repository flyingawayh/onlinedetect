/**
 * 
 */
package com.iqiyi.test.qipu;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.googlecode.protobuf.format.JsonFormat;
import com.iqiyi.test.lego.LegoVedio;
import com.iqiyi.test.lego.VideoInfo;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.qiyi.knowledge.client.DataCenter;
import com.qiyi.knowledge.client.QueryService;
import com.qiyi.knowledge.client.ServiceException;

import knowledge.pub.query.Properties.QueryHitFormat;
import knowledge.pub.query.QueryService.QueryResponse;
import knowledge.pub.query.QueryService.TermsQueryRequest;

/**
 * @author Administrator
 *
 */
public class QipuFusionService {

    private static Logger LOG = Logger.getLogger(QipuFusionService.class);

	static {
		PropertyConfigurator.configure(QipuFusionService.class
	                .getResource("/MonitorofLego.log4j.properties"));
	}
	
	//获得奇谱FusionService实例
	public QueryService getService(DataCenter dataCenter){
		QueryService service = null;
        switch (dataCenter) {
        case TEST:
        	//测试环境
            service = QueryService.newBuilder().dataCenter(DataCenter.TEST)
                    .qps(10).username("mems").password("hao123").build();
            break;
        case BJDXT:
            // Send email to Knowledge@qiyi.com for username/password
            service = QueryService.newBuilder().dataCenter(DataCenter.BJDXT)
                    .qps(10).username("lego_test").password("1j@!0+#8")
                    .build();
            break;
        default:
            LOG.error("Not supported data center: " + dataCenter);
            System.exit(1);
        }
		return service;
	}
	
	//根据FusionService实例，获得查询结果。结果为json格式的字符串
	public String getFusionResponse(QueryService service,List<Long> ids){	
		QueryResponse response = null;
		try {
	        String idStr = Joiner.on(";").join(ids);
	        TermsQueryRequest.Builder builder = TermsQueryRequest.newBuilder();
	        TermsQueryRequest request = builder
	                .setFormat(QueryHitFormat.QIPU_ENTITY)
	                .setFrom(0)
	                .setSize(1000)
	                .addAllInclude(Lists.newArrayList("base.display_name","metadata.entity_id","available_status"))
	                .addTerm("id:[" + idStr + "]")
	                .addTerm("available_status:ONLINE")
	                .build();
	        response = service.termsQuery(request);
	        System.out.println("response: "
	                + JsonFormat.printToString(response));
	    } catch (ServiceException e) {
	        e.printStackTrace();
	        Assert.assertNull(e);
	    } finally {
	        
	    }
		return JsonFormat.printToString(response);
	}
	
	//对FusionService返回的json串进行解析，获得存在端为上线状态的奇谱id
	public List<Long> getOnlineQipuIds(String json){
		List<Long> ids = new ArrayList<Long>();
		Object document =Configuration.defaultConfiguration().jsonProvider().parse(json);
		Object size = JsonPath.read(document, "$.total");	
		for(int i=0;i<Integer.valueOf(size.toString());i++){
			Object qipuId = JsonPath.read(document, "$.hit["+i+"].base.id");	
			System.out.println(qipuId.toString());
			ids.add(Long.valueOf(qipuId.toString()));
		}
		return ids;
	}
	
	//调用FusionService前，对奇谱id再次进行过滤，滤掉不是节目和专辑的id
	public List<Long> getIds(List<Long> ids){
		List<Long> qipuIds = new ArrayList<Long>();
		Iterator<Long> iter = ids.iterator();
		while(iter.hasNext()){
			String str = String.valueOf(iter.next());
			if((!(str.equals("0")))&&(str.endsWith("00")||str.endsWith("01"))){
				qipuIds.add(Long.valueOf(str));
			}
		}
		return qipuIds;
	}
	
	//获取有端处于上线状态的所有节目信息，提供给写邮件内容。
	public List<VideoInfo> getAllOnlineQipuIds(DataCenter dataCenter,List<Long> ids){
		List<VideoInfo> listInfo = new ArrayList<VideoInfo>();
		List<Long> onlineIds = new ArrayList<Long>();
		List<Long> finalIds = new ArrayList<Long>();
		int baseNum = 800;
		QueryService service = getService(dataCenter);
		List<Long> qipuIds = getIds(ids);
		int page = (int)Math.ceil(qipuIds.size()/(baseNum*1.0));
		int count = 0;
		for(int i=0;i<page;i++){
			count++;
			if(count!=page){
				for(int j=i*baseNum;j<(i+1)*baseNum;j++){
					onlineIds.add(qipuIds.get(j));
				}
			}else{
				for(int j=i*baseNum;j<qipuIds.size();j++){
					onlineIds.add(qipuIds.get(j));
				}
			}
			String json = getFusionResponse(service,onlineIds);
			onlineIds.removeAll(onlineIds);
			List<Long> idList = getOnlineQipuIds(json);
			finalIds.addAll(idList);
			idList = null;
		}
		LegoVedio video = new LegoVedio();
		try {
			if(finalIds.size()!=0){
				listInfo = video.getInfo(finalIds);
			}
			service.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return listInfo;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		QipuFusionService service = new QipuFusionService();
		//QueryService query =  service.getService(DataCenter.TEST);
		List<Long> ids = new ArrayList<Long>();
		List<VideoInfo> info = new ArrayList<VideoInfo>();

		ids.add(735384300L);
		ids.add(735368300L);
		ids.add(735327300L);
		ids.add(735327400L);
		ids.add(735327500L);
		ids.add(735327600L);
		ids.add(735327000L);

		/*String json = service.getFusionResponse(query, ids);
		service.getOnlineQipuIds(json);*/
		info = service.getAllOnlineQipuIds(DataCenter.TEST, ids);
		System.out.println(info);
	}

}
