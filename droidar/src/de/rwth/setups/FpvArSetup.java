package de.rwth.setups;

import commands.Command;
import commands.DebugCommandPositionEvent;
import commands.ui.CommandInUiThread;
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
	private final GeoObj posH;
	
	public FpvArSetup() {
		posA = new GeoObj(121.583019, 25.059303);
		posB = new GeoObj(121.589778, 25.055143);
		posC = new GeoObj(121.580637, 25.066126);
		posD = new GeoObj(121.578834, 25.062277);
		posE = new GeoObj(121.589885, 25.062938);
		posH = new GeoObj(121.585141, 25.058715);
		
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
		
		camera = new GLCamera();
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

		mesh.setPosition(new Vec(10, 0, 0)/*Vec.getNewRandomPosInXYPlane(new Vec(), 0.1f, 1f)*/);
		x.setComp(mesh);
		CommandShowToast.show(myTargetActivity, "Object spawned at " + x.getMySurroundGroup().getPosition());
		world.add(x);
	}
	
	
	@Override
	public void _c_addActionsToEvents(AbstractEventManager eventManager,
			CustomGLSurfaceView arView, SystemUpdater updater) {
		
		rotateGLCameraAction = new ActionWithRemoteSensorData(world, camera);
		updater.addObjectToUpdateCycle(rotateGLCameraAction);
		
		eventManager.addOnOrientationChangedAction(rotateGLCameraAction);
		eventManager.addOnLocationChangedAction(rotateGLCameraAction);

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
		spawnObj(posH, GLFactory.getInstance().newArrow());
		
		guiSetup.addButtonToBottomView(new DebugCommandPositionEvent(rotateGLCameraAction,
				posA), "Go to pos A");
		guiSetup.addButtonToBottomView(new DebugCommandPositionEvent(rotateGLCameraAction,
				posB), "Go to pos B");
		guiSetup.addButtonToBottomView(new DebugCommandPositionEvent(rotateGLCameraAction,
				posC), "Go to pos C");
		guiSetup.addButtonToBottomView(new DebugCommandPositionEvent(rotateGLCameraAction,
				posD), "Go to pos D");
		guiSetup.addButtonToBottomView(new DebugCommandPositionEvent(rotateGLCameraAction,
				posE), "Go to pos E");
		guiSetup.addButtonToBottomView(new DebugCommandPositionEvent(rotateGLCameraAction,
				posH), "Go to pos H");
		
		
		addSpawnButtonToUI(posA, "Spawn at posA", guiSetup);
		addSpawnButtonToUI(posB, "Spawn at posB", guiSetup);
		addSpawnButtonToUI(posC, "Spawn at posC", guiSetup);
		addSpawnButtonToUI(posD, "Spawn at posD", guiSetup);
		addSpawnButtonToUI(posE, "Spawn at posE", guiSetup);
		//addSpawnButtonToUI(posH, "Spawn at posH", guiSetup);
		
		addGpsPosOutputButtons(guiSetup);
	}

	private void addGpsPosOutputButtons(GuiSetup guiSetup) {
		guiSetup.addButtonToBottomView(new CommandInUiThread() {

			@Override
			public void executeInUiThread() {
				Vec pos = camera.getGPSPositionVec();
				String text = "latitude=" + pos.y + ", longitude=" + pos.x;
				CommandShowToast.show(myTargetActivity, text);
			}
		}, "Show Camera GPS pos");

		guiSetup.addButtonToBottomView(new CommandInUiThread() {

			@Override
			public void executeInUiThread() {
				GeoObj pos = EventManager.getInstance()
						.getCurrentLocationObject();
				String text = "latitude=" + pos.getLatitude() + ", longitude="
						+ pos.getLongitude();
				CommandShowToast.show(myTargetActivity, text);
			}
		}, "Show real GPS pos");

		guiSetup.addButtonToBottomView(new CommandInUiThread() {

			@Override
			public void executeInUiThread() {
				GeoObj pos = EventManager.getInstance()
						.getZeroPositionLocationObject();
				String text = "latitude=" + pos.getLatitude() + ", longitude="
						+ pos.getLongitude();
				CommandShowToast.show(myTargetActivity, text);
			}
		}, "Show zero GPS pos");
	}
	
	private void addSpawnButtonToUI(final GeoObj pos, String buttonText,
			GuiSetup guiSetup) {
		guiSetup.addButtonToTopView(new Command() {
			@Override
			public boolean execute() {

				MeshComponent mesh = GLFactory.getInstance().newArrow();
				spawnObj(pos, mesh);
				return true;
			}

		}, buttonText);
	}	
}
