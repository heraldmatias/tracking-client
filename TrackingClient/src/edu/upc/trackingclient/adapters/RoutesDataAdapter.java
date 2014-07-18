package edu.upc.trackingclient.adapters;

import java.util.ArrayList;

import edu.upc.trackingclient.ListRoutesActivity;
import edu.upc.trackingclient.ListRoutesActivity.MyViewHolder;
import edu.upc.trackingclient.R;
import edu.upc.trackingclient.TrackMapActivity;
import edu.upc.trackingclient.entity.Route;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class RoutesDataAdapter extends BaseAdapter implements OnClickListener {

	private ListRoutesActivity activity;
	private LayoutInflater layoutInflater;
	private ArrayList<Route> lista;
	
	
	
	
	public RoutesDataAdapter(ListRoutesActivity activity,
			LayoutInflater layoutInflater, ArrayList<Route> lista) {
		this.activity = activity;
		this.layoutInflater = layoutInflater;
		this.lista = lista;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return this.lista.size();
	}

	@Override
	public boolean areAllItemsEnabled() {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int pos) {
		// TODO Auto-generated method stub
		return pos;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		MyViewHolder holder;
		if(convertView == null){
			convertView = layoutInflater.inflate(R.layout.routerow, parent, false);
			holder = new MyViewHolder();
			holder.cliente = (TextView) convertView.findViewById(R.id.cliente_row);
			holder.destino = (TextView) convertView.findViewById(R.id.destino_row);
			holder.trackButton = (Button) convertView.findViewById(R.id.btn_track_row);
			holder.trackButton.setTag(holder);
			convertView.setTag(holder);
		}else{
			holder = (MyViewHolder) convertView.getTag();
		}
		
		convertView.setOnClickListener(this);
		
		Route route = lista.get(pos);
		holder.route = route;
		holder.cliente.setText(route.getCliente());
		holder.destino.setText(route.getLatlong());
		holder.trackButton.setOnClickListener(this);
		
		
		return convertView;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		MyViewHolder holder = (MyViewHolder) v.getTag();
		if(v instanceof Button){
			Intent intent = new Intent(activity, TrackMapActivity.class);
			intent.putExtra("destino", holder.route.getLatlong());
			this.activity.startActivity(intent);
		}else if (v instanceof View){
			Intent intent = new Intent(activity, TrackMapActivity.class);
			intent.putExtra("destino", holder.route.getLatlong());
			this.activity.startActivity(intent);
		}
	}

}
