package io.thingsofvalue.edu.domain;

import io.thingsofvalue.edu.util.JsonUtil;

public class KaKao {
	//
	String msg_id;
	String dest_phone;
	String send_phone;
	String sender_key;
	String msg_body;
	String ad_flag;
	
	public String getMsg_id() {
		return msg_id;
	}
	public void setMsg_id(String msg_id) {
		this.msg_id = msg_id;
	}
	public String getDest_phone() {
		return dest_phone;
	}
	public void setDest_phone(String dest_phone) {
		this.dest_phone = dest_phone;
	}
	public String getSend_phone() {
		return send_phone;
	}
	public void setSend_phone(String send_phone) {
		this.send_phone = send_phone;
	}
	public String getSender_key() {
		return sender_key;
	}
	public void setSender_key(String sender_key) {
		this.sender_key = sender_key;
	}
	public String getMsg_body() {
		return msg_body;
	}
	public void setMsg_body(String msg_body) {
		this.msg_body = msg_body;
	}
	public String getAd_flag() {
		return ad_flag;
	}
	public void setAd_flag(String ad_flag) {
		this.ad_flag = ad_flag;
	}
	
	public String toJson() {
		return JsonUtil.toJson(this);
	}
	
	

}
