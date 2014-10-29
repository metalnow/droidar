package system;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue.IdleHandler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.widget.Toast;

public class FpvCameraView extends CameraView implements Runnable {
	private static final String LOG_TAG = "FPVCameraView";
	private static final boolean DEBUG = false;
	
	protected Context context;
	private SurfaceHolder holder;
    Thread mainLoop = null;
	private Bitmap bmp=null;
	private Handler handler;
	private final VideoHandler videoHandler = new VideoHandler(this); 

	private boolean cameraExists=false;
	private boolean shouldStop=false;
	
	// /dev/videox (x=cameraId+cameraBase) is used.
	// In some omap devices, system uses /dev/video[0-3],
	// so users must use /dev/video[4-].
	// In such a case, try cameraId=0 and cameraBase=4
	private int cameraId=0;
	private int cameraBase=0;
	
	// This definition also exists in ImageProc.h.
	// Webcam must support the resolution 640x480 with YUYV format. 
	static final int IMG_WIDTH=640;
	static final int IMG_HEIGHT=480;

	// The following variables are used to draw camera images.
    private int winWidth=0;
    private int winHeight=0;
    private Rect rect;
    private int dw, dh;
    private float rate;
    // for manipulation of original image
	public Matrix mx = new Matrix();
	// for rendering to canvas
    private float scale_x, scale_y, pos_x;
	private Matrix mx_canvas = new Matrix();
	
	private boolean detectingCamera = false;
	
    // JNI functions
    public native int prepareCamera(int videoid);
    public native int prepareCameraWithBase(int videoid, int camerabase);
    public native void processCamera();
    public native void stopCamera();
    public native void pixeltobmp(Bitmap bitmap);
    static {
        System.loadLibrary("ImageProc");
    }
	
	
	public FpvCameraView(Context context) {
		super(context);
		initCamera(context);
	}
	
	public FpvCameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initCamera(context);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if(DEBUG) Log.d("WebCam", "surfaceCreated");
		if(bmp==null){
			bmp = Bitmap.createBitmap(IMG_WIDTH, IMG_HEIGHT, Bitmap.Config.ARGB_8888);
		}
		// /dev/videox (x=cameraId + cameraBase) is used
		//int ret = prepareCameraWithBase(cameraId, cameraBase);		
		//if(ret!=-1) cameraExists = true;
		
		detectingCamera = true;
		
		Thread detectThread = new Thread() {
            public void run() {
                int ret = -1;
                int camID = 0;
                int camBase = cameraBase;
                do {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    ret = prepareCameraWithBase(camID, camBase);
                    if(DEBUG) Log.d("WebCam", "Detect: " + ret + " base: " + camBase + " camID: " + camID);
                    camID++;
                    if ( camID == cameraId + 1 ){
                        if ( camBase == 0 )
                            camBase = 4;
                        else
                            camBase = 0;
                    }
                    camID = camID % (cameraId+1);
                }while ( ret == -1 );
                cameraExists = (ret!=-1);
                detectingCamera = false;
            }
        };
        detectThread.start();		
		
        // Busy Waiting
		while( detectingCamera ) {
			
		}
		
