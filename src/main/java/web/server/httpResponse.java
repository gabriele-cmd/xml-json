package web.server;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Date;

public class httpResponse {
    PrintWriter out = null;
    OutputStream dataOut = null;
    String fileRequested = null;
    int fileLength = 0;
    String content = null;
    File file = null;

    private void response(PrintWriter out, OutputStream dataOut, String fileRequested, int httpCode, int fileLength, byte[] fileData, String content, File file) throws IOException{
        if(httpCode == 200){
            out.println("HTTP/1.1 200 OK"); //status code 200: TUTTO OK
        }else if(httpCode == 301){
            out.println("HTTP/1.1 301 REINDIRIZZATO"); //status code 301: RISORSA SPOSTATA
        }else if(httpCode == 404){
            out.println("HTTP/1.1 404 File Not Found"); //status code 404: FILE NON TROVATO
        }else if(httpCode == 501){
            out.println("HTTP/1.1 501 Not Implemented"); //status code con 501: ERRORE SERVER
        }

        out.println("Server: Java HTTP Server from SSaurel : 1.0");
		//out.println("Date: " + new Date());
		out.println("Location: " + fileRequested);
		out.println("Content-type: " + content);
		out.println("Content-length: " + fileLength);
		out.println(); //per far capire che stiamo passando dagli header al contenuto si usa DOPPIO SPAZIO!
		out.flush(); // flush character output stream buffer
		
		dataOut.write(fileData, 0, fileLength);
		dataOut.flush();
    }
    
}
