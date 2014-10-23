package de.rwth.setups;

import geo.GeoObj;
import gl.CustomGLSurfaceView;
import gl.GL1Renderer;
import gl.GLCamera;
import gl.GLFactory;
import gui.GuiSetup;
import actions.Action;
import actions.ActionCalcRelativePos;
import actions.ActionMoveCameraBuffered;
import actions.ActionRotateCameraBuffered;
import actions.ActionWASDMovement;
import actions.ActionWaitForAccuracy;
import android.app.Activity;
import android.location.Location;
import android.view.SurfaceView;
import system.AbstractEventManager;
import system.EventManager;
import system.FpvCameraView;
import system.SerialEventManager;
import system.Setup;
import util.Log;
import util.Vec;
import worldData.SystemUpdater;
import worldData.World;

public class FpvArSetup extends Setup {

	private GLCamera camera;
	private World world;
	private ActionRotateCameraBuffered rotateGLCameraAction;
	
	public FpvArSetup() {
		
	}

	@Override
	protected SurfaceView initCameraView(Activity a) {
		return new FpvCameraView(a);
	}
	
	@Override
	public void _0_initEventManager() {
		SerialEventManager.getInstance().registerListeners(getActivity(),false);	
	}
	
	
	@Override
	public void _a_initFieldsIfNecessary() {
	}

	@Override
	public void _b_addWorldsToRenderer(GL1Renderer glRenderer,
			GLFactory objectFactory, GeoObj currentPosition) {
		
		camera = new GLCamera(new Vec(0, 0, 1));
		world = new World(camera);
		
		world.add(objectFactory.newTextObject("DroidAR", new Vec(10, 1, 1),
				getActivity(), camera));
		
		glRenderer.addRenderElement(world);

	}

	@Override
	public void _c_addActionsToEvents(AbstractEventManager eventManager,
			CustomGLSurfaceView arView, SystemUpdater updater) {
		
		rotateGLCameraAction = new ActionRotateCameraBuffered(camera);
		updater.addObjectToUpdateCycle(rotateGLCameraAction);
		
		eventManager.addOnOrientationChangedAction(rotateGLCameraAction);

		eventManager.addOnTrackballAction(new ActionMoveCameraBuffered(camera,
				5, 25));
		
		//eventManager.addOnLocationChangedAction(new ActionCalcRelativePos(world, camera));

	}

	@Override
	public void _d_addElementsToUpdateThread(SystemUpdater updater) {
		updater.addObjectToUpdateCycle(world);
	}

	@Override
	public void _e2_addElementsToGuiSetup(GuiSetup guiSetup, Activity activity) {
		// TODO Auto-generated method stub

	}

}
