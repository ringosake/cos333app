package com.example.cos333app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.webkit.URLUtil;

public class ImageDownloadTask extends AsyncTask<String, Void, Bitmap> {
	FileOutputStream fos;
	String email;
	String groupID;
	private static String backupLink = "http://2.bp.blogspot.com/_Ze5Xm5fW-4o/TUnUBt6ADUI/AAAAAAAABJA/2WGSLTNK1K4/s1600/broken-link-image-gif.jpg";
	 
	/**
	 * Prepares a download task. First arg should be URL. Subsequent args are optional; if used, the second arg should
	 * be email and the second arg should be groupID.
	 */
    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        //if (urls.length > 1) {
        	String email = urls[1];
        	this.email = urls[1];
        //}
        //if (urls.length > 2) {
        	String groupID = urls[2];
        	this.groupID = urls[2];
        //}
        Bitmap mIcon11 = null;
        urldisplay = urldisplay.replace(" ", "");
        if (!URLUtil.isValidUrl(urldisplay)) {
        	urldisplay = backupLink;
        	try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
        	return mIcon11;
        }
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        Log.d("downloaded", "Successfully downloaded file");
        return mIcon11;
    }

    protected void onPostExecute(Bitmap bmp) {
    	// Do your staff here to save image
    	// --- this method will save your downloaded image to SD card ---
    	
    	//ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    	OutputStream fOut = null;
    	
    	//--- you can select your preferred CompressFormat and quality. 
    	//  I'm going to use JPEG and 100% quality ---
    	//bmp.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
    	//--- create a new file on SD card ---
    	String userName = this.email;
    	userName = userName.replace("@", "");
    	userName = userName.replace(".", "");
    	File fileStump = new File(Environment.getExternalStorageDirectory() // change code above to refer to this dir
			 			+ File.separator + "group_logos" + File.separator + userName + File.separator); // + this.groupID + ".jpg"); // name these dynamically
    	if (!fileStump.exists())	
        	fileStump.mkdirs();
    	File file = new File(fileStump.toString() + File.separator + this.groupID + ".jpg");
    	Log.d("filename", file.toString());
    	Log.d("file_separator", File.separator);
        //Log.d("filez", Environment.getExternalStorageDirectory().toString());
    	if (!file.exists()) {
    		try {
    			file.createNewFile();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
    	try {
    		fOut = new FileOutputStream(file);
    	} catch (Exception e) {
    		e.printStackTrace();
    		Log.d("broken file", file.toString());
    	}
    	bmp.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
    	/*
    	try {
    		file.createNewFile();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	//--- create a new FileOutputStream and write bytes to file ---
    	try {
    		FileOutputStream fos = new FileOutputStream(file);
    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    	}*/
    	//Log.d("make file", "Got past the attempt at making a new file");
    	/*
    	try {
    		fos.write(bytes.toByteArray());
    		fos.close();
    		//Toast.makeText(this, "Image saved", Toast.LENGTH_SHORT).show();
    	} catch (IOException e) {
    		e.printStackTrace();
    	} */
    	//Log.d("write file", "Got past the attempt to write the data into the new file");

	    return;
    }
}
