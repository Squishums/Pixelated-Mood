package com.squishums.android.pixelatedmood.util;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

/**
 * A abstract utility class to avoid object allocations in preformance-critical
 * sections. The object pool contains a set number of allocated objects, from
 * which objects are "bound" (made available for outside use), and "released"
 * (return to the pool, at which point their data becomes undefined).
 * 
 * Subclasses must implement the createObject() function, which is used at the
 * beginning to allocate the objects.
 * 
 * Attempting to bind more objects than exist, or returning more objects than
 * the maximum pool size are both errors.
 *
 * @param <T> - the type of object pool to create.
 */
public abstract class ObjectPool<T> {

	private static final String LOG_TAG = ObjectPool.class.getName();
	private static final int DEFAULT_SIZE = 5;
	
	// The list of unbound objects in the pool.
	protected List<T> mObjects;
	// The maximum size of the pool.
	private int mSize;
	
	
	/**
	 * Creates an object pool with the default size.
	 */
	public ObjectPool() {
		setSize(DEFAULT_SIZE);
	}
	
	/**
	 * Creates an object pool with a custom size.
	 * 
	 * @param size - the size of object pool to create.
	 */
	public ObjectPool(int size) {
		setSize(size);
	}
	
	/**
	 * Sets the size of the object pool. The pool is then refilled with unbound
	 * objects.
	 * 
	 * This requires the reallocation of objects in the pool, so use of this
	 * function should be kept to a minimum.
	 * 
	 * @param size - the new size of the object pool.
	 */
	 void setSize(int size) {
		mSize = size;
		mObjects = new ArrayList<T>(size);
		
		fill();
	}
	
	 /**
	  * Returns an object back to the object pool. The data within the object
	  * becomes undefined until it is rebound.
	  * 
	  * @param object - the object to return.
	  */
	public void release(T object) {
		if (mObjects.size() == mSize) {
			Log.e(LOG_TAG, "Attempted to release more objects than should exist.");
		}
		mObjects.add(object);
	}
	
	/**
	 * Binds an object from the object pool. The data of the object is undefined
	 * when first bound.
	 * 
	 * @return the bound object.
	 */
	public T bindObject() {
		if (mObjects.size() == 0) {
			Log.e(LOG_TAG, "Attempted to bind more objects than available.");
			return null;
		}

		return mObjects.remove(mObjects.size() - 1);
	}
	
	
	/**
	 * Fills the object pool with freshly allocated objects.
	 */
	public void fill() {
		for (int i = 0; i < mSize; i++) {
			mObjects.add(createObject());
		}
	}
	
	/**
	 * Overwrite this method to create a new object to put into the pool.
	 * 
	 * @return the newly allocated object.
	 */
	protected abstract T createObject();

	/**
	 * The maximum capacity of the object pool.
	 * 
	 * @return
	 */
	public int capacity() {
		return mSize;
	}
	
	/**
	 * The number of objects remaining in the pool.
	 * @return
	 */
	public int remaining() {
		return mSize - mObjects.size();
	}
}
