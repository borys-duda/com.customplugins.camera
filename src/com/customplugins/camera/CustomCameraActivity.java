
package com.customplugins.camera;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import static android.hardware.Camera.Parameters.FOCUS_MODE_AUTO;
import static android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;

public class CustomCameraActivity extends Activity {

    private static final String TAG = CustomCameraActivity.class.getSimpleName();
    private static final float ASPECT_RATIO = 126.0f / 86;

    public static String FILENAME = "Filename";
    public static String QUALITY = "Quality";
    public static String TARGET_WIDTH = "TargetWidth";
    public static String TARGET_HEIGHT = "TargetHeight";
    public static String DestinationType = "DestinationType";
    public static String PictureSourceType = "PictureSourceType";
    public static String Direction = "Direction";
    
    public static String IMAGE_URI = "ImageUri";
    public static String IMAGE_BITMAP = "ImageBitmap";
    public static String DATA_URL = "DataURL";
    public static String ERROR_MESSAGE = "ErrorMessage";
    public static int RESULT_ERROR = 2;

    private Camera camera;
    private RelativeLayout layout;
    private FrameLayout cameraPreviewView;
    private ImageView borderTopLeft;
    private ImageView borderTopRight;
    private ImageView borderBottomLeft;
    private ImageView borderBottomRight;
    private ImageButton captureButton;
    
    private ImageView photoView;
    private Button useButton;
    private Button retakeButton;
    
    private static int currentCameraId;
    
    Bitmap capturedImage;
    byte[] imageData;

    @Override
    protected void onResume() {
        super.onResume();
        try {
        	currentCameraId = getIntent().getIntExtra(Direction, 0); //Camera.CameraInfo.CAMERA_FACING_BACK;
            camera = Camera.open(currentCameraId);
            configureCamera();
            displayCameraPreview();
        } catch (Exception e) {
            finishWithError("Camera is not accessible");
        }
    }

    private void configureCamera() {
        Camera.Parameters cameraSettings = camera.getParameters();
        cameraSettings.setJpegQuality(100);
        List<String> supportedFocusModes = cameraSettings.getSupportedFocusModes();
        if (supportedFocusModes.contains(FOCUS_MODE_CONTINUOUS_PICTURE)) {
            cameraSettings.setFocusMode(FOCUS_MODE_CONTINUOUS_PICTURE);
        } else if (supportedFocusModes.contains(FOCUS_MODE_AUTO)) {
            cameraSettings.setFocusMode(FOCUS_MODE_AUTO);
        }
        cameraSettings.setFlashMode(FOCUS_MODE_AUTO);
        camera.setParameters(cameraSettings);
    }

