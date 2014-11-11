package actions;

import gl.GLCamRotationController;
import gl.GLUtilityClass;
import system.EventManager;
import system.Setup;
import util.Calculus;
import worldData.Updateable;
import actions.algos.Algo;
import android.hardware.SensorManager;

public class ActionWithRemoteSensorData extends Action {

	private static final String LOG_TAG = "ActionWithSensorProcessing";

	private final GLCamRotationController myTargetCamera;

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

	public ActionWithRemoteSensorData(GLCamRotationController targetCamera) {
		myTargetCamera = targetCamera;
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
			myTargetCamera.setRotationMatrix(rotationMatrix, 0);
		}
		return true;
	}	
	
}
