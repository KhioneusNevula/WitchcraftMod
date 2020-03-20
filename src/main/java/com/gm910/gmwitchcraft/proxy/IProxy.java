package com.gm910.gmwitchcraft.proxy;

import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public interface IProxy {
	
	public static enum ProxySide {
		CLIENT(true),
		SERVER(false);
		
		private boolean isClient;
		private ProxySide(boolean isClient) {
			this.isClient = isClient;
		}
		
		public boolean isClient() {
			return isClient;
		}
		public boolean isServer() {
			return !isClient;
		}
	}
	
	public void registerItemRenderer(Item item, int meta, String id);

	public void registerVariantRenderer(Item item, int meta, String filename, String id);
	
	public Side getSide();
	
	public default boolean isClient() {
		return getSide().isClient();
	}
	
	public default boolean isServer() {
		return getSide().isServer();
	}
	
	public default MinecraftServer getServer() {
		return null;
	}
	
}