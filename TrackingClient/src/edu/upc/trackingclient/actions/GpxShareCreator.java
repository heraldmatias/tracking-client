package edu.upc.trackingclient.actions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import edu.upc.trackingclient.R;
import edu.upc.trackingclient.actions.utils.ProgressListener;
import edu.upc.trackingclient.db.GPStracking;
import edu.upc.trackingclient.db.GPStracking.Media;
import edu.upc.trackingclient.db.GPStracking.MetaData;
import edu.upc.trackingclient.utils.Constants;

public class GpxShareCreator extends GpxCreator {

	private static final String SERVICE_HOST = "http://192.168.2.8/WSTest.php";//"http://50.116.43.119:8080/ServiceTracking-1.0-SNAPSHOT/webresources/detalleruta/save";
	private static final String TAG = "OGT.OsmSharing";
	public static final String OSM_FILENAME = "OSM_Trace";
	private String responseText;
	private Uri mFileUri;
	private String description;
	private String status;
	private Integer rutaId;

	public GpxShareCreator(Context context, Uri trackUri,
			String chosenBaseFileName,String description, String status, boolean attachments,
			ProgressListener listener) {
		super(context, trackUri, chosenBaseFileName, attachments, listener);
		this.description = description;
		this.status = status;
		
		SharedPreferences mPrefs = context.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
		this.rutaId = mPrefs.getInt("RUTA", 0);
	}

	public void resumeOsmSharing(Uri fileUri, Uri trackUri) {
		mFileUri = fileUri;
		mTrackUri = trackUri;
		execute();
	}

	@Override
	protected Uri doInBackground(Void... params) {
		if (mFileUri == null) {
			mFileUri = super.doInBackground(params);
		}
		sendToOsm(mFileUri, mTrackUri);
		return mFileUri;
	}

	@Override
	protected void onPostExecute(Uri resultFilename) {
		super.onPostExecute(resultFilename);

		CharSequence text = mContext.getString(R.string.action_share_success)
				+ responseText;
		if(responseText==null){
			text = mContext.getString(R.string.action_share_failed);
			Toast toast = Toast.makeText(mContext, text, Toast.LENGTH_LONG);
			toast.show();
		}else{
			Toast toast = Toast.makeText(mContext, text, Toast.LENGTH_LONG);
			toast.show();
		}
	}

	/**
	 * POST a (GPX) file to the 0.6 API of the OpenStreetMap.org website
	 * publishing this track to the public.
	 * 
	 * @param fileUri
	 * @param contentType
	 */
	private void sendToOsm(final Uri fileUri, final Uri trackUri) {
		File gpxFile = new File(fileUri.getEncodedPath());

		String url = SERVICE_HOST;
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpResponse response = null;
		int statusCode = 0;
		Cursor metaData = null;
		String sources = null;
		HttpEntity responseEntity = null;
		try {
			metaData = mContext.getContentResolver().query(
					Uri.withAppendedPath(trackUri, "metadata"),
					new String[] { MetaData.VALUE }, MetaData.KEY + " = ? ",
					new String[] { Constants.DATASOURCES_KEY }, null);
			if (metaData.moveToFirst()) {
				sources = metaData.getString(0);
			}
			if (sources != null) {
				throw new IOException(
						"Unable to upload track with materials derived from Google Maps.");
			}

			// The POST to the create node
			HttpPost method = new HttpPost(url);

			String tags = mContext.getString(R.string.app_tag) + " "
					+ queryForNotes();

			MultipartEntityBuilder builder = MultipartEntityBuilder.create();        

			/* example for setting a HttpMultipartMode */
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
					
			builder.addPart("my_file", new FileBody(gpxFile));
			builder.addTextBody("tags", tags);
			builder.addTextBody("description", description);
			builder.addTextBody("status", status);
			builder.addTextBody("rutaId", this.rutaId.toString());
			
			HttpEntity entity = builder.build();
			method.setEntity(entity);
			
			response = httpclient.execute(method);
			// Read the response
			statusCode = response.getStatusLine().getStatusCode();
			responseEntity = response.getEntity();
			InputStream stream = responseEntity.getContent();
			
			responseText = XmlCreator.convertStreamToString(stream);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {			
			if (metaData != null) {
				metaData.close();
			}
		}

		if (statusCode != 200) {
			Log.e(TAG, "Failed to upload to error code " + statusCode + " "
					+ responseText);
		}
			
	}

	private String queryForNotes() {
		StringBuilder tags = new StringBuilder();
		ContentResolver resolver = mContext.getContentResolver();
		Cursor mediaCursor = null;
		Uri mediaUri = Uri.withAppendedPath(mTrackUri, "media");
		try {
			mediaCursor = resolver.query(mediaUri, new String[] { Media.URI },
					null, null, null);
			if (mediaCursor.moveToFirst()) {
				do {
					Uri noteUri = Uri.parse(mediaCursor.getString(0));
					if (noteUri.getScheme().equals("content")
							&& noteUri.getAuthority().equals(
									GPStracking.AUTHORITY + ".string")) {
						String tag = noteUri.getLastPathSegment().trim();
						if (!tag.contains(" ")) {
							if (tags.length() > 0) {
								tags.append(" ");
							}
							tags.append(tag);
						}
					}
				} while (mediaCursor.moveToNext());
			}
		} finally {
			if (mediaCursor != null) {
				mediaCursor.close();
			}
		}
		return tags.toString();
	}
}
