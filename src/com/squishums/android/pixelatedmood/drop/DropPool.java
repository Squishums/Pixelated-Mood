package com.squishums.android.pixelatedmood.drop;

import com.squishums.android.pixelatedmood.util.ObjectPool;


/**
 * An object pool of drops.
 */
public class DropPool extends ObjectPool<Drop> {
	
	/**
	 * Creates a drop pool with the default size.
	 */
	public DropPool() {
		super();
	}
	
	/**
	 * Creates a drop pool with a custom size.
	 * 
	 * @param size - the size of the drop pool to create.
	 */
	public DropPool(int size) {
		super(size);
	}
	
	@Override
	protected Drop createObject() {
		return new Drop(); 
	}
}
