package com.gm910.goeturgy.proxy;

import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public interface IProxy {

	public void registerItemRenderer(Item item, int meta, String id);
	
	public MinecraftServer getServer();
	
	public default boolean isClient() {
		return getServer() == null;
	}
	
	public default boolean isServer() {
		return getServer() != null;
	}
	
	public World getWorld(int d);
	
	public default void preInit() {
		
	}
	
	public default void init() {
		
	}
	
	public default void postInit() {
		
	}
}
