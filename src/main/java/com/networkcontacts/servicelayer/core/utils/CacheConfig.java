package com.networkcontacts.servicelayer.core.utils;

import com.google.gson.JsonObject;

public class CacheConfig {
	
	private static CacheConfig cache;
	private JsonObject databaseConfig;
	private JsonObject serviceConfig;
	
	public void setConfig(JsonObject config) {
		databaseConfig = config.get("database").getAsJsonObject();
		serviceConfig = config.get("service").getAsJsonObject();
	
	}
	
	public JsonObject getDatabaseConfig() {return databaseConfig;}
	public JsonObject getServiceConfig() {return serviceConfig;}
	
	public static CacheConfig getInstance() {
		if (cache == null)
			cache = new CacheConfig();
		return cache;
	} 
}