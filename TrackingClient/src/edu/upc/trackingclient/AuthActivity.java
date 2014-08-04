package edu.upc.trackingclient;

import edu.upc.trackingclient.entity.Conductor;
import edu.upc.trackingclient.tasks.AuthServiceTask;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AuthActivity extends Activity {

	private Conductor user;
	private InputMethodManager inMgr;
	SharedPreferences mPrefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_auth);
		
		this.inMgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		
		final Button button = (Button) findViewById(R.id.button01);
		final Button btnSalir = (Button) findViewById(R.id.Button02);
		final EditText txtUser = (EditText) findViewById(R.id.editText1);
		final EditText txtPass = (EditText) findViewById(R.id.editText2);
		
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if(!txtUser.getText().toString().equals("") & !txtPass.getText().toString().equals("")){
	            	inMgr.hideSoftInputFromWindow(button.getWindowToken(), 0);
					AuthServiceTask task = new AuthServiceTask(AuthActivity.this);
					try {
						task.execute(txtUser.getText().toString(), txtPass.getText().toString());
						
						//if(user != null){
						//sendMessage(v);
						//}
					} catch (Exception e) {
						alert("No se encontraron datos");
						task.cancel(true);
						Log.d("lgin", "debe cerrar");
						
					}            	
	            }else{
	            	alert("Ingrese su usuario y clave");
	            }
            }
        });
        
        btnSalir.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}

	public void sendMessage() {
	    Intent intent = new Intent(this, ListRoutesActivity.class);	    
	 // Open the shared preferences
 		mPrefs = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
 		// Get a SharedPreferences editor
 		SharedPreferences.Editor mEditor = mPrefs.edit();
 		mEditor.putInt("CONDUCTOR_ID", user.getId());
		mEditor.commit();
		
	    intent.putExtra("user", user.getId());
	    startActivity(intent);
	}
	
	public void alert(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.auth, menu);
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

	public void setUser(Conductor user) {
		this.user = user;
		if(user != null){
			sendMessage();
		}
	}
	
}
