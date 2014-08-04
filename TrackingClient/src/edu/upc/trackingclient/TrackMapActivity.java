package edu.upc.trackingclient;

//import com.google.android.maps.MapActivity;

//import android.support.v7.app.ActionBarActivity;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import edu.upc.trackingclient.db.GPStracking;
import edu.upc.trackingclient.db.GPStracking.Tracks;
import edu.upc.trackingclient.db.GPStracking.Waypoints;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class TrackMapActivity extends Activity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

	private final static String TAG = "MAP";
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private final static int CONNECTION_FAILURE_GPS_REQUEST = 10000;
	// Milliseconds per second
	private static final int MILLISECONDS_PER_SECOND = 1000;
	// Update frequency in seconds
	public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
	// Update frequency in milliseconds
	private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND
			* UPDATE_INTERVAL_IN_SECONDS;
	// The fastest update frequency, in seconds
	private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
	// A fast frequency ceiling in milliseconds
	private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND
			* FASTEST_INTERVAL_IN_SECONDS;
	// Define an object that holds accuracy and frequency parameters
	LocationRequest mLocationRequest;
	LocationClient mLocationClient;
	
	boolean mUpdatesRequested;
	private GoogleMap map;
	private LatLng latLngDestino;
	private LatLng latLngposicion;
	SharedPreferences mPrefs;
	SharedPreferences.Editor mEditor;
	ContentResolver provider;
	private long mTrackId = -1;
	private long mSegmentId = -1;
	@SuppressWarnings("unused")
	private long mWaypointId = -1;
	private int mPrecision;
	private boolean mStartNextSegment;
	
	private Location mPreviousLocation;
	@SuppressWarnings("unused")
	private float mDistance;
	private float mMaxAcceptableAccuracy = 20;
	
	private static final int MAX_REASONABLE_SPEED = 90;
	public static final int LOGGING_GLOBAL = 4;
	private static final int MAX_REASONABLE_ALTITUDECHANGE = 200;
	
	private Queue<Double> mAltitudes;
	private Vector<Location> mWeakLocations;
	/**
	 * If speeds should be checked to sane values
	 */
	private boolean mSpeedSanityCheck;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_track_map);

		Intent intent = getIntent();
		String destino = intent.getStringExtra("destino");
		destino = destino.replace("(", "").replace(")", "");
		String[] separated = destino.split(";");
		String latString = separated[0];
		String longString = separated[1];

		double latitude = Double.parseDouble(latString);
		double longitude = Double.parseDouble(longString);
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();
		latLngDestino = new LatLng(latitude, longitude);

		mAltitudes = new LinkedList<Double>();
		mWeakLocations = new Vector<Location>(3);

		mLocationRequest = LocationRequest.create();
		// Use high accuracy
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		// Set the update interval to 5 seconds
		mLocationRequest.setInterval(UPDATE_INTERVAL);
		// Set the fastest update interval to 1 second
		mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

		// Open the shared preferences
		mPrefs = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
		// Get a SharedPreferences editor
		mEditor = mPrefs.edit();
		/*
		 * Create a new location client, using the enclosing class to handle
		 * callbacks.
		 */
		mLocationClient = new LocationClient(this, this, this);
		// Start with updates turned off
		mUpdatesRequested = true;

		mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
		mEditor.commit();

		provider = getContentResolver();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onPostCreate(savedInstanceState);
		startNewTrack();
		CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLngDestino,
				17);
		BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.finish_flag);
		// mark destiny
		map.addMarker(new MarkerOptions().position(latLngDestino).title(
				mPrefs.getString("DESTINO", "")).icon(icon));
		map.animateCamera(update);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.track_map, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_start) {
			final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

			if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				buildAlertMessageNoGps();
			} else {
				mLocationClient.connect();
			}
			return true;
		} else 	if (id == R.id.action_pause) {
			mStartNextSegment = true;
			mLocationClient.disconnect();
			return true;
		} else 	if (id == R.id.action_note) {
			mStartNextSegment = true;
			mLocationClient.disconnect();
			
			Integer conductorId = mPrefs.getInt("CONDUCTOR", 0);
			Integer rutaId = mPrefs.getInt("RUTA", 0);
			String fileName = "tracking" + mTrackId + "_" + conductorId + "_" + rutaId;
					
			CheckPointDialogFragment dialog = new CheckPointDialogFragment(
					Uri.withAppendedPath(Tracks.CONTENT_URI, ""+mTrackId), fileName);
			dialog.show(getFragmentManager(), TAG);
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}

	/*
	 * Called by Location Services if the connection to the location client
	 * drops because of an error.
	 */
	@Override
	public void onDisconnected() {
		// Display the connection status
		Toast.makeText(this, "Disconnected. Please re-connect.",
				Toast.LENGTH_SHORT).show();
	}

	/*
	 * Called by Location Services if the attempt to Location Services fails.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		if (connectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this,
						CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
			} catch (IntentSender.SendIntentException e) {
				// Log the error
				e.printStackTrace();
			}
		} else {
			/*
			 * If no resolution is available, display a dialog to the user with
			 * the error.
			 */

			showDialog(connectionResult.getErrorCode());
		}
	}

	// Define the callback method that receives location updates
	@Override
	public void onLocationChanged(Location location) {
		// storeLocation(location);
		CameraUpdate update = null;
		// Might be claiming GPS disabled but when we were paused this changed
		// and this location proves so

		Location filteredLocation = locationFilter(location);
		if (filteredLocation != null) {
			if (mStartNextSegment) {
				mStartNextSegment = false;
				// Obey the start segment if the previous location is unknown or
				// far away
				if (mPreviousLocation == null
						|| filteredLocation.distanceTo(mPreviousLocation) > 4 * mMaxAcceptableAccuracy) {
					startNewSegment();
				}
			} else if (mPreviousLocation != null) {
				mDistance += mPreviousLocation.distanceTo(filteredLocation);
			}
			storeLocation(filteredLocation);
			broadcastLocation(filteredLocation);
			mPreviousLocation = location;
			BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_maps_indicator_current_position);
			latLngposicion = new LatLng(filteredLocation.getLatitude(),
					filteredLocation.getLongitude());
			update = CameraUpdateFactory.newLatLngZoom(latLngposicion, 17);
			map.animateCamera(update);
			map.addMarker(new MarkerOptions().position(latLngposicion).icon(icon));
		}

	}

	@Override
	protected void onPause() {
		// Save the current setting for updates
		mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
		mEditor.commit();
		super.onPause();
	}

	@Override
	protected void onStart() {
		super.onStart();
		mLocationClient.connect();
	}

	@Override
	protected void onResume() {
		super.onResume();
		/*
		 * Get any previous setting for location updates Gets "false" if an
		 * error occurs
		 */
		if (mPrefs.contains("KEY_UPDATES_ON")) {
			mUpdatesRequested = mPrefs.getBoolean("KEY_UPDATES_ON", false);

			// Otherwise, turn off location updates
		} else {
			mEditor.putBoolean("KEY_UPDATES_ON", false);
			mEditor.commit();
		}
		mLocationClient.connect();
	}

	/*
	 * Called by Location Services when the request to connect the client
	 * finishes successfully. At this point, you can request the current
	 * location or start periodic updates
	 */
	@Override
	public void onConnected(Bundle dataBundle) {
		// Display the connection status
		//Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
		Log.d("CLIENT CONNECT", "INICIA LA RECOLECCION DE POSICION");
		// If already requested, start periodic updates
		if (mUpdatesRequested) {
			Log.d("CLIENT CONNECT", "PERIODICAL UPDATES");
			mLocationClient.requestLocationUpdates(mLocationRequest, this);
		}
	}

	@Override
	protected void onStop() {
		// If the client is connected
		if (mLocationClient.isConnected()) {
			/*
			 * Remove location updates for a listener. The current Activity is
			 * the listener, so the argument is "this".
			 */
			// /removeLocationUpdates(this);
		}
		/*
		 * After disconnect() is called, the client is considered "dead".
		 */
		//mLocationClient.disconnect();
		super.onStop();
	}

	public static class ErrorDialogFragment extends DialogFragment {
		// Global field to contain the error dialog
		private Dialog mDialog;

		// Default constructor. Sets the dialog field to null
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		// Set the dialog to display
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		// Return a Dialog to the DialogFragment.
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Decide what to do based on the original request code
		Log.d(TAG, "request = " + requestCode);
		Log.d(TAG, "result = " + resultCode);
		switch (requestCode) {

		case CONNECTION_FAILURE_RESOLUTION_REQUEST: {
			/*
			 * If the result code is Activity.RESULT_OK, try to connect again
			 */
			switch (resultCode) {
			case Activity.RESULT_OK:
				/*
				 * Try the request again
				 */

				break;
			}
		}
			break;
		case CONNECTION_FAILURE_GPS_REQUEST: {
			final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

			if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				mLocationClient.connect();
			}
		}
			break;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
	}

	private void startNewTrack() {
		// mDistance = 0;
		// Uri newTrack = this.getContentResolver().insert(Tracks.CONTENT_URI,
		// new ContentValues(0));
		// mTrackId = Long.valueOf(newTrack.getLastPathSegment()).longValue();

		ContentValues values = new ContentValues();

		values.put(GPStracking.Tracks.NAME, mPrefs.getString("DESTINO", ""));
		values.put(GPStracking.Tracks.CONDUCTOR, mPrefs.getInt("CONDUCTOR", 0));
		values.put(GPStracking.Tracks.RUTA, mPrefs.getInt("RUTA", 0));
		// Iniciado A = Partida I = Iniciado
		values.put(GPStracking.Tracks.ESTADO, mPrefs.getString("ESTADO", "A"));

		Uri newTrack = provider
				.insert(Uri.withAppendedPath(GPStracking.CONTENT_URI, "tracks"),
						values);
		mTrackId = Long.valueOf(newTrack.getLastPathSegment()).longValue();
		
		startNewSegment();
	}

	private void startNewSegment() {
		this.mPreviousLocation = null;
		Uri newSegment = this.getContentResolver()
				.insert(Uri.withAppendedPath(Tracks.CONTENT_URI, mTrackId
						+ "/segments"), new ContentValues(0));
		mSegmentId = Long.valueOf(newSegment.getLastPathSegment()).longValue();
		// crashProtectState();
	}

	/**
	 * Use the ContentResolver mechanism to store a received location
	 * 
	 * @param location
	 */
	public void storeLocation(Location location) {

		Log.e(TAG, String.format(
				"Not logging but storing location %s, prepare to fail",
				location.toString()));

		ContentValues args = new ContentValues();

		args.put(Waypoints.LATITUDE, Double.valueOf(location.getLatitude()));
		args.put(Waypoints.LONGITUDE, Double.valueOf(location.getLongitude()));
		args.put(Waypoints.SPEED, Float.valueOf(location.getSpeed()));
		args.put(Waypoints.TIME, Long.valueOf(System.currentTimeMillis()));
		if (location.hasAccuracy()) {
			args.put(Waypoints.ACCURACY, Float.valueOf(location.getAccuracy()));
		}
		if (location.hasAltitude()) {
			args.put(Waypoints.ALTITUDE, Double.valueOf(location.getAltitude()));

		}
		if (location.hasBearing()) {
			args.put(Waypoints.BEARING, Float.valueOf(location.getBearing()));
		}

		Uri waypointInsertUri = Uri.withAppendedPath(Tracks.CONTENT_URI,
				mTrackId + "/segments/" + mSegmentId + "/waypoints");
		Uri inserted = this.getContentResolver()
				.insert(waypointInsertUri, args);
		mWaypointId = Long.parseLong(inserted.getLastPathSegment());
	}

	/**
	 * Consult broadcast options and execute broadcast if necessary
	 * 
	 * @param location
	 */
	public void broadcastLocation(Location location) {
		// Intent intent = new Intent(Constants.STREAMBROADCAST);
		//
		// if (mStreamBroadcast)
		// {
		// final long minDistance = (long)
		// PreferenceManager.getDefaultSharedPreferences(this).getFloat("streambroadcast_distance_meter",
		// 5000F);
		// final long minTime = 60000 *
		// Long.parseLong(PreferenceManager.getDefaultSharedPreferences(this).getString("streambroadcast_time",
		// "1"));
		// final long nowTime = location.getTime();
		// if (mPreviousLocation != null)
		// {
		// mBroadcastDistance += location.distanceTo(mPreviousLocation);
		// }
		// if (mLastTimeBroadcast == 0)
		// {
		// mLastTimeBroadcast = nowTime;
		// }
		// long passedTime = (nowTime - mLastTimeBroadcast);
		// intent.putExtra(Constants.EXTRA_DISTANCE, (int) mBroadcastDistance);
		// intent.putExtra(Constants.EXTRA_TIME, (int) passedTime/60000);
		// intent.putExtra(Constants.EXTRA_LOCATION, location);
		// intent.putExtra(Constants.EXTRA_TRACK,
		// ContentUris.withAppendedId(Tracks.CONTENT_URI, mTrackId));

		// boolean distanceBroadcast = minDistance > 0 && mBroadcastDistance >=
		// minDistance;
		// boolean timeBroadcast = minTime > 0 && passedTime >= minTime;
		// if (distanceBroadcast || timeBroadcast)
		// {
		// if (distanceBroadcast)
		// {
		// mBroadcastDistance = 0;
		// }
		// if (timeBroadcast)
		// {
		// mLastTimeBroadcast = nowTime;
		// }
		// this.sendBroadcast(intent,
		// "android.permission.ACCESS_FINE_LOCATION");
		// }
		// }
	}

	@SuppressWarnings("unused")
	private boolean isNetworkConnected() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connMgr.getActiveNetworkInfo();

		return (info != null && info.isConnected());
	}

	/**
	 * Some GPS waypoints received are of to low a quality for tracking use.
	 * Here we filter those out.
	 * 
	 * @param proposedLocation
	 * @return either the (cleaned) original or null when unacceptable
	 */
	public Location locationFilter(Location proposedLocation) {
		// Do no include log wrong 0.0 lat 0.0 long, skip to next value in
		// while-loop
		Boolean flag = true;
		if (proposedLocation != null
				&& (proposedLocation.getLatitude() == 0.0d || proposedLocation
						.getLongitude() == 0.0d)) {
			Log.w(TAG,
					"A wrong location was received, 0.0 latitude and 0.0 longitude... ");
			proposedLocation = null;
			flag = false;
		}

		// Do not log a waypoint which is more inaccurate then is configured to
		// be acceptable
		if (proposedLocation != null
				&& proposedLocation.getAccuracy() > mMaxAcceptableAccuracy) {
			Log.w(TAG,
					String.format(
							"A weak location was received, lots of inaccuracy... (%f is more then max %f)",
							proposedLocation.getAccuracy(),
							mMaxAcceptableAccuracy));
			proposedLocation = addBadLocation(proposedLocation);
			flag = false;
		}

		// Do not log a waypoint which might be on any side of the previous
		// waypoint
		if (proposedLocation != null
				&& mPreviousLocation != null
				&& proposedLocation.getAccuracy() > mPreviousLocation
						.distanceTo(proposedLocation)) {
			Log.w(TAG,
					String.format(
							"A weak location was received, not quite clear from the previous waypoint... (%f more then max %f)",
							proposedLocation.getAccuracy(),
							mPreviousLocation.distanceTo(proposedLocation)));
			proposedLocation = addBadLocation(proposedLocation);
			flag = false;
		}

		// Speed checks, check if the proposed location could be reached from
		// the previous one in sane speed
		// Common to jump on network logging and sometimes jumps on Samsung
		// Galaxy S type of devices
		if (mSpeedSanityCheck && proposedLocation != null
				&& mPreviousLocation != null) {
			// To avoid near instant teleportation on network location or
			// glitches cause continent hopping
			float meters = proposedLocation.distanceTo(mPreviousLocation);
			long seconds = (proposedLocation.getTime() - mPreviousLocation
					.getTime()) / 1000L;
			float speed = meters / seconds;
			if (speed > MAX_REASONABLE_SPEED) {
				Log.w(TAG,
						"A strange location was received, a really high speed of "
								+ speed + " m/s, prob wrong...");
				proposedLocation = addBadLocation(proposedLocation);
				flag = false;
				// Might be a messed up Samsung Galaxy S GPS, reset the logging
				if (speed > 2 * MAX_REASONABLE_SPEED
						&& mPrecision != LOGGING_GLOBAL) {
					Log.w(TAG,
							"A strange location was received on GPS, reset the GPS listeners");
					mLocationClient.disconnect();
					// stopListening();
					// mLocationManager.removeGpsStatusListener(mStatusListener);
					// mLocationManager = (LocationManager)
					// this.getSystemService(Context.LOCATION_SERVICE);
					// sendRequestStatusUpdateMessage();
					// sendRequestLocationUpdatesMessage();
				}
			}
		}

		// Remove speed if not sane
		if (mSpeedSanityCheck && proposedLocation != null
				&& proposedLocation.getSpeed() > MAX_REASONABLE_SPEED) {
			Log.w(TAG, "A strange speed, a really high speed, prob wrong...");
			proposedLocation.removeSpeed();
			flag = false;
		}

		// Remove altitude if not sane
		if (mSpeedSanityCheck && proposedLocation != null
				&& proposedLocation.hasAltitude()) {
			if (!addSaneAltitude(proposedLocation.getAltitude())) {
				Log.w(TAG,
						"A strange altitude, a really big difference, prob wrong...");
				proposedLocation.removeAltitude();
				flag = false;
			}
		}
		// Older bad locations will not be needed
		if (proposedLocation != null) {
			mWeakLocations.clear();
		}
		return flag?proposedLocation:null;
	}

	private Location addBadLocation(Location location) {
		mWeakLocations.add(location);
		if (mWeakLocations.size() < 3) {
			location = null;
		} else {
			Location best = mWeakLocations.lastElement();
			for (Location whimp : mWeakLocations) {
				if (whimp.hasAccuracy() && best.hasAccuracy()
						&& whimp.getAccuracy() < best.getAccuracy()) {
					best = whimp;
				} else {
					if (whimp.hasAccuracy() && !best.hasAccuracy()) {
						best = whimp;
					}
				}
			}
			synchronized (mWeakLocations) {
				mWeakLocations.clear();
			}
			location = best;
		}
		return location;
	}

	private boolean addSaneAltitude(double altitude) {
		boolean sane = true;
		double avg = 0;
		int elements = 0;
		// Even insane altitude shifts increases alter perception
		mAltitudes.add(altitude);
		if (mAltitudes.size() > 3) {
			mAltitudes.poll();
		}
		for (Double alt : mAltitudes) {
			avg += alt;
			elements++;
		}
		avg = avg / elements;
		sane = Math.abs(altitude - avg) < MAX_REASONABLE_ALTITUDECHANGE;

		return sane;
	}

	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Tu GPS esta deshabilitado, Quieres habilitarlo?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								startActivityForResult(
										new Intent(
												android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS),
										CONNECTION_FAILURE_GPS_REQUEST);
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,
							final int id) {
						dialog.cancel();
					}
				});
		final AlertDialog alert = builder.create();
		alert.show();

	}

}
