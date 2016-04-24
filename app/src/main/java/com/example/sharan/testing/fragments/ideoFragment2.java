package com.example.sharan.testing.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.grafika.gles.FullFrameRect;
import com.android.grafika.gles.Texture2dProgram;
import com.chris.video.PlayMovieSurfaceActivity;
import com.chris.video.SquareCameraPreview;
import com.chris.video.TextureMovieEncoder;
import com.example.sharan.testing.ProgressView;
import com.example.sharan.testing.R;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ideoFragment2 extends Fragment implements SurfaceTexture.OnFrameAvailableListener, /*AdapterView.OnItemSelectedListener,*/ View.OnClickListener
{

    private static final String  TAG     = ideoFragment2.class.getSimpleName();
    private static final boolean VERBOSE = false;

    // Camera filters; must match up with cameraFilterNames in strings.xml
    static final int FILTER_NONE            = 0;
    static final int FILTER_BLACK_WHITE     = 1;
    static final int FILTER_BLUR            = 2;
    static final int FILTER_SHARPEN         = 3;
    static final int FILTER_EDGE_DETECT     = 4;
    static final int FILTER_EMBOSS          = 5;
    static final int PREVIEW_SIZE_MAX_WIDTH = 640;

    private SquareCameraPreview   mGLView;
    private CameraSurfaceRenderer mRenderer;
    private Camera                mCamera;
    private CameraHandler         mCameraHandler;
    private boolean               mRecordingEnabled;      // controls button state
    private boolean               mPauseEnabled;
    private View                  mCaptureButton;

    Context con;
    //    TextView text;
    Button  toggleRelease;

    private int mCoverHeight;
    private int mPreviewHeight;

    private int mCameraPreviewWidth, mCameraPreviewHeight;

    // this is static so it survives activity restarts
    private static TextureMovieEncoder sVideoEncoder = new TextureMovieEncoder();

    //**************************************************************************
    ProgressView progressView;
    int totalRecordingTime = 14000;
    int remainingTime      = 14000;



    CountDownTimer counter2;

    public ideoFragment2()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

    }

    long startRecording=0;
    long startPause=0;
    long total3=0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_ideo_fragment2, container, false);

        con = getActivity();

        File outputFile = new File(Environment.getExternalStorageDirectory(), "test.mp4");

        mCameraHandler = new CameraHandler(ideoFragment2.this);

        mPauseEnabled = false;

        mRecordingEnabled = sVideoEncoder.isRecording();
        mCaptureButton = v.findViewById(R.id.capture_image_button);
        mCaptureButton.setOnClickListener(this);
        // Configure the GLSurfaceView.  This will start the Renderer thread, with an
        // appropriate EGL context.
        mGLView = (SquareCameraPreview) v.findViewById(R.id.camera_preview_view);
        mGLView.setEGLContextClientVersion(2);     // select GLES 2.0
        mRenderer = new CameraSurfaceRenderer(mCameraHandler, sVideoEncoder, outputFile);
        mGLView.setRenderer(mRenderer);
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        progressView = (ProgressView) v.findViewById(R.id.recorder_progress);
        progressView.setTotalTime(totalRecordingTime);

        counter2 = new CountDownTimer(remainingTime, 100)
        {

            @Override
            public void onTick(long millisUntilFinished)
            {
                remainingTime = remainingTime - 100;

                if (remainingTime <= 0)
                {
                    onFinish();
                }

                Log.e("remainingTime", "" + remainingTime);
            }

            @Override
            public void onFinish()
            {

                mRecordingEnabled = false;
                startCameraRecording();
                Log.e("remainingTime", "Finishhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");

            }
        };

        mCaptureButton.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    startRecording=System.currentTimeMillis();


                    if (remainingTime > 0)
                    {
                        progressView.setCurrentState(ProgressView.State.START);
                        counter2.start();

                        mRecordingEnabled = true;

                        startCameraRecording();
                    }

                }
                else if (event.getAction() == MotionEvent.ACTION_UP)
                {

                    startPause=System.currentTimeMillis();

                    pauseCameraWorking();

                    progressView.setCurrentState(ProgressView.State.PAUSE);

                    long total = totalRecordingTime - remainingTime;

//                    Log.e("startRecording1",""+startRecording);
//                    Log.e("startRecording2",""+startPause);


                    long total2=startPause-startRecording;

                    total3 +=total2;

//                    Log.e("startRecording3",""+total3);
                    progressView.putProgressList((int) total3);

                    counter2.cancel();

                }


//                Log.e("hello","Helo");

                return false;
            }
        });



        toggleRelease = (Button) v.findViewById(R.id.togPause_button);

        return v;

    }

    @Override
    public void onResume()
    {
        Log.d(TAG, "onResume -- acquiring camera");
        super.onResume();
        openCamera();//1088, 1088);      // updates mCameraPreviewWidth/Height

        // Set the preview aspect ratio.
        //        AspectFrameLayout layout = (AspectFrameLayout) findViewById(R.id.cameraPreview_afl);
        //        layout.setAspectRatio(//(double) mCameraPreviewHeight / mCameraPreviewWidth);
        //        		(double) mCameraPreviewWidth / mCameraPreviewHeight);

        mGLView.onResume();
        mGLView.queueEvent(new Runnable()
        {
            @Override
            public void run()
            {
                mRenderer.setCameraPreviewSize(mCameraPreviewWidth, mCameraPreviewHeight);
            }
        });
        Log.d(TAG, "onResume complete: " + this);
    }

    @Override
    public void onPause()
    {
        Log.d(TAG, "onPause -- releasing camera");
        super.onPause();
        releaseCamera();
        mGLView.queueEvent(new Runnable()
        {
            @Override
            public void run()
            {
                // Tell the renderer that it's about to be paused so it can clean up.
                //TODO Stop MediaCodec? users may click home button and never come back again
                mRenderer.notifyPausing();
            }
        });
        mGLView.onPause();
        Log.d(TAG, "onPause complete");
    }

    @Override
    public void onDestroy()
    {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        mCameraHandler.invalidateHandler();     // paranoia
    }

    private Camera.Size determineBestPreviewSize(Camera.Parameters parameters)
    {
        return determineBestSize(parameters.getSupportedPreviewSizes(), PREVIEW_SIZE_MAX_WIDTH);
    }

    //    private Size determineBestPictureSize(Camera.Parameters parameters) {
    //        return determineBestSize(parameters.getSupportedPictureSizes(), PICTURE_SIZE_MAX_WIDTH);
    //    }

    private Camera.Size determineBestSize(List<Camera.Size> sizes, int widthThreshold)
    {
        Camera.Size bestSize   = null;
        Camera.Size size;
        int         numOfSizes = sizes.size();
        for (int i = 0; i < numOfSizes; i++)
        {
            size = sizes.get(i);
            boolean isDesireRatio = (size.width / 4) == (size.height / 3);
            boolean isBetterSize  = (bestSize == null) || size.width > bestSize.width;

            if (isDesireRatio && isBetterSize)
            {
                bestSize = size;
            }
        }

        if (bestSize == null)
        {
            Log.d(TAG, "cannot find the best camera size");
            return sizes.get(sizes.size() - 1);
        }

        return bestSize;
    }

    /**
     Opens a camera, and attempts to establish preview mode at the specified width and height.
     <p>
     Sets mCameraPreviewWidth and mCameraPreviewHeight to the actual width/height of the preview.
     */
    private void openCamera()
    {//int desiredWidth, int desiredHeight) {
        if (mCamera != null)
        {
            throw new RuntimeException("camera already initialized");
        }

        Camera.CameraInfo info = new Camera.CameraInfo();

        // Try to find a front-facing camera (e.g. for videoconferencing).
        int numCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numCameras; i++)
        {
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK)
            {
                mCamera = Camera.open(i);
                break;
            }
        }
        if (mCamera == null)
        {
            Log.d(TAG, "No front-facing camera found; opening default");
            mCamera = Camera.open();    // opens startRecording back-facing camera
        }
        mGLView.setCamera(mCamera);
        if (mCamera == null)
        {
            throw new RuntimeException("Unable to open camera");
        }

        Camera.Parameters parms = mCamera.getParameters();

        Camera.Size bestPreviewSize = determineBestPreviewSize(parms);

        parms.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);

        //        CameraUtils.choosePreviewSize(parms, desiredWidth, desiredHeight);

        // Give the camera a hint that we're recording video.  This can have a big
        // impact on frame rate.
        parms.setRecordingHint(true);
        //        parms.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);

        // leave the frame rate set to default
        mCamera.setParameters(parms);
        //        setCameraDisplayOrientation(Camera.CameraInfo.CAMERA_FACING_BACK, mCamera);
        determineDisplayOrientation();

        int[]       fpsRange           = new int[2];
        Camera.Size mCameraPreviewSize = parms.getPreviewSize();
        parms.getPreviewFpsRange(fpsRange);
        String previewFacts = mCameraPreviewSize.width + "x" + mCameraPreviewSize.height;
        if (fpsRange[0] == fpsRange[1])
        {
            previewFacts += " @" + (fpsRange[0] / 1000.0) + "fps";
        }
        else
        {
            previewFacts += " @[" + (fpsRange[0] / 1000.0) +
                      " - " + (fpsRange[1] / 1000.0) + "] fps";
        }

        //        text.setText(previewFacts);

        mCameraPreviewWidth = mCameraPreviewSize.width;
        mCameraPreviewHeight = mCameraPreviewSize.height;
    }

    //    public void setCameraDisplayOrientation(int facing, final Camera camera) {
    //        int result = getCameraDisplayOrientation(facing);
    //        camera.setDisplayOrientation(result);
    //    }

    private int getBackCameraID()
    {
        return Camera.CameraInfo.CAMERA_FACING_BACK;
    }

    /**
     Determine the current display orientation and rotate the camera preview accordingly
     */
    private void determineDisplayOrientation()
    {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(getBackCameraID(), cameraInfo);

        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        int degrees  = 0;

        switch (rotation)
        {
            case Surface.ROTATION_0:
            {
                degrees = 0;
                break;
            }
            case Surface.ROTATION_90:
            {
                degrees = 90;
                break;
            }
            case Surface.ROTATION_180:
            {
                degrees = 180;
                break;
            }
            case Surface.ROTATION_270:
            {
                degrees = 270;
                break;
            }
        }

        final int displayOrientation;

        // Camera direction
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
        {
            // Orientation is angle of rotation when facing the camera for
            // the camera image to match the natural orientation of the device
            int tmpDisplayOrientation = (cameraInfo.orientation + degrees) % 360;
            displayOrientation = (360 - tmpDisplayOrientation) % 360;
        }
        else
        {
            displayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
        }

        mCamera.setDisplayOrientation(displayOrientation);
        mGLView.queueEvent(new Runnable()
        {
            @Override
            public void run()
            {
                // notify the renderer that we want to change the encoder's state
                mRenderer.setDisplayOrientation(displayOrientation);
            }
        });
    }

    //    public int getCameraDisplayOrientation(int facing) {
    //        int rotation = getWindowManager().getDefaultDisplay()
    //                .getRotation();
    //        int degrees = 0;
    //        switch (rotation) {
    //            case Surface.ROTATION_0:
    //                degrees = 0;
    //                break;
    //            case Surface.ROTATION_90:
    //                degrees = 90;
    //                break;
    //            case Surface.ROTATION_180:
    //                degrees = 180;
    //                break;
    //            case Surface.ROTATION_270:
    //                degrees = 270;
    //                break;
    //        }
    //
    //        int result;
    //        if (facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
    //            result = (0 + degrees) % 360;
    //        } else { // back-facing
    //            result = (90 - degrees + 360) % 360;
    //        }
    //        return result;
    //    }

    /**
     Stops camera preview, and releases the camera to the system.
     */
    private void releaseCamera()
    {
        if (mCamera != null)
        {
            mCamera.stopPreview();
            mGLView.setCamera(null);
            //            mCamera.setPreviewTexture(null);
            mCamera.release();
            mCamera = null;
            Log.d(TAG, "releaseCamera -- done");
        }
    }

    /**
     onClick handler for "record" button.
     */
    public void clickToggleRecording(@SuppressWarnings("unused") View unused)
    {
        mRecordingEnabled = !mRecordingEnabled;
        //        if (!mRecordingEnabled) {
        //        	Intent intent = new Intent();
        //        	intent.putExtra("video", Environment.getExternalStorageDirectory() + "/test.mp4");
        //        	intent.setClass(this, PlayMovieSurfaceActivity.class);
        //        	this.startActivity(intent);
        //        }
        mGLView.queueEvent(new Runnable()
        {
            @Override
            public void run()
            {
                // notify the renderer that we want to change the encoder's state
                mRenderer.changeRecordingState(mRecordingEnabled);
            }
        });
    }

    /**
     onClick handler for "pause" button.
     */
    public void clickTogglePause(@SuppressWarnings("unused") View unused)
    {
        mPauseEnabled = !mPauseEnabled;
        mGLView.queueEvent(new Runnable()
        {
            @Override
            public void run()
            {
                // notify the renderer that we want to change the encoder's state
                mRenderer.changePauseState(mPauseEnabled);
            }
        });
        updateControls2();
    }

    public void startCameraRecording()
    {

        mGLView.queueEvent(new Runnable()
        {
            @Override
            public void run()
            {
                // notify the renderer that we want to change the encoder's state
                mRenderer.changeRecordingState(mRecordingEnabled);
            }
        });
    }

    public void pauseCameraWorking()
    {
        mPauseEnabled = !mPauseEnabled;
        mGLView.queueEvent(new Runnable()
        {
            @Override
            public void run()
            {
                // notify the renderer that we want to change the encoder's state
                mRenderer.changePauseState(mPauseEnabled);
            }
        });
        updateControls2();
    }

    /**
     Updates the on-screen controls to reflect the current state of the app.
     */
    private void updateControls2()
    {

//        int id = mPauseEnabled ? R.string.toggleRecordingResume : R.string.toggleRecordingPause;
//        toggleRelease.setText(id);

        //CheckBox cb = (CheckBox) findViewById(R.id.rebindHack_checkbox);
        //cb.setChecked(TextureRender.sWorkAroundContextProblem);
    }

    /**
     Connects the SurfaceTexture to the Camera preview output, and starts the preview.
     */
    private void handleSetSurfaceTexture(SurfaceTexture st)
    {
        st.setOnFrameAvailableListener(this);
        try
        {
            mCamera.setPreviewTexture(st);
        }
        catch (IOException ioe)
        {
            throw new RuntimeException(ioe);
        }
        mCamera.startPreview();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture st)
    {
        // The SurfaceTexture uses this to signal the availability of a new frame.  The
        // thread that "owns" the external texture associated with the SurfaceTexture (which,
        // by virtue of the context being shared, *should* be either one) needs to call
        // updateTexImage() to latch the buffer.
        //
        // Once the buffer is latched, the GLSurfaceView thread can signal the encoder thread.
        // This feels backward -- we want recording to be prioritized over rendering -- but
        // since recording is only enabled some of the time it's easier to do it this way.
        //
        // Since GLSurfaceView doesn't establish a Looper, this will *probably* execute on
        // the main UI thread.  Fortunately, requestRender() can be called from any thread,
        // so it doesn't really matter.
        if (VERBOSE)
            Log.d(TAG, "ST onFrameAvailable");
        mGLView.requestRender();
    }

    /**
     Handles camera operation requests from other threads.  Necessary because the Camera must only be accessed from one thread.
     <p>
     The object is created on the UI thread, and all handlers run there.  Messages are sent from other threads, using sendMessage().
     */
    static class CameraHandler extends Handler
    {
        public static final int MSG_SET_SURFACE_TEXTURE = 0;
        public static final int MSG_STOP_RECORDING      = 1;

        // Weak reference to the Activity; only access this from the UI thread.
        private WeakReference<ideoFragment2> mWeakActivity;

        public CameraHandler(ideoFragment2 activity)
        {
            mWeakActivity = new WeakReference<ideoFragment2>(activity);
        }

        /**
         Drop the reference to the activity.  Useful as a paranoid measure to ensure that attempts to access a stale Activity through a handler are caught.
         */
        public void invalidateHandler()
        {
            mWeakActivity.clear();
        }

        @Override  // runs on UI thread
        public void handleMessage(Message inputMessage)
        {
            int what = inputMessage.what;
            Log.d(TAG, "CameraHandler [" + this + "]: what=" + what);

            ideoFragment2 activity = mWeakActivity.get();
            if (activity == null)
            {
                Log.w(TAG, "CameraHandler.handleMessage: activity is null");
                return;
            }

            switch (what)
            {
                case MSG_SET_SURFACE_TEXTURE:
                    activity.handleSetSurfaceTexture((SurfaceTexture) inputMessage.obj);
                    break;
                case MSG_STOP_RECORDING:
                    Intent intent = new Intent();
                    intent.putExtra("video", Environment.getExternalStorageDirectory() + "/test.mp4");
                    intent.setClass(activity.getContext(), PlayMovieSurfaceActivity.class);
                    activity.startActivity(intent);
                    break;
                default:
                    throw new RuntimeException("unknown msg " + what);
            }
        }
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.capture_image_button:
               /* mRecordingEnabled = !mRecordingEnabled;
                mGLView.queueEvent(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // notify the renderer that we want to change the encoder's state
                        mRenderer.changeRecordingState(mRecordingEnabled);
                    }
                });*/
                break;
            default:
                break;
        }

    }

}

class CameraSurfaceRenderer implements GLSurfaceView.Renderer
{
    private static final String  TAG     = CameraSurfaceRenderer.class.getSimpleName();
    private static final boolean VERBOSE = false;

    private static final int RECORDING_OFF     = 0;
    private static final int RECORDING_ON      = 1;
    private static final int RECORDING_RESUMED = 2;
    private static final int RECORDING_PAUSE   = 3;

    private ideoFragment2.CameraHandler mCameraHandler;
    private TextureMovieEncoder         mVideoEncoder;
    private File                        mOutputFile;

    private FullFrameRect mFullScreen;

    private final float[] mSTMatrix = new float[16];
    private int mTextureId;

    private          SurfaceTexture mSurfaceTexture;
    private          long           mLastTimeStamp;
    private          boolean        mRecordingEnabled;
    private volatile boolean        mPauseEnabled;
    private          int            mRecordingStatus;
    private          int            mFrameCount;

    // width/height of the incoming camera preview frames
    private boolean mIncomingSizeUpdated;
    private int     mIncomingWidth;
    private int     mIncomingHeight;

    private int mCurrentFilter;
    private int mNewFilter;
    private int mDisplayOrientation;

    /**
     Constructs CameraSurfaceRenderer.
     @param cameraHandler Handler for communicating with UI thread
     @param movieEncoder  video encoder object
     @param outputFile    output file for encoded video; forwarded to movieEncoder
     */
    public CameraSurfaceRenderer(ideoFragment2.CameraHandler cameraHandler, TextureMovieEncoder movieEncoder, File outputFile)
    {
        mCameraHandler = cameraHandler;
        mVideoEncoder = movieEncoder;
        mOutputFile = outputFile;

        mTextureId = -1;

        mRecordingStatus = -1;
        mRecordingEnabled = false;
        mPauseEnabled = false;
        mFrameCount = -1;
        mLastTimeStamp = 0;

        mIncomingSizeUpdated = false;
        mIncomingWidth = mIncomingHeight = -1;

        // We could preserve the old filter mode, but currently not bothering.
        mCurrentFilter = -1;
        mNewFilter = ideoFragment2.FILTER_NONE;
    }