    private void displayCameraPreview() {
        cameraPreviewView.removeAllViews();
        cameraPreviewView.addView(new CustomCameraPreview(this, camera));
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        layout = new RelativeLayout(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layout.setLayoutParams(layoutParams);
        createCameraPreview();
        createTopLeftBorder();
        createTopRightBorder();
//        createBottomLeftBorder();
//        createBottomRightBorder();
//        layoutBottomBorderImagesRespectingAspectRatio();
        createCaptureButton();
        setContentView(layout);
    }

    private void createCameraPreview() {
        cameraPreviewView = new FrameLayout(this);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        cameraPreviewView.setLayoutParams(layoutParams);
        layout.addView(cameraPreviewView);
    }

    private void createTopLeftBorder() {
        borderTopLeft = new ImageView(this);
        setBitmap(borderTopLeft, "border_top_left.png");
//        borderTopLeft.setImageResource(R.drawable.border_top_left);
//        borderTopLeft.setImageResource(getResources().getIdentifier("border_top_left", "id", getPackageName()));

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(dpToPixels(50), dpToPixels(150));
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        if (isXLargeScreen()) {
            layoutParams.topMargin = dpToPixels(800);
            layoutParams.leftMargin = dpToPixels(600);
        } else if (isLargeScreen()) {
            layoutParams.topMargin = dpToPixels(400);
            layoutParams.leftMargin = dpToPixels(300);
        } else {
            layoutParams.topMargin = dpToPixels(80);
            layoutParams.leftMargin = dpToPixels(60);
        }
        borderTopLeft.setLayoutParams(layoutParams);
        layout.addView(borderTopLeft);
    }

    private void createTopRightBorder() {
        borderTopRight = new ImageView(this);
        setBitmap(borderTopRight, "border_top_right.png");
//        borderTopRight.setImageResource(getResources().getIdentifier("border_top_right", "id", getPackageName()));

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(dpToPixels(50), dpToPixels(150));
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        if (isXLargeScreen()) {
            layoutParams.topMargin = dpToPixels(800);
            layoutParams.rightMargin = dpToPixels(600);
        } else if (isLargeScreen()) {
            layoutParams.topMargin = dpToPixels(400);
            layoutParams.rightMargin = dpToPixels(300);
        } else {
            layoutParams.topMargin = dpToPixels(80);
            layoutParams.rightMargin = dpToPixels(60);
        }
        borderTopRight.setLayoutParams(layoutParams);
        layout.addView(borderTopRight);
    }

    private void createBottomLeftBorder() {
        borderBottomLeft = new ImageView(this);
        setBitmap(borderBottomLeft, "border_bottom_left.png");
//        borderBottomLeft.setImageResource(getResources().getIdentifier("border_bottom_left", "id", getPackageName()));
        
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(dpToPixels(50), dpToPixels(50));
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        if (isXLargeScreen()) {
            layoutParams.leftMargin = dpToPixels(600);
        } else if (isLargeScreen()) {
            layoutParams.leftMargin = dpToPixels(300);
        } else {
            layoutParams.leftMargin = dpToPixels(60);
        }
        borderBottomLeft.setLayoutParams(layoutParams);
        layout.addView(borderBottomLeft);
    }

    private void createBottomRightBorder() {
        borderBottomRight = new ImageView(this);
        setBitmap(borderBottomRight, "border_bottom_right.png");
//        borderBottomRight.setImageResource(getResources().getIdentifier("border_bottom_right", "id", getPackageName()));
        
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(dpToPixels(50), dpToPixels(50));
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        if (isXLargeScreen()) {
            layoutParams.rightMargin = dpToPixels(600);
        } else if (isLargeScreen()) {
            layoutParams.rightMargin = dpToPixels(300);
        } else {
            layoutParams.rightMargin = dpToPixels(60);
        }
        borderBottomRight.setLayoutParams(layoutParams);
        layout.addView(borderBottomRight);
    }

    private void layoutBottomBorderImagesRespectingAspectRatio() {
        RelativeLayout.LayoutParams borderTopLeftLayoutParams = (RelativeLayout.LayoutParams)borderTopLeft.getLayoutParams();
        RelativeLayout.LayoutParams borderTopRightLayoutParams = (RelativeLayout.LayoutParams)borderTopRight.getLayoutParams();
        RelativeLayout.LayoutParams borderBottomLeftLayoutParams = (RelativeLayout.LayoutParams)borderBottomLeft.getLayoutParams();
        RelativeLayout.LayoutParams borderBottomRightLayoutParams = (RelativeLayout.LayoutParams)borderBottomRight.getLayoutParams();
        float height = (screenWidthInPixels() - borderTopRightLayoutParams.rightMargin - borderTopLeftLayoutParams.leftMargin) * ASPECT_RATIO;
        borderBottomLeftLayoutParams.bottomMargin = screenHeightInPixels() - Math.round(height) - borderTopLeftLayoutParams.topMargin;
        borderBottomLeft.setLayoutParams(borderBottomLeftLayoutParams);
        borderBottomRightLayoutParams.bottomMargin = screenHeightInPixels() - Math.round(height) - borderTopRightLayoutParams.topMargin;
        borderBottomRight.setLayoutParams(borderBottomRightLayoutParams);
    }

    private int screenWidthInPixels() {
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        return size.x;
    }

    private int screenHeightInPixels() {
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        return size.y;
    }

    private void createCaptureButton() {
        captureButton = new ImageButton(getApplicationContext());
        setBitmap(captureButton, "capture_button.png");
//        captureButton.setImageResource(getResources().getIdentifier("capture_button", "id", getPackageName()));
        captureButton.setBackgroundColor(Color.TRANSPARENT);
        captureButton.setScaleType(ScaleType.FIT_CENTER);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(dpToPixels(75), dpToPixels(75));
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.bottomMargin = dpToPixels(10);
        captureButton.setLayoutParams(layoutParams);
        captureButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                setCaptureButtonImageForEvent(event);
                return false;
            }
        });
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePictureWithAutoFocus();
            }
        });
        layout.addView(captureButton);
    }

    private void setCaptureButtonImageForEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            setBitmap(captureButton, "capture_button_pressed.png");
