package edu.upc.trackingclient;

import edu.upc.trackingclient.actions.GpxShareCreator;
import edu.upc.trackingclient.actions.utils.ProgressListener;
import edu.upc.trackingclient.adapters.StatusCodes;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.Toast;

@SuppressLint("InflateParams")
public class CheckPointDialogFragment extends DialogFragment implements OnItemSelectedListener {

	private Notification mNotification;
	private NotificationManager mNotificationManager;
	private RemoteViews mContentView;
	private static final int PROGRESS_STEPS = 10;
	@SuppressWarnings("unused")
	private String mErrorDialogMessage;
	@SuppressWarnings("unused")
	private Throwable mErrorDialogException;
	protected Uri trackUri;
	protected String fileName;
	private int barProgress = 0;
	
	
	public CheckPointDialogFragment(Uri trackUri, String filename) {
		this.trackUri = trackUri;
		this.fileName = filename;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			
		LayoutInflater inflater = getActivity().getLayoutInflater();
		final View view = inflater.inflate(R.layout.checkpoint_dialog, null); 
		builder.setView(view)
				.setPositiveButton(R.string.action_post_track,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								final Spinner spStatus = (Spinner) view.findViewById(R.id.spStatus);
								final EditText txtDescription = (EditText) view.findViewById(R.id.txtDescription);
								
								String description = txtDescription.getText().toString();
								String status = StatusCodes.getCodeFromPosition(spStatus.getSelectedItemPosition());
								
								GpxShareCreator shareCreator = new GpxShareCreator(
										getActivity().getBaseContext(), 
										trackUri, 
										fileName,
										description,
										status,
										true, 
										new CheckPointDialogFragment.ShareProgressListener(
												fileName));
								shareCreator.execute();
							}
						})
				.setNegativeButton(R.string.user_cancel_button,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								CheckPointDialogFragment.this.getDialog()
										.cancel();
							}
						});
		return builder.create();
	}

	public class ShareProgressListener implements ProgressListener

	{
		private String mFileName;
		private int mProgress;

		public ShareProgressListener(String sharename) {
			mFileName = sharename;
		}

		public void startNotification() {
			String ns = Context.NOTIFICATION_SERVICE;
			mNotificationManager = (NotificationManager) getActivity()
					.getSystemService(ns);
			int icon = android.R.drawable.ic_menu_save;
			CharSequence tickerText = getString(R.string.ticker_saving) + "\""
					+ mFileName + "\"";

			mNotification = new Notification();
			PendingIntent contentIntent = PendingIntent.getActivity(
					getActivity(), 0, new Intent(getActivity(),
							TrackMapActivity.class)
							.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
					PendingIntent.FLAG_UPDATE_CURRENT);

			mNotification.contentIntent = contentIntent;
			mNotification.tickerText = tickerText;
			mNotification.icon = icon;
			mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
			mContentView = new RemoteViews(getActivity().getPackageName(),
					R.layout.savenotificationprogress);
			mContentView.setImageViewResource(R.id.icon, icon);
			mContentView.setTextViewText(R.id.progresstext, tickerText);

			mNotification.contentView = mContentView;
		}

		private void updateNotification() {
			// Log.d( "TAG", "Progress " + progress + " of " + goal );
			if (mProgress > 0 && mProgress < Window.PROGRESS_END) {
				if ((mProgress * PROGRESS_STEPS) / Window.PROGRESS_END != barProgress) {
					barProgress = (mProgress * PROGRESS_STEPS)
							/ Window.PROGRESS_END;
					mContentView.setProgressBar(R.id.progress,
							Window.PROGRESS_END, mProgress, false);
					mNotificationManager.notify(
							R.layout.savenotificationprogress, mNotification);
				}
			} else if (mProgress == 0) {
				mContentView.setProgressBar(R.id.progress, Window.PROGRESS_END,
						mProgress, true);
				mNotificationManager.notify(R.layout.savenotificationprogress,
						mNotification);
			} else if (mProgress >= Window.PROGRESS_END) {
				mContentView.setProgressBar(R.id.progress, Window.PROGRESS_END,
						mProgress, false);
				mNotificationManager.notify(R.layout.savenotificationprogress,
						mNotification);
			}
		}

		public void endNotification(Uri file) {
			mNotificationManager.cancel(R.layout.savenotificationprogress);
		}

		@Override
		public void setIndeterminate(boolean indeterminate) {
			Log.w("POST", "Unsupported indeterminate progress display");
		}

		@Override
		public void started() {
			startNotification();
		}

		@Override
		public void setProgress(int value) {
			mProgress = value;
			updateNotification();
		}

		@Override
		public void finished(Uri result) {
			endNotification(result);
		}

		@Override
		public void showError(String task, String errorDialogMessage,
				Exception errorDialogException) {
			endNotification(null);

			mErrorDialogMessage = errorDialogMessage;
			mErrorDialogException = errorDialogException;
			if (!getActivity().isFinishing()) {
				Toast toast = Toast.makeText(getActivity(), errorDialogMessage,
						Toast.LENGTH_LONG);
				toast.show();
			} else {
				Toast toast = Toast.makeText(getActivity(), errorDialogMessage,
						Toast.LENGTH_LONG);
				toast.show();
			}
		}
	}
	
    public void onItemSelected(AdapterView<?> parent, View view, 
            int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }


}