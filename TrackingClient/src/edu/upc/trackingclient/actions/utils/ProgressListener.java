package edu.upc.trackingclient.actions.utils;

import android.app.Activity;
import android.net.Uri;

public interface ProgressListener
{
   void setIndeterminate(boolean indeterminate);

   /**
    * Signifies the start of background task, will be followed by setProgress(int) calls. 
    */
   void started();

   /**
    * Set the progress on the scale of 0...10000
    * 
    * @param value
    * 
    * @see Activity.setProgress 
    * @see Window.PROGRESS_END
    */
   void setProgress(int value);

   /**
    * Signifies end of background task and the location of the result
    * 
    * @param result
    */
   void finished(Uri result);
   
   void showError(String task, String errorMessage, Exception exception);
}