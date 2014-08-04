package edu.upc.trackingclient.adapters;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class StatusCodes {

	static LinkedHashMap<String, String> statusCodes = new LinkedHashMap<String, String>();

	static {
		init();
	}
//	“Entregado”, “En espera del cliente” o “No entregado” 
//	 “problemas con el producto”, “cliente no habido”, “rechazo del producto” o “producto incompleto”
	public static void init() {
		statusCodes.put("Entregado", "A");
		statusCodes.put("En espera del cliente", "E");
		statusCodes.put("No entregado", "N");
		statusCodes.put("Problemas con el producto", "P");
		statusCodes.put("Cliente no habido", "H");
		statusCodes.put("Rechazo del producto", "R");
		statusCodes.put("Producto incompleto", "I");
	}

	public static String getCode(String param) {
		return statusCodes.get(param);
	}
	
	public static Integer getPositionFromCode(String param) {		
		Integer index = new ArrayList<String>(statusCodes.values()).indexOf(param);
		return index;
	}
	
	public static String getCodeFromPosition(Integer index) {		
		String item = new ArrayList<String>(statusCodes.values()).get(index);
		return item;
	}

}
