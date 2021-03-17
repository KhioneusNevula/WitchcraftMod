package com.gm910.goeturgy.proxy;

import com.gm910.goeturgy.init.TileInit;
import com.gm910.goeturgy.keyhandling.ModKey;
import com.gm910.goeturgy.registering.RenderHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class ClientProxy implements IProxy {
	
	public boolean isGodMode;

	@Override
	public void registerItemRenderer(Item item, int meta, String id) {
		ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), id));
	}
	
	@Override
	public MinecraftServer getServer() {
		// TODO Auto-generated method stub
		return FMLCommonHandler.instance().getMinecraftServerInstance();
	}

	@Override
	public World getWorld(int d) {
		// TODO Auto-generated method stub
		return Minecraft.getMinecraft().world != null ? (Minecraft.getMinecraft().world.provider.getDimension() == d ? Minecraft.getMinecraft().world : null) : null;
	}
	
	@Override
	public void preInit() {
		// TODO Auto-generated method stub
		
		TileInit.registerSpecialRenderers();
		RenderHandler.registerEntRenderers();
	}
	
	@Override
	public void init() {
	    
        for (ModKey key : ModKey.keys.keySet()) {
        	ClientRegistry.registerKeyBinding(ModKey.keys.get(key));
        }
	}
}
