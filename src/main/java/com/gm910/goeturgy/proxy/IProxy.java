package com.gm910.goeturgy.proxy;

import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public interface IProxy {

	public void registerItemRenderer(Item item, int meta, String id);
	
	public MinecraftServer getServer();
	
	public default boolean isClient() {
		return this instanceof ClientProxy;
	}
	
	public default ServerProxy getAsServerProxy() {
		return isServer() ? (ServerProxy) this : null;
	}
	
	public default ClientProxy getAsClientProxy() {
		return isClient() ? (ClientProxy) this : null;
	}
	
	public default boolean isServer() {
		return this instanceof ServerProxy;
	}
	
	public World getWorld(int d);
	
	public default void preInit() {
		
	}
	
	public default void init() {
		
	}
	
	public default void postInit() {
		
	}
	
	public default void tick() {
		
	}
}