    /**
     Notifies the renderer thread that the activity is pausing.
     <p>
     For best results, call this *after* disabling Camera preview.
     */
    public void notifyPausing()
    {
        if (mSurfaceTexture != null)
        {
            Log.d(TAG, "renderer pausing -- releasing SurfaceTexture");
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        if (mFullScreen != null)
        {
            mFullScreen.release(false);     // assume the GLSurfaceView EGL context is about
            mFullScreen = null;             //  to be destroyed
        }
        mIncomingWidth = mIncomingHeight = -1;
    }

    /**
     Notifies the renderer that we want to stop or start recording.
     */
    public void changeRecordingState(boolean isRecording)
    {
        Log.d(TAG, "changeRecordingState: was " + mRecordingEnabled + " now " + isRecording);
        mRecordingEnabled = isRecording;
    }

    public void changePauseState(boolean isPause)
    {
        mPauseEnabled = isPause;
    }

    public void setDisplayOrientation(int orientation)
    {
        mDisplayOrientation = orientation;
    }

    /**
     Changes the filter that we're applying to the camera preview.
     */
  /*  public void changeFilterMode(int filter)
    {
        mNewFilter = filter;
    }*/

    /**
     Updates the filter program.
     */
    public void updateFilter()
    {
        Texture2dProgram.ProgramType programType;
        float[]                      kernel   = null;
        float                        colorAdj = 0.0f;

        Log.d(TAG, "Updating filter to " + mNewFilter);
        switch (mNewFilter)
        {
            case ideoFragment2.FILTER_NONE:
                programType = Texture2dProgram.ProgramType.TEXTURE_EXT;
                break;
            case ideoFragment2.FILTER_BLACK_WHITE:
                // (In a previous version the TEXTURE_EXT_BW variant was enabled by a flag called
                // ROSE_COLORED_GLASSES, because the shader set the red channel to the B&W color
                // and green/blue to zero.)
                programType = Texture2dProgram.ProgramType.TEXTURE_EXT_BW;
                break;
            case ideoFragment2.FILTER_BLUR:
                programType = Texture2dProgram.ProgramType.TEXTURE_EXT_FILT;
                kernel = new float[]{1f / 16f, 2f / 16f, 1f / 16f, 2f / 16f, 4f / 16f, 2f / 16f, 1f / 16f, 2f / 16f, 1f / 16f};
                break;
            case ideoFragment2.FILTER_SHARPEN:
                programType = Texture2dProgram.ProgramType.TEXTURE_EXT_FILT;
                kernel = new float[]{0f, -1f, 0f, -1f, 5f, -1f, 0f, -1f, 0f};
                break;
            case ideoFragment2.FILTER_EDGE_DETECT:
                programType = Texture2dProgram.ProgramType.TEXTURE_EXT_FILT;
                kernel = new float[]{-1f, -1f, -1f, -1f, 8f, -1f, -1f, -1f, -1f};
                break;
            case ideoFragment2.FILTER_EMBOSS:
                programType = Texture2dProgram.ProgramType.TEXTURE_EXT_FILT;
                kernel = new float[]{2f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, -1f};
                colorAdj = 0.5f;
                break;
            default:
                throw new RuntimeException("Unknown filter mode " + mNewFilter);
        }

        // Do we need a whole new program?  (We want to avoid doing this if we don't have
        // too -- compiling a program could be expensive.)
        if (programType != mFullScreen.getProgram().getProgramType())
        {
            mFullScreen.changeProgram(new Texture2dProgram(programType));
            // If we created a new program, we need to initialize the texture width/height.
            mIncomingSizeUpdated = true;
        }

        // Update the filter kernel (if any).
        if (kernel != null)
        {
            mFullScreen.getProgram().setKernel(kernel, colorAdj);
        }

        mCurrentFilter = mNewFilter;
    }

    /**
     Records the size of the incoming camera preview frames.
     <p>
     It's not clear whether this is guaranteed to execute before or after onSurfaceCreated(), so we assume it could go either way.  (Fortunately they both run on the same thread, so we at least know
     that they won't execute concurrently.)
     */
    public void setCameraPreviewSize(int width, int height)
    {
        Log.d(TAG, "setCameraPreviewSize");
        mIncomingWidth = width;
        mIncomingHeight = height;
        mIncomingSizeUpdated = true;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config)
    {
        Log.d(TAG, "onSurfaceCreated");

        // We're starting up or coming back.  Either way we've got a new EGLContext that will
        // need to be shared with the video encoder, so figure out if a recording is already
        // in progress.
        mRecordingEnabled = mVideoEncoder.isRecording();
        if (mRecordingEnabled)
        {
            mRecordingStatus = RECORDING_RESUMED;
        }
        else
        {
            mRecordingStatus = RECORDING_OFF;
        }

        // Set up the texture blitter that will be used for on-screen display.  This
        // is *not* applied to the recording, because that uses a separate shader.
        mFullScreen = new FullFrameRect(new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT));

        mTextureId = mFullScreen.createTextureObject();

        // Create a SurfaceTexture, with an external texture, in this EGL context.  We don't
        // have a Looper in this thread -- GLSurfaceView doesn't create one -- so the frame
        // available messages will arrive on the main thread.
        mSurfaceTexture = new SurfaceTexture(mTextureId);

        // Tell the UI thread to enable the camera preview.
        mCameraHandler.sendMessage(mCameraHandler.obtainMessage(ideoFragment2.CameraHandler.MSG_SET_SURFACE_TEXTURE, mSurfaceTexture));
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height)
    {
        Log.d(TAG, "onSurfaceChanged " + width + "x" + height);
    }

