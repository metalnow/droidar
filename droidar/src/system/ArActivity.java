package system;

import java.io.IOException;

import org.taulabs.uavtalk.UAVObject;

import de.rwth.ObjectManagerActivity;
import drone.DroneManager;
import util.Log;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.lang.Math;

/**
 * This is an example activity which demonstrates how to use a Setup object. It
 * wraps the Setup object and forwards all needed events to it.
 * 
 * @author Simon Heinen
 * 
 */
public class ArActivity extends ObjectManagerActivity {

	private static final String LOG_TAG = "ArActivity";

	private static Setup staticSetupHolder;

	private Setup mySetupToUse;

	/**
	 * Called when the activity is first created.
	 * 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(LOG_TAG, "main onCreate");
		if (staticSetupHolder != null) {
			mySetupToUse = staticSetupHolder;
			staticSetupHolder = null;
			runSetup();
		} else {
			Log.e(LOG_TAG, "There was no Setup specified to use. "
					+ "Please use ArActivity.show(..) when you "
					+ "want to use this way of starting the AR-view!");
			this.finish();
		}
	}

	public static void startWithSetup(Activity currentActivity, Setup setupToUse) {
		ArActivity.staticSetupHolder = setupToUse;
		currentActivity.startActivity(new Intent(currentActivity,
				ArActivity.class));
	}

	public static void startWithSetupForResult(Activity currentActivity,
			Setup setupToUse, int requestCode) {
		ArActivity.staticSetupHolder = setupToUse;
		currentActivity.startActivityForResult(new Intent(currentActivity,
				ArActivity.class), requestCode);
	}

	private void runSetup() {
		mySetupToUse.run(this);
	}

	@Override
	protected void onRestart() {
		if (mySetupToUse != null)
			mySetupToUse.onRestart(this);
		super.onRestart();
	}

	@Override
	protected void onResume() {
		if (mySetupToUse != null)
			mySetupToUse.onResume(this);
		super.onResume();
	}

	@Override
	public void onStart() {
		if (mySetupToUse != null)
			mySetupToUse.onStart(this);
		super.onStart();			
		//DroneManager.droneManager().setRotationVector(0.03108196f, -0.6640874f, 0.06775749f);
		//DroneManager.droneManager().setRotationVector(270.f, 0.f, 90.f);
		DroneManager.droneManager().setRotationVector(0.f, 0.f, 0.f);
		
		Thread update = new Thread() {
			@Override
			public void run() {
				float pitch = 0;
				float hdg = 0;
				float roll = 0;
				try {
					Thread.sleep(5000);
					while(true) {
							/*
							float[] angles = new float[3];
							float[] left = new float[3];
							float[] up = new float[3];
							float[] forward = new float[3];
							
							angles[0] = (float)Math.toRadians(pitch);
							angles[1] = (float)Math.toRadians(hdg);
							angles[2] = (float)Math.toRadians(roll);
							
							anglesToAxes( angles, left, up, forward );
							*/
							DroneManager.droneManager().setRotationVector(pitch, hdg, roll);			
							
							//hdg += 1;						
							//if ( hdg >= 360.0 )
							//	hdg -= 360.0;
							Thread.sleep(50);						
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		//update.start();
		
	}

	@Override
	public void onStop() {
		if (mySetupToUse != null)
			mySetupToUse.onStop(this);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if (mySetupToUse != null)
			mySetupToUse.onDestroy(this);
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		if (mySetupToUse != null)
			mySetupToUse.onPause(this);
		super.onPause();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((mySetupToUse != null)
				&& (mySetupToUse.onKeyDown(this, keyCode, event)))
			return true;
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (((mySetupToUse != null) && mySetupToUse.onCreateOptionsMenu(menu)))
			return true;
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (mySetupToUse != null)
			return mySetupToUse.onMenuItemSelected(featureId, item);
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d(LOG_TAG, "main onConfigChanged");
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
			Log.d(LOG_TAG, "orientation changed to landscape");
		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
			Log.d(LOG_TAG, "orientation changed to portrait");
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onConnected() {
		super.onConnected();

		UAVObject obj = objMngr.getObject("AttitudeActual");
		if (obj != null) {
			registerObjectUpdates(obj);
			objectUpdated(obj);
		}		
		/*
		registerObjectUpdates(objMngr.getObjects());
		
		UAVObject obj = objMngr.getObject("Pitch");
		if (obj != null) {
			obj.updateRequested(); // Make sure this is correct and been updated
			registerObjectUpdates(obj);
			objectUpdated(obj);
		}

		obj = objMngr.getObject("Roll");
		if (obj != null) {
			obj.updateRequested(); // Make sure this is correct and been updated
			registerObjectUpdates(obj);
			objectUpdated(obj);
		}
		
		obj = objMngr.getObject("Yaw");
		if (obj != null) {
			obj.updateRequested(); // Make sure this is correct and been updated
			registerObjectUpdates(obj);
			objectUpdated(obj);
		}
		*/		
	}
	
	/**
	 * Called whenever any objects subscribed to via registerObjects
	 */
	@Override
	protected void objectUpdated(UAVObject obj) {
		
		if (obj.getName().compareTo("AttitudeActual") == 0) {
			double pitch = obj.getField("Pitch").getDouble();
			double roll = obj.getField("Roll").getDouble();		
			//double hdg = obj.getField("Heading").getDouble();
			double hdg = obj.getField("Yaw").getDouble();
			
			/*
			float[] angles = new float[3];
			float[] left = new float[3];
			float[] up = new float[3];
			float[] forward = new float[3];
			
			angles[0] = (float)(pitch);
			angles[1] = (float)(hdg);
			angles[2] = (float)(roll);
			
			anglesToAxes( angles, left, up, forward );
			
			DroneManager.droneManager().setRotationVector(forward[0], forward[1], forward[2]);
			*/			
			
			DroneManager.droneManager().setRotationVector((float)pitch, (float)hdg, (float)roll);
		}
	}	
	
	private void anglesToAxes(float[] angles, float[] left, float[] up, float[] forward)
	{
	    float DEG2RAD = 3.141593f / 180;
	    float sx, sy, sz, cx, cy, cz;
	    double theta;

	    // rotation angle about X-axis (pitch)
	    theta = angles[0] * DEG2RAD;
	    sx = (float)Math.sin(theta);
	    cx = (float)Math.cos(theta);

	    // rotation angle about Y-axis (yaw)
	    theta = angles[1] * DEG2RAD;
	    sy = (float)Math.sin(theta);
	    cy = (float)Math.cos(theta);

	    // rotation angle about Z-axis (roll)
	    theta = angles[2] * DEG2RAD;
	    sz = (float)Math.sin(theta);
	    cz = (float)Math.cos(theta);

	    // determine left axis
	    left[0] = cy*cz;
	    left[1] = sx*sy*cz + cx*sz;
	    left[2] = -cx*sy*cz + sx*sz;

	    // determine up axis
	    up[0] = -cy*sz;
	    up[1] = -sx*sy*sz + cx*cz;
	    up[2] = cx*sy*sz + sx*cz;

	    // determine forward axis
	    forward[0] = sy;
	    forward[1] = -sx*cy;
	    forward[2] = cx*cy;
	}	
	

}