package web.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Date;

public class Main {
    public static void main(String[] args) throws IOException {

		try {
			ServerSocket serverConnect = new ServerSocket(8080);
			System.out.println("\nServer started.\nListening for connections on port : " + JavaHTTPServer.PORT + " ...\n");

			//ci mettiamo in ascolto di messaggi dal SERVER finché l'utente non CHIUDE la connessione
			while (true) {
				JavaHTTPServer myServer = new JavaHTTPServer(serverConnect.accept());
				
				if (JavaHTTPServer.verbose) {
					System.out.println("Connecton opened. (" + new Date() + ")");
				}
				
				//crea un THREAD dedicato per GESTIRE la CONNESSIONE di un client
				Thread thread = new Thread(myServer);
				thread.start();
			}
			
		} catch (IOException e) {
			System.err.println("Server Connection error : " + e.getMessage());
		}
	}
}
