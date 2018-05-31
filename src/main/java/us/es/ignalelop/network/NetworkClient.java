package us.es.ignalelop.network;

public abstract class NetworkClient {
    protected NetworkClientRunnable runnable;

    public NetworkClient(NetworkClientRunnable runnable) {
        // Inicialización de contenido
        this.runnable = runnable;
    }

    public abstract void init();
}