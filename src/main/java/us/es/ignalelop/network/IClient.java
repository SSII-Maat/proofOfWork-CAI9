package us.es.ignalelop.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import org.apache.log4j.Logger;

import us.es.ignalelop.network.ExitConditionException;
import us.es.ignalelop.network.NetworkClient;
import us.es.ignalelop.network.NetworkClientRunnable;

import org.apache.log4j.LogManager;

public class IClient extends NetworkClient {
    private final InetAddress address;
    private final int port;
    private Logger logger = LogManager.getRootLogger();

    public IClient(NetworkClientRunnable runnable, InetAddress address, int port) {
        super(runnable);
        this.address = address;
        this.port = port;
        init();
    }

    @Override
    public void init() {
        try {
            Socket clientSocket = new Socket(this.address, this.port);
            
            boolean exitCondition = true;
            BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            while(exitCondition && clientSocket.isConnected() &&
             !clientSocket.isOutputShutdown() && !clientSocket.isInputShutdown()) {
                try {
                    String response = super.runnable.sendingRun(clientSocket);
                    if(response != null) {
                        bw.write(response);
                        bw.newLine();
                        bw.flush();
                    }

                    String clientMessage = br.readLine();
                    if(clientMessage != null) {
                        response = super.runnable.receivingRun(clientMessage.trim(), clientSocket);
                        if(response != null) { 
                            bw.write(response);
                            bw.newLine();
                            bw.flush();
                        }
                    }
                } catch(ExitConditionException ece) {
                    logger.debug("Exit condition found");
                    exitCondition = false;
                    break;
                } catch(SocketException se) {
                    // La conexión se ha cerrado, cancelamos el bucle
                    exitCondition = false;
                } catch(Exception e) {
                    logger.warn("Exception thrown in client socket: ");
                    e.printStackTrace();
                    exitCondition = false;
                }
                
            }
            
            clientSocket.close();
        } catch(Exception e) {
            e.printStackTrace();
            logger.fatal("Exception: "+e.getMessage());
        }
        
    }
}