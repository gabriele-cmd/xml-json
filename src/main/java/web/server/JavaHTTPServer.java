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
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


public class JavaHTTPServer implements Runnable{ 

	//inizializzo le classi per usare le varie funzioni
	ContentType contentType = new ContentType();

	
	static final File WEB_ROOT = new File("src/main/resources"); //path
	static final String DEFAULT_FILE = "index.html"; //pagina di default
	static final String FILE_NOT_FOUND = "pages/404.html"; //pagina di errore Client
	static final String METHOD_NOT_SUPPORTED = "not_supported.html"; //pagina di errore del metodo (errore Server)

	public File getWebRoot(){
		return WEB_ROOT;
	}

	public String getMethodnotSupp(){
		return METHOD_NOT_SUPPORTED;
	}
	
	static final int PORT = 8080; //porta per la connessione socket
	
	static final boolean verbose = true; //modalità verbose che fornisce ulteriori dettagli e logs su quello che il computer sta facendo e su ciò che i driver stanno caricando
	
	private Socket connect; //socket per la connessione CLIENT-SERVER 
	
    //costruttore con istanza del SOCKET (porta usata = 8080)
	public JavaHTTPServer(Socket c) {
		connect = c;
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
			

			String content = null;

			//GET e HEAD sono gli unici metodi supportati, altrimenti ERRORE del SERVER
			if (!method.equals("GET")  &&  !method.equals("HEAD")) {
				if (verbose) {
					System.out.println("501 Not Implemented : " + method + " method.");
				}
				
				//501 SERVER ERROR HEADERS
				serverError(out, dataOut);
				
			} else {
				
				if(fileRequested.endsWith("/")){
					fileRequested.substring(0, fileRequested.length()-1);
				}

				if(fileRequested.endsWith(".json/")){
					root value = XmlDeserializer();
					JSONSerializer(value);
				}
 				
				//metodi utilizzabili GET o HEAD
				if (fileRequested.endsWith("/")) {
					fileRequested += DEFAULT_FILE; //aggiunge index.html all'url

					File file = new File(WEB_ROOT, fileRequested);
					int fileLength = (int) file.length();
					content = contentType.getContentType(fileRequested);

					if (method.equals("GET")) { //il metodo GET ci reindirizza correttamente
						//200 OK HEADERS
						fileOK(out, dataOut, fileRequested, fileLength, content, file);
					}

				}
				else{

					File file = new File(WEB_ROOT, fileRequested);
					int fileLength = (int) file.length();
					content = contentType.getContentType(fileRequested);
					
					if(file.isFile() && file.exists()){
						if(method.equals("GET"))
						//200 OK HEADERS
						fileOK(out, dataOut, fileRequested, fileLength, content, file);

						if (verbose) {
							System.out.println("File " + fileRequested + " of type " + content + " returned");
						}

					}else{
						if(fileRequested.endsWith(".html") || fileRequested.endsWith(".css") || fileRequested.endsWith(".js") || fileRequested.endsWith(".jpg") || fileRequested.endsWith(".png") || fileRequested.endsWith(".gif") || fileRequested.endsWith("jpeg") || fileRequested.endsWith("webp")){
							//404 ERROR HEADERS
							fileNotFound(out, dataOut, fileRequested);

						}else{
							//301 FILE MOVED HEADERS
							fileMoved(out, dataOut, fileRequested);
						}
					}
				}			
			}
			
		} catch (FileNotFoundException fnfe) { //se quindi il fileRequested non esiste, genera un'eccezione che viene gestita come seguito
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
	public byte[] readFileData(File file, int fileLength) throws IOException {
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

	//quando avviene un Errore del SERVER restituisce questi header
	private void serverError(PrintWriter out, OutputStream dataOut) throws IOException{
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
	}

	//quando il file è OK restituisce questi header
	private void fileOK(PrintWriter out, OutputStream dataOut, String fileRequested, int fileLength, String content, File file) throws IOException{
		byte[] fileData = readFileData(file, fileLength);
						
		//invia gli Headers
		out.println("HTTP/1.1 200 OK"); //status code 200: TUTTO OK
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

	//quando la risorsa richiesta è SPOSTATA restituisce questi header
	private void fileMoved(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException{
		fileRequested += "/";
		//invia gli Headers
		out.println("HTTP/1.1 301 REINDIRIZZATO"); //status code 301: RISORSA SPOSTATA
		out.println("Server: Java HTTP Server from SSaurel : 1.0");
		out.println("Date: " + new Date());
		out.println("Location: " + fileRequested);
		out.println(); //per far capire che stiamo passando dagli header al contenuto si usa DOPPIO SPAZIO!
		out.flush(); //flush character output stream buffer
						
		dataOut.flush();
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
		out.println("Location: " + fileRequested);
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

	private root XmlDeserializer() throws JsonParseException, JsonMappingException, IOException{
		File file = new File("src/main/resources/classe.xml"); //indico il percorso del file da deserializzare
        XmlMapper xmlMapper = new XmlMapper();
        root value = xmlMapper.readValue(file, root.class);
		return value;

	}
	
	private void JSONSerializer(root value) throws JsonParseException, JsonMappingException, IOException{
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT); //Stampo le stringhe una sotto l'altra
		objectMapper.writeValue(new File("src/main/resources/classe.json"), value);
		

		//System.out.println(newFile);
	}
}
