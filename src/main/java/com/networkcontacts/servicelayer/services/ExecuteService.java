package com.networkcontacts.servicelayer.services;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.naming.InvalidNameException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.headers.Header;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.transport.http.HTTPConduit;
import org.json.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.networkcontacts.servicelayer.core.models.engine.ServiceInfo;
import com.networkcontacts.servicelayer.core.models.general.SOAPGeneralService;
import com.networkcontacts.servicelayer.core.utils.Const;
import com.networkcontacts.servicelayer.core.utils.Utils;
import com.networkcontacts.servicelayer.services.interceptors.BooleanIntrospector;
import com.networkcontacts.servicelayer.services.interceptors.XMLLoggingInInterceptor;

//CovelliGay2volte

@Path("/executeService/")
public class ExecuteService extends SOAPGeneralService{

	@POST
	@Path("/execute/{functionName}")
	@Consumes(MediaType.WILDCARD)
	public Response execute(@PathParam("functionName") String functionName, String input) throws Throwable{
		Client client = null;
		Response response = null;
		try{
			JsonObject jInput = Utils.getJsonUtility().fromStringToJson(input);

			ServiceInfo serviceInfo = Utils.getJsonUtility().fromJsonToObject(jInput, ServiceInfo.class);
			String wsdl = serviceInfo.getWsdl();

			SOAPGeneralService generalService = new SOAPGeneralService();
			client = generalService.getClient(wsdl);

			List<?> classes = generalService.getPartClass(functionName);

			JsonObject HTTPCREDENTIAL = serviceInfo.getHttpCredential();
			if(HTTPCREDENTIAL!=null && Utils.getJsonUtility().fromJsonToObject(HTTPCREDENTIAL, LinkedHashMap.class).size()==2){
				HTTPConduit http = (HTTPConduit) client.getConduit();
				http.getAuthorization().setUserName(HTTPCREDENTIAL.get(Const.JsonConst.USER).getAsString());            
				http.getAuthorization().setPassword(HTTPCREDENTIAL.get(Const.JsonConst.PWD).getAsString());        
			}

			JsonObject HEADER = serviceInfo.getHeader();
			if(HEADER!=null && Utils.getJsonUtility().fromJsonToObject(HEADER, LinkedHashMap.class).size()!=0){
				List<Header> headersList = new ArrayList<Header>();
				LinkedHashMap<String, Object> headerMap = Utils.getJsonUtility().fromJsonToObject(HEADER, LinkedHashMap.class);
				for(String key: headerMap.keySet()){
					JsonObject currentHeader = HEADER.getAsJsonObject(key);

					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder db = dbf.newDocumentBuilder();
					Document docContainer = db.newDocument();
					Element container = docContainer.createElement(key);
					container.setAttribute(currentHeader.getAsJsonPrimitive(Const.JsonConst.XMLNS_DESCR).getAsString(), 
							currentHeader.getAsJsonPrimitive(Const.JsonConst.XMLNS_VALUE).getAsString());

					JsonArray keyElements = null;
					if(currentHeader.get(Const.JsonConst.ELEMENTS).isJsonArray()){
						keyElements = currentHeader.getAsJsonArray(Const.JsonConst.ELEMENTS);
					}else{
						keyElements = new JsonArray();
						keyElements.add(currentHeader.get(Const.JsonConst.ELEMENTS));
					}

					for(JsonElement keyElement: keyElements){
						JsonObject currentElement = keyElement.getAsJsonObject();
						Element innerElement = docContainer.createElement(currentElement.getAsJsonPrimitive(Const.JsonConst.KEY).getAsString());
						innerElement.appendChild(docContainer.createTextNode(currentElement.getAsJsonPrimitive(Const.JsonConst.VALUE).getAsString()));
						container.appendChild(innerElement);
					}

					Header element = new Header(new QName(key), container);
					headersList.add(element);

				}
				client.getRequestContext().put(Header.HEADER_LIST, headersList);
			}

			/*
			 * Il primo elemento della lista è la descrizione della classe di input.
			 */
			List<Object> paramsList = new ArrayList<>();
			Object request = null;

			if(classes.get(0) instanceof Class<?>){
				/*
				 * La classe di input è descritta come una classe java....
				 */
				Class<?> inputClass = (Class<?>)classes.get(0);
				request = Utils.getJsonUtility().fromJsonToObject(jInput, inputClass); 
				JsonElement jsonRequest = Utils.getJsonUtility().fromObjectToJson(request);
				LinkedHashMap<String, Object> mapRequest = (LinkedHashMap<String, Object> ) Utils.getJsonUtility().fromJsonToObject(jsonRequest.getAsJsonObject(), LinkedHashMap.class);
				for(String key: mapRequest.keySet()){
					PropertyDescriptor propertyDescriptor = PropertyUtils.getPropertyDescriptor(request, key);
					Method method = propertyDescriptor.getReadMethod();
					if(Boolean.class.isAssignableFrom(propertyDescriptor.getPropertyType())){
						BeanUtilsBean beanUtilsBean = new BeanUtilsBean();
						beanUtilsBean.getPropertyUtils().addBeanIntrospector(new BooleanIntrospector());
						Object value = beanUtilsBean.getProperty(request, propertyDescriptor.getName());
						paramsList.add(new Boolean(value!=null?value.toString():"false"));
					}else{
						paramsList.add(method.invoke(request));
					}
				}
			}else{
				/*
				 * La classe di input è descritta come un JsonObject
				 */
				JsonObject inputClass = (JsonObject)classes.get(0);

				LinkedHashMap<String, Object> inputValuesMap = (LinkedHashMap<String, Object>)Utils.getJsonUtility().fromJsonToObject(jInput, LinkedHashMap.class);
				LinkedHashMap<String, Object> inputDescriptorMap = Utils.getJsonUtility().fromJsonToObject(inputClass, LinkedHashMap.class);

				for(String key: inputDescriptorMap.keySet()){
					paramsList.add(inputValuesMap.get(key));
				}
			}

			try{
				Object[] params = paramsList.toArray(new Object[paramsList.size()]);
				client.invoke(functionName,params);
			}catch (IllegalArgumentException e) {
				if(request!=null)
					try{
						client.invoke(functionName,request);
					}catch (Fault fault) {
						System.out.println("Ho eseguito: client.invoke(functionName,request) ma -> " + fault.toString());
						//throw e;
					}
			}catch (Fault e) {
				System.out.println("Ho eseguito: client.invoke(functionName,params) ma -> " + e.toString());
				//	throw e;
			}
			
			XMLLoggingInInterceptor xml = (XMLLoggingInInterceptor) client.getInInterceptors().get(0);
			JsonObject jo = Utils.getJsonUtility().fromJSONtoJson(XML.toJSONObject(xml.getBuffer().toString()));
			response = makeResponse(Response.Status.OK, jo);	
			/*		}catch (Fault e) {
	    	response = makeResponse(Response.Status.INTERNAL_SERVER_ERROR, e.getCause()!=null?e.getCause().toString():e.toString());	
			 */		}catch (ConnectException e) {
				 e.printStackTrace();
				 response = makeResponse(Response.Status.REQUEST_TIMEOUT,e.toString());	
			 }catch (InvalidNameException e) {
				 e.printStackTrace();
				 response = makeResponse(Response.Status.NOT_FOUND,e.toString());	
			 }catch (Throwable e) {
				 e.printStackTrace();
				 response = makeResponse(Response.Status.BAD_REQUEST,e.toString());	
			 }finally {
				 if(client!=null) client.destroy();
			 }
		return response;

	}

}
