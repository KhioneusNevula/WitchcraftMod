package com.gm910.goeturgy.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import akka.japi.Pair;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;

public class GMNBT {

	public static <T> NBTTagList makeList(Iterable<T> iter, Function<T, NBTBase> serializer) {
		NBTTagList ls = new NBTTagList();
		for (T ob : iter) {
			ls.appendTag(serializer.apply(ob));
		}
		return ls;
	}
	
	
	public static NBTTagList makePosList(Iterable<BlockPos> iter) {
		return makeList(iter, (s) -> (ServerPos.toNBT(s)));
	}
	
	public static void forEach(NBTTagList ls, Consumer<NBTBase> cons) {
		for (NBTBase base : ls) {
			cons.accept(base);
		}
	}
	
	public static <T> List<T> createList(NBTTagList ls, Function<NBTBase, T> func) {
		List<T> lws = new ArrayList<>();
		if (ls == null) {
			throw new IllegalArgumentException("Null NBTTagList");
		}
		for (NBTBase b : ls) {
			lws.add(func.apply(b));
		}
		return lws;
	}
	
	public static List<BlockPos> createPosList(NBTTagList ls) {
		return createList(ls, (b) -> NBTUtil.getPosFromTag((NBTTagCompound)b));
	}
	
	public static List<BlockPos> createServerPosList(NBTTagList ls) {
		return createList(ls, (b) -> ServerPos.fromNBT((NBTTagCompound)b));
	}
	
	public static <T, K> Map<T, K> createMap(NBTTagList ls, Function<NBTBase, T> keyFunc, Function<NBTBase, K> valFunc) {
		Map<T, K> lws = new HashMap<>();
	
		for (NBTBase b : ls) {
			
			lws.put(keyFunc.apply(b), valFunc.apply(b));
		}
		return lws;
	}
	
	public static <T, K> Map<T, K> createMap(NBTTagList ls, Function<NBTBase, Pair<T, K>> func) {
		Map<T, K> lws = new HashMap<>();
		
		for (NBTBase b : ls) {
			
			Pair<T, K> p = func.apply(b);
			
			lws.put(p.first(), p.second());
		}
		return lws;
	}
	
}
