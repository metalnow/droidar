package drone;

import android.location.Location;
import listeners.DroneListener;
import events.DroneEvent;
import events.DroneEvent.Drone;

public class DroneManager /*implements Runnable*/ {
	
	public static DroneManager myInstance;
	
	public static DroneManager droneManager() {
		if ( myInstance == null )
			myInstance = new DroneManager();
		return myInstance;
	}
	
	private DroneEvent event = new DroneEvent();
	private boolean running = false;
	Thread mainLoop = null;
	
	public DroneManager() {
		initSerialDrone();
		//mainLoop = new Thread(this);
		//mainLoop.start();
	}

	private void initSerialDrone() {
		// TODO Auto-generated method stub
		
	}

	public void registerListener( DroneListener listener ) {
		event.addDroneListener(listener);
	}
	
	public void unregisterListener( DroneListener listener) {
		event.removeDroneListener( listener );
	}
	
	public void stop() {
		running = false;
	}

	public void setPosition( float lo, float la ) {
		event.drone.setType(Drone.TYPE_POSITION);

		event.location.reset();
		event.location.setLongitude(lo);
		event.location.setLatitude(la);
		
		event.receivedData();
	}
	
	public void setLocation( Location location ) {
		event.drone.setType(Drone.TYPE_POSITION);
		
		event.location.set(location);
		
		event.receivedData();
		
	}
		
	public void setRotationVector( float x, float y, float z ) {
		event.drone.setType(Drone.TYPE_ROTATION_VECTOR);
				
		event.values[0] = x;
		event.values[1] = y;
		event.values[2] = z;
		
		event.receivedData();
	}
		
	/*
	private float xStep = (0.68026644f - (-0.5695711f)) / 1000f;
	private float yStep = (-0.15445209f - (-0.3063118f)) / 1000f;
	private float zStep = (0.7028675f - (-0.6624234f)) / 1000f;
	
	private int step = 0;
	
	public void run() {
		running = true;
		step = 0;
		while ( running ) {
			
			event.drone.setType(Drone.TYPE_ROTATION_VECTOR);
			
			
			event.values[0] = -0.5695711f + step * xStep;
			event.values[1] = -0.3063118f + step * yStep;
			event.values[2] = -0.6624234f + step * zStep;
						
			step++;
			step = step % 1000;
			
			event.receivedData();
		}
	}
	*/

}
