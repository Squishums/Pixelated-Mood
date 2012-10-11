package com.squishums.android.pixelatedmood.drop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.squishums.android.pixelatedmood.component.PixelatedComponent;


/**
 * The DropManager -- the heart of the CES. This class provides methods to access
 * drops and their components. As well, the DropManager class contains the
 * pool of unused drops, to avoid rapid allocation and deallocation of objects.
 */
public class DropManager {
		
	@SuppressWarnings("rawtypes")
	private Map<Class, HashMap<Drop, ? extends PixelatedComponent>> mComponents =
			 new HashMap<Class, HashMap<Drop, ? extends PixelatedComponent>>();

	// The object pool of drops, containing the unbound drops. 
	private DropPool mDropPool;
	// A lift containing all of the bound drops (i.e. those on the screen).
	private List<Drop> mBoundDrops;	
	
	
	public DropManager() { }
	
	/**
	 * Attempts to bind a drop from the drop pool.
	 * 
	 * @return a new Drop object if successful. null otherwise.
	 */
	public Drop bindDrop() {
		Drop drop = mDropPool.bindObject();
		mBoundDrops.add(drop);
		return drop;
	}
	
	/**
	 * Returns a drop to the drop pool. The component data for released
	 * drops is undefined.
	 * 
	 * @param drop - the drop to release
	 */
	public void releaseDrop(Drop drop) {
		mBoundDrops.remove(drop);
		mDropPool.release(drop);
	}
	
	/**
	 * Sets the maximum number of drops available. This also completely removes
	 * all previously allocated drops.
	 * 
	 * @param dropCount - the new maximum number of drops.
	 */
	public void setDropCount(int dropCount) {
		mDropPool = new DropPool(dropCount);
		mDropPool.fill();
		mBoundDrops = new ArrayList<Drop>(dropCount);
	}
	
	/**
	 * Returns the component of type componentType that is associated with drop.
	 * If the drop does not have any components of the specified type associated
	 * with it, null is returned.
	 * 
	 * @param drop - the drop to grab components for.
	 * @param componentType - the class of the component to return.
	 * @return the component of type componentType associated with drop.
	 */
	public <T extends PixelatedComponent> T getComponent(Drop drop, Class<T> componentType) {
		Map<Drop, ? extends PixelatedComponent> components = mComponents.get(componentType);
		
		if (components == null) {
			return null;
		}
		
		@SuppressWarnings("unchecked")
		T result = (T) components.get(drop);
		if (result == null) {
			return null;
		}
		
		return result;
	}

	/**
	 * Return all components of type componentType.
	 * 
	 * Warning: This will also return the components of unbound drops, for which
	 * the data is undefined. 
	 * 
	 * @param componentType - the type of the components to return.
	 * @return a map of all with their associated component of the specified
	 * 		type.
	 */
	public <T extends PixelatedComponent> Map<Drop, T> getComponentsOfType(
			Class<T> componentType) {
		@SuppressWarnings("unchecked")
		Map<Drop, T> components = (Map<Drop, T>) mComponents.get(componentType);
		if (components == null) {
			return null;
		}
		
		return components;
	}
	
	/**
	 * Associates component with drop. The component should not already be
	 * associated with a drop, including this one.
	 * 
	 * @param drop - the drop to associate component with.
	 * @param component - a component.
	 */
	public <T extends PixelatedComponent> void addComponent(Drop drop, T component) {
		@SuppressWarnings("unchecked")
		HashMap<Drop, T> components =
				(HashMap<Drop, T>) mComponents.get(component.getClass());
		if (components == null) {
			components = new HashMap<Drop, T>();
			mComponents.put(component.getClass(), components);
		}
		
		components.put(drop, component);
	}
	
	/**
	 * Removes the component of type componentType from drop.
	 * 
	 * @param drop - the drop to remove 
	 * @param componentType - the type of the component to remove.
	 */
	public <T extends PixelatedComponent> void removeComponent(
			Drop drop, Class<T> componentType) {
		@SuppressWarnings("unchecked")
		HashMap<Drop, T> components =
				(HashMap<Drop, T>) mComponents.get(componentType);
		if (components == null) {
			// No entities for this component type, so the postcondition is
			// already true.
			return;
		}
		
		components.remove(drop);
	}
	
	/**
	 * Returns all bound drops.
	 * 
	 * A bound drop is one for which component data is defined and most likely
	 * is visible on the screen.
	 * 
	 * @return all bound drops
	 */
	public List<Drop> getBoundDrops() {
		return mBoundDrops;
	}
	
	/**
	 * Returns the number of bound drops.
	 * 
	 * @return
	 */
	public int getDropCount() {
		return mBoundDrops.size();
	}
}
