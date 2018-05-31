package us.es.ignalelop;

import java.net.InetAddress;
import java.net.UnknownHostException;

import us.es.ignalelop.network.IClient;
import us.es.ignalelop.network.IServer;

public class Main {
    public static void main(String[] args) {
        Thread clientThread = new Thread(new Runnable(){
        
            @Override
            public void run() {
                Client clientImpl = new Client();

                InetAddress address = null;

                try {
                    address = InetAddress.getLocalHost();
                } catch(UnknownHostException uhe) {

                }

                IClient client = new IClient(clientImpl, address, 8080);
            }

        });
        clientThread.setName("Client");

        Thread serverThread = new Thread(new Runnable(){
        
            @Override
            public void run() {
                Server serverImpl = new Server();
                IServer server = new IServer(serverImpl, 8080);
            }

        });
        serverThread.setName("Server");

        try {
            serverThread.start();
            clientThread.start();

            serverThread.join();
            clientThread.join();
        } catch(InterruptedException ie) {

        }
    }
}