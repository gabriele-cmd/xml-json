package web.server;


import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;


public class JavaHTTPServer implements Runnable{ 
	
	static final File WEB_ROOT = new File("./src/main/resources"); //path
	static final String DEFAULT_FILE = "index.html"; //pagina di default
	static final String FILE_NOT_FOUND = "404.html"; //pagina di errore Client
	static final String METHOD_NOT_SUPPORTED = "not_supported.html"; //pagina di errore del metodo (errore Server)
	
	static final int PORT = 8080; //porta per la connessione socket
	
	static final boolean verbose = true; //modalità verbose che fornisce ulteriori dettagli e logs su quello che il computer sta facendo e su ciò che i driver stanno caricando
	
	private Socket connect; //socket per la connessione CLIENT-SERVER 
	
    //costruttore con istanza del SOCKET (porta usata = 8080)
	public JavaHTTPServer(Socket c) {
		connect = c;
	}
	
	public static void main(String[] args) {

		try {
			ServerSocket serverConnect = new ServerSocket(8080);
			System.out.println("Server started.\nListening for connections on port : " + JavaHTTPServer.PORT + " ...\n");
			
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

	@Override
	public void run() {
		//gestione di una singola connessione di un client
		BufferedReader in = null; 
		PrintWriter out = null; 
		BufferedOutputStream dataOut = null;
		String fileRequested = null;
		
		try {
			in = new BufferedReader(new InputStreamReader(connect.getInputStream())); //inizializzo stream di INPUT
			out = new PrintWriter(connect.getOutputStream()); //inizializzo stream di OUTPUT per gli Headers
			dataOut = new BufferedOutputStream(connect.getOutputStream()); //inizializzo stream di OUTPUT verso il Client per i file richiesti
			
			String input = in.readLine(); //prendo l'input da tastiera del Client (prima RICHIESTA Client)
			StringTokenizer parse = new StringTokenizer(input);// we parse the request with a string tokenizer
			String method = parse.nextToken().toUpperCase(); //si prende il metodo usato  dal Client (UpperCase per renderlo nel formato GET o HEAD)
			fileRequested = parse.nextToken().toLowerCase(); //si prende il file richiesto (fileRequested)
			
			//GET e HEAD sono gli unici metodi supportati, altrimenti ERRORE del SERVER
			if (!method.equals("GET")  &&  !method.equals("HEAD")) {
				if (verbose) {
					System.out.println("501 Not Implemented : " + method + " method.");
				}
				
				//file non supportati
				File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);
				int fileLength = (int) file.length();
				String contentMimeType = "text/html";
				byte[] fileData = readFileData(file, fileLength);
					
				//invio Header
				out.println("HTTP/1.1 501 Not Implemented"); //status code con 501: ERRORE SERVER
				out.println("Server: Java HTTP Server from SSaurel : 1.0");
				out.println("Date: " + new Date());
				out.println("Content-type: " + contentMimeType);
				out.println("Content-length: " + fileLength);
				out.println(); //per far capire che stiamo passando dagli header al contenuto si usa DOPPIO SPAZIO!
				out.flush(); // flush character output stream buffer
				// file
				dataOut.write(fileData, 0, fileLength);
				dataOut.flush();
				
			} else {

				File file = new File(WEB_ROOT, fileRequested);
				int fileLength = (int) file.length();
				String content = getContentType(fileRequested);

				//metodi utilizzabili GET o HEAD
				if (fileRequested.endsWith("/")) {
					fileRequested += DEFAULT_FILE; //aggiunge index.html all'url

				}else{
					byte[] fileData = readFileData(file, fileLength);
					fileRequested += "/";

					//invia gli Headers
					out.println("HTTP/1.1 301 REINDIRIZZATO"); //status code 301: RISORSA SPOSTATA
					out.println("Server: Java HTTP Server from SSaurel : 1.0");
					out.println("Date: " + new Date());
					out.println("Location: " + fileRequested);
					out.println("Content-type: " + content);
					out.println("Content-length: " + fileLength);
					out.println(); //per far capire che stiamo passando dagli header al contenuto si usa DOPPIO SPAZIO!
					out.flush(); //flush character output stream buffer
					
					dataOut.write(fileData, 0, fileLength);
					dataOut.flush();
				}
				
				if (method.equals("GET")) { //il metodo GET ci reindirizza correttamente
					byte[] fileData = readFileData(file, fileLength);
					
					//invia gli Headers
					out.println("HTTP/1.1 200 OK"); //status code 200: TUTTO OK
					out.println("Server: Java HTTP Server from SSaurel : 1.0");
					out.println("Date: " + new Date());
					out.println("Content-type: " + content);
					out.println("Content-length: " + fileLength);
					out.println(); //per far capire che stiamo passando dagli header al contenuto si usa DOPPIO SPAZIO!
					out.flush(); //flush character output stream buffer
					
					dataOut.write(fileData, 0, fileLength);
					dataOut.flush();
				}
				
				if (verbose) {
					System.out.println("File " + fileRequested + " of type " + content + " returned");
				}
				
			}
			
		} catch (FileNotFoundException fnfe) { //se quindi il fileRequested non esista, genere un'eccezione che viene gestita come seguito
			try {
				fileNotFound(out, dataOut, fileRequested);
			} catch (IOException ioe) {
				System.err.println("Error with file not found exception : " + ioe.getMessage());
			}
			
		} catch (IOException ioe) {
			System.err.println("Server error : " + ioe);
		} finally {
			try {
				in.close();
				out.close();
				dataOut.close();
				connect.close(); //chiusura socket e connessione
			} catch (Exception e) {
				System.err.println("Error closing stream : " + e.getMessage());
			} 
			
			if (verbose) {
				System.out.println("Connection closed.\n");
			}
		}
		
		
	}
	
	//legge un file fornito in Input, prendendo come parametri anche la sua lunghezza in byte
	private byte[] readFileData(File file, int fileLength) throws IOException {
		FileInputStream fileIn = null;
		byte[] fileData = new byte[fileLength];
		
		try {
			fileIn = new FileInputStream(file);
			fileIn.read(fileData);
		} finally {
			if (fileIn != null) 
				fileIn.close();
		}
		
		return fileData;
	}
	
	//ritorna il tipo di contenuto supportato ma conta solo se il file è di tipo HTM o HTML
	private String getContentType(String fileRequested) {
		if (fileRequested.endsWith(".htm")  ||  fileRequested.endsWith(".html"))
			return "text/html";
		else
			return "text/plain";
	}
	
	//quando il file richiesto non esiste da questo errore
	private void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException {
		File file = new File(WEB_ROOT, FILE_NOT_FOUND);
		int fileLength = (int) file.length();
		String content = "text/html";
		byte[] fileData = readFileData(file, fileLength);
		
		out.println("HTTP/1.1 404 File Not Found"); //status code 404: FILE NON TROVATO
		out.println("Server: Java HTTP Server from SSaurel : 1.0");
		out.println("Date: " + new Date());
		out.println("Content-type: " + content);
		out.println("Content-length: " + fileLength);
		out.println(); //per far capire che stiamo passando dagli header al contenuto si usa DOPPIO SPAZIO!
		out.flush(); // flush character output stream buffer
		
		dataOut.write(fileData, 0, fileLength);
		dataOut.flush();
		
		if (verbose) {
			System.out.println("File " + fileRequested + " not found");
		}
	}
	
}
