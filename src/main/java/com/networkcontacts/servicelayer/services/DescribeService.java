package com.networkcontacts.servicelayer.services;

import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.lang.model.SourceVersion;
import javax.naming.InvalidNameException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.interceptor.Fault;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.networkcontacts.servicelayer.core.models.engine.ServiceInfo;
import com.networkcontacts.servicelayer.core.models.general.SOAPGeneralService;
import com.networkcontacts.servicelayer.core.utils.Const;
import com.networkcontacts.servicelayer.core.utils.Utils;
import com.networkcontacts.utils.REST.RestServiceGeneral;

@Path("/describeService/")
public class DescribeService extends RestServiceGeneral {
	@POST
	@Path("/allFunction/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllFunction(String ServiceInfoInput) throws Throwable{
		Response response = null;
		Client client = null;
		try{
			JsonObject jInput = Utils.getJsonUtility().fromStringToJson(ServiceInfoInput);
			ServiceInfo serviceInfo = Utils.getJsonUtility().fromJsonToObject(jInput, ServiceInfo.class);
			SOAPGeneralService generalService = new SOAPGeneralService();

			client = generalService.getClient(serviceInfo.getWsdl());
			JsonElement allFunction = Utils.getJsonUtility().fromObjectToJson(generalService.getOperatioName());

			response = makeResponse(Response.Status.OK, allFunction);
		}catch (Fault e) {
			response = makeResponse(Response.Status.INTERNAL_SERVER_ERROR,e.getCause().toString());	
		}catch (InvalidNameException e) {
			response = makeResponse(Response.Status.NOT_FOUND,e.toString());	
		}catch (Throwable e) {
			e.printStackTrace();
			response = makeResponse(Response.Status.BAD_REQUEST,e.toString());	
		}finally {
			if(client!=null) client.destroy();
		}
		return response;
	}

	@POST
	@Path("/envelope/")
	@Produces({MediaType.APPLICATION_JSON})
	public Response serviceEnvelope(String ServiceInfoInput) throws Throwable{
		Client client =null;
		Response response = null;
		try{
			JsonObject jInput = Utils.getJsonUtility().fromStringToJson(ServiceInfoInput);
			ServiceInfo serviceInfo = Utils.getJsonUtility().fromJsonToObject(jInput, ServiceInfo.class);
			SOAPGeneralService generalService = new SOAPGeneralService();

			client =  generalService.getClient(serviceInfo.getWsdl());

			List<?> classes = generalService.getPartClass(serviceInfo.getOperationName());
			JsonElement objectTemplateJson = null;

			int index = "I".equalsIgnoreCase(serviceInfo.getType())?0:1;
			boolean recursiveFlag = "I".equalsIgnoreCase(serviceInfo.getType())?true:false;

			if(classes.get(index) instanceof Class<?>){
				Object objectTemplate = recursiveInstance( ((Class<?>)classes.get(index)).newInstance(), recursiveFlag);
				objectTemplateJson = Utils.getJsonUtility().fromObjectToJson(objectTemplate);
			}else{
				objectTemplateJson = (JsonObject)classes.get(index);
			}

			if("I".equalsIgnoreCase(serviceInfo.getType())){
				objectTemplateJson.getAsJsonObject().addProperty(Const.JsonConst.CREDENTIAL, "[" + JsonObject.class + "] Credenziali in forma di username e password (quando previste)");
				objectTemplateJson.getAsJsonObject().addProperty(Const.JsonConst.HEADER,  "[" + JsonObject.class + "] elementi da aggiungere alla soapenv:Header (quando previsti)");
				objectTemplateJson.getAsJsonObject().addProperty(Const.JsonConst.WSDL, serviceInfo.getWsdl());
			}

			response = makeResponse(Response.Status.OK, objectTemplateJson);
		}catch (Fault e) {
			response = makeResponse(Response.Status.INTERNAL_SERVER_ERROR,e.getCause().toString());	
		}catch (InvalidNameException e) {
			response = makeResponse(Response.Status.NOT_FOUND,e.toString());	
		}catch (Throwable e) {
			e.printStackTrace();
			response = makeResponse(Response.Status.BAD_REQUEST,e.toString());	
		}finally {
			if(client!=null) client.destroy();
		}
		return response;
	}

	private Object recursiveInstance(Object beanClazz, boolean inspectCollection) throws Throwable{
		PropertyDescriptor[] propertyDescriptors = Utils.getPropertyDescriptors(beanClazz);
		if(propertyDescriptors!=null && propertyDescriptors.length!=0){
			BeanUtils.describe(beanClazz);
			for(PropertyDescriptor pd: propertyDescriptors){
				String name = pd.getName();
				if("class".equals(name)) continue;
				Class propertyType = null;
				try{
					propertyType = pd.getPropertyType();
					if(Collection.class.isAssignableFrom(propertyType) && inspectCollection){
						String returnTypeName = pd.getReadMethod().getGenericReturnType().getTypeName();
						if(returnTypeName!=null){
							returnTypeName = returnTypeName.replaceAll("java.util.List<", "").replaceAll(">", "");
							if(SourceVersion.isName(returnTypeName)){
								Object element = Thread.currentThread().getContextClassLoader().loadClass(returnTypeName).newInstance(); 
								Collections.addAll((Collection)pd.getReadMethod().invoke(beanClazz), element);
								recursiveInstance(element, inspectCollection);
							}
						}
					}else if(Boolean.class.isAssignableFrom(propertyType)){
						pd.getWriteMethod().invoke(beanClazz, new Boolean(false));
					}else if(BigDecimal.class.isAssignableFrom(propertyType)){
						pd.getWriteMethod().invoke(beanClazz, new BigDecimal(0));
					}else if(Long.class.isAssignableFrom(propertyType)){
						pd.getWriteMethod().invoke(beanClazz, new Long(0));
					}else if(XMLGregorianCalendar.class.isAssignableFrom(propertyType)){
						pd.getWriteMethod().invoke(beanClazz, DatatypeFactory.newInstance().newXMLGregorianCalendar());
					}else{
						Object innerObject = propertyType.newInstance();
						pd.getWriteMethod().invoke(beanClazz, innerObject);
						recursiveInstance(innerObject, inspectCollection);
					}
				}catch (InstantiationException e) {
					//System.err.println(propertyType);
				}
			}
		}
		return beanClazz;
	}

}
