package com.networkcontacts.servicelayer.server;

import java.net.InetAddress;

import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.deploy.PropertiesConfigurationManager;
import org.eclipse.jetty.deploy.providers.WebAppProvider;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.jsr356.server.ServerContainer;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import com.networkcontacts.servicelayer.core.models.engine.WebSocket;
import com.networkcontacts.servicelayer.core.utils.CacheConfig;
import com.networkcontacts.servicelayer.core.utils.Const;
import com.networkcontacts.servicelayer.core.utils.Utils;

public class StartEnv {
	private final String WEBAPP_RESOURCES_LOCATION = "/webapp";

	public void start() throws Throwable {

		CacheConfig.getInstance().setConfig(Utils.parseConfig().get("config").getAsJsonObject());
		if(!Utils.isPortInUse(InetAddress.getLocalHost().getHostAddress(), CacheConfig.getInstance().getServiceConfig().get("start_port").getAsInt())) {
			/*
			 * E'importante configurare il massimo numero di thread eseguibili all'interno del contenitore.
			 */
			QueuedThreadPool threadPool = new QueuedThreadPool();
			threadPool.setMaxThreads(500);

			Server jettyServer = new Server(threadPool);

			/*
			 * Abilita il server a leggere e comprendere le annotazioni.
			 */
			org.eclipse.jetty.webapp.Configuration.ClassList classlist = org.eclipse.jetty.webapp.Configuration.ClassList.setServerDefault(jettyServer);
			classlist.addAfter("org.eclipse.jetty.webapp.FragmentConfiguration", "org.eclipse.jetty.plus.webapp.EnvConfiguration", "org.eclipse.jetty.plus.webapp.PlusConfiguration");
			classlist.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration", "org.eclipse.jetty.annotations.AnnotationConfiguration");

			/*
			 * Lo scheduler serve a monitorare la directory preposta al deploy (webapps), cosi che quando la versione del WAR è nuova
			 * la sostituazione del codice viene eseguita 'a caldo'.
			 */
			jettyServer.addBean(new ScheduledExecutorScheduler());

			HttpConfiguration http_config = new HttpConfiguration();
			http_config.setSecureScheme("https");
			http_config.setSecurePort(CacheConfig.getInstance().getServiceConfig().get("secure_port").getAsInt());
			http_config.setOutputBufferSize(32768);
			http_config.setRequestHeaderSize(8192);
			http_config.setResponseHeaderSize(8192);
			http_config.setSendServerVersion(true);
			http_config.setSendDateHeader(false);

			ServerConnector http = new ServerConnector(jettyServer, new HttpConnectionFactory(http_config));
			http.setPort(CacheConfig.getInstance().getServiceConfig().get("start_port").getAsInt());
			jettyServer.addConnector(http);

			/*
			 * Prevediamo la configurazione SSL ma è necessario comprenderne il funzionamento...per cui è TEMPORANEAMENTE SOSPESO!
			 */
			SslContextFactory sslContextFactory = new SslContextFactory();
			sslContextFactory.setKeyStorePath(Utils.getPropertiesUtility().getProperty(Const.Properties.BASE_PATH) + "/etc/keystore");
			sslContextFactory.setKeyStorePassword("OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4");
			sslContextFactory.setKeyManagerPassword("OBF:1u2u1wml1z7s1z7a1wnl1u2g");
			sslContextFactory.setTrustStorePath(Utils.getPropertiesUtility().getProperty(Const.Properties.BASE_PATH) + "/etc/keystore");
			sslContextFactory.setTrustStorePassword("OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4");
			sslContextFactory.setExcludeCipherSuites("SSL_RSA_WITH_DES_CBC_SHA",
					"SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA",
					"SSL_RSA_EXPORT_WITH_RC4_40_MD5",
					"SSL_RSA_EXPORT_WITH_DES40_CBC_SHA",
					"SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA",
					"SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA");

			// SSL HTTP Configuration
			HttpConfiguration https_config = new HttpConfiguration(http_config);
			https_config.addCustomizer(new SecureRequestCustomizer());

			// SSL Connector
			ServerConnector sslConnector = new ServerConnector(jettyServer,
					new SslConnectionFactory(sslContextFactory,HttpVersion.HTTP_1_1.asString()),
					new HttpConnectionFactory(https_config));
			sslConnector.setPort(CacheConfig.getInstance().getServiceConfig().get("secure_port").getAsInt());
			//  jettyServer.addConnector(sslConnector);

			/*
			 * Questo contesto riferisce l'ambiente generale del container esposto 
			 * sotto la context root "NC_INFORMATION_RETRIEVAL"
			 * E' il contesto in cui sono esposti i servizi web che servono per interrogare il container.
			 * */
			WebAppContext webAppContext = new WebAppContext();
			String webxmlLocation = StartEnv.class.getResource(WEBAPP_RESOURCES_LOCATION + "/WEB-INF/web.xml").toString();
			webAppContext.setDescriptor(webxmlLocation);
			String resLocation = StartEnv.class.getResource(WEBAPP_RESOURCES_LOCATION).toString();
			webAppContext.setResourceBase(resLocation);
			webAppContext.getServletContext().setExtendedListenerTypes(true);
			/*
			 * La classe ContextListener implementa la logica che allo start-up dell'ambiente individua il main container
			 * o ne elegge uno tra quelli attivi.
			 * */
			//	    webAppContext.addEventListener(new ContextListener());
			//	    webAppContext.addEventListener(new ExecutionDelegate());
			webAppContext.setContextPath("/servicelayer");

			/*
			 * Questo contesto implementa un WebSocket allo start-up dell'ambiente
			 */
			ServletContextHandler webSocketcontext = new ServletContextHandler(ServletContextHandler.SESSIONS);
			webSocketcontext.setContextPath("/servicelayerSocket"); 

			ContextHandlerCollection contexts = new ContextHandlerCollection();
			contexts.setHandlers(new Handler[] { webAppContext , webSocketcontext});

			/*
			 * Il deployer ha senso SSE un server è contenitore di applicazioni e non solo esecutore di servizi.
			 **/
			DeploymentManager deployer = new DeploymentManager();
			deployer.setContexts(contexts);
			deployer.setContextAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",".*/servlet-api-[^/]*\\.jar$");

			/*
			 * webapp_provider implementa la logica del deploy per webApp esterne al server.
			 * Il provider interroga la directory DEPLOY_PATH, individua tutti gli archivi presenti e li rende disponibili nel server,
			 * la context root di ciascun WAR coincide con il nome del file (privo dell'estensione .war).
			 * 
			 */
			WebAppProvider webapp_provider = new WebAppProvider();
			webapp_provider.setMonitoredDirName(Utils.getPropertiesUtility().getProperty(Const.Properties.DEPLOY_PATH));
			webapp_provider.setScanInterval(1);
			webapp_provider.setExtractWars(true);
			webapp_provider.setConfigurationManager(new PropertiesConfigurationManager());
			deployer.addAppProvider(webapp_provider);
			jettyServer.addBean(deployer);

			jettyServer.setHandler(contexts);

			ServerContainer wscontainer = WebSocketServerContainerInitializer.configureContext(webSocketcontext);
			wscontainer.addEndpoint(WebSocket.class);

			jettyServer.start();
			System.out.println("Application started...");
			/*		Utils.getLogUtility().info("JETTY DEFAULT avviato correttamente sulla porta " + CacheConfig.getInstance().getServiceConfig().get("start_port"), 
				Const.Log.HANDLER_PREFIX);*/

			ContextMonitor monitor = new ContextMonitor(CacheConfig.getInstance().getServiceConfig().get("stop_port").getAsInt(), new Server[]{jettyServer});

			monitor.start();

			jettyServer.join();
		} else {
			System.out.print("Port already is use: Application not started!!!\n");
			System.exit(-1);
		}
	}


	public static void main(String[] args) {
		StartEnv startEnv = new StartEnv();
		try {
			startEnv.start();			
		} catch (Throwable e) {
			/*		Utils.getLogUtility().error("Impossibile avviare JETTY DEFAULT ! \n" + e, 
										Const.Log.HANDLER_PREFIX); */
			e.printStackTrace();
		}
	}

}
