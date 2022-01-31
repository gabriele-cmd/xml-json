package web.server;

public class ContentType {
    //ritorna il tipo di contenuto supportato ma conta solo se il file è di tipo HTM o HTML
	public String getContentType(String fileRequested) {
		if (fileRequested.endsWith(".htm")  ||  fileRequested.endsWith(".html"))
			return "text/html";

		else if(fileRequested.endsWith(".css"))
			return "text/css";

		else if(fileRequested.endsWith(".png"))
			return "image/png";

		else if(fileRequested.endsWith(".javascript") || fileRequested.endsWith(".js"))
			return "text/javascript";

		else if(fileRequested.endsWith(".jpeg"))
			return "image/jpeg";

		else if(fileRequested.endsWith(".webp"))
			return "image/webp";

		else if(fileRequested.endsWith(".gif"))
			return "image/gif";

		else if(fileRequested.endsWith(".jpg"))
			return "image/jpg";
		
		else if(fileRequested.endsWith(".json"))
			return "application/json";

		else if(fileRequested.endsWith(".xml"))
			return "application/xml";

		else 
			return "text/plain";
	}
}
