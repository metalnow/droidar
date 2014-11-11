package de.rwth.setups;

import commands.ui.CommandShowToast;

import geo.GeoObj;
import gl.Color;
import gl.CustomGLSurfaceView;
import gl.GL1Renderer;
import gl.GLCamera;
import gl.GLFactory;
import gl.scenegraph.MeshComponent;
import gui.GuiSetup;
import actions.Action;
import actions.ActionCalcRelativePos;
import actions.ActionMoveCameraBuffered;
import actions.ActionRotateCameraBuffered;
import actions.ActionWASDMovement;
import actions.ActionWaitForAccuracy;
import actions.ActionWithRemoteSensorData;
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
	private ActionWithRemoteSensorData rotateGLCameraAction;
	
	private final GeoObj posA;
	private final GeoObj posB;
	private final GeoObj posC;
	private final GeoObj posD;
	private final GeoObj posE;
	
	public FpvArSetup() {
		posA = new GeoObj(25.059303, 121.583019);
		posB = new GeoObj(25.055143, 121.589778);
		posC = new GeoObj(25.066126, 121.580637);
		posD = new GeoObj(25.062277, 121.578834);
		posE = new GeoObj(25.062938, 121.589885);
		
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
		
		camera = new GLCamera(new Vec(0, 0, 0));
		world = new World(camera);
/*		
		world.add(objectFactory.newTextObject("DroidAR", new Vec(10, 1, 1),
				getActivity(), camera));

		world.add(objectFactory.newTextObject("DroidAR1", new Vec(10, 1, 2),
				getActivity(), camera));

		world.add(objectFactory.newTextObject("DroidAR2", new Vec(10, 2, -1),
				getActivity(), camera));
*/
		world.add(objectFactory.newTextObject("DroidAR3", new Vec(10, 0, 0),
				getActivity(), camera));

		world.add(objectFactory.newTextObject("DroidAR4", new Vec(0, 10, 0),
				getActivity(), camera));

		world.add(objectFactory.newTextObject("DroidAR5", new Vec(0, 0, 10),
				getActivity(), camera));

		world.add(objectFactory.newTextObject("DroidAR6", new Vec(-10, 0, 0),
				getActivity(), camera));

		world.add(objectFactory.newTextObject("DroidAR7", new Vec(0, -10, 0),
				getActivity(), camera));

		world.add(objectFactory.newTextObject("DroidAR8", new Vec(0, 0, -10),
				getActivity(), camera));
		
		spawnObj(posA, GLFactory.getInstance().newCircle(Color.green()));
		spawnObj(posB, GLFactory.getInstance().newCircle(Color.silver1()));
		spawnObj(posC, GLFactory.getInstance().newCircle(Color.red()));
		spawnObj(posD, GLFactory.getInstance().newCircle(Color.green()));
		spawnObj(posE, GLFactory.getInstance().newCircle(Color.blue()));		
		
		glRenderer.addRenderElement(world);

	}

	private void spawnObj(final GeoObj pos, MeshComponent mesh) {
		GeoObj x = new GeoObj(pos);

		mesh.setPosition(Vec.getNewRandomPosInXYPlane(new Vec(), 0.1f, 1f));
		x.setComp(mesh);
		//CommandShowToast.show(myTargetActivity, "Object spawned at " + x.getMySurroundGroup().getPosition());
		world.add(x);
	}
	
	
	@Override
	public void _c_addActionsToEvents(AbstractEventManager eventManager,
			CustomGLSurfaceView arView, SystemUpdater updater) {
		
		rotateGLCameraAction = new ActionWithRemoteSensorData(world, camera);
		updater.addObjectToUpdateCycle(rotateGLCameraAction);
		
		eventManager.addOnOrientationChangedAction(rotateGLCameraAction);

		//eventManager.addOnTrackballAction(new ActionMoveCameraBuffered(camera, 5, 25));
		
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
