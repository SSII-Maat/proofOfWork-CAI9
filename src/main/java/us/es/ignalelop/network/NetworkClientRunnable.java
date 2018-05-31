package us.es.ignalelop.network;

import java.net.Socket;

public abstract class NetworkClientRunnable {
    public abstract String sendingRun(Socket clientSocket) throws Exception;
    public abstract String receivingRun(String response, Socket clientSocket) throws Exception;
}