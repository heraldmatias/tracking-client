package edu.upc.trackingclient.tasks;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class ListRoutesHelper {
	private static final String SERVICE_HOST = "http://10.0.2.2:8050/ServiceTracking/webresources/detalleruta/byconductor/";
	private static final int HTTP_STATUS_OK = 200;
	private static final byte[] buff = new byte[1024];
	
	public static class ApiException extends Exception{
		private static final long serialVersionUID = 1L;
		
		public ApiException(String msg){
			super(msg);
		}
		
		public ApiException(String msg, Throwable thr){
			super(msg, thr);
		}
	}
	
	protected static synchronized String downloadFromServer(String... params) throws ApiException{
		String retval = null;
		HttpClient client = new DefaultHttpClient();
		String id = params[0];
		HttpGet request = new HttpGet(SERVICE_HOST+id);
		Log.d("API", "va leer url");
		try {
			HttpResponse response = client.execute(request);
			StatusLine status = response.getStatusLine();
			if(status.getStatusCode()!=HTTP_STATUS_OK){
				throw new ApiException("No se pudo cargar la lista de pedidos");
			}
			Log.d("API", "leyendo url");
			HttpEntity entity = response.getEntity();
			InputStream ist = entity.getContent();
			ByteArrayOutputStream content = new ByteArrayOutputStream();
			Log.d("API", "cargar el json");
			int readCount = 0;
			while((readCount=ist.read(buff)) != -1){
				content.write(buff, 0, readCount);
			}
			retval = new String(content.toByteArray());
			Log.d("API-VALOR", retval);
		} catch (Exception e) {
			Log.d("API", "no se pudo conectar");
			throw new ApiException("Ocurrio un error al conectar al servidor" + e.getMessage(), e);
		}
		
		return retval;
	}
	
	
	
}
