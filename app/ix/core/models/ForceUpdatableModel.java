package ix.core.models;
/*
 * This is just to have a forcable model update
 * that signals ebean that it should actually trigger
 * an update, even if it doesn't think anything has changed.
 * 
 * There may be a way to do this with native ebeans, but it
 * is not immediately apparent
 * 
 * 
 * If you implement ForceUpdatableModel, you should also
 * have some some private field that will be changed
 * when forceUpdate is called, as well as a call to 
 * super.update
 */
public interface ForceUpdatableModel {
	public void forceUpdate();
	public boolean tryUpdate();
}
