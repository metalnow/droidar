package actions;

import geo.GeoCalcer;
import gl.GLCamRotationController;
import gl.GLCamera;
import gl.GLUtilityClass;
import system.EventManager;
import system.Setup;
import util.Calculus;
import util.Log;
import worldData.Updateable;
import worldData.World;
import actions.algos.Algo;
import android.hardware.SensorManager;
import android.location.Location;

public class ActionWithRemoteSensorData extends Action {

	private static final String LOG_TAG = "ActionWithRemoteSensorData";

	private World myWorld;
	private GLCamera myCamera;
	private GeoCalcer myGeoCalcer;
	
	public Algo magnetAlgo;
	public Algo accelAlgo;
	public Algo orientAlgo;
	public Algo accelBufferAlgo;
	public Algo magnetBufferAlgo;
	public Algo orientationBufferAlgo;

	private float[] myAccelValues = new float[3];
	private float[] myMagnetValues = new float[3];
	private float[] myOrientValues = new float[3];

	private boolean accelChanged;
	private float[] myNewAccelValues;
	private boolean magnetoChanged;
	private float[] myNewMagnetValues;
	private boolean orientationDataChanged;
	private float[] myNewOrientValues;

	private final float[] unrotatedMatrix = Calculus.createIdentityMatrix();
	private float[] rotationMatrix = Calculus.createIdentityMatrix();

	private final int screenRotation;

	private double nullLongitude;
	private double nullLatitude;
	private double nullAltitude;
	
	/**
	 * On default this is false, because the altitude values received via GPS
	 * are very inaccurate
	 * 
	 * set this to true if your scenario need to take altitude values into
	 * account
	 */
	public static boolean USE_ALTITUDE_VALUES = false;
	/**
	 * set this to false if you want to position objects at the real 0 altitude,
	 * because otherwise if you set altitude to 0 the current device altitude
	 * will be used
	 */
	public static final boolean USE_DEVICE_ALTI_FOR_ZERO = true;

	private static final double MAX_METER_DISTANCE = 1000; // 500 meter

	private static final boolean LOG_SHOW_POSITION = false;
	
	
	public ActionWithRemoteSensorData(World world, GLCamera targetCamera) {
		myWorld = world;
		myCamera = targetCamera;
		screenRotation = Setup.getScreenOrientation();
	}	
	
	@Override
	public synchronized boolean onAccelChanged(float[] values) {
		return false;
	}

	@Override
	public synchronized boolean onMagnetChanged(float[] values) {
		return false;
	}	
	
	@Override
	public synchronized boolean onOrientationChanged(float[] values) {
		if (orientAlgo != null) {
			myNewOrientValues = orientAlgo.execute(values);
		} else {
			myNewOrientValues = values;
		}
		orientationDataChanged = true;
		return true;

	}

	@Override
	public synchronized boolean update(float timeDelta, Updateable parent) {
		if (orientationDataChanged) {
			orientationDataChanged = false;
			if (orientationBufferAlgo != null) {
				orientationBufferAlgo.execute(myOrientValues,
						myNewOrientValues, timeDelta);
			} else {
				myOrientValues = myNewOrientValues;
			}
			GLUtilityClass.getRotationMatrixFromAttitude(unrotatedMatrix,
					myOrientValues);

			/*
			 * Then in addition the values have to be remapped of the device is
			 * used in landscape mode or if it is a tablet etc
			 */

			if (EventManager.isTabletDevice) {
				/*
				 * change accel sensor data according to
				 * http://code.google.com/p
				 * /libgdx/source/browse/trunk/backends/gdx
				 * -backend-android/src/com
				 * /badlogic/gdx/backends/android/AndroidInput.java
				 */
				SensorManager.remapCoordinateSystem(unrotatedMatrix,
						SensorManager.AXIS_X, SensorManager.AXIS_Y,
						rotationMatrix);
			} else {
				/*
				 * TODO do this for all 4 rotation possibilities!
				 */
				/*
				if (screenRotation == Surface.ROTATION_90) {
					// then rotate it according to the screen rotation:
					SensorManager.remapCoordinateSystem(unrotatedMatrix,
							SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X,
							rotationMatrix);
				} else */{
					/*
					 * else its in portrait mode so no remapping needed
					 */
					rotationMatrix = unrotatedMatrix;
				}
			}
			myCamera.setRotationMatrix(rotationMatrix, 0);
		}
		return true;
	}	
	
	@Override
	public boolean onLocationChanged(Location location) {
		if (nullLatitude == 0 || nullLongitude == 0) {
			/*
			 * if the nullLat or nullLong are 0 this method was probably never
			 * called before (TODO problem when living in greenwhich e.g.?)
			 */
			resetWorldZeroPositions(location);
		} else {
			/*
			 * the following calculations were extracted from
			 * GeoObj.calcVirtualPosition() for further explanation how they
			 * work read the javadoc there. the two calculations were extracted
			 * to increase performance because this method will be called every
			 * time a new GPS-position arrives
			 */
			final double latitudeDistInMeters = (location.getLatitude() - nullLatitude) * 111133.3333;
			final double longitudeDistInMeters = (location.getLongitude() - nullLongitude)
					* 111319.4917 * Math.cos(nullLatitude * 0.0174532925);

			if (LOG_SHOW_POSITION) {
				Log.v(LOG_TAG, "latutude dist (north(+)/south(-))="
						+ latitudeDistInMeters);
				Log.v(LOG_TAG, "longitude dist (east(+)/west(-))="
						+ longitudeDistInMeters);
			}

			if (worldShouldBeRecalced(latitudeDistInMeters,
					longitudeDistInMeters)) {
				resetWorldZeroPositions(location);
			} else {
				if (USE_ALTITUDE_VALUES) {
					/*
					 * if the altitude values should be used calculate the
					 * correct height
					 */
					final double relativeHeight = location.getAltitude()
							- nullAltitude;
					myCamera.setNewPosition((float) longitudeDistInMeters,
							(float) latitudeDistInMeters,
							(float) relativeHeight);
				} else {
					// else dont change the z value
					myCamera.setNewPosition((float) longitudeDistInMeters,
							(float) latitudeDistInMeters);
				}

			}
		}

		return true;
	}	
	
	private void resetCameraToNullPosition() {
		myCamera.resetPosition(false);
	}

	private boolean worldShouldBeRecalced(double latDistMet, double longDistMet) {
		if (Math.abs(latDistMet) > MAX_METER_DISTANCE)
			return true;
		if (Math.abs(longDistMet) > MAX_METER_DISTANCE)
			return true;
		return false;
	}

	public void resetWorldZeroPositions(Location location) {
		Log.d(LOG_TAG, "Reseting virtual world positions");
		setNewNullValues(location);
		resetCameraToNullPosition();
		calcNewWorldPositions();
	}

	private void setNewNullValues(Location location) {
		nullLatitude = location.getLatitude();
		nullLongitude = location.getLongitude();
		nullAltitude = location.getAltitude();
		EventManager.getInstance().setZeroLocation(location);
	}

	private void calcNewWorldPositions() {
		if (myGeoCalcer == null)
			myGeoCalcer = new GeoCalcer();
		myGeoCalcer.setNullPos(nullLatitude, nullLongitude, nullAltitude);
		myWorld.accept(myGeoCalcer);
	}	
	
}
