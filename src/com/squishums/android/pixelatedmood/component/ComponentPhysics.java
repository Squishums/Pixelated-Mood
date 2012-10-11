package com.squishums.android.pixelatedmood.component;


/**
 * Component for data relating to the movement of the entity. The velocity is
 * used by all components that impart accelleration onto the entity.
 */
public class ComponentPhysics extends PixelatedComponent {

	// How strongly the wind effects the entity. This is mulitplied by the
	// wind strength and added to the entity's position.
	public float windFactorX;
	public float windFactorY;
	// The velocity for the entity. The velocity is added to the entity's
	// position every game tick, but is also dampened. The dampening factor
	// is set in the set's preferences.
	public float veloX = 0;
	public float veloY = 0;
	
	public ComponentPhysics() { }
}
