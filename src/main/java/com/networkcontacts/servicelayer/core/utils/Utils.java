package com.networkcontacts.servicelayer.core.utils;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;

import org.apache.commons.beanutils.PropertyUtilsBean;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.networkcontacts.utils.JSON.JsonUtility;
import com.networkcontacts.utils.JSON.JsonUtilityBuilder;
import com.networkcontacts.utils.LOG.LogUtility;
import com.networkcontacts.utils.PROP.PropertiesUtility;
import com.networkcontacts.utils.XML.XmlUtility;

public class Utils {

	private static JsonUtility jsonUtility = new JsonUtilityBuilder().addDateSupport("dd/MM/yyyy HH:mm:ss") 
			.addFloatSupport()
			.addHtmlSupport()
			.addPrettyPrint()
			.create();
	private static PropertiesUtility propertiesUtility = new  PropertiesUtility("Jetty_default");

	private static LogUtility log;
	public static JsonUtility getJsonUtility() {return jsonUtility;}
	public static PropertiesUtility getPropertiesUtility() {return propertiesUtility;}

	public static JsonObject parseConfig() throws Throwable {
		return XmlUtility.parseXML(propertiesUtility.getProperty(Const.Properties.CONFIG_PATH) + "/config.xml").getAsJsonObject();
	}

	public static LogUtility getLogUtility(){
		if(log == null) log = new LogUtility(propertiesUtility.getProperty(Const.Properties.CONFIG_PATH));
		return log;
	}
	
	public static PropertyDescriptor[] getPropertyDescriptors(Object beanClazz) throws Throwable{
		PropertyUtilsBean pub = new PropertyUtilsBean();
		PropertyDescriptor[] pdS = pub.getPropertyDescriptors(beanClazz);
		return pdS;
	}
	
	public static LinkedHashMap<?,?> getMap(JsonObject jObject) throws Exception{
		Type MapType = new TypeToken<LinkedHashMap<?,?>>() {}.getType(); 
		LinkedHashMap<?,?> toMap =(LinkedHashMap<?, ?>) (jObject!=null? getJsonUtility().fromJsonToObject(jObject, MapType.getClass()):null);
	    return toMap;
	}
	
	public static boolean isPortInUse(String host, int port) throws UnknownHostException, IOException {
		// Assume no connection is possible.
		boolean result = false;

		try {
			(new Socket(host, port)).close();
			result = true;
		}
		catch(SocketException e) {}

		return result;
	}

}
