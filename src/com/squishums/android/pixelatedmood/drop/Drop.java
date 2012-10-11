package com.squishums.android.pixelatedmood.drop;

import com.squishums.android.pixelatedmood.component.ComponentPhysics;
import com.squishums.android.pixelatedmood.component.ComponentPosition;
import com.squishums.android.pixelatedmood.component.ComponentRenderable;
import com.squishums.android.pixelatedmood.component.PixelatedComponent;


/**
 * The entity in the CES. It exists only as a wrapper around the various
 * components.
 * 
 * These drops should not be stored for times longer than one frame. A drop
 * might be released during that time, so it's component data will be undefined. 
 */
public class Drop {
	
	/*
	 * A static reference back to the DropManager. This allows for the
	 * redirections of the drop's methods back to the DropManager. Comamnds
	 * *could* be done through the DropManager, but this way is both
	 * cleaner, and clearer
	 */
	private static DropManager mDropManager;
	
	/**
	 * Constructs a drop containing all possible components.
	 */
	protected Drop() {
		// If the system ever becomes more complex, drops could be spawned with
		// different sets of components, but at the momemnt there is no need as
		// all of the components are always in use.
		addComponent(new ComponentPosition());
		addComponent(new ComponentPhysics());
		addComponent(new ComponentRenderable());
	}
	
	/**
	 * Adds a component to the drop.
	 * 
	 * @param component - the component to be added.
	 */
	public <T extends PixelatedComponent> void addComponent(T component) {
		mDropManager.addComponent(this, component);
	}
	
	/**
	 * Removes a component from the drop.
	 * 
	 * @param component - the component to be removed.
	 */
	public <T extends PixelatedComponent> void removeComponent(Class<T> component) {
		mDropManager.removeComponent(this, component);
	}
	
	/**
	 * Returns the component of the specified type that is associated with this
	 * drop.
	 * 
	 * @param componentType - the class of the component to return.
	 * @return the 
	 */
	public <T extends PixelatedComponent> T getComponent(Class<T> componentType) {
		return mDropManager.getComponent(this, componentType);
	}
	
	/**
	 * Returns the DropManager for this drop.
	 * 
	 * Note that since there is currently only one DropManager, this will return
	 * the same manager for every drop.
	 * 
	 * @return the DropManager associated with this drop
	 */
	public static DropManager getDropManager() {
		return mDropManager;
	}
	
	/**
	 * Associates a DropManager with this drop.
	 * 
	 * @param dropManager the DropManager to associate with this drop.
	 */
	public static void setDropManager(DropManager dropManager) {
		mDropManager = dropManager;
	}
}
