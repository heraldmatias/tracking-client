package edu.upc.trackingclient.helpers;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.NameValuePair;


import android.util.Log;

public class AuthHelper {

	private static final String SERVICE_HOST = "http://50.116.43.119:8080/ServiceTracking-1.0-SNAPSHOT/webresources/conductor/auth";
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
	
	public static synchronized String auth(String... _params) throws ApiException{
		String retval = null;
		HttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(SERVICE_HOST);
		Log.d("API", "va leer url");
		try {
			String user = _params[0];
			String clave = _params[1];
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("user", user));
			params.add(new BasicNameValuePair("pass", clave));
			request.setEntity(new UrlEncodedFormEntity(params));			
			HttpResponse response = client.execute(request);
			StatusLine status = response.getStatusLine();
			if(status.getStatusCode()!=HTTP_STATUS_OK){
				throw new ApiException("No se pudo cargar la lista de pedidos"+status.getStatusCode());
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
			Log.d("API", "no se pudo conectar "+e.getMessage());
			throw new ApiException("Ocurrio un error al conectar al servidor" + e.getMessage(), e);
		}
		
		return retval;
	}
}
