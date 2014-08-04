package edu.upc.trackingclient;

import java.util.ArrayList;

import edu.upc.trackingclient.adapters.RoutesDataAdapter;
import edu.upc.trackingclient.entity.Route;
import edu.upc.trackingclient.tasks.ListRoutesServiceTask;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ListRoutesActivity extends Activity {

	private ListView routesList;
	private InputMethodManager inMgr;
	private LayoutInflater layoutInflater;
	private Button routesButton;
	//private ListRoutesServiceTask 
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_list_routes);
		Intent intent = getIntent();
		final Integer conductorId = intent.getIntExtra("user", 0);
        
		this.routesList = (ListView) findViewById(R.id.routes_list);
		this.inMgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		this.layoutInflater = LayoutInflater.from(this);
		this.routesButton = (Button) findViewById(R.id.btn_listar);
		
//		final Object data = getLastNonConfigurationInstance();
//		
//		if (data != null) {
//			setRutas((ArrayList<Route>) data);
//	    }

		
		this.routesButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				inMgr.hideSoftInputFromWindow(routesButton.getWindowToken(), 0);
				ListRoutesServiceTask task = new ListRoutesServiceTask(ListRoutesActivity.this);
				try {
					task.execute(conductorId.toString());
				} catch (Exception e) {
					// TODO: handle exception
					task.cancel(true);
					alert("No se encontraron datos");
				}
			}
		});
		
		
		
//		final Object[] data = (Object[]) getLastCustomNonConfigurationInstance();
//		if(data != null){
//			this.rutas = (ArrayList<Route>)data[0];
//			//cargar a lista
//			routesList.setAdapter(new RoutesDataAdapter(this, layoutInflater, rutas));
//		}
        
	}
	
	public void alert(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		Log.d("lista", "ORIENTATIO CHANGE");
		return null;
//	    final ArrayList<Route> frutas = new ArrayList<Route>();
//	    frutas = this.rutas;
//	    return frutas;
//		return this.rutas;
	}
	
//	@Override
//	public Object onRetainCustomNonConfigurationInstance() {
//		// TODO Auto-generated method stub
//		Object[] data = new Object[1];
//		data[0] = this.rutas;
//		return data;
//	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.list_routes, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void setRutas(ArrayList<Route> rutas) {
		//aca se llama el listview
		routesList.setAdapter(new RoutesDataAdapter(this, layoutInflater, rutas));
	}
	
	
	public static class MyViewHolder{
		public TextView cliente;
		public Spinner destino;
		public Button trackButton;
		public Route route;
	}
	
}
