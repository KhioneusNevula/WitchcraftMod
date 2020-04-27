package com.gm910.goeturgy.util;

import java.util.HashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class NonNullMap<T, K> extends HashMap<T, K>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1125277270772665756L;
	
	public final Function<T, K> supplier;
	
	public NonNullMap(Function<T, K> sup) {
		supplier = sup;
	}
	
	/**
	 * Generates using the supplier for the given values
	 * @param vals
	 * @return
	 */
	public NonNullMap<T, K> generateValues(T...vals) {
		for (T t : vals) {
			this.put(t, supplier.apply(t));
		}
		return this;
	}
	
	public NonNullMap(Supplier<K> sup) {
		this((n) -> sup.get());
	}
	
	public static <T,K> NonNullMap<T, K> create(Function<T, K> sup) {
		return new NonNullMap<T, K> (sup);
	}
	
	public static <T,K> NonNullMap<T, K> create(Supplier<K> sup) {
		return new NonNullMap<T, K> (sup);
	}
	
	public static <T,K> NonNullMap<T, K> create(K sup) {
		return new NonNullMap<T, K> (sup);
	}
	
	public static <T,K> NonNullMap<T, K> create(Class<K> sup, Object...args) {
		return new NonNullMap<T, K> (() -> GMReflection.construct(sup, args));
	}
	
	public NonNullMap(NonNullMap<T, K> other) {
		super(other);
		this.supplier = other.supplier;
	}
	
	public NonNullMap(K value) {
		this(() -> value);
	}
	
	@Override
	public K get(Object key) {
		if (super.get(key) == null) {
			try {
				super.put((T) key, supplier.apply((T)key));
			} catch (ClassCastException e) {
				return super.get(key);
			}
		}
		return super.get(key);
	}
	
	public Function<T, K> getSupplier() {
		return supplier;
	}
	
	
	public K generateDefaultValue(T key) {
		return supplier.apply(key);
	}
	
	/**
	 * Put one value supplied by the given function for each of these keys
	 * @param forEach
	 * @param values
	 * @return
	 */
	public NonNullMap<T, K> forEach(Function<T, K> forEach, T...keys) {
		for (T key : keys) {
			this.put(key, forEach.apply(key));
		}
		
		return this;
	}
	

}
