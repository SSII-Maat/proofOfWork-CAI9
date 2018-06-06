package us.es.ignalelop;

import java.net.Socket;
import java.security.MessageDigest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import us.es.ignalelop.network.ExitConditionException;
import us.es.ignalelop.network.NetworkClientRunnable;

public class Client extends NetworkClientRunnable {
    private String message;
    private int difficulty;
    private int state = 0;

	@Override
	public String sendingRun(Socket clientSocket) throws Exception {
        if(this.state == 1) {
            // Realizamos la preparación del objeto
            JsonObject response = new JsonObject();
            // El objeto que realizará el hashing
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            // Y los datos de la iteración.
            long nonce = 0l;
            boolean notFound = true;
            String objetive = new String(new char[this.difficulty]).replace('\0', '0');
            
            while(notFound) {
                String iterationMessage = Long.toString(nonce) + message;
                byte[] iterationHash = messageDigest.digest(iterationMessage.getBytes());
                String iterationResult = Utils.getHex(iterationHash);
                if(iterationResult.substring(0, this.difficulty).equals(objetive)) {
                    // Hemos encontrado la combinación ganadora
                    notFound = false;
                    
                    response.addProperty("hash", iterationResult);
                    response.addProperty("nonce", nonce);

                    return response.toString();
                } else {
                    // Probamos otro resultado
                    nonce++;
                    // Si el nonce vuelve a ser 0, hemos pasado a números negativos, cancelamos el bucle
                    if(nonce == 0) {
                        // Indicamos que no se pudo realizar el hash
                        response.addProperty("status", "Error");
                        break;
                    }
                }
            }

            // Retornamos que no se haya podido hacer la prueba.
            return response.toString();
        } else {
            return null;
        }
        
	}

	@Override
	public String receivingRun(String response, Socket clientSocket) throws Exception {
        JsonObject jsonObject = new JsonParser().parse(response).getAsJsonObject();

        switch(this.state) {
            case 0:
                // Recibiremos el objeto JSON para empezar
                this.message = jsonObject.get("message").getAsString();
                this.difficulty = jsonObject.get("difficulty").getAsInt();
                this.state++;

                break;
            case 1:
                // Recibimos el estado de nuestra prueba
                String status = jsonObject.get("status").getAsString();
                System.out.println("Entra en resultado, estado: "+status);
                if(status.equals("OK")) {
                    // Todo bien, vamos a cerrar la conexión
                    System.out.println("Se ha realizado la prueba con éxito");
                    throw new ExitConditionException();
                } else {
                    // Hay un problema, se vuelve a intentar
                    this.state = 0;
                }
                break;
        }
        
		return null;
	}

}