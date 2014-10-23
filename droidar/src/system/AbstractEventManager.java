package system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import listeners.eventManagerListeners.LocationEventListener;
import listeners.eventManagerListeners.OrientationChangedListener;
import listeners.eventManagerListeners.TrackBallEventListener;

import commands.Command;

import geo.GeoObj;
import geo.GeoUtils;
import gl.GLCamera;
import util.Log;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.location.Location;
import android.view.KeyEvent;
import android.view.MotionEvent;

public abstract class AbstractEventManager {

	private static final String LOG_TAG = "Abstract Event Manager";

	private static AbstractEventManager myInstance;

	public static boolean isTabletDevice = false;
	
	// all the predefined actions:
	protected List<TrackBallEventListener> onTrackballEventList;
	protected List<OrientationChangedListener> onOrientationChangedList;
	protected List<LocationEventListener> onLocationChangedList;
	public HashMap<Integer, Command> myOnKeyPressedCommandList;

	public List<LocationEventListener> getOnLocationChangedAction() {
		return onLocationChangedList;
	}

	public List<OrientationChangedListener> getOnOrientationChangedAction() {
		return onOrientationChangedList;
	}

	public List<TrackBallEventListener> getOnTrackballEventAction() {
		return onTrackballEventList;
	}

	protected GeoObj zeroPos;
	protected GeoObj currentLocation;
	protected Activity myTargetActivity;
	
	/**
	 * @param c
	 * @param newInstance
	 *            pass a subclass of {@link EventManager} here
	 */
	public static final void initInstance(Context c, AbstractEventManager newInstance) {
		isTabletDevice = deviceHasLargeScreenAndOrientationFlipped(c);
		initInstance(newInstance);
	}

	public static AbstractEventManager getInstance() {
		return myInstance;
	}

	protected static void initInstance(AbstractEventManager instance) {
		myInstance = instance;
	}	
	
	
	public void registerListeners(Activity targetActivity,
								  boolean useAccelAndMagnetoSensors) {
		myTargetActivity = targetActivity;		
	}	
	
