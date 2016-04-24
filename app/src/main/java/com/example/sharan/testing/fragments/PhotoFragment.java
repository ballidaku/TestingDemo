package com.example.sharan.testing.fragments;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.sharan.testing.HelperS;
import com.example.sharan.testing.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class PhotoFragment extends Fragment implements View.OnClickListener/*, View.OnTouchListener*/
{
    private        SurfaceView   preview          = null;
    private        SurfaceHolder previewHolder    = null;
    static private Camera        camera           = null;
    private        boolean       inPreview        = false;
    private        boolean       cameraConfigured = false;
    ImageView button_capture;
    String    fileName;
    Context   con;
    //Uri outuri;
    int no_of_capture = 0;
    private int front_or_back_camera;
    ImageView flash;
    Bitmap    b;
    boolean pic_clicked = false;
    boolean pressed     = false;
    int width;
    private boolean isLighOn = false;
    Camera.Parameters   p;
    OnOrientationChange onOrientationChange;
    private int outputOrientation      = -1;
    private int lastPictureOrientation = -1;

    int camwidth, camheight;

    boolean menuVisibleS=false;

    public PhotoFragment()
    {
        super();
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        View rootView = inflater.inflate(R.layout.fragment_photo, container, false);

        con = getActivity();

        preview = (SurfaceView) rootView.findViewById(R.id.preview);

        (button_capture = (ImageView) rootView.findViewById(R.id.button_capture)).setOnClickListener(this);
        ( rootView.findViewById(R.id.button_switch_camera)).setOnClickListener(this);
        (flash = (ImageView) rootView.findViewById(R.id.flash)).setOnClickListener(this);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point   size    = new Point();
        display.getSize(size);
        width = size.x;

        ViewGroup.LayoutParams params = preview.getLayoutParams();
        params.height = width;
        params.width = width;

        preview.setLayoutParams(params);
        //        preview.setOnTouchListener(this);

        onOrientationChange = new OnOrientationChange(con);
        front_or_back_camera = Camera.CameraInfo.CAMERA_FACING_BACK;

        return rootView;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if(menuVisibleS)
        {
            start_work();
        }

    }

    @Override
    public void onPause()
    {
        onOrientationChange.disable();
        stop_work();
        super.onPause();
    }

    @Override
    public void setMenuVisibility(boolean menuVisible)
    {
        super.setMenuVisibility(menuVisible);

        Log.e("menuVisible", "" + menuVisible);

        menuVisibleS=menuVisible;

        if (menuVisible == false)
        {
            stop_work();
        }
        else if (menuVisible == true)
        {
            start_work();
        }

    }

    public void start_work()
    {
        onOrientationChange.enable();
        Log.e("ttttttt", "gggggggg");

        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        if (camera == null)
        {

            camera = Camera.open();
        }

        Camera.Parameters pictureParams = camera.getParameters();
        setCameraPictureOrientation(pictureParams);

        refresh_camera();
    }

    public void stop_work()
    {
        if (camera != null)
        {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.lock();
            camera.release();
            camera = null;

            // preview.getHolder().removeCallback(preview);
        }
        inPreview = false;
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.button_capture:

                pressed = true;
                //camera.takePicture(shutterCallback, rawCallback, jpegCallback);
                camera.takePicture(null, null, jpegCallback);
                pic_clicked = true;

                break;

            case R.id.button_switch_camera:
                switchCamera();
                break;

            case R.id.flash:
                p = camera.getParameters();
                flash_on_off();
                break;

            default:
                break;
        }
    }

    //front_or_back_camera  == 1 Front camera
    //front_or_back_camera  == 0 Back camera

    private void flash_on_off()
    {
        if (isLighOn && front_or_back_camera == 0)
        {
            Log.i("info", "torch is turn off!");
            p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(p);
            flash.setBackgroundResource(R.mipmap.noflash);
            camera.startPreview();
            isLighOn = false;
        }
        else if (front_or_back_camera == 0)
        {
            Log.i("info", "torch is turn on!");
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(p);
            flash.setBackgroundResource(R.mipmap.flash);
            camera.startPreview();
            isLighOn = true;
        }

    }

    private void switchCamera()
    {
        if (front_or_back_camera == Camera.CameraInfo.CAMERA_FACING_BACK)
        {
            flash.setBackgroundResource(R.mipmap.noflash);
            isLighOn=false;
            front_or_back_camera = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        else
        {
            front_or_back_camera = Camera.CameraInfo.CAMERA_FACING_BACK;
        }

        if (camera != null)
        {
            camera.stopPreview(); // stop preview
            camera.release(); // release previous camera
        }

        // Create an instance of Camera
        camera = getCameraInstance(front_or_back_camera);

        //	if(camera == null) return;

        refresh_camera();
    }

    public void refresh_camera()
    {
        cameraConfigured = false;
        setCameraDisplayOrientation(PhotoFragment.this, front_or_back_camera, camera);
        //initPreview(width, width);
        initPreview(camwidth, camheight);
        startPreview();

    }

    private void startPreview()
    {
        if (cameraConfigured && camera != null)
        {
            camera.startPreview();
            inPreview = true;
        }
    }

    private Camera getCameraInstance(int type)
    {
        Camera c               = null;
        int    numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++)
        {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == type)
            {
                try
                {
                    c = Camera.open(i); // attempt to get a Camera instance
                }
                catch (Exception e)
                {
                    // Camera is not available

                    Toast.makeText(con, "Camera not available.", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
        return c;
    }

	/*	@Override
        public void onBackPressed()
		{
			if(pic_clicked == true)
			{
				startActivity(new Intent(con, Camera_Images.class));
				this.finish();
			}
			else
			{
				Custom_camera.this.finish();
			}
		}*/

    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters)
    {
        Camera.Size result = null;
        for (Camera.Size size : parameters.getSupportedPreviewSizes())
        {
            Log.e("size1..." + width + "...." + height, "w :" + size.width + "....h :" + size.height);
            if (size.width >= width && size.height >= height)
            {
                if (result == null)
                {
                    result = size;
                }
                else
                {

                    Log.e("size2", "w :" + size.width + "h :" + size.height);

                    int resultArea = result.width * result.height;
                    int newArea    = size.width * size.height;
                    if (newArea > resultArea)
                    {
                        result = size;

                        Log.e("IIIIIIIIIIIIIIIIII", "NNNNNNNNNNNNNNNNNNNNNNNNNN");
                    }
                }
            }
        }
        return (result);
    }

    private void initPreview(int width, int height)
    {
        if (camera != null && previewHolder.getSurface() != null)
        {
            try
            {
                camera.setPreviewDisplay(previewHolder);
            }
            catch (Throwable t)
            {

                Toast.makeText(con, t.getMessage(), Toast.LENGTH_LONG).show();
            }
            if (!cameraConfigured)
            {
                Camera.Parameters parameters = camera.getParameters();

                Camera.Size size = getBestPreviewSize(0, 0, parameters);

                Log.e("size", "" + size);
                if (size != null)
                {

                    camwidth = size.width;
                    camheight = size.height;
                    Log.e("WWWWWWWWWWWWWWW : " + size.width, "HHHHHHHHHHHHHHHHH : " + size.height);
                    parameters.setPreviewSize(size.width, size.height);
                    camera.setParameters(parameters);
                    cameraConfigured = true;
                }
            }
        }
    }



    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback()
    {
        public void surfaceCreated(SurfaceHolder holder)
        {
            Log.e("surfaceCreated", "surfaceCreated");
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
        {
            Log.e("surfaceChanged", "surfaceChanged");

            //			camwidth = width;
            //			camheight = height;
            Log.e("SurfaceHolder Dimen" + width, "" + height);
            initPreview(width, height);
            startPreview();
        }

        public void surfaceDestroyed(SurfaceHolder holder)
        {
            if (camera != null)
            {
                camera.release();
            }
        }
    };

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
            // Log.e("OnOrientationChange........................",

            if (camera != null && orientation != ORIENTATION_UNKNOWN)
            {
                int newOutputOrientation = getCameraPictureRotation(orientation);
                if (newOutputOrientation != outputOrientation)
                {
                    outputOrientation = newOutputOrientation;

                    Camera.Parameters params = camera.getParameters();
                    params.setRotation(outputOrientation);
                    try
                    {
                        camera.setParameters(params);
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

    private void setCameraPictureOrientation(Camera.Parameters params)
    {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(front_or_back_camera, info);
        if (getActivity().getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
        {
            outputOrientation = getCameraPictureRotation(getActivity().getWindowManager().getDefaultDisplay().getOrientation());
        }
        else if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
        {
            outputOrientation = (360 - lastPictureOrientation) % 360;
        }
        else
        {
            outputOrientation = lastPictureOrientation;
        }
        if (lastPictureOrientation != outputOrientation)
        {
            params.setRotation(outputOrientation);
            lastPictureOrientation = outputOrientation;
        }
    }

    int result;

    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void setCameraDisplayOrientation(Fragment activity, int cameraId, android.hardware.Camera camera)
    {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        int degrees  = 0;

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

            camera.setDisplayOrientation(result);
        }
        catch (NullPointerException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback()
    {
        public void onShutter()
        {
            // Log.d(TAG, "onShutter'd");
        }
    };
    Camera.PictureCallback rawCallback     = new Camera.PictureCallback()
    {
        public void onPictureTaken(byte[] data, Camera camera)
        {
            // Log.d(TAG, "onPictureTaken - raw");
        }
    };
    Camera.PictureCallback jpegCallback    = new Camera.PictureCallback()
    {
        public void onPictureTaken(byte[] data, Camera camera)
        {

            //	new SaveImageTask().execute(data);
            try
            {
                Calendar c          = Calendar.getInstance();
                int      day        = c.get(Calendar.DAY_OF_MONTH);
                int      month      = c.get(Calendar.MONTH) + 1;
                int      year       = c.get(Calendar.YEAR);
                int      hour       = c.get(Calendar.HOUR_OF_DAY);
                int      minute     = c.get(Calendar.MINUTE);
                int      second     = c.get(Calendar.SECOND);
                String   n          = day + "" + month + "" + year + "_" + hour + "" + minute + "" + second;
                String   image_name = n + ".jpg";
                File     filepath   = Environment.getExternalStorageDirectory();

                if (!HelperS.checkFolder(filepath, "Demo"))
                {
                    File dir = new File(filepath.getAbsolutePath() + "/Demo/");
                    dir.mkdirs();
                }

                File dir = new File("/sdcard/Demo/");

                File file = new File(dir, image_name);

                BitmapFactory.Options opt;
                opt = new BitmapFactory.Options();
                opt.inTempStorage = new byte[16 * 1024];
                Camera.Parameters parameters = camera.getParameters();
                Camera.Size       size       = parameters.getPictureSize();
                int               height11   = size.height;
                int               width11    = size.width;

                Log.e("width11.........." + width11, "height11........." + height11);

                float mb = (width11 * height11) / 1024000;

                Log.e("mb", "" + mb);

                if (mb > 3f)
                    opt.inSampleSize = 2;

				/*if(mb > 4f) opt.inSampleSize = 4;
				else if(mb > 3f) opt.inSampleSize = 2;*/

                try
                {
                    b = BitmapFactory.decodeByteArray(data, 0, data.length, opt);
                }
                catch (OutOfMemoryError e)
                {
                    e.printStackTrace();
                }

                Matrix matrix = new Matrix();

                if (front_or_back_camera == Camera.CameraInfo.CAMERA_FACING_BACK)
                    matrix.postRotate(lastPictureOrientation);

                Log.e("Picture dimensions : " + b.getWidth(), "" + b.getHeight());

                int width = b.getWidth() > b.getHeight() ? b.getHeight() : b.getHeight();

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, width, width, true);

                //Bitmap rotated_bmp = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
                Bitmap rotated_bmp = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getHeight(), scaledBitmap.getHeight(), matrix, true);

                b.recycle();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                {
                    new SaveToSdCard(rotated_bmp, file).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                }
                else
                {
                    new SaveToSdCard(rotated_bmp, file).execute();
                }

                pressed = false;

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    };

    private class SaveImageTask extends AsyncTask<byte[], Void, Void>
    {

        @Override
        protected Void doInBackground(byte[]... data)
        {
            FileOutputStream outStream = null;

            // Write to SD Card
            try
            {
                File sdCard = Environment.getExternalStorageDirectory();
                File dir    = new File(sdCard.getAbsolutePath() + "/camtest");
                dir.mkdirs();

                String fileName = String.format("%d.jpg", System.currentTimeMillis());
                File   outFile  = new File(dir, fileName);

                Log.e("BalliNavjot", outFile.getAbsolutePath());

                outStream = new FileOutputStream(outFile);
                outStream.write(data[0]);
                outStream.flush();
                outStream.close();

				/*b = BitmapFactory.decodeByteArray(data[0], 0,data[0].length);

				Matrix matrix = new Matrix();
				matrix.postRotate(lastPictureOrientation);

				Log.e("Picture dimensions : " + b.getWidth(), "" + b.getHeight());

				//Bitmap rotated_bmp = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
				Bitmap rotated_bmp = Bitmap.createBitmap(b, 0, 0, b.getHeight(), b.getHeight(), matrix, true);


				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				{
					new SaveToSdCard(rotated_bmp, outFile).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

				}
				else
				{
					new SaveToSdCard(rotated_bmp, outFile).execute();
				}*/

                Log.d("TAG", "onPictureTaken - wrote bytes: " + data.length + " to " + outFile.getAbsolutePath());

                //refreshGallery(outFile);
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return null;
        }

    }

	/*public Bitmap resizeImageForImageView(Bitmap bitmap) {
	    Bitmap resizedBitmap = null;
	    int originalWidth = bitmap.getWidth();
	    int originalHeight = bitmap.getHeight();

	    Log.e(""+originalWidth, ""+originalHeight);
	    int newWidth = -1;
	    int newHeight = -1;
	    float multFactor = -1.0F;
	    if(originalHeight > originalWidth) {
	        newHeight = 2048;
	        multFactor = (float) originalWidth/(float) originalHeight;
	        newWidth = (int) (newHeight*multFactor);
	    } else if(originalWidth > originalHeight) {
	        newWidth = 2048;
	        multFactor = (float) originalHeight/ (float)originalWidth;
	        newHeight = (int) (newWidth*multFactor);
	    } else if(originalHeight == originalWidth) {
	        newHeight = 2048;
	        newWidth = 2048;
	    }
	    Matrix matrix = new Matrix();
		matrix.postRotate(lastPictureOrientation);

	    resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
	    return resizedBitmap;
	}

	public Bitmap fit_image(Bitmap originalImage,int width,int height )
	{
		Bitmap background = Bitmap.createBitmap((int)width, (int)height, Config.ARGB_8888);
		float originalWidth = originalImage.getWidth(), originalHeight = originalImage.getHeight();
		Canvas canvas = new Canvas(background);
		float scale = width/originalWidth;
		float xTranslation = 0.0f, yTranslation = (height - originalHeight * scale)/2.0f;
		Matrix transformation = new Matrix();
		transformation.postTranslate(xTranslation, yTranslation);
		transformation.preScale(scale, scale);
		Paint paint = new Paint();
		paint.setFilterBitmap(true);
		canvas.drawBitmap(originalImage, transformation, paint);
		return background;
	}*/

    private class SaveToSdCard extends AsyncTask<Void, Void, Void>
    {
        Bitmap           bmp       = null;
        File             file      = null;
        FileOutputStream outStream = null;

        public SaveToSdCard(Bitmap bmp, File file)
        {
            this.bmp = bmp;
            this.file = file;
        }

        protected void onPreExecute()
        {
        }

        @Override
        protected Void doInBackground(Void... arg0)
        {
            try
            {
                outStream = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                outStream.flush();
                outStream.close();
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            // addImageToGallery(file.getAbsolutePath(),con);

           /* Intent i = new Intent(con, Captured_Image.class);
            i.putExtra("image", file.getAbsolutePath().toString());
            i.putExtra("type", "I");
            startActivity(i);*/

            //            ((Activity) con).finish();
        }
    }
}
