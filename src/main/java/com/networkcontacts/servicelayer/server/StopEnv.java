package com.networkcontacts.servicelayer.server;

import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

import com.networkcontacts.servicelayer.core.utils.CacheConfig;
import com.networkcontacts.servicelayer.core.utils.Const;
import com.networkcontacts.servicelayer.core.utils.Utils;

public class StopEnv {
    public void stop() throws Throwable{
        try {
    		CacheConfig.getInstance().setConfig(Utils.parseConfig().get("config").getAsJsonObject());
            Socket s = new Socket(InetAddress.getByName(Const.Properties.LOCAL_ADDRESS), CacheConfig.getInstance().getServiceConfig().get("stop_port").getAsInt());
            s.setSoLinger(false, 0);
            OutputStream out = s.getOutputStream();
            out.write(("stop\r\n").getBytes());
            out.flush();
            s.close();
        } catch (ConnectException e) {
        	e.printStackTrace();
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }

	public static void main(String[] args) throws Throwable{
		// TODO Auto-generated method stub
		StopEnv stopEnv = new StopEnv();
		stopEnv.stop();
	}

}
