package com.networkcontacts.servicelayer.core.utils;

public class Const {
	public static class Properties{
		public static String LOCAL_ADDRESS =					"127.0.0.1";
		public static String BASE_PATH =						"base.path";
		public static String CONFIG_PATH =						"config.path";
		public static String JSON_PATH =						"json.path";
		public static String DEPLOY_PATH =						"deploy.path";
		public static String REQUEST_DATABASE_STATE_WARNING =	"warning";
		public static String REQUEST_DATABASE_STATE_OK =		"ok";
		public static String REQUEST_DATABASE_STATE_UNDEFINED =	"undefined";
	}
	
	
	public static class Operations{
		public static String OPERATION_STOP =			"StopJettyMonitor";
		public static final String CREATING = 			"di creazione";
		public static final String UPDATING = 			"di aggiornamento";
		public static final String INDEXING = 			"di indicizzazione";
	}
	
	public static class Log{
		public static String HANDLER_PREFIX =			"JETTY_DEFAULT";
		public static String UNSUPPORTED_MONITOR =		"Unsupported monitor operation";
	}
	
	
	public static class Info{
		public static String BAD_PORT =					"Bad stop PORT";
		public static String STOPPING_SERVER =			"Stopping server ";
	}	
	
	public class JsonConst{
		public static final String USER = 				"username";
		public static final String PWD = 				"password";
		public static final String ELEMENTS = 			"elements";
		public static final String XMLNS_DESCR = 		"xmlnsDescr";
		public static final String XMLNS_VALUE = 		"xmlnsValue";
		public static final String KEY = 				"key";
		public static final String VALUE = 				"value";
		public static final String CREDENTIAL = 		"httpCredential";
		public static final String HEADER = 			"header";
		public static final String WSDL = 				"wsdl";
	}

}
