package com.networkcontacts.servicelayer.core.models.engine;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.JsonObject;

@XmlRootElement
public class ServiceInfo {
	
	private String wsdl;
	private String operationName;
	private String type;
	
	private JsonObject httpCredential;
	private JsonObject header;

	public String getWsdl() {return wsdl;}
	public void setWsdl(String wsdl) {this.wsdl = wsdl;}

	public String getOperationName() {return operationName;}
	public void setOperationName(String operationName) {this.operationName = operationName;}

	public String getType() {return type==null||type.length()==0||(!"O".equals(type)&&!"I".equals(type) )?"I":type;}
	public void setType(String type) {this.type = type;}

	public JsonObject getHttpCredential() {return httpCredential;}
	public void setHttpCredential(JsonObject httpCredential) {this.httpCredential = httpCredential;}
	
	public JsonObject getHeader() {return header;}
	public void setHeader(JsonObject header) {this.header = header;}
	

}