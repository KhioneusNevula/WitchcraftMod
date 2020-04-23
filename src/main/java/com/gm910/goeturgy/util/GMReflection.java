package com.gm910.goeturgy.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GMReflection {

	public static <T> T construct(Class<T> clazz, Object...args) {
		
		Class<?>[] classes = new Class[args.length];
		for (int i = 0; i < args.length; i++) {
			classes[i] = args[i].getClass();
		}
		Constructor<T> construct = null;
		try {
			construct = clazz.getDeclaredConstructor(classes);
		} catch (NoSuchMethodException | SecurityException e) {
			return null;
		}
		T obj = null;
		construct.setAccessible(true);
		try {
			obj = construct.newInstance(args);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			return null;
		}
		return obj;
	}
	
	public static <K, T> T runMethod(String name, Class<K> clazz, K runner, Class<T> returnType, Object...args) {
		Class<?>[] classes = new Class[args.length];
		for (int i = 0; i < args.length; i++) {
			classes[i] = args[i].getClass();
		}
		Method m = null;
		try {
			m = clazz.getDeclaredMethod(name, classes);
		} catch (NoSuchMethodException | SecurityException e) {
			return null;
		}
		m.setAccessible(true);
		try {
			return (T) m.invoke(runner, args);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			return null;
		}
		
	}

}
