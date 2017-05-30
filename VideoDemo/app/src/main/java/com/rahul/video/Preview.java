package com.rahul.video;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Preview extends ViewGroup implements SurfaceHolder.Callback {
    private final String TAG = "Preview";

    SurfaceView mSurfaceView;
    SurfaceHolder mHolder;
    int heightmax ;
	int widthmax ;
    Size mPreviewSize;
    List<Size> mSupportedPreviewSizes;
    Camera mCamera;

    @SuppressWarnings("deprecation")
    Preview(Context context, SurfaceView sv) {
        super(context);

        mSurfaceView = sv;
//        addView(mSurfaceView);
       
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void setCamera(Camera camera) {
    	mCamera = camera;
    	if (mCamera != null) {
    		mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
    		requestLayout();

    		// get Camera parameters
    		Camera.Parameters params = mCamera.getParameters();
            /////////////////////////////////////////////////////////////////
//            List<Size> previewsizes = params.getSupportedPreviewSizes();
//            List<Size> videosizes = params.getSupportedVideoSizes();
//            int desiredwidth=1080, desiredheight=1080;
//
//            Size optimalPreviewSize = getOptimalPreviewSize(previewsizes, desiredwidth, desiredheight);
//            params.setPreviewSize(optimalPreviewSize.width, optimalPreviewSize.height);
            /////////////////////////////////////////////////////////////////

    		List<String> focusModes = params.getSupportedFocusModes();
    		if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
    			// set the focus mode
    			params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
    			// set Camera parameters
    			mCamera.setParameters(params);
    		}
    		
    	}
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We purposely disregard child measurements because act as a
        // wrapper to a SurfaceView that centers the camera preview instead
        // of stretching it.
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
        	
          mPreviewSize=maxSize();
          
        }
    }

    public Size maxSize(){
    	
//    	heightmax =0;
//    	widthmax =0;
    	Size sizeMax=mSupportedPreviewSizes.get(0);
		//long totalsize = heightmax*widthmax;
    	//long maxsize=mSupportedPreviewSizes.get(0).height*mSupportedPreviewSizes.get(0).width;
    	
    	for(Size size:mSupportedPreviewSizes){
    		if(size.height*size.width>sizeMax.width*sizeMax.height){
    			sizeMax = size;
    			
    		}
    	}
    	
    	return sizeMax;
//    	for(int i = 0;i<mSupportedPreviewSizes.size();i++){
//    		if(maxsize>totalsize){
//    			heightmax = mSupportedPreviewSizes.get(i).height;
//    			widthmax = mSupportedPreviewSizes.get(i).width;
//    			totalsize=maxsize;
//    		}
//    	}
    	
    	
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed && getChildCount() > 0) {
            final View child = getChildAt(0);

            final int width = r - l;
            final int height = b - t;

            int previewWidth = width;
            int previewHeight = height;
            if (mPreviewSize != null) {
                previewWidth = mPreviewSize.width;
                previewHeight = mPreviewSize.height;
            }

            // Center the child SurfaceView within the parent.
            if (width * previewHeight > height * previewWidth) {
                final int scaledChildWidth = previewWidth * height / previewHeight;
                child.layout((width - scaledChildWidth) / 2, 0,
                        (width + scaledChildWidth) / 2, height);
            } else {
                final int scaledChildHeight = previewHeight * width / previewWidth;
                child.layout(0, (height - scaledChildHeight) / 2,
                        width, (height + scaledChildHeight) / 2);
            }
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
            }
        } catch (IOException exception) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public void refreshCamera(Camera camera) {
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }
        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }
        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings
        setCamera(camera);
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d(VIEW_LOG_TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
 
	Camera.AutoFocusCallback mnAutoFocusCallback = new Camera.AutoFocusCallback() {
		@Override
		public void onAutoFocus(boolean success, Camera camera) {

			

		}
	};
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    	if(mCamera != null) {
    		Camera.Parameters parameters = mCamera.getParameters();
//    		parameters.setPictureSize(mPreviewSize.width, mPreviewSize.height);
            ////////////////////////////////////////////////////////////////////
            List<Size> previewsizes = parameters.getSupportedPreviewSizes();
            List<Size> videosizes = parameters.getSupportedVideoSizes();
            ArrayList<Integer> list = new ArrayList<>();
            for (Size size : videosizes){
                list.add(size.height);
            }
            MainActivity.highestSquareResolution = Collections.max(list);
            Log.v("highestSquareResolution",""+MainActivity.highestSquareResolution);

            Size optimalPreviewSize = getOptimalPreviewSize(previewsizes, MainActivity.highestSquareResolution, MainActivity.highestSquareResolution);

            Size optimalVideoSize = getOptimalPreviewSize(videosizes, MainActivity.highestSquareResolution, MainActivity.highestSquareResolution);

            parameters.setPreviewSize(optimalPreviewSize.width, optimalPreviewSize.height);

            //////////////////////////////////////////////////////

    		requestLayout();

    		mCamera.setParameters(parameters);
    		mCamera.startPreview();
    		
    	}
    }

    ///////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;
        if (sizes == null)
            return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            Log.d("Camera", "Checking size " + size.width + "w " + size.height
                    + "h");
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the
        // requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

}
