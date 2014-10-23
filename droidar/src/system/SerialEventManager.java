package system;

import util.Log;
import android.content.Context;
import android.content.res.Configuration;

public class SerialEventManager extends AbstractEventManager {
	
	private static final String LOG_TAG = "Serial Event Manager";
	
	public SerialEventManager() {
		// TODO Auto-generated constructor stub
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
		if (AbstractEventManager.getInstance() == null) {
			Log.e(LOG_TAG, "EventManager instance was not initialized!");
			initInstance(new SerialEventManager());
		}
		return AbstractEventManager.getInstance();
	}
		

	
}
