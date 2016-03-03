package com.customplugins.camera;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;

import static com.customplugins.camera.CustomCameraActivity.DestinationType;
import static com.customplugins.camera.CustomCameraActivity.Direction;
import static com.customplugins.camera.CustomCameraActivity.ERROR_MESSAGE;
import static com.customplugins.camera.CustomCameraActivity.FILENAME;
import static com.customplugins.camera.CustomCameraActivity.PictureSourceType;
import static com.customplugins.camera.CustomCameraActivity.QUALITY;
import static com.customplugins.camera.CustomCameraActivity.RESULT_ERROR;
import static com.customplugins.camera.CustomCameraActivity.TARGET_HEIGHT;
import static com.customplugins.camera.CustomCameraActivity.TARGET_WIDTH;

public class CustomCamera extends CordovaPlugin {
	
	private static final int TAKE_PHOTO = 2;
	private static final int SELECT_PICTURE = 3;
	private static int destinationType;
	private static int mQuality;
	private static int mTargetWidth;
	private static int mTargetHeight;
	private static int mPictureSourceType;
	private static int mDirection = 0;
	
    private CallbackContext callbackContext;
    Context context;
    Uri imageUri;
    
	@Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
	    if (!hasRearFacingCamera()) {
	        callbackContext.error("No rear camera detected");
	        return false;
	    }
	    this.callbackContext = callbackContext;
	    context = cordova.getActivity().getApplicationContext();
	    
	    mQuality = args.getInt(1);
	    mTargetWidth = args.getInt(2);
	    mTargetHeight = args.getInt(3);
	    destinationType = args.getInt(4);
	    mPictureSourceType = args.getInt(5);
	    mDirection = args.getInt(6);
	    if(mPictureSourceType == 1)
	    {
	    	Intent intent = new Intent(context, CustomCameraActivity.class);
	    	
			File image= createCaptureFile();
			imageUri = Uri.fromFile(image);
			
		    intent.putExtra(FILENAME, imageUri);
		    intent.putExtra(QUALITY, mQuality);
		    intent.putExtra(TARGET_WIDTH, mTargetWidth);
		    intent.putExtra(TARGET_HEIGHT, mTargetHeight);
		    intent.putExtra(DestinationType, destinationType);
		    intent.putExtra(PictureSourceType, mPictureSourceType);
		    intent.putExtra(Direction, mDirection);
		    cordova.startActivityForResult(this, intent, TAKE_PHOTO);
	    } else {
	    	Intent intent1 = new Intent();
	        intent1.setType("image/*");
	        intent1.setAction(Intent.ACTION_GET_CONTENT);
	        this.cordova.startActivityForResult((CordovaPlugin) this, Intent.createChooser(intent1,
	                "Select Picture"), SELECT_PICTURE);
	    }
	    
        return true;
    }

	private boolean hasRearFacingCamera() {
	    Context context = cordova.getActivity().getApplicationContext();
	    return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
	}

    /**
	 * Create a file in the applications temporary directory based upon the
	 * supplied encoding.
	 * 
	 * @return a File object pointing to the temporary picture
	 */
	private File createCaptureFile() {
		Date date = new Date() ;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss") ;
		
		File photo = new File(getTempDirectoryPath(this.cordova.getActivity().getApplicationContext()), dateFormat.format(date) + ".jpg");
		return photo;
	}
	
	/**
	 * Determine if we can use the SD Card to store the temporary file. If not
	 * then use the internal cache directory.
	 * 
	 * @return the absolute path of where to store the file
	 */
	private String getTempDirectoryPath(Context ctx) {
		File cache = null;

		// SD Card Mounted
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			cache = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath()
					+ "/Android/data/"
					+ ctx.getPackageName() + "/cache/");
		}
		// Use internal storage
		else {
			cache = ctx.getCacheDir();
		}

		// Create the cache directory if it doesn't exist
		if (!cache.exists()) {
			cache.mkdirs();
		}

		return cache.getAbsolutePath();
	}
    
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (resultCode == Activity.RESULT_OK) {
	    	if (requestCode == SELECT_PICTURE) {
				Uri selectedImageUri = intent.getData();
				if(destinationType == 1)
			    {
					String strPath = getPath(selectedImageUri);
					this.callbackContext.success(strPath);
			    } else {
			    	Bitmap bitmap;
					try {
						bitmap = MediaStore.Images.Media.getBitmap(cordova.getActivity().getContentResolver(), selectedImageUri);
						bitmap = correctCaptureImageOrientation(bitmap);
//			            this.callbackContext.success(getBytes(bitmap));
			            this.callbackContext.success(encodeTobase64(bitmap));
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    }
	    	} else {
	    		if(destinationType == 1)
			    {
		    		String strUri = imageUri.toString();
		    		callbackContext.success(strUri);
			    } else {
			    	Bitmap bitmap;
					try {
						bitmap = MediaStore.Images.Media.getBitmap(cordova.getActivity().getContentResolver(), imageUri);
			            this.callbackContext.success(encodeTobase64(bitmap));
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    }
	    	}
	    } else if (resultCode == RESULT_ERROR) {
	        String errorMessage = intent.getExtras().getString(ERROR_MESSAGE);
	        if (errorMessage != null) {
	            callbackContext.error(errorMessage);
	        } else {
	            callbackContext.error("Failed to take picture");
	        }
	    }
    }
	

    
        private Bitmap correctCaptureImageOrientation(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }


	public static byte[] getBytes(Bitmap image)
	{
		Bitmap immagex=image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        immagex.compress(Bitmap.CompressFormat.JPEG, mQuality, baos);
        byte[] b = baos.toByteArray();
        return b;
	}
	
	public static String encodeTobase64(Bitmap image)
    {
        Bitmap immagex=image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        immagex.compress(Bitmap.CompressFormat.JPEG, mQuality, baos);
        immagex.recycle();
        immagex = null;
        byte[] b = baos.toByteArray();
        try {
			baos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        baos = null;
        String imageEncoded = Base64.encodeToString(b,Base64.DEFAULT);

        Log.e("LOOK", imageEncoded);
        return imageEncoded;
    }

	/**
     * helper to retrieve the path of an image URI
     */
    public String getPath(Uri uri) {
            // just some safety built in 
            if( uri == null ) {
                // TODO perform some logging or show user feedback
                return null;
            }
            // try to retrieve the image from the media store first
            // this will only work for images selected from gallery
            String[] projection = { MediaStore.Images.Media.DATA };
            Cursor cursor = cordova.getActivity().managedQuery(uri, projection, null, null, null);
            if( cursor != null ){
                int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            }
            // this is our fallback here
            return uri.getPath();
    }
}
