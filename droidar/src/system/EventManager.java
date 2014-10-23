package system;

import geo.GeoObj;
import geo.GeoUtils;
import gl.GLCamera;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import listeners.eventManagerListeners.LocationEventListener;
import listeners.eventManagerListeners.OrientationChangedListener;
import listeners.eventManagerListeners.TrackBallEventListener;
import util.Log;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;

import commands.Command;

/**
 * this EventManager is attached to the main {@link Thread} and should react on
 * any kind of event or input
 * 
 * @author Spobo
 * 
 */

public class EventManager extends AbstractEventManager implements LocationListener, SensorEventListener {

	private static final String LOG_TAG = "Event Manager";


	public EventManager() {
	}

	/**
	 * @param c
	 * @param newInstance
	 *            pass a subclass of {@link EventManager} here
	 */
	public static final void initInstance(Context c, EventManager newInstance) {
		isTabletDevice = deviceHasLargeScreenAndOrientationFlipped(c);
		initInstance(newInstance);
	}

	public static AbstractEventManager getInstance() {
		if (AbstractEventManager.getInstance() == null) {
			Log.e(LOG_TAG, "EventManager instance was not initialized!");
			initInstance(new EventManager());
		}
		return AbstractEventManager.getInstance();
	}

	@Override
	public void registerListeners(Activity targetActivity,
			boolean useAccelAndMagnetoSensors) {
		super.registerListeners(targetActivity, useAccelAndMagnetoSensors);
		registerSensorUpdates(targetActivity, useAccelAndMagnetoSensors);
		registerLocationUpdates();
	}

	protected void registerSensorUpdates(Activity myTargetActivity,
			boolean useAccelAndMagnetoSensors) {
		SensorManager sensorManager = (SensorManager) myTargetActivity
				.getSystemService(Context.SENSOR_SERVICE);

		if (useAccelAndMagnetoSensors) {
			/*
			 * To register the EventManger for magnet- and accelerometer-sensor
			 * events, two Sensor-objects have to be obtained and then the
			 * EventManager is set as the Listener for these type of sensor
			 * events. The update rate is set by SENSOR_DELAY_GAME to a high
			 * frequency required to react on fast device movement
			 */
			Sensor magnetSensor = sensorManager
					.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
			sensorManager.registerListener(this, magnetSensor,
					SensorManager.SENSOR_DELAY_GAME);
			Sensor accelSensor = sensorManager
					.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			sensorManager.registerListener(this, accelSensor,
					SensorManager.SENSOR_DELAY_GAME);
			Sensor sensorFusion = sensorManager
					.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
			sensorManager.registerListener(this, sensorFusion,
					SensorManager.SENSOR_DELAY_GAME);
		} else {
			// Register orientation Sensor Listener:
			Sensor orientationSensor = sensorManager.getDefaultSensor(11);// Sensor.TYPE_ROTATION_VECTOR);
			sensorManager.registerListener(this, orientationSensor,
					SensorManager.SENSOR_DELAY_GAME);
		}
	}

	/**
	 * This method will try to find the best location source available (probably
	 * GPS if enabled). Remember to wait some seconds before calling this if you
	 * activated GPS programmatically using {@link GeoUtils#enableGPS(Activity)}
	 * 
	 * @return true if the Eventmanager was registered correctly
	 */
	@Override
	public boolean registerLocationUpdates() {

		if (myTargetActivity == null) {
			Log.e(LOG_TAG, "The target activity was undefined while "
					+ "trying to register for location updates");
		}

		try {
			return SimpleLocationManager.getInstance(myTargetActivity)
					.requestLocationUpdates(this);
		} catch (Exception e) {
			Log.e(LOG_TAG, "There was an error registering the "
					+ "EventManger for location-updates. The phone might be "
					+ "in airplane-mode..");
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void onAccuracyChanged(Sensor s, int accuracy) {
		// Log.d("sensor onAccuracyChanged", arg0 + " " + arg1);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
			return;
		}
		float[] values = event.values.clone();

		if (onOrientationChangedList != null) {

			for (int i = 0; i < onOrientationChangedList.size(); i++) {

				if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
					onOrientationChangedList.get(i).onAccelChanged(values);
				}
				if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
					onOrientationChangedList.get(i).onMagnetChanged(values);
				}
				// else sensor input is set to orientation mode
				if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
					onOrientationChangedList.get(i)
							.onOrientationChanged(values);
				}
			}
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		if (onLocationChangedList != null) {
			for (int i = 0; i < onLocationChangedList.size(); i++) {
				LocationEventListener l = onLocationChangedList.get(i);
				if (!l.onLocationChanged(location)) {
					Log.w(LOG_TAG, "Action " + l
							+ " returned false so it will be "
							+ "removed from the location listener list!");
					getOnLocationChangedAction().remove(l);
				}
			}
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.w(LOG_TAG, "Didnt handle onProviderDisabled of " + provider);
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.w(LOG_TAG, "Didnt handle onProviderEnabled of " + provider);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.i(LOG_TAG, "Status change of " + provider + ": " + status);
		if (myTargetActivity != null) {
			if (!registerLocationUpdates()) {
				Log.d(LOG_TAG, "EventManager was already contained in "
						+ "to the listener list of SimpleLocationManager");
			}
		} else {
			Log.w(LOG_TAG, "Didnt handle onStatusChanged of " + provider
					+ "(status=" + status + ")");
		}

	}

	@Override
	public void resumeEventListeners(Activity targetActivity,
			boolean useAccelAndMagnetoSensors) {
		registerListeners(targetActivity, useAccelAndMagnetoSensors);
	}

	@Override
	public void pauseEventListeners() {
		SensorManager sensorManager = (SensorManager) myTargetActivity
				.getSystemService(Context.SENSOR_SERVICE);
		sensorManager.unregisterListener(this);

		SimpleLocationManager.getInstance(myTargetActivity)
				.pauseLocationManagerUpdates();
	}

	/**
	 * see {@link SimpleLocationManager#setMaxNrOfBufferedLocations(int)}
	 * 
	 * @param maxNrOfBufferedLocations
	 */
	public void setMaxNrOfBufferedLocations(int maxNrOfBufferedLocations) {
		SimpleLocationManager.getInstance(myTargetActivity)
				.setMaxNrOfBufferedLocations(maxNrOfBufferedLocations);
	}

}
