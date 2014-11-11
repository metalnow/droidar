package events;

import listeners.DroneListener;

public class DroneEvent {
	public final float[] values = new float[3];
	
	private DroneListener droneListener;
	public Drone drone = new Drone();
		
	public void addDroneListener( DroneListener listener ) {
		droneListener = listener;
	}
	
	public void removeDroneListener(DroneListener listener) {		
		if ( droneListener == listener )
			droneListener = null;
	}
	
	public void receivedData() {
		if ( droneListener != null )
			droneListener.onReceivedDataChanged(this);
	}

	
	public class Drone {
		
	  public static final int TYPE_ACCELEROMETER = 1;
	  	  
	  public static final int TYPE_MAGNETIC_FIELD = 2;
	  
	  public static final int TYPE_ORIENTATION = 3;
	  
	  public static final int TYPE_GYROSCOPE = 4;
	  
	  public static final int TYPE_ALTITUDE = 5;
	  
	  public static final int TYPE_POSITION = 10;
	  
	  public static final int TYPE_ROTATION_VECTOR = 11;
	  
	  private int type = -1;
	  
	  public int getType() {
		  return type;
	  }
	  
	  public void setType(int type) {
		  this.type = type;
	  }		  
		  
	}
	
	
}