	/**
	 * This method returns true if the device is a tablet, can be used to handle
	 * the different default orientation
	 * 
	 * @param c
	 * @return
	 */
	public static boolean deviceHasLargeScreenAndOrientationFlipped(Context c) {
		/*
		 * Configuration.SCREENLAYOUT_SIZE_XLARGE only available for higher
		 * Android versions, constant value is 4 so hardcoded here
		 */
		int Configuration_SCREENLAYOUT_SIZE_XLARGE = 4;
		return (c.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration_SCREENLAYOUT_SIZE_XLARGE;
	}	
	
	public void resumeEventListeners(Activity targetActivity,
			boolean useAccelAndMagnetoSensors) {
	}

	public void pauseEventListeners() {
	}
	
	/**
	 * This method differs from the normal
	 * {@link EventManager#getCurrentLocationObject()} because it will return
	 * the geoPos of the virtual (0,0,0) position. The other method would return
	 * the current device position (and because of this also the current camera
	 * position)
	 * 
	 * @return the zero position. This will NOT be a copy so do not modify it!
	 */
	public GeoObj getZeroPositionLocationObject() {
		if (zeroPos == null) {
			Log.d(LOG_TAG, "Zero pos was not yet received! "
					+ "The last known position of the device will be used "
					+ "at the zero position.");
			zeroPos = getCurrentLocationObject().copy();
		}
		return zeroPos;
	}

	public void setZeroLocation(Location location) {
		if (zeroPos == null) {
			zeroPos = new GeoObj(location);
		} else {
			zeroPos.setLocation(location);
		}
	}
	
	public boolean registerLocationUpdates() {
		return false;
	}
	
	// /**
	// * The Android system will be asked directly and if the external location
	// * manager knows where the device is located at the moment, this location
	// * will be returned
	// *
	// * @return a new {@link GeoObj} or null if there could be no current
	// * location calculated
	// */
	// public GeoObj getNewCurrentLocationObjectFromSystem() {
	// return getAutoupdatingCurrentLocationObjectFromSystem().copy();
	// }
	public boolean onTrackballEvent(MotionEvent event) {
		if (onTrackballEventList != null) {
			boolean result = true;
			for (int i = 0; i < onTrackballEventList.size(); i++) {
				result &= onTrackballEventList.get(i).onTrackballEvent(
						event.getX(), event.getY(), event);
			}
			return result;
		}
		return false;
	}
	
	
	public void addOnOrientationChangedAction(OrientationChangedListener action) {
		Log.d(LOG_TAG, "Adding onOrientationChangedAction");
		if (onOrientationChangedList == null) {
			onOrientationChangedList = new ArrayList<OrientationChangedListener>();
		}
		onOrientationChangedList.add(action);
	}

	public void addOnTrackballAction(TrackBallEventListener action) {
		Log.d(LOG_TAG, "Adding onTouchMoveAction");
		if (onTrackballEventList == null) {
			onTrackballEventList = new ArrayList<TrackBallEventListener>();
		}
		onTrackballEventList.add(action);

	}

	public void addOnLocationChangedAction(LocationEventListener action) {
		Log.d(LOG_TAG, "Adding onLocationChangedAction");
		if (onLocationChangedList == null) {
			onLocationChangedList = new ArrayList<LocationEventListener>();
		}
		onLocationChangedList.add(action);
	}

	public void addOnKeyPressedCommand(int keycode, Command c) {
		if (myOnKeyPressedCommandList == null) {
			myOnKeyPressedCommandList = new HashMap<Integer, Command>();
		}
		myOnKeyPressedCommandList.put(keycode, c);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode >= 19 && keyCode <= 22) {
			/*
			 * if the keycode is on of the numbers from 19 to 22 it is a pseudo
			 * trackball event (eg the motorola milestone has pseudo trackball).
			 * here hare the codes (lets hope they are the same on each phone;):
			 * 
			 * top=19 down=20 left=21 right=22
			 */
			if (onTrackballEventList != null) {
				final float stepLength = 0.3f;
				float x = 0, y = 0;
				switch (keyCode) {
				case 19:
					y = -stepLength;
					break;
				case 20:
					y = stepLength;
					break;
				case 21:
					x = -stepLength;
					break;
				case 22:
					x = stepLength;
					break;
				}
				boolean result = true;

				for (int i = 0; i < onTrackballEventList.size(); i++) {
					result &= onTrackballEventList.get(i).onTrackballEvent(x,
							y, null);
				}

				return result;
			}

			return false;
		}

		if (myOnKeyPressedCommandList == null) {
			return false;
		}
		Command commandForThisKey = myOnKeyPressedCommandList.get(keyCode);
		if (commandForThisKey != null) {
			Log.d("Command", "Key with command was pressed so executing "
					+ commandForThisKey);
			return commandForThisKey.execute();
		}
		return false;
	}	
	
	@Deprecated
	public void setCurrentLocation(Location location) {
		currentLocation.setLocation(location);
	}

	/**
	 * This will return the current position of the device according to the
	 * Android system values.
	 * 
	 * The resulting coordinates can differ from
	 * {@link GLCamera#getGPSLocation()} if the camera was not moved according
	 * to the GPS input (eg moved via trackball).
	 * 
	 * Also check the {@link EventManager#getZeroPositionLocationObject()}
	 * method, if you want to know where the virtual zero position (of the
	 * OpenGL world) is.
	 */
	public GeoObj getCurrentLocationObject() {

		Location locaction = getCurrentLocation();
		if (locaction != null) {
			if (currentLocation == null) {
				currentLocation = new GeoObj(locaction, false);
			} else {
				currentLocation.setLocation(locaction);
			}
			return currentLocation;
		} else {
			Log.e(LOG_TAG,
					"Couldn't receive Location object for current location");
		}

		// if its still null set it to a default geo-object:
		if (currentLocation == null) {
			Log.e(LOG_TAG, "Current position set to default 0,0 position");
			currentLocation = new GeoObj(false);
		}

		return currentLocation;
	}

	/**
	 * use SimpleLocationManager.getCurrentLocation(Context) instead This method
	 * will try to get the most accurate position currently available. This
	 * includes also the last known position of the device if no current
	 * position sources can't be accessed so the returned position might be
	 * outdated
	 * 
	 * Uses {@link GeoUtils#getCurrentLocation(Context)}. <br>
	 * <br>
	 * If you need permanent location updates better create a
	 * {@link LocationEventListener} and register it at
	 * {@link EventManager#addOnLocationChangedAction(LocationEventListener)}
	 * instead of calling this method here frequently.
	 * 
	 * @return
	 */
	@Deprecated
	public Location getCurrentLocation() {
		return GeoUtils.getCurrentLocation(myTargetActivity);
	}	
}
