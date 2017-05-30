package com.rahul.video.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.rahul.video.MainActivity;

import java.io.File;

/**
 * Created by rahul on 28-05-2017.
 */

public class CompressCropUtils {

    public static void compressCropVideo(final Context context, String inPath, final String outPath){
        final ProgressDialog mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setMessage("Compressing video...");
        mProgressDialog.show();
        ////////////////////////////////////
        FFmpeg ffmpeg = FFmpeg.getInstance(context);
        int res = MainActivity.highestSquareResolution;
        File file = new File(outPath);
        if (file.exists()){
            file.delete();
        }
        final String[] command = ("-i "+inPath+" -vf crop="+res+":"+res+" -threads 5 -preset ultrafast -strict -2 "+ outPath).split(" ");
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    if (mProgressDialog != null && mProgressDialog.isShowing()){
                        mProgressDialog.dismiss();
                    }
                    Utils.toast(context,"Compression Failed!!"+"\n"+s);
                }

                @Override
                public void onSuccess(String s) {
                    if (mProgressDialog != null && mProgressDialog.isShowing()){
                        mProgressDialog.dismiss();
                    }
                    MediaScannerConnection.scanFile(context,
                            new String[]{outPath}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i("ExternalStorage", "Scanned " + path + ":");
                                    Log.i("ExternalStorage", "-> uri=" + uri);
                                }
                            });
                    Utils.savePreferences(context,"FINAL_VIDEO",outPath);
                    Utils.toast(context,"Compressed successfully!!");
                }

                @Override
                public void onProgress(String s) {
                    //Log.d("TAG", "Started command : ffmpeg "+command);
                    int index=0;
                    if (s.contains("size")){
                        index = s.indexOf("size");
                    }
                    mProgressDialog.setMessage("Processing...\n"+s.substring(index));
                }

                @Override
                public void onStart() {
                    Log.d("TAG", "Started command : ffmpeg " + command);
                    mProgressDialog.setMessage("Compressing video...");
                }

                @Override
                public void onFinish() {
                    Log.d("TAG", "Finished command : ffmpeg "+command);
                    if (mProgressDialog != null && mProgressDialog.isShowing()){
                        mProgressDialog.dismiss();
                    }
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // do nothing for now
            if (mProgressDialog != null && mProgressDialog.isShowing()){
                mProgressDialog.dismiss();
            }
        }
    }
}