        mainLoop = new Thread(this);
        mainLoop.start();				
	}

	@Override
	public void setPreviewAccordingToScreenOrientation(int width, int height) {
	}	
	
	public void initCamera(Context context) {
		this.context = context;
		if(DEBUG) Log.d("WebCam","CameraPreview constructed");
		setFocusable(true);
		
		holder = getHolder();
		holder.addCallback(this);		
	}
	
	@Override
	public void resumeCamera() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void releaseCamera() {
		if(cameraExists){
			shouldStop = true;
			while(shouldStop){
				try{ 
					Thread.sleep(100); // wait for thread stopping
				}catch(Exception e){}
			}
		}
		stopCamera();		
	}

	@Override
	public void run() {
		Looper.prepare();
		
		/*
		 * currently every message will trigger saving an image
		 */
		 handler = new Handler() {
	            public void handleMessage(Message msg) {

	            	if(cameraExists)
	            	{
		        		Date date = new Date();
		            	
		        		File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator + "simplewebcam");
		        		directory.mkdirs();
		            	String filename = directory.getAbsoluteFile() + File.separator + String.valueOf(date.getTime()) + ".jpg";
		            	
		            	try {
		         	       FileOutputStream out = new FileOutputStream(filename);
		         	       bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
		         	       Toast.makeText(context,"Saved image: " + filename, Toast.LENGTH_SHORT).show();
		         		} catch (Exception e) {
		         		       e.printStackTrace();
	         		       Toast.makeText(context,"Failed to save image: " + filename, Toast.LENGTH_SHORT).show();
		         		}
		            	
	            	}
	            	else
	            	{
	            		Toast.makeText(context,"No Camera", Toast.LENGTH_LONG).show();
	            	}
	            }
	        };
	        
	        /*
	         * add idle handler, this is where the video is processed
	         */
	        Looper.myQueue().addIdleHandler(new VideoHandler(this));
			
			Looper.loop();		
	}
	
	
    private class VideoHandler implements IdleHandler 
    {
    	
    	FpvCameraView mCp;
    	Matrix canvas_pos_scale = new Matrix(); 
    	
    	public VideoHandler(FpvCameraView cp) {
    		mCp = cp;
		}

		@Override
		public boolean queueIdle() {

			//loop in the idle queue unless there are new messages
			while(true && 
				  (cameraExists || DEBUG)&& 
				  !(handler.hasMessages(0))) 
			{
	        	//Log.d("runnable","inside");
	        	//obtaining display area to draw a large image
	        	if(winWidth==0)
	        	{
	        		winWidth=mCp.getWidth();
	        		winHeight=mCp.getHeight();

	        		if(winWidth*3/4<=winHeight)
	        		{
	        			scale_x = ((float)(dw+winWidth-1)/(float)FpvCameraView.IMG_WIDTH);
	        			scale_y = ((float)(dh+winWidth*3/4-1)/(float)FpvCameraView.IMG_HEIGHT);
	        		}
	        		else
	        		{
	        			scale_x = ((float)(dw+winHeight*4/3 -1)/(float)FpvCameraView.IMG_WIDTH);
	        			scale_y = ((float)(dh+winHeight-1)/(float)FpvCameraView.IMG_HEIGHT);
	        		}
		        	canvas_pos_scale.setScale(scale_x, scale_y);

                    WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                    Display display = wm.getDefaultDisplay();

                    Point size = new Point();
                    display.getSize(size);
                    int width = size.x;
                    pos_x = ( width - (float)FpvCameraView.IMG_WIDTH * scale_x) / 2.0f;
	        	}

	            Canvas canvas = getHolder().lockCanvas();
	            if (canvas != null)
	            {	            	
		        	if(DEBUG)
		        	{
		        		bmp = Bitmap.createBitmap(mCp.winWidth, mCp.winHeight,Config.ARGB_8888);
		        	}
		        	else
		        	{
			        	// obtaining a camera image (pixel data are stored in an array in JNI).
			        	processCamera();
			        	// camera image to bmp
			        	pixeltobmp(bmp);	        		
		        	}
	            	
	            	mx_canvas.reset();
	            	// first apply flipping etc.
	            	mx_canvas.postConcat(mx);
	            	// second scale the image to fit the screen
	            	mx_canvas.postConcat(canvas_pos_scale);
                    mx_canvas.postTranslate( 100, 0 );
	        		Log.d("canvas matrix",mx_canvas.toString());

	            	// draw camera bmp on canvas
	            	canvas.drawBitmap(bmp, mx_canvas, null);
	            	
	            	getHolder().unlockCanvasAndPost(canvas);
	            }
	            else
	            {
	            	Log.e("idleQueue","Canvas empty");
	            }

	            if(shouldStop){
	            	shouldStop = false;  
	            }	        
	        }

			return true;
		}
    }	
	
}
