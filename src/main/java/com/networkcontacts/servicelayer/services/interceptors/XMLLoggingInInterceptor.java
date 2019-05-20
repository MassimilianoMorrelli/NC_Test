package com.networkcontacts.servicelayer.services.interceptors;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.headers.Header;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import com.greenbird.xml.prettyprinter.PrettyPrinter;

public class XMLLoggingInInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final String LOCAL_NAME = "MessageID";

    private static final int PROPERTIES_SIZE = 128;

    private String name;

    protected PrettyPrinter prettyPrinter = null;
    protected Logger logger;
    protected Level reformatSuccessLevel;
    protected Level reformatFailureLevel;
    protected StringBuilder buffer;


    public XMLLoggingInInterceptor() {
        this(LogUtils.getLogger(XMLLoggingInInterceptor.class), Level.INFO,  Level.WARNING);
    }

    public XMLLoggingInInterceptor(PrettyPrinter prettyPrinter) {
        this(LogUtils.getLogger(XMLLoggingInInterceptor.class), Level.INFO,  Level.WARNING);

        this.prettyPrinter = prettyPrinter;
    }

    public XMLLoggingInInterceptor(Logger logger, Level reformatSuccessLevel, Level reformatFailureLevel) {
        super(Phase.RECEIVE);
        this.logger = logger;
        this.reformatSuccessLevel = reformatSuccessLevel;
        this.reformatFailureLevel = reformatFailureLevel;
    }

    public XMLLoggingInInterceptor(PrettyPrinter prettyPrinter, Logger logger, Level reformatSuccessLevel, Level reformatFailureLevel) {
        this(logger, reformatSuccessLevel, reformatFailureLevel);
        this.prettyPrinter = prettyPrinter;
        this.logger = logger;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void handleMessage(Message message) throws Fault {

        if (!logger.isLoggable(reformatSuccessLevel)) {
             return;
        }

        InputStream in = message.getContent(InputStream.class);
        if (in == null) {
            return;
        }

        //StringBuilder buffer;

        CachedOutputStream cache = new CachedOutputStream();
        try {
            InputStream origIn = in;
            IOUtils.copy(in, cache);

            if (cache.size() > 0) {
                in = cache.getInputStream();
            } else {
                in = new ByteArrayInputStream(new byte[0]);
            }

            // set the inputstream back as message payload
            message.setContent(InputStream.class, in);

            cache.close();
            origIn.close();

            int contentSize = (int) cache.size();

            buffer = new StringBuilder(contentSize + PROPERTIES_SIZE);

            cache.writeCacheTo(buffer, "UTF-8");
        } catch (IOException e) {
            throw new Fault(e);
        }

        // decode chars from bytes
        char[] chars = new char[buffer.length()];
        buffer.getChars(0, chars.length, chars, 0);

        // reuse buffer
        buffer.setLength(0);

        // perform local logging - to the buffer 
       // buffer.append(name);

        logProperties(buffer, message);

        // pretty print XML
        if(prettyPrinter.process(chars, 0, chars.length, buffer)) {
            // log as normal
            logger.log(reformatSuccessLevel, buffer.toString());
        } else {
            // something unexpected - log as exception
            buffer.append(" was unable to format XML:\n");
            buffer.append(chars); // unmodified XML

            logger.log(reformatFailureLevel, buffer.toString());
        }
    }


    /**
     * Gets theMessageID header in the list of headers.
     *
     */
    protected String getIdHeader(Message message) {
        return getHeader(message, LOCAL_NAME);
    }

    protected String getHeader(Message message, String name) {
        List<Header> headers = (List<Header>) message.get(Header.HEADER_LIST);

        if(headers != null) {
            for(Header header:headers) {
                if(header.getName().getLocalPart().equalsIgnoreCase(name)) {
                    return header.getObject().toString();
                }
            }
        }
        return null;
    }        

    /**
     * Method intended for use within subclasses. Log custom field here.
     * 
     * @param message message
    */

    protected void logProperties(StringBuilder buffer, Message message) {
        final String messageId = getIdHeader(message);
        if(messageId != null) {
            buffer.append(" MessageId=");
            buffer.append(messageId);
        }
    }

    public void setPrettyPrinter(PrettyPrinter prettyPrinter) {
        this.prettyPrinter = prettyPrinter;
    }

    public PrettyPrinter getPrettyPrinter() {
        return prettyPrinter;
    }

    public Logger getLogger() {
        return logger;
    }

    public String getName() {
        return name;
    }

    public Level getReformatFailureLevel() {
        return reformatFailureLevel;
    }

    public Level getReformatSuccessLevel() {
        return reformatSuccessLevel;
    }

    public StringBuilder getBuffer(){
    	return buffer;
    }
    
    public void setReformatFailureLevel(Level reformatFailureLevel) {
        this.reformatFailureLevel = reformatFailureLevel;
    }

    public void setReformatSuccessLevel(Level reformatSuccessLevel) {
        this.reformatSuccessLevel = reformatSuccessLevel;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}