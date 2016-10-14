/**
 *  不使用ReadService服务，改用FusionService
 */
package com.iqiyi.test.qipu;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.google.protobuf.Message;
import com.iqiyi.test.lego.VideoInfo;
import com.iqiyi.test.quartz.JobDetect;
import com.qiyi.knowledge.client.DataCenter;
import com.qiyi.knowledge.client.ReadService;

import knowledge.pub.Common;
import knowledge.pub.Knowledge;

/**
 * @author huhonghui@qiyi.com
 *
 */
public class QipuService {
	/* private static Logger LOG = Logger.getLogger(QipuService.class);

	static {
		PropertyConfigurator.configure(QipuService.class
	                .getResource("/MonitorofLego.log4j.properties"));
	}
	
	//获取Readerservice实例
	public ReadService getService(DataCenter dataCenter){
		ReadService service = null;
		switch (dataCenter) {
        case TEST:
        	//测试环境
            service = ReadService.newBuilder().dataCenter(DataCenter.TEST)
                    .qps(10).username("mems").password("hao123").build();
            break;
        case BJDXT:
            // Send email to Knowledge@qiyi.com for username/password
            service = ReadService.newBuilder().dataCenter(DataCenter.BJDXT)
                    .qps(10).username("***").password("***")
                    .build();
            break;
        default:
            LOG.error("Not supported data center: " + dataCenter);
            System.exit(1);
		}
		return service;
	}
	
	//从ReadService中根据指定的奇谱id及指定列，获取message列表
	public Knowledge.Episode getQipuEntityByQipuId(Long qipuId,ReadService service){
        List<Long> qipuIdList = new ArrayList<Long>();
        qipuIdList.add(qipuId);
        Knowledge.Episode episode = null ;
        List<Common.ColumnGroup> columnGroupList = new ArrayList<Common.ColumnGroup>();
        columnGroupList.add(Common.ColumnGroup.ACCESS_PLAY_CONTROL);
        List<Message> messageList = new ArrayList<Message>();
        messageList = service.getEntity(qipuIdList, columnGroupList);
        if(messageList != null && messageList.size() > 0){
            episode = (Knowledge.Episode)messageList.get(0);
        }
        return episode;
    }

	//判断是否有端处于上线状态
	public boolean isOnline(Knowledge.Episode episode){
		List<knowledge.pub.Properties.AccessPlayControl> accessPlayControlList = episode.getAccessPlayControlList();
		
		boolean isPageStatus = false;
        Common.PagePublishStatus pagePublishStatus = episode.getBase().getPagePublishStatus();
        if(null != pagePublishStatus && "PAGE_ONLINE".equals(pagePublishStatus.toString())){
            isPageStatus = true;
        }
        for(knowledge.pub.Properties.AccessPlayControl accessPlayControl : accessPlayControlList){
            if(null == accessPlayControl.getDefaultControl()){
                //log.info("DefaultControl为空。[qipuId:{}]",qipuId);
                continue;
            }
            if(null == accessPlayControl.getDefaultControl().getAvailabeStatus()){
                //log.info("AvailabeStatus为空。[qipuId:{}]",qipuId);
                continue;
            }
            if(null == accessPlayControl.getPlayPlatform()){
               // log.info("PlayPlatform为空。[qipuId:{}]",qipuId);
                continue;
            }
            if(("ONLINE").equals(accessPlayControl.getDefaultControl().getAvailabeStatus().toString())){
                *//**
                 *
                 * 对于下面3个端，只有默认播控和页面状态都是在线，才是在线
                 * PC_QIYI = 14;
                 * H5_QIYI = 15;
                 * PAD_WEB_QIYI = 16;
                 *//*
                if(Arrays.asList("PC_QIYI", "H5_QIYI", "PAD_WEB_QIYI").contains(accessPlayControl.getPlayPlatform().toString())){
                    if(isPageStatus){
                        return true;
                    }
                }else {
                    return true;
                }
            }
        }
        return false;
	}
	
	//获取存在上线状态的奇谱id列表
	public List<VideoInfo> getOnlineQipuIds(List<VideoInfo> qipuIds){
		List<VideoInfo> onlineIds = new ArrayList<VideoInfo>();
		ReadService service = getService(DataCenter.TEST);
		Knowledge.Episode episode;
		if(!(qipuIds==null||qipuIds.size()==0)){
			Iterator<VideoInfo> iter = qipuIds.iterator();
			while(iter.hasNext()){
				VideoInfo info = new VideoInfo();
				info = iter.next();
				Long id = info.getQipuId();
				String str = String.valueOf(id);
				if((!(str.equals("0")))&&(str.endsWith("00")||str.endsWith("01"))){
					episode =  getQipuEntityByQipuId(id, service);
					if(episode!=null){
						boolean bool = isOnline(episode);
						if(bool){
							onlineIds.add(info);
							System.out.println("11111:  "+ id);
						}
					}
				}
			}
		}
		
		return onlineIds;
	}
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		List<VideoInfo> qipuIds = new ArrayList<VideoInfo>();
		List<VideoInfo> onlineIds = new ArrayList<VideoInfo>();

		JobDetect dect = new JobDetect();
		List<VideoInfo> ensureIds = null;
		String[] titles = {"ceshi"};
		//获取乐高节目库中显示名称含有待检测字符串的所有节目奇谱id
		try {
			ensureIds =dect.getWaitEnsureIds(titles);
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//获取所监控节目中存在端上线的所有节目奇谱id
		QipuService service = new QipuService();
		onlineIds = service.getOnlineQipuIds(ensureIds);
		
		System.out.println(onlineIds);
	}*/
}
