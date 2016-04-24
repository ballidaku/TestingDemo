package com.example.sharan.testing.fragments;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharan.testing.GIV;
import com.example.sharan.testing.HelperS;
import com.example.sharan.testing.ProgressView;
import com.example.sharan.testing.R;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class VideoFragment extends Fragment implements View.OnClickListener
{
    private CamcorderProfile camcorderProfile;
    ImageView button_capture;
    private Camera camera = null;
    MediaRecorder recorder;
    boolean recording = false;

    boolean previewRunning = false;
    private SurfaceHolder holder  = null;
    private SurfaceView   preview = null;
    private int front_or_back_camera;
    private boolean cameraConfigured = false;
    int       width;
    ImageView flash;
    private boolean isLighOn = false;
    Camera.Parameters   p;
    String              path;
    ProgressBar         progressBar;
    CountDownTimer      counter,counter2;
    //    RelativeLayout      progress_relative_lay;
    OnOrientationChange onOrientationChange;
    Context             con;
    SharedPreferences   rem_pref;
    TextView            timer_text;
    ProgressView        progressView;

    boolean menuVisibleS       = false;
    boolean isfragment_created = false;

    int totalRecordingTime      = 14000;
    int remainingTime      = 14000;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        con = getActivity();
        rem_pref = con.getSharedPreferences("Remember", con.MODE_WORLD_READABLE);

        View rootView = inflater.inflate(R.layout.fragment_video, container, false);
        camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
        preview = (SurfaceView) rootView.findViewById(R.id.preview);
        (button_capture = (ImageView) rootView.findViewById(R.id.button_capture)).setOnClickListener(this);
        //        progress_relative_lay = (RelativeLayout) rootView.findViewById(R.id.progress_relative_lay);
        (progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar)).setOnClickListener(this);
        (rootView.findViewById(R.id.button_switch_camera)).setOnClickListener(this);
        (flash = (ImageView) rootView.findViewById(R.id.flash)).setOnClickListener(this);

        counter2 = new CountDownTimer(remainingTime, 100)
        {

            @Override
            public void onTick(long millisUntilFinished)
            {
                remainingTime=remainingTime-100;
            }

            @Override
            public void onFinish()
            {
            }
        };


        flash.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if(event.getAction()==MotionEvent.ACTION_DOWN)
                {
                    progressView.setCurrentState(ProgressView.State.START);
                    counter2.start();
                }
                else if(event.getAction()==MotionEvent.ACTION_UP)
                {
                    progressView.setCurrentState(ProgressView.State.PAUSE);
                    counter2.cancel();
//                    totalTime = System.currentTimeMillis() - firstTime - pausedTime - ((long) (1.0 / (double) frameRate) * 1000);
                   long total= totalRecordingTime-remainingTime;
                    progressView.putProgressList((int) total);
                }


                return false;
            }
        });

        timer_text = (TextView) rootView.findViewById(R.id.timer_text);
        progressView = (ProgressView)rootView. findViewById(R.id.recorder_progress);
        progressView.setTotalTime(totalRecordingTime);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point   size    = new Point();
        display.getSize(size);
        width = size.x;
        ViewGroup.LayoutParams params = preview.getLayoutParams();
        params.height = width;
        params.width = width;
        preview.setLayoutParams(params);

        onOrientationChange = new OnOrientationChange(con);
        front_or_back_camera = Camera.CameraInfo.CAMERA_FACING_BACK;

        isfragment_created = true;
        Log.e("onCreate", "onCreate");

        button_capture.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {

                if (event.getAction() == MotionEvent.ACTION_BUTTON_PRESS)
                {
                    //                    start_camera_recording();
                }
                else if (event.getAction() == MotionEvent.ACTION_BUTTON_RELEASE)
                {
                    //                    pause_camera_recording();
                }
                return false;
            }
        });

        return rootView;
    }

    private void pause_camera_recording()
    {
        if (recording == true)
        {
            recorder.stop();
        }
    }

    private void start_camera_recording()
    {
        if (recorder == null)
        {
            refresh_recoder();
        }
        recording = true;

        recorder.start();

        //*******************************************Start Timer*********************************************
        if (stopped)
        {
            startTime = System.currentTimeMillis() - elapsedTime;
        }
        else
        {
            startTime = System.currentTimeMillis();
        }
        mHandler.removeCallbacks(startTimer);
        mHandler.postDelayed(startTimer, 0);

        //***************************************************************************************************

        counter = new CountDownTimer(14000, 14000 / 100)
        {
            int progress = 0;

            @Override
            public void onTick(long millisUntilFinished)
            {

                progress += 1;
                //                    Log.e("progress!", "" + progress);
                progressBar.setProgress(progress);
            }

            @Override
            public void onFinish()
            {
                ok_tata();
            }
        }.start();




    }

    @Override
    public void onResume()
    {
        super.onResume();

        Log.e("onResume", "onResume");

        if (menuVisibleS == true)
        {
            start_camera();
            onOrientationChange.enable();
        }
    }

    public void start_camera()
    {

        holder = preview.getHolder();
        holder.addCallback(surfaceCallback);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        if (camera == null)
        {
            camera = Camera.open();
        }

        refresh_camera();
    }

    public void stop_camera()
    {
        if (camera != null)
        {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.lock();
            camera.release();
            camera = null;
        }
    }

    @Override
    public void setMenuVisibility(boolean menuVisible)
    {
        super.setMenuVisibility(menuVisible);

        Log.e("menuVisible", "" + menuVisible);

        menuVisibleS = menuVisible;

        if (menuVisible == true && isfragment_created)
        {
            ((GIV) getActivity()).whichFragment = "VideoFragment";

            start_camera();
            onOrientationChange.enable();
        }
        else if (menuVisible == false && isfragment_created)
        {
            stop_camera();
        }
    }

    @Override
    public void onPause()
    {
        if (recording == true)
        {
            start_work();
        }
        else
        {
            onOrientationChange.disable();
            stop_camera();
        }
        super.onPause();
    }

    public void refresh_camera()
    {
        cameraConfigured = false;
        setCameraDisplayOrientation(getActivity(), front_or_back_camera, camera);
        initPreview(width, width);
        startPreview();
    }

    private void startPreview()
    {
        if (cameraConfigured && camera != null)
        {
            camera.startPreview();
        }
    }

    private void initPreview(int width, int height)
    {
        if (camera != null && holder.getSurface() != null)
        {
            try
            {
                Log.e("initPreview", "initPreview");
                camera.setPreviewDisplay(holder);
            }
            catch (Throwable t)
            {
                Log.e("PreviewDemo-surfaceCallback", "Exception in setPreviewDisplay()", t);
                Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
            if (!cameraConfigured)
            {
                p = camera.getParameters();
                Camera.Size size = getBestPreviewSize(width, height, p);
                if (size != null)
                {
                    p.setPreviewSize(size.width, size.height);
                    p.setPreviewSize(camcorderProfile.videoFrameWidth, camcorderProfile.videoFrameHeight);
                    p.setPreviewFrameRate(camcorderProfile.videoFrameRate);
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(p);
                    cameraConfigured = true;
                }
            }
        }
    }

    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters)
    {
        Camera.Size result = null;
        for (Camera.Size size : parameters.getSupportedPreviewSizes())
        {
            if (size.width <= width && size.height <= height)
            {
                if (result == null)
                {
                    result = size;
                }
                else
                {
                    int resultArea = result.width * result.height;
                    int newArea    = size.width * size.height;
                    if (newArea > resultArea)
                    {
                        result = size;
                    }
                }
            }
        }
        return (result);
    }

    int result;

    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera)
    {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        int degrees  = 0;
        Log.e("rotation.......", "" + rotation);
        switch (rotation)
        {
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
        Log.e("degrees........", "" + degrees);
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
        {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        }
        else
        { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        try
        {
            Log.e("result.....", "" + result);
            camera.setDisplayOrientation(result);
        }
        catch (NullPointerException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback()
    {
        public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3)
        {
            Log.e("surfaceChanged", "surfaceChanged");
            refresh_recoder();
        }

        @Override
        public void surfaceCreated(SurfaceHolder arg0)
        {
            Log.e("surfaceCreated", "surfaceCreated");
            previewRunning = true;
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder arg0)
        {

            Log.e("surfaceDestroyed", "surfaceDestroyed");
            Log.e("Hello", "" + recording);
            if (recorder != null)
            {
                Log.e("Hello", "" + recording);
                if (recording == true)
                {
                    recorder.stop();
                    recording = false;
                }
                recorder.release();
            }
            previewRunning = false;

        }

    };

    public void refresh_recoder()
    {
        if (recording == false)
        {
            if (previewRunning)
            {
                camera.stopPreview();
            }
            try
            {
                setCameraDisplayOrientation(getActivity(), front_or_back_camera, camera);
                camera.setPreviewDisplay(holder);
                camera.startPreview();
                previewRunning = true;
            }
            catch (IOException e)
            {
                Log.e("LOGTAG", e.getMessage());
                e.printStackTrace();
            }
            prepareRecorder();
        }
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {

            case R.id.button_switch_camera:

                if (recording == false)
                {
                    switchCamera();
                }
                break;

            /*case R.id.progressBar:
                start_work();
                break;*/

            case R.id.button_capture:
                start_work();
                break;

            case R.id.flash:
//                flash_on_off();
            default:
                break;
        }
    }

    private void flash_on_off()
    {
        Log.e("mCurrentCamera", "" + front_or_back_camera);
        if (isLighOn && front_or_back_camera == 0)
        {
            Log.i("info", "torch is turn off!");

            if (recording == false)
            {
                camera.lock();
            }
            p.setFlashMode(p.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH) ? Camera.Parameters.FLASH_MODE_OFF : Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(p);
            flash.setBackgroundResource(R.mipmap.noflash);
            if (recording == false)
            {
                camera.unlock();
            }
            isLighOn = false;
        }
        else if (front_or_back_camera == 0)
        {
            Log.i("info", "torch is turn on!");
            if (recording == false)
            {
                camera.lock();
            }
            p.setFlashMode(p.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH) ? Camera.Parameters.FLASH_MODE_OFF : Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(p);
            flash.setBackgroundResource(R.mipmap.flash);
            if (recording == false)
            {
                camera.unlock();
            }
            isLighOn = true;
        }

    }

    //front_or_back_camera  == 1 Front camera
    //front_or_back_camera  == 0 Back camera

    private void switchCamera()
    {
        if (Camera.getNumberOfCameras() >= 2)
        {

            if (front_or_back_camera == Camera.CameraInfo.CAMERA_FACING_BACK)
            {
                flash_on_off();
                front_or_back_camera = Camera.CameraInfo.CAMERA_FACING_FRONT;
            }
            else
            {
                front_or_back_camera = Camera.CameraInfo.CAMERA_FACING_BACK;
            }
            stop_camera();

            if (camera == null)
            {
                camera = Camera.open(front_or_back_camera);
            }

            refresh_camera();

            refresh_recoder();

        }
    }

    private void start_work()
    {
        if (recording == true)
        {
            counter.cancel();

            ok_tata();

            //            Log.v("LOGTAG", "Recording Stopped");
        }
        else
        {

        }
    }

    public void ok_tata()
    {
        try
        {
            //*******************************************Stop Timer*******************************************
            mHandler.removeCallbacks(startTimer);
            stopped = true;

            //************************************************************************************************

            recorder.stop();
            stop_camera();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        recording = false;

        Intent i = new Intent(getActivity(), GIV.class);
        i.putExtra("image", path);
        i.putExtra("type", "V");

        recorder = null;

        startActivity(i);
        getActivity().finish();
    }

    public void finish_on_spot()   // accessed on back pressed in Camera Activity
    {
        if (recording == true)
        {
            counter.cancel();

            //*******************************************Stop Timer*****************************************************
            mHandler.removeCallbacks(startTimer);
            stopped = true;
            //************************************************************************************************

            try
            {
                recorder.stop();
                recorder.release();
                recorder = null;
                camera.reconnect();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            recording = false;

        }

        getActivity().finish();
    }

    private int outputOrientation      = -1;
    private int lastPictureOrientation = -1;

    private class OnOrientationChange extends OrientationEventListener
    {
        public OnOrientationChange(Context context)
        {
            super(context);
            disable();
        }

        @Override
        public void onOrientationChanged(int orientation)
        {
            if (camera != null && orientation != ORIENTATION_UNKNOWN)
            {
                int newOutputOrientation = getCameraPictureRotation(orientation);
                if (newOutputOrientation != outputOrientation)
                {
                    outputOrientation = newOutputOrientation;
                    Log.e("outputOrientation...", "" + outputOrientation);
                    try
                    {
                        lastPictureOrientation = outputOrientation;
                        Log.e("lastPictureOrientation", "" + lastPictureOrientation);

                    }
                    catch (Exception e)
                    {
                        Log.e(getClass().getSimpleName(), "Exception updating camera parameters in orientation change", e);
                        // TODO: get this info out to hosting app
                    }
                }
            }
        }
    }

    private int getCameraPictureRotation(int orientation)
    {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(front_or_back_camera, info);
        int rotation = 0;
        orientation = (orientation + 45) / 90 * 90;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
        {
            rotation = (info.orientation - orientation + 360) % 360;
        }
        else
        { // back-facing camera
            rotation = (info.orientation + orientation) % 360;
        }
        return (rotation);
    }

    private void prepareRecorder()
    {
        Log.e("prepareRecorder:result", "" + result);
        recorder = new MediaRecorder();

        if (front_or_back_camera == Camera.CameraInfo.CAMERA_FACING_BACK)
        {
            try
            {
                recorder.setOrientationHint(outputOrientation);
            }
            catch (Exception e)
            {
                recorder.setOrientationHint(result);
                e.printStackTrace();
            }
        }
        else
        {
            recorder.setOrientationHint(270);
        }

        recorder.setPreviewDisplay(holder.getSurface());

        camera.unlock();
        recorder.setCamera(camera);

        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        recorder.setProfile(camcorderProfile);

        Calendar c      = Calendar.getInstance();
        int      day    = c.get(Calendar.DAY_OF_MONTH);
        int      month  = c.get(Calendar.MONTH) + 1;
        int      year   = c.get(Calendar.YEAR);
        int      hour   = c.get(Calendar.HOUR_OF_DAY);
        int      minute = c.get(Calendar.MINUTE);
        int      second = c.get(Calendar.SECOND);
        String   n      = day + "" + month + "" + year + "_" + hour + "" + minute + "" + second;

        File filepath = Environment.getExternalStorageDirectory();

        if (!HelperS.checkFolder(filepath, "Muser"))
        {
            File dir = new File(filepath.getAbsolutePath() + "/Muser/");
            dir.mkdirs();
        }
        File dir = new File("/sdcard/Muser/");

        try
        {
            File newFile = File.createTempFile(n.replaceAll("-", ""), ".mp4", dir);
            path = newFile.getAbsolutePath();
            recorder.setOutputFile(newFile.getAbsolutePath());
        }
        catch (IOException e)
        {
            Log.v("LOGTAG", "Couldn't create file");
            e.printStackTrace();
            //finish();
        }

        try
        {
            recorder.prepare();
        }
        catch (IllegalStateException e)
        {
            e.printStackTrace();
            //finish();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            //finish();
        }
    }

    private void updateTimer(float time)
    {
        secs = (long) (time / 1000);
        mins = (long) ((time / 1000) / 60);
        //hrs = (long) (((time / 1000) / 60) / 60);
        /* Convert the seconds to String * and format to ensure it has * a leading zero when required */
        secs = secs % 60;
        seconds = String.valueOf(secs);
        if (secs == 0)
        {
            seconds = "00";
        }
        if (secs < 10 && secs > 0)
        {
            seconds = "0" + seconds;
        }
        /* Convert the minutes to String and format the String */
        mins = mins % 60;
        minutes = String.valueOf(mins);
        if (mins == 0)
        {
            minutes = "00";
        }
        if (mins < 10 && mins > 0)
        {
            minutes = "0" + minutes;
        }

        timer_text.setText(/*hours + ":" +*/ minutes + ":" + seconds);
        Log.e("zzzzzzzzz",/*hours + ":" +*/ minutes + ":" + seconds);
        //		Log.e("wwwww.","" + milliseconds);
    }

    private Runnable startTimer = new Runnable()
    {
        public void run()
        {
            elapsedTime = System.currentTimeMillis() - startTime;
            updateTimer(elapsedTime);
            mHandler.postDelayed(this, REFRESH_RATE);
        }
    };

    private Handler mHandler = new Handler();
    private long startTime;
    private long elapsedTime;
    private final int REFRESH_RATE = 100;
    private String /*hours,*/minutes, seconds/*,milliseconds*/;
    private long secs, mins/*,hrs,msecs*/;
    private boolean stopped = false;

}
