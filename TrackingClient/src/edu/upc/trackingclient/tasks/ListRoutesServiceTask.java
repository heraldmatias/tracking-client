package edu.upc.trackingclient.tasks;


import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.upc.trackingclient.ListRoutesActivity;
import edu.upc.trackingclient.R;
import edu.upc.trackingclient.entity.Route;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class ListRoutesServiceTask extends AsyncTask<String, Integer, String> {
	
	private ProgressDialog progDialog;
	private Context context;
	private ListRoutesActivity activity;
	
	public ListRoutesServiceTask(ListRoutesActivity activity){
		super();
		this.activity = activity;
		this.context = activity.getApplicationContext();
	}
	
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		progDialog = ProgressDialog.show(this.activity, "Cargando", this.context.getResources().getString(R.string.action_load_routes),true);
	}

	@Override
	protected String doInBackground(String... params) {
		// TODO Auto-generated method stub
		try {
			String result = ListRoutesHelper.downloadFromServer(params);
			return result;
		} catch (Exception e) {
			return new String();
		}
	}
	
	@Override
	protected void onPostExecute(String result) {
		ArrayList<Route> lista = new ArrayList<Route>();
		
		progDialog.dismiss();
		
		if(result.length()==0){
			this.activity.alert("No se pudo obtener datos");
			return;
		}
		
		try {
			JSONArray jsonArray = new JSONArray(result);
			Log.d("conversion", jsonArray.toString());
			for (int i = 0; i < jsonArray.length() ; i++) {
				JSONObject json = jsonArray.getJSONObject(i);
				JSONObject cliente = json.getJSONObject("clienteId");
				JSONObject ruta = json.getJSONObject("rutaId");
				JSONObject conductor = ruta.getJSONObject("conductorId");
				Log.d("conversion", cliente.getString("razonsocial"));
				lista.add(new Route(cliente.getString("latlong"), cliente.getString("razonsocial"), ruta.getString("estado"), json.getInt("detallerutaid"), conductor.getInt("conductorid")));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		this.activity.setRutas(lista);
	}
}
