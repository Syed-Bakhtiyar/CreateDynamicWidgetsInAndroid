package bilal.com.createdynamicwidgets;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraActivity extends Activity implements Camera.PictureCallback, SurfaceHolder.Callback,View.OnClickListener {

    public static final String EXTRA_CAMERA_DATA = "camera_data";

    private static final String KEY_IS_CAPTURING = "is_capturing";
    private static final String TAG = "Tag" ;

    LinearLayout doneButton, reset;

    private Camera mCamera;
    private ImageView mCameraImage;
    private ImageView flash;
    private SurfaceView mCameraPreview;
    private ImageView mCaptureImageButton;
    private byte[] mCameraData;
    private boolean mIsCapturing;

    int rotationField = -1;

    TextView title;

//    ImageView reset;

    public static Uri uri;

//    ImageView doneButton;

    ProgressDialog dialogSave;

    TextView check;

    Camera.Parameters parameters;

    private File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/StorePerfectApp/takePictures/");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
//                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName="MI_"+ timeStamp +".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    private View.OnClickListener mCaptureImageButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            captureImage();
        }
    };

    private View.OnClickListener mRecaptureImageButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCaptureImageButton.setVisibility(View.VISIBLE);

            setupImageCapture();

//            Intent intent = getIntent();
//
//            finish();
//
//            startActivity(intent);
        }
    };

    private View.OnClickListener mDoneButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {



        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);

        dialogSave = new ProgressDialog(this);



//        check = (TextView) findViewById(R.id_for_report_summary.check);

        dialogSave.setMessage("Saving Image...");

//        check.setVisibility(View.GONE);

        dialogSave.setCanceledOnTouchOutside(false);


        title = (TextView) findViewById(R.id.title);

        flash = (ImageView) findViewById(R.id.flash);

//        title.setText(StaticClass.jsDetailName);

        findViewById(R.id.button_back).setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {

                CameraActivity.this.onBackPressed();
            }
        });



        OrientationEventListener orientationListener = new OrientationEventListener(CameraActivity.this, SensorManager.SENSOR_DELAY_NORMAL) {
            public void onOrientationChanged(int orientation) {

                Log.d("orientation", ""+orientation);

//                if(canShow(orientation)){
//                    show();
//                } else if(canDismiss(orientation)){
//                    dismiss();
//                }
            }
        };

        flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);

                mCamera.setParameters(parameters);

                mCamera.startPreview();

                Toast.makeText(CameraActivity.this, "On Mode", Toast.LENGTH_SHORT).show();


//
//                if(parameters.getFlashMode() == Camera.Parameters.FLASH_MODE_OFF){
//
//
//                }else {
//
//                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
//
//                    mCamera.setParameters(parameters);
//
//                    mCamera.startPreview();
//
//                    Toast.makeText(CameraActivity.this, "On Mode", Toast.LENGTH_SHORT).show();
//
//
//                }



            }
        });



        mCameraImage = (ImageView) findViewById(R.id.camera_image_view);
        mCameraImage.setVisibility(View.GONE);
        mCameraImage.setScaleType(ImageView.ScaleType.MATRIX);

//        findViewById(R.id_for_report_summary.rotate).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Matrix matrix = new Matrix();
//                matrix.postRotate(90);
//                mCameraImage.setImageMatrix(matrix);
//
//            }
//        });

        mCameraPreview = (SurfaceView) findViewById(R.id.preview_view);
        final SurfaceHolder surfaceHolder = mCameraPreview.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.setKeepScreenOn(true);
        mCaptureImageButton = (ImageView) findViewById(R.id.capture_image_button);
        reset = (LinearLayout) findViewById(R.id.reset);
//        ok = (ImageView) findViewById(R.id_for_report_summary.done);
        mCaptureImageButton.setOnClickListener(mCaptureImageButtonClickListener);

        doneButton = (LinearLayout) findViewById(R.id.done);

        doneButton.setVisibility(View.GONE);

        reset.setVisibility(View.GONE);

        reset.setOnClickListener(mRecaptureImageButtonClickListener);

        doneButton.setOnClickListener(this);

        mIsCapturing = true;
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putBoolean(KEY_IS_CAPTURING, mIsCapturing);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mIsCapturing = savedInstanceState.getBoolean(KEY_IS_CAPTURING, mCameraData == null);
        if (mCameraData != null) {
            setupImageDisplay();
        } else {
            setupImageCapture();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mCamera == null) {
            try {
                mCamera = Camera.open();
                mCamera.setPreviewDisplay(mCameraPreview.getHolder());

                parameters = mCamera.getParameters();

                Display display = ((WindowManager)
                        getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

                int screenOrientation = display.getRotation();

                int rotation = 0;

                switch (screenOrientation){

                    case Surface.ROTATION_0:

                        findViewById(R.id.toplayout).setVisibility(View.VISIBLE);
                        rotationField = Surface.ROTATION_0;
                        rotation = Surface.ROTATION_0;
                        mCamera.setDisplayOrientation(90);
                        break;

                    case Surface.ROTATION_90:
                        findViewById(R.id.toplayout).setVisibility(View.GONE);
                        rotationField = Surface.ROTATION_90;
                        rotation = Surface.ROTATION_90;
                        mCamera.setDisplayOrientation(0);
                        break;

                    case Surface.ROTATION_270:
                        findViewById(R.id.toplayout).setVisibility(View.GONE);
                        rotationField = Surface.ROTATION_270;
                        rotation = Surface.ROTATION_270;
                        mCamera.setDisplayOrientation(180);
                        break;

                    case Surface.ROTATION_180:
                        rotationField = Surface.ROTATION_180;
                        rotation = Surface.ROTATION_180;
                        mCamera.setDisplayOrientation(270);
                        break;



                }

                Log.d("rotaion", "onResume: "+rotation);

//                mCamera.setDisplayOrientation(90);
                if (mIsCapturing) {
                    mCamera.startPreview();
                }
            } catch (Exception e) {
                Toast.makeText(CameraActivity.this, "Unable to open camera.", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        mCameraData = data;
        setupImageDisplay();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(holder);
                if (mIsCapturing) {
                    mCamera.startPreview();
                }
            } catch (IOException e) {
                Toast.makeText(CameraActivity.this, "Unable to start camera preview.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    private void captureImage() {


        mCamera.takePicture(null, null, new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

                if (data != null) {
                    int screenWidth = getResources().getDisplayMetrics().widthPixels;
                    int screenHeight = getResources().getDisplayMetrics().heightPixels;
                    Bitmap bm = BitmapFactory.decodeByteArray(data, 0, (data != null) ? data.length : 0);

                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && rotationField == 0) {
                        // Notice that width and height are reversed
                        Bitmap scaled = Bitmap.createScaledBitmap(bm, screenHeight, screenWidth, true);
                        int w = scaled.getWidth();
                        int h = scaled.getHeight();
                        // Setting post rotate to 90
                        Matrix mtx = new Matrix();
                        mtx.postRotate(90);
                        // Rotating Bitmap
                        bm = Bitmap.createBitmap(scaled, 0, 0, w, h, mtx, true);


                    }else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && rotationField == 1){// LANDSCAPE MODE
                        //No need to reverse width and height
                        Bitmap scaled = Bitmap.createScaledBitmap(bm, screenWidth,screenHeight , true);
                        int w = scaled.getWidth();
                        int h = scaled.getHeight();
                        // Setting post rotate to 90
                        Matrix mtx = new Matrix();
                        mtx.postRotate(0);
                        // Rotating Bitmap


                        bm = Bitmap.createBitmap(scaled, 0, 0, w, h, mtx, true);
                    }else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && rotationField == 3){

                        Bitmap scaled = Bitmap.createScaledBitmap(bm, screenWidth,screenHeight , true);
                        int w = scaled.getWidth();
                        int h = scaled.getHeight();
                        // Setting post rotate to 90
                        Matrix mtx = new Matrix();
                        mtx.postRotate(180);



                        bm = Bitmap.createBitmap(scaled, 0, 0, w, h, mtx, true);


                    }
                    bitmapGlobal = bm;
//                    photoPreview.setImageBitmap(bm);
                }
//                isImageCaptured = true;
//                photoPreview.setVisibility(View.VISIBLE);
//                surfaceView.setVisibility(View.GONE);
            }
        } );
        mCaptureImageButton.setVisibility(View.GONE);
        mCameraImage.setVisibility(View.VISIBLE);
        doneButton.setVisibility(View.VISIBLE);
        reset.setVisibility(View.VISIBLE);

    }

    private void setupImageCapture() {


        try {
            if (mCamera != null) {
                reset.setVisibility(View.GONE);
                doneButton.setVisibility(View.GONE);
                mCameraImage.setVisibility(View.INVISIBLE);

                mCameraPreview.setVisibility(View.VISIBLE);
                mCamera.stopPreview();
                mCamera.startPreview();
                mCaptureImageButton.setVisibility(View.VISIBLE);
//        mCaptureImageButton.setText(R.string.capture_image);
                mCaptureImageButton.setOnClickListener(mCaptureImageButtonClickListener);
            }
        }catch (Exception e){

            Log.d(TAG, ""+e);

        }
    }

    private void setupImageDisplay() {
        Bitmap bitmap = BitmapFactory.decodeByteArray(mCameraData, 0, mCameraData.length);
        mCameraImage.setImageBitmap(bitmap);
        bitmapGlobal = bitmap;
//        this.setResult(100);
        mCamera.stopPreview();
        mCameraPreview.setVisibility(View.INVISIBLE);
        mCameraImage.setVisibility(View.VISIBLE);
//        mCaptureImageButton.setText(R.string.recapture_image);
        mCaptureImageButton.setOnClickListener(mRecaptureImageButtonClickListener);
    }

    public static Bitmap bitmapGlobal = null;

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.done) {


            new SavingFile().execute();
        }


    }


    class SavingFile extends AsyncTask<Void,Void,Void> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialogSave.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            if(bitmapGlobal != null) {

                File pictureFile = getOutputMediaFile();
                if (pictureFile == null) {
                    Log.d(TAG,
                            "Error creating media file, check storage permissions: ");// e.getMessage());
//                return;
                }
                try {

//                    BitmapFactory.Options o = new BitmapFactory.Options();
//                    o.inJustDecodeBounds = true;
//                    BitmapFactory.decodeStream(new FileInputStream(f),null,o);
//
//                    //The new size we want to scale to
//                    final int REQUIRED_SIZE=70;
//
//                    //Find the correct scale value. It should be the power of 2.
//                    int scale=1;
//                    while(o.outWidth/scale/2>=REQUIRED_SIZE && o.outHeight/scale/2>=REQUIRED_SIZE)
//                        scale*=2;
//
//                    //Decode with inSampleSize
//                    BitmapFactory.Options o2 = new BitmapFactory.Options();
//                    o2.inSampleSize=scale;
//
                    bitmapGlobal = Bitmap.createScaledBitmap(bitmapGlobal,480,640,false);

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                    bitmapGlobal.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);


                    FileOutputStream fos = new FileOutputStream(pictureFile);

                    fos.write(byteArrayOutputStream.toByteArray());


                    fos.close();

                    uri = Uri.parse(pictureFile.getPath());

//                    finish();

                } catch (FileNotFoundException e) {

                    Log.d(TAG, "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d(TAG, "Error accessing file: " + e.getMessage());
                }
            }

//
//            dialogSave.dismiss();
//
//            finish();



            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Intent intent = new Intent();
            intent.putExtra(EXTRA_CAMERA_DATA, mCameraData);
            setResult(RESULT_OK, intent);

            finish();


        }
    }


}








