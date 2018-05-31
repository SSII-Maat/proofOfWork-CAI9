package us.es.ignalelop.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import javax.xml.namespace.QName;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import us.es.ignalelop.network.ExitConditionException;
import us.es.ignalelop.network.NetworkClient;
import us.es.ignalelop.network.NetworkClientRunnable;

public class IServer extends NetworkClient {
    private ServerSocket serverSocket;
    private final int port;
    private Logger logger = LogManager.getRootLogger();

    public IServer(NetworkClientRunnable runnable, int port) {
        super(runnable);
        this.port = port;
        init();
    }

    @Override   
    public void init() {
        try {
            this.serverSocket = new ServerSocket(this.port);

            while(true) {
                Socket clientSocket = this.serverSocket.accept();

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
                    } catch(SocketException se) {
                        // La conexión se ha cerrado, cancelamos el bucle
                        exitCondition = false;
                    } catch(Exception e) {
                        logger.warn("Exception thrown in server socket: ");
                        e.printStackTrace();
                    }
                    
                }
                clientSocket.close();
            }
        } catch(Exception e) {
            logger.fatal("Exception: "+e.getMessage());
        }
    }
    
}