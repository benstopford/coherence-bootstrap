package com.benstopford.coherence.bootstrap.structures.framework.cluster;

import java.io.*;

/**
 * BTS, 05-Feb-2009
 */
public class PersistentPortTracker {
    private static final String PORT_FILE_NAME = "port";

    public void incrementExtendPort() {
        int port = readPort();
        setPort(port, "com.benstopford.extend.port");
    }
    public void incrementExtendPort(String prop) {
        int port = readPort();
        setPort(port, prop);
    }

    private void setPort(int port, String property) {
        port++;
        System.setProperty(property, String.valueOf(port));
        writePort(port);
    }

    private void writePort(int port) {
    	BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(PORT_FILE_NAME));
            out.write(String.valueOf(port));
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
        	if(out!=null){
        		try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        }
    }

    private int readPort() {
    	BufferedReader in = null;
        int port = 32000;
        try {
            in = new BufferedReader(new FileReader(PORT_FILE_NAME));
            String str;
            while ((str = in.readLine()) != null) {
                port = Integer.valueOf(str);
            }
            in.close();
        } catch (FileNotFoundException e) {
            System.err.println("Could not find port file " + PORT_FILE_NAME + " so using default of " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
        	if(in!=null){
        		try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        }
        return port;
    }
}
