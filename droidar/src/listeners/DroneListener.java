package listeners;

import events.DroneEvent;

public interface DroneListener {
	public void onReceivedDataChanged(DroneEvent event);
}