//            captureButton.setImageResource(getResources().getIdentifier("capture_button_pressed", "id", getPackageName()));
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            setBitmap(captureButton, "capture_button.png");
//            captureButton.setImageResource(getResources().getIdentifier("capture_button", "id", getPackageName()));
        }
    }

    private void takePictureWithAutoFocus() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS)) {
            camera.autoFocus(new AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    takePicture();
                }
            });
        } else {
            takePicture();
        }
    }

    private void takePicture() {
        try {
            camera.takePicture(null, null, new PictureCallback() {
                @Override
                public void onPictureTaken(byte[] jpegData, Camera camera) {
                	camera.stopPreview();
                	try {
                		imageData = jpegData;
                		
                        capturedImage = getScaledBitmap(jpegData, true);
                        capturedImage = correctCaptureImageOrientation(capturedImage);
                        
                        onPause();
                        
                        Handler refresh = new Handler(Looper.getMainLooper());
                        refresh.post(new Runnable() {
                            public void run()
                            {
                                layout.removeAllViewsInLayout();
                                createPhotoView();
                                createUseButton();
                                createRetakeButton();
                            }
                        });
                    } catch (Exception e) {
                        finishWithError("Failed to save image");
                    }
                }
            });
        } catch (Exception e) {
            finishWithError("Failed to take image");
        }
    }
    
    private Bitmap getScaledBitmap(byte[] jpegData, boolean preview) {
    	
        int targetWidth = getIntent().getIntExtra(TARGET_WIDTH, -1);
        int targetHeight = getIntent().getIntExtra(TARGET_HEIGHT, -1);
        if (targetWidth <= 0 && targetHeight <= 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length);
        	if(preview)
        	{
        		targetWidth = bitmap.getWidth()/2;
        		targetHeight = bitmap.getHeight()/2;
        	} else
        		return bitmap;
        }

        // get dimensions of image without scaling
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, options);

        // decode image as close to requested scale as possible
        options.inJustDecodeBounds = false;
        options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight);
        Bitmap bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, options);

        // set missing width/height based on aspect ratio
        float aspectRatio = ((float)options.outHeight) / options.outWidth;
        if (targetWidth > 0 && targetHeight <= 0) {
            targetHeight = Math.round(targetWidth * aspectRatio);
        } else if (targetWidth <= 0 && targetHeight > 0) {
            targetWidth = Math.round(targetHeight / aspectRatio);
        }

        // make sure we also
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int requestedWidth, int requestedHeight) {
        int originalHeight = options.outHeight;
        int originalWidth = options.outWidth;
        int inSampleSize = 1;
        if (originalHeight > requestedHeight || originalWidth > requestedWidth) {
            int halfHeight = originalHeight / 2;
            int halfWidth = originalWidth / 2;
            while ((halfHeight / inSampleSize) > requestedHeight && (halfWidth / inSampleSize) > requestedWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private Bitmap correctCaptureImageOrientation(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private void finishWithError(String message) {
        Intent data = new Intent().putExtra(ERROR_MESSAGE, message);
        setResult(RESULT_ERROR, data);
        finish();
    }

    private int dpToPixels(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private boolean isXLargeScreen() {
        int screenLayout = getResources().getConfiguration().screenLayout;
        return (screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private boolean isLargeScreen() {
        int screenLayout = getResources().getConfiguration().screenLayout;
        return (screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    private void setBitmap(ImageView imageView, String imageName) {
        try {
            InputStream imageStream = getAssets().open("www/img/cameraoverlay/" + imageName);
            Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
            imageView.setImageBitmap(bitmap);
            imageStream.close();
        } catch (Exception e) {
            Log.e(TAG, "Could load image", e);
        }
    }
    
    private void createPhotoView() {
		photoView = new ImageView(this);

		photoView.setImageBitmap(capturedImage);
	    /*
		Bitmap bitmap;
		try {
			Uri fileUri = (Uri) getIntent().getExtras().get(
        			FILENAME);
			bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), fileUri);
			photoView.setImageBitmap(bitmap);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			finish();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			finish();
		}
		*/
		DisplayMetrics metrics = this.getResources().getDisplayMetrics();
		int width = metrics.widthPixels;
		int height = metrics.heightPixels;
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        layoutParams.topMargin = dpToPixels(0);
        layoutParams.leftMargin = dpToPixels(0);
        photoView.setLayoutParams(layoutParams);
        layout.addView(photoView);
	}
    
    private class CompleteCapturedImageTask extends AsyncTask<byte[], Void, Boolean> {
    	private ProgressDialog dialog = new ProgressDialog(CustomCameraActivity.this);

        /** progress dialog to show user that the backup is processing. */
        /** application context. */
        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Please wait");
            this.dialog.show();
        }
        
        @Override
        protected Boolean doInBackground(byte[]... jpegData) {
            try {
            	
            	Uri fileUri = (Uri) getIntent().getExtras().get(
            			FILENAME);
        		int quality = getIntent().getIntExtra(QUALITY, 80);
            	File capturedImageFile = new File(fileUri.getPath());
            	capturedImage = getScaledBitmap(imageData, false);
                capturedImage = correctCaptureImageOrientation(capturedImage);
        		capturedImage.compress(CompressFormat.JPEG, quality, new FileOutputStream(capturedImageFile));
        		setResult(RESULT_OK);
                finish();
            } catch (Exception e) {
                finishWithError("Failed to save image");
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    private void createUseButton() {
		useButton = new Button(getApplicationContext());
//        captureButton.setImageResource(getResources().getIdentifier("capture_button", "id", getPackageName()));
        useButton.setBackgroundColor(Color.TRANSPARENT);
        useButton.setText("Use");
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(dpToPixels(120), dpToPixels(40));
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        if (isXLargeScreen()) {
            layoutParams.rightMargin = dpToPixels(150);
        } else if (isLargeScreen()) {
            layoutParams.rightMargin = dpToPixels(70);
        } else {
            layoutParams.rightMargin = dpToPixels(15);
        }
        layoutParams.bottomMargin = dpToPixels(10);
        useButton.setLayoutParams(layoutParams);
        useButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
//                setCaptureButtonImageForEvent(event);
                return false;
            }
        });
        useButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	
            	onResume();
            	new CompleteCapturedImageTask().execute(new byte[1]);
//                takePictureWithAutoFocus();
            	
            }
        });
        layout.addView(useButton);
    }
	
	
	private void createRetakeButton() {
		retakeButton = new Button(getApplicationContext());
//        captureButton.setImageResource(getResources().getIdentifier("capture_button", "id", getPackageName()));
		retakeButton.setBackgroundColor(Color.TRANSPARENT);
		retakeButton.setText("Retake");
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(dpToPixels(120), dpToPixels(40));
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        if (isXLargeScreen()) {
            layoutParams.leftMargin = dpToPixels(150);
        } else if (isLargeScreen()) {
            layoutParams.leftMargin = dpToPixels(70);
        } else {
            layoutParams.leftMargin = dpToPixels(15);
        }
        
        layoutParams.bottomMargin = dpToPixels(10);
        
        retakeButton.setLayoutParams(layoutParams);
        retakeButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
//                setCaptureButtonImageForEvent(event);
                return false;
            }
        });
        retakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handler refresh = new Handler(Looper.getMainLooper());
                refresh.post(new Runnable() {
                    public void run()
                    {
                        layout.removeAllViewsInLayout();
                        createCameraPreview();
                        createTopLeftBorder();
                        createTopRightBorder();
//                        createBottomLeftBorder();
//                        createBottomRightBorder();
//                        layoutBottomBorderImagesRespectingAspectRatio();
                        createCaptureButton();
                        onResume();
                    }
                });
            }
        });
        layout.addView(retakeButton);
    }
}
