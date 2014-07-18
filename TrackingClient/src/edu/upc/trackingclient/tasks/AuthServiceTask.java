package edu.upc.trackingclient.tasks;

import org.json.JSONException;
import org.json.JSONObject;

import edu.upc.trackingclient.AuthActivity;
import edu.upc.trackingclient.R;
import edu.upc.trackingclient.entity.Conductor;

import edu.upc.trackingclient.helpers.AuthHelper;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class AuthServiceTask extends AsyncTask<String, Integer, String> {
	
	private ProgressDialog progDialog;
	private Context context;
	private AuthActivity activity;

	public AuthServiceTask(AuthActivity activity){
		super();
		this.activity = activity;
		this.context = activity.getApplicationContext();
	}
	
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		progDialog = ProgressDialog.show(this.activity, "Autenticando", this.context.getResources().getString(R.string.user_login_button),true);
	}

	@Override
	protected String doInBackground(String... params) {
		// TODO Auto-generated method stub
		try {
			String result = AuthHelper.auth(params);
			return result;
		} catch (Exception e) {
			return new String();
		}
	}
	
	@Override
	protected void onPostExecute(String result) {
		
		progDialog.dismiss();
		Log.d("servicio_auth", "destruir cudro");
		Conductor user = null;	
		
		if(result.length()==0){
			this.activity.alert("No se pudo obtener datos");
			return;
		}
		
		try {
			JSONObject json = new JSONObject(result);
			Log.d("conversion", json.toString());
			user = new Conductor();	
			user.setCelular(json.getString("celular"));
			user.setId(json.getInt("conductorid"));
			user.setNombres(json.getString("nombres") + " " +json.getString("apellidos"));
			user.setDni(json.getString("dni"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		this.activity.setUser(user);
	}
	

}
