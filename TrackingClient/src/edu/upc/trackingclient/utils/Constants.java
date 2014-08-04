package edu.upc.trackingclient.utils;

import java.io.File;

import edu.upc.trackingclient.db.GPStracking;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;

public class Constants {

	public static final String DATASOURCES_KEY = "DATASOURCES";
	public static final String SDDIR_DIR = "SDDIR_DIR";
	public static final String DEFAULT_EXTERNAL_DIR = "/TracksClient/";
	public static final String TMPICTUREFILE_SUBPATH = "media_tmp.tmp";
	public static final Uri NAME_URI = Uri.parse("content://"
			+ GPStracking.AUTHORITY + ".string");

	public static String getSdCardDirectory(Context ctx) {
		// Read preference and ensure start and end with '/' symbol
		String dir = PreferenceManager.getDefaultSharedPreferences(ctx)
				.getString(SDDIR_DIR, DEFAULT_EXTERNAL_DIR);
		if (!dir.startsWith("/")) {
			dir = "/" + dir;
		}
		if (!dir.endsWith("/")) {
			dir = dir + "/";
		}
		dir = Environment.getExternalStorageDirectory().getAbsolutePath() + dir;

		// If neither exists or can be created fall back to default
		File dirHandle = new File(dir);
		if (!dirHandle.exists() && !dirHandle.mkdirs()) {
			dir = Environment.getExternalStorageDirectory().getAbsolutePath()
					+ DEFAULT_EXTERNAL_DIR;
		}
		return dir;
	}

}
