package com.bng.ussd.wrapper;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="NotifySOAPHeader")
public class NotifySoapHeader {
	 
	private String linkid;

	public String getLinkid() {
		return linkid;
	}

	@XmlElement(name="linkid",namespace="http://www.huawei.com.cn/schema/common/v2_1")
	public void setLinkid(String linkid) {
		this.linkid = linkid;
	}
	
	

}
