/**
 * 
 */
package com.iqiyi.test.lego;

/**
 * @author Administrator
 * 节目信息
 */
public class VideoInfo {
	public String displayName;  //节目显示名称
	public String freeType;		//付免费
	public Long qipuId;			//奇谱id
	public Long entityId;		//节目id
	public String createTime;	//创建时间
	public String updateTime;	//更新时间
	public String createUser;		//创建人id
	public String createUserName; //创建者
	
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public Long getQipuId() {
		return qipuId;
	}
	public void setQipuId(Long qipuId) {
		this.qipuId = qipuId;
	}
	public Long getEntityId() {
		return entityId;
	}
	public void setEntityId(Long entityId) {
		this.entityId = entityId;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public String getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}
	public String getFreeType() {
		return freeType;
	}
	public void setFreeType(String freeType) {
		this.freeType = freeType;
	}
	public String getCreateUser() {
		return createUser;
	}
	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}
	public String getCreateUserName() {
		return createUserName;
	}
	public void setCreateUserName(String createUserName) {
		this.createUserName = createUserName;
	}
	
}