    @Override
    public void onDrawFrame(GL10 unused)
    {
        if (VERBOSE)
            Log.d(TAG, "onDrawFrame tex=" + mTextureId);
        boolean showBox = false;

        // Latch the latest frame.  If there isn't anything new, we'll just re-use whatever
        // was there before.
        mSurfaceTexture.updateTexImage();

        // If the recording state is changing, take care of it here.  Ideally we wouldn't
        // be doing all this in onDrawFrame(), but the EGLContext sharing with GLSurfaceView
        // makes it hard to do elsewhere.

        Log.e("mRecordingEnabled", "" + mRecordingEnabled);
        Log.e("mRecordingStatus", "" + mRecordingStatus);
        if (mRecordingEnabled)
        {
            switch (mRecordingStatus)
            {
                case RECORDING_OFF:
                    Log.d(TAG, "START recording");
                    // start recording
                    if (mDisplayOrientation == 90 || mDisplayOrientation == 270)
                    {
                        int temp = mIncomingWidth;
                        mIncomingWidth = mIncomingHeight;
                        mIncomingHeight = temp;
                    }
                    mVideoEncoder.startRecording(new TextureMovieEncoder.EncoderConfig(mOutputFile, mIncomingWidth, mIncomingHeight, 1000000, EGL14.eglGetCurrentContext()));
                    mRecordingStatus = RECORDING_ON;
                    break;
                case RECORDING_RESUMED:
                    Log.d(TAG, "RESUME recording");
                    mVideoEncoder.updateSharedContext(EGL14.eglGetCurrentContext());
                    mRecordingStatus = RECORDING_ON;
                    break;
                case RECORDING_ON:
                    // yay
                    break;
                default:
                    throw new RuntimeException("unknown status " + mRecordingStatus);
            }
        }
        else
        {
            switch (mRecordingStatus)
            {
                case RECORDING_ON:
                case RECORDING_RESUMED:
                    // stop recording
                    Log.d(TAG, "STOP recording");
                    mVideoEncoder.stopRecording();
                    mRecordingStatus = RECORDING_OFF;
                    mCameraHandler.sendMessage(mCameraHandler.obtainMessage(ideoFragment2.CameraHandler.MSG_STOP_RECORDING));
                    break;
                case RECORDING_OFF:
                    // yay
                    break;
                default:
                    throw new RuntimeException("unknown status " + mRecordingStatus);
            }
        }

        // Set the video encoder's texture name.  We only need to do this once, but in the
        // current implementation it has to happen after the video encoder is started, so
        // we just do it here.
        //
        // TODO: be less lame.
        mVideoEncoder.setTextureId(mTextureId);

        // Tell the video encoder thread that a new frame is available.
        // This will be ignored if we're not actually recording.
        if (!mPauseEnabled)
        {
            mVideoEncoder.frameAvailable(mSurfaceTexture);
        }
        else
        {
            mVideoEncoder.pause();
        }

        if (mIncomingWidth <= 0 || mIncomingHeight <= 0)
        {
            // Texture size isn't set yet.  This is only used for the filters, but to be
            // safe we can just skip drawing while we wait for the various races to resolve.
            // (This seems to happen if you toggle the screen off/on with power button.)
            Log.i(TAG, "Drawing before incoming texture size set; skipping");
            return;
        }
        // Update the filter, if necessary.
        if (mCurrentFilter != mNewFilter)
        {
            updateFilter();
        }
        if (mIncomingSizeUpdated)
        {
            mFullScreen.getProgram().setTexSize(mIncomingWidth, mIncomingHeight);
            mIncomingSizeUpdated = false;
        }

        // Draw the video frame.
        mSurfaceTexture.getTransformMatrix(mSTMatrix);
        mFullScreen.drawFrame(mTextureId, mSTMatrix);

        // Draw a flashing box if we're recording.  This only appears on screen.
        showBox = (mRecordingStatus == RECORDING_ON);
        if (showBox && (++mFrameCount & 0x04) == 0)
        {
            //            drawBox();
        }
    }

    /**
     Draws a red box in the corner.
     */
    private void drawBox()
    {
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        GLES20.glScissor(0, 0, 100, 100);
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
    }
}




