package com.gm910.gmwitchcraft;

import com.gm910.gmwitchcraft.network.WitchNetworkHandler;
import com.gm910.gmwitchcraft.proxy.IProxy;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid=Reference.MODID, name=Reference.NAME, version=Reference.VERSION)
public class WitchcraftMod {
	@Instance
	public static WitchcraftMod instance;
	
	public static final String CLIENT = "com.gm910.gmwitchcraft.proxy.ClientProxy";
    public static final String COMMON = "com.gm910.gmwitchcraft.proxy.ServerProxy";
	
	@SidedProxy(clientSide = CLIENT, serverSide = COMMON)
    public static IProxy proxy;
	
	@EventHandler
	public static void pre(FMLPreInitializationEvent event) {
		WitchNetworkHandler.INSTANCE.registerMessage(WitchNetworkHandler.WitchMessageHandler.class, WitchNetworkHandler.WitchMessage.class, 0, Side.SERVER);
	}
	
	@EventHandler
	public static void init(FMLInitializationEvent event) {
		
	}
	
	@EventHandler
	public static void post(FMLPostInitializationEvent event) {
		
	}
	
	@EventHandler
	public static void serverStarting(FMLServerStartingEvent event) {
		
	}
	
}
