package system;

import drone.DroneManager;
import events.DroneEvent;
import events.DroneEvent.Drone;
import listeners.DroneListener;
import util.Log;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Sensor;

public class SerialEventManager extends AbstractEventManager implements DroneListener {
	
	private static final String LOG_TAG = "Serial Event Manager";
	
	public SerialEventManager() {		
	}
	
	/**
	 * @param c
	 * @param newInstance
	 *            pass a subclass of {@link EventManager} here
	 */
	public static final void initInstance(Context c, SerialEventManager newInstance) {
		isTabletDevice = deviceHasLargeScreenAndOrientationFlipped(c);
		initInstance(newInstance);
	}

	public static AbstractEventManager getInstance() {
		if (AbstractEventManager.getInstance() == null || 
				!(AbstractEventManager.getInstance() instanceof SerialEventManager)) {
			Log.e(LOG_TAG, "EventManager instance was not initialized!");
			initInstance(new SerialEventManager());
		}
		return AbstractEventManager.getInstance();
	}

	@Override
	public void registerListeners(Activity targetActivity,
			boolean useAccelAndMagnetoSensors) {
		super.registerListeners(targetActivity, useAccelAndMagnetoSensors);
		DroneManager.droneManager().registerListener(this);
	}	
	
	@Override
	public void unregisterListener() {		
		super.unregisterListener();
		DroneManager.droneManager().unregisterListener(this);
	}
	
	@Override
	public void onReceivedDataChanged(DroneEvent event) {

		if (onOrientationChangedList != null) {
			float[] values = event.values.clone();

			for (int i = 0; i < onOrientationChangedList.size(); i++) {

				if (event.drone.getType() == Drone.TYPE_ACCELEROMETER) {
					onOrientationChangedList.get(i).onAccelChanged(values);
				}
				if (event.drone.getType() == Drone.TYPE_MAGNETIC_FIELD) {
					onOrientationChangedList.get(i).onMagnetChanged(values);
				}
				// else sensor input is set to orientation mode
				if (event.drone.getType() == Drone.TYPE_ROTATION_VECTOR) {
					onOrientationChangedList.get(i)
							.onOrientationChanged(values);
				}
			}
		}
		
		if ( onLocationChangedList != null ) {
			for (int i = 0; i < onOrientationChangedList.size(); i++) {
				if (event.drone.getType() == Drone.TYPE_POSITION) {
					onLocationChangedList.get(i).onLocationChanged(event.location);
				}
				if (event.drone.getType() == Drone.TYPE_ALTITUDE) {
					
				}
			}
			
		}
		
	}
		

	
}
