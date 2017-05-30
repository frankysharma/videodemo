package com.rahul.video;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.rahul.video.utils.CompressCropUtils;
import com.rahul.video.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Context context;
    private Preview preview;
    private FrameLayout frame;
    private Button btRecord;
    private Button buttonCameraToggle;
    private Camera camera;
    Camera.Parameters params;
    private boolean cameraFront = false;
    private int heightForPreview;
    private MediaRecorder mediaRecorder;
    public static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/VideoDemo/";
    private String videoPath;
    public static int highestSquareResolution;
    private boolean recording = false;
    private RelativeLayout rlMiddle;
    private LinearLayout llButtons;
    FFmpeg ffmpeg;
    private boolean croppingNotSupported;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        context = MainActivity.this;
        btRecord = (Button)findViewById(R.id.btRecord);
        buttonCameraToggle = (Button)findViewById(R.id.buttonCameraToggle);
        rlMiddle = (RelativeLayout)findViewById(R.id.rlMiddle);
        llButtons = (LinearLayout) findViewById(R.id.llButtons);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        heightForPreview = displayMetrics.widthPixels;

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(heightForPreview,heightForPreview);
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.KutCameraFragment);
        lp.gravity = Gravity.CENTER_VERTICAL;
        rlMiddle.setLayoutParams(lp);
        preview = new Preview(this, surfaceView);
        frame = (FrameLayout) findViewById(R.id.preview);
        frame.addView(preview);
        ffmpeg = FFmpeg.getInstance(context);
        loadFFMpegBinary();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            int hasCameraPermission = checkSelfPermission(Manifest.permission.CAMERA);

            List<String> permissions = new ArrayList<String>();

            if (hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.CAMERA);

            } else {
                initCamera();
            }
            if (!permissions.isEmpty()) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), 111);
            }
        } else {
            initCamera();
        }
    }

    private void initCamera() {
        if (camera != null){
            //camera.stopPreview();
            camera.release();
            camera=null;
        }
        camera = Camera.open();
        camera.startPreview();
        params = camera.getParameters();
        if (camera != null) {
            if (Build.VERSION.SDK_INT >= 14)
                setCameraDisplayOrientation(MainActivity.this,
                        Camera.CameraInfo.CAMERA_FACING_BACK, camera);
            preview.setCamera(camera);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 111: {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        System.out.println("Permissions --> " + "Permission Granted: " + permissions[i]);
                        initCamera();
                    } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        System.out.println("Permissions --> " + "Permission Denied: " + permissions[i]);
                        Utils.toast(context,"Camera permission not granted");
                    }
                }
            }
            break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    private void loadFFMpegBinary() {
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    croppingNotSupported = true;
                    //showUnsupportedExceptionDialog();
                }
            });
        } catch (FFmpegNotSupportedException e) {
            croppingNotSupported = true;
            //showUnsupportedExceptionDialog();
        }
    }

    private void setCameraDisplayOrientation(Activity activity, int cameraId,
                                             android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btBack:
                finish();
                break;
            case R.id.buttonCameraToggle:
                int camerasNumber = Camera.getNumberOfCameras();
                if (camerasNumber > 1) {
                    //release the old camera instance
                    //switch camera, from the front and the back and vice versa
                    releaseCamera();
                    chooseCamera();
                } else {
                    Toast toast = Toast.makeText(context, "Sorry, your phone has only one camera!", Toast.LENGTH_LONG);
                    toast.show();
                }
                break;
            case R.id.btRecord:
                if (!recording){
                    buttonCameraToggle.setVisibility(View.GONE);
                    recordVideo(true);
                } else {
                    buttonCameraToggle.setVisibility(View.VISIBLE);
                    recordVideo(false);
                }
                break;
            case R.id.btPlay:
                startActivity(new Intent(context,PlayerActivity.class));
                break;
            case R.id.btCompress:
                final String inPath = Utils.getPrefrences(context,"VIDEO");
                final String outPath = path + "Video-Final.mp4";
                //new VideoCompressor(context).execute(inPath,outPath);
                if (!croppingNotSupported) {
                    CompressCropUtils.compressCropVideo(context, inPath, outPath);
                } else {
                    Utils.toast(context,"Device doesn't support ffmpeg compression or cropping!!");
                }
                break;
        }
    }

    private void recordVideo(boolean start) {
        if (start){
            //	START VIDEO RECORDING
            releaseCamera();
            if (cameraFront) {
                camera = Camera.open(findFrontFacingCamera());
            } else {
                camera = Camera.open(findBackFacingCamera());
            }
            preview.refreshCamera(camera);
            if (Build.VERSION.SDK_INT >= 14)
                setCameraDisplayOrientation(MainActivity.this,
                        Camera.CameraInfo.CAMERA_FACING_BACK, camera);
            if (!prepareMediaRecorder()) {
                Toast.makeText(context,"Fail in prepareMediaRecorder()",Toast.LENGTH_SHORT).show();
                finish();
            }
            // work on UiThread for better performance
            runOnUiThread(new Runnable() {
                public void run() {
                    // If there are stories, add them to the table
                    try {
                        mediaRecorder.start();
                        recording = true;
                        btRecord.setText("Stop");
                    } catch (final Exception ex) {
                        recording = false;
                        btRecord.setText("Record");
                    }
                }
            });
        } else {
            //	STOP VIDEO RECORDING
            mediaRecorder.stop(); // stop the recording
            releaseMediaRecorder(); // release the MediaRecorder object
            recording = false;
            btRecord.setText("Record");
            Toast.makeText(context,"Video saved!!",Toast.LENGTH_SHORT).show();
            btRecord.setVisibility(View.GONE);
            llButtons.setVisibility(View.VISIBLE);
        }
    }

    private boolean prepareMediaRecorder() {

        mediaRecorder = new MediaRecorder();
        camera.unlock();
        File dir = new File(path);
        if(!dir.exists())
            dir.mkdirs();
        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        videoPath = path + "Video.mp4";
        Utils.savePreferences(context,"VIDEO",videoPath);
        mediaRecorder.setOutputFile(videoPath);
        mediaRecorder.setMaxDuration(60000); // set maximum duration
        mediaRecorder.setMaxFileSize(200000000); // set maximum file size in bytes
        if (cameraFront) {
            mediaRecorder.setOrientationHint(270);
        } else {
            mediaRecorder.setOrientationHint(90);
        }

        MediaScannerConnection.scanFile(context,
                new String[]{videoPath}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d("EXCEPTIONN",""+e);
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d("EXCEPTIONN",""+e);
            releaseMediaRecorder();
            return false;
        }
        return true;

    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset(); // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            camera.lock(); // lock camera for later use
        }
    }

    private void releaseCamera() {
        // stop and release camera
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    public void chooseCamera() {
        //if the camera preview is the front
        if (cameraFront) {
            int cameraId = findBackFacingCamera();
            if (cameraId >= 0) {
                //open the backFacingCamera
                //set a picture callback
                //refresh the preview

                camera = Camera.open(cameraId);
                //jpegCallback = getPictureCallback();
                setCameraDisplayOrientation(MainActivity.this,
                        Camera.CameraInfo.CAMERA_FACING_BACK, camera);
                preview.refreshCamera(camera);
            }
        } else {
            int cameraId = findFrontFacingCamera();
            if (cameraId >= 0) {
                //open the backFacingCamera
                //set a picture callback
                //refresh the preview

                camera = Camera.open(cameraId);
                //jpegCallback = getPictureCallback();
                setCameraDisplayOrientation(MainActivity.this,
                        Camera.CameraInfo.CAMERA_FACING_FRONT, camera);
                preview.refreshCamera(camera);
            }
        }
    }

    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;
    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        //Search for the back facing camera
        //get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        //for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;
            }
        }
        return cameraId;
    }

}
