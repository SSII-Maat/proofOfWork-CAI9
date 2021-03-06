package us.es.ignalelop;

import java.awt.geom.IllegalPathStateException;
import java.net.Socket;
import java.security.MessageDigest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import us.es.ignalelop.network.ExitConditionException;
import us.es.ignalelop.network.NetworkClientRunnable;

public class Server extends NetworkClientRunnable {
    private int state = 0;
    private String resultMessage = "b6f6991d03df0e2e04dafffcd6bc418aac66049e2cd74b80f14ac86db1e3f0da";
    private int difficulty = 2;

    @Override
	public String sendingRun(Socket clientSocket) throws Exception {
        switch(this.state) {
            case 0:
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("message", resultMessage);
                jsonObject.addProperty("difficulty", difficulty);
                this.state++;

                return jsonObject.toString();
            case 1:
                return null;
            case 2:
                // Cerramos la conexión
                throw new ExitConditionException();
            default:
                // Condición rarisima, vamos a informar de error
                throw new IllegalPathStateException("Invalid logic path");
        }
	}

	@Override
	public String receivingRun(String response, Socket clientSocket) throws Exception {
        // Un único estado, que haya salido bien la operación de prueba
        JsonObject jsonObject = new JsonParser().parse(response).getAsJsonObject();
        // Si la respuesta no tiene status, en cuyo caso se trata de un error...
        if(jsonObject.get("status") == null) {
            String objective = new String(new char[this.difficulty]).replace('\0', '0');

            String receivedHash = jsonObject.get("hash").getAsString();
            int nonce = jsonObject.get("nonce").getAsInt();

            // Comprobamos si realmente esta operación es la correcta
            String convertedMessage = Integer.toString(nonce) + resultMessage;
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            String ourHash = Utils.getHex(messageDigest.digest(convertedMessage.getBytes()));

            JsonObject responseJson = new JsonObject();

            // Comprobamos si el hash calculado es el recibido y si cumple las condiciones
            if(ourHash.equals(receivedHash) && ourHash.substring(0, this.difficulty).equals(objective)) {
                // Éxito, retornamos que ha salido bien
                System.out.println("El nonce enviado es correcto");
                responseJson.addProperty("status", "OK");
                this.state++;
            } else {
                // Enviamos mensaje de fallo
                System.out.println("Ha fallado el nonce transmitido");
                responseJson.addProperty("status", "Error");
            }

            return responseJson.toString();
        } else {
            // El cliente reintentará realizar la operación, retornamos nada
            return null;
        }
	}
}