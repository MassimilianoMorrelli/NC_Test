package com.networkcontacts.servicelayer.core.models.general;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.naming.InvalidNameException;
import javax.ws.rs.core.Context;

import org.apache.cxf.binding.Binding;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.transport.http.HTTPConduit;

import com.google.gson.JsonObject;
import com.greenbird.xml.prettyprinter.PrettyPrinter;
import com.greenbird.xml.prettyprinter.PrettyPrinterFactory;
import com.networkcontacts.servicelayer.services.interceptors.XMLLoggingInInterceptor;
import com.networkcontacts.utils.REST.RestServiceGeneral;

public class SOAPGeneralService extends RestServiceGeneral{
	
	@Context
	private Client client;
	private XMLLoggingInInterceptor xmlLoggingInInterceptor;
	
	private void isOperation(String name) throws Throwable{
		if(!getOperatioName().contains(name)) throw new InvalidNameException("'" + name + "' is invalid function name.");
	}
	
	public Client getClient(String url) throws Throwable{
    	JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
    	
    	List<String> bindingFiles = new ArrayList<String>();
    	bindingFiles.add("binding.xml"); 
    	
    	URL wsdlURL = new URL(url);
    	client = dcf.createClient(wsdlURL, getClass().getClassLoader(), bindingFiles);

    	client.getRequestContext().put("javax.xml.ws.client.receiveTimeout", 120000);// 120 seconds
        client.getRequestContext().put("javax.xml.ws.client.connectionTimeout", 120000);// 120 seconds
        
        LoggingOutInterceptor loggingOutInterceptor = new LoggingOutInterceptor();
        loggingOutInterceptor.setPrettyLogging(true);
        
        PrettyPrinterFactory factory = PrettyPrinterFactory.newInstance();
        factory.setXmlDeclaration(false);
        PrettyPrinter prettyPrinter = factory.newPrettyPrinter(); 
        xmlLoggingInInterceptor = new XMLLoggingInInterceptor(prettyPrinter);
        
        client.getInInterceptors().add(xmlLoggingInInterceptor);
        client.getOutInterceptors().add(loggingOutInterceptor);
        
        HTTPConduit httpConduit = (HTTPConduit) client.getConduit();
        TLSClientParameters tlsClientParameters = new TLSClientParameters();
		tlsClientParameters.setDisableCNCheck(true);
		httpConduit.setTlsClientParameters(tlsClientParameters);
        
    	return client;
	}

	
	public List<String> getOperatioName() throws Throwable{
		List<String> allFuncions = new ArrayList<String>();
    	Endpoint endpoint =  client.getEndpoint();
    	Binding binding = endpoint.getBinding();;
    	BindingInfo bindingInfo = binding.getBindingInfo(); 
    	Collection<BindingOperationInfo> operations = bindingInfo.getOperations();
    	for (BindingOperationInfo boi : operations) {
            OperationInfo oi = boi.getOperationInfo();
            allFuncions.add(oi.getName().getLocalPart());
    	}
		return allFuncions;
	}
	
	public List<?> getPartClass(String functionName) throws Throwable{
		isOperation(functionName);
		List classes = new ArrayList();
    	Endpoint endpoint =  client.getEndpoint();
    	Binding binding = endpoint.getBinding();
    	BindingInfo bindingInfo = binding.getBindingInfo(); 
    	Collection<BindingOperationInfo> operations = bindingInfo.getOperations();
    	for (BindingOperationInfo boi : operations) {
            OperationInfo oi = boi.getOperationInfo();
    		if(functionName.equals(oi.getName().getLocalPart())){
                classes.add(introspectPart(boi.getInput()));
                classes.add(introspectPart( boi.getOutput()));
                break;
    		}
    	}
    	return classes;
	}
	
	private Object introspectPart(BindingMessageInfo bindingMessageInfo){
		List<MessagePartInfo> parts = bindingMessageInfo.getMessageParts();		
        /*
         * Le classi di I/O sono suddivise in parti. 
         * In genere è la prima di queste parti ad essere signifcativa: inputParts.get(0).
         * Può succedere però che inputParts.get(0) sia un classe che rappresenta un tipo primitivo,
         * questo significa che l'oggetto di I/O non è rappresentato da una classe, ma da singoli parametri:
         * 					F(val1, val2, ..., valn) invece di F(Object) 
         */
        if(isPrimitiveOrPrimitiveWrapperOrString(parts.get(0).getTypeClass())){
        	JsonObject jsonObject = new JsonObject();
        	for(MessagePartInfo part: parts){
        		jsonObject.addProperty(part.getConcreteName().toString(), "[" + part.getTypeClass() + "]");
        	}
        	return jsonObject;
        }else{
            Class<?> inputClass = parts.get(0).getTypeClass();  
            return inputClass;
        }
	}
	
	protected boolean isPrimitiveOrPrimitiveWrapperOrString(Class<?> type) {
	    return (type.isPrimitive() && type != void.class) ||
	        type == Double.class || type == Float.class || type == Long.class ||
	        type == Integer.class || type == Short.class || type == Character.class ||
	        type == Byte.class || type == Boolean.class || type == String.class;
	}
}
