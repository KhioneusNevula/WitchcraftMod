package com.gm910.goeturgy.capability;

public interface ICapabilityWithOwner<T> {

	public void setOwner(T newe);
	public T getOwner();
}
