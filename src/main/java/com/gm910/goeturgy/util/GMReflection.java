package com.gm910.goeturgy.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

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
		} catch (IllegalAccessException | InstantiationException e) {
			return null;
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e.getCause());
		}
		return obj;
	}
	
	public static <K, T> T runMethod(String name, Class<K> clazz, @Nullable K runner, Class<T> returnType, Object...args) {
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
			return returnType.cast(m.invoke(runner, args));
		} catch (IllegalAccessException e) {
			return null;
		} catch (ClassCastException ex) {
			throw new IllegalArgumentException("Given method return type " + returnType + " does not match actual return type");
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public static <K> void runVoidMethod(String name, Class<K> clazz, @Nullable K runner, Object...args) {
		runMethod(name, clazz, runner, Void.class, args);
	}
	
	@SuppressWarnings("unchecked")
	public static <K, T> T accessField(String name, Class<K> clazz, @Nullable K object, Class<T> fieldType) {
		Field m = null;
		try {
			m = clazz.getDeclaredField(name);
		} catch (NoSuchFieldException | SecurityException e) {
			return null;
		}
		m.setAccessible(true);
		try {
			return (T) m.get(object);
		} catch (IllegalAccessException e) {
			return null;
		} catch (ClassCastException ex) {
			throw new IllegalArgumentException("Given field type " + fieldType + " does not match actual field type");
		}
		
	}
	
	/**
	 * Returns previous field value and sets field value
	 * @param <K>
	 * @param <T>
	 * @param name
	 * @param clazz
	 * @param object
	 * @param fieldValue
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <K, T> T setField(String name, Class<K> clazz, @Nullable K object, T fieldValue) {
		Field m = null;
		T prevVal = null;
		try {
			m = clazz.getDeclaredField(name);
		} catch (NoSuchFieldException | SecurityException e) {
			return null;
		}
		m.setAccessible(true);
		try {
			prevVal = (T) m.get(object);
		} catch (IllegalAccessException e) {
			return null;
		} catch (ClassCastException ex) {
			throw new IllegalArgumentException("Field type for insertion " + fieldValue + " does not match actual field type");
		}
		
		try {
			m.set(object, fieldValue);
		} catch (IllegalAccessException e) {
			return null;
		}
		return prevVal;
		
	}
	
	public static List<Field> getFields(Class<?> clazz, Predicate<? super Field> condition) {
		ArrayList<Field> fields = Lists.newArrayList(clazz.getDeclaredFields());
		fields.removeIf(condition.negate());
		fields.forEach((field) -> field.setAccessible(true));
		return fields;
	}
	
	public static <T> void setEachField(Class<T> clazz, @Nullable T obj, Object value, Predicate<? super Field> condition) {
		List<Field> fields = getFields(clazz, condition);
		fields.forEach((field) -> {
			try {
				field.set(obj, value);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		});
	}
	
}
