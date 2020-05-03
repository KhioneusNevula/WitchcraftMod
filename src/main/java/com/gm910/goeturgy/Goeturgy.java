package com.gm910.goeturgy;

import org.apache.logging.log4j.Logger;

import com.gm910.goeturgy.init.TileInit;
import com.gm910.goeturgy.messages.Messages;
import com.gm910.goeturgy.messages.Messages.ClientMessageHandler;
import com.gm910.goeturgy.messages.Messages.IRunnableTaskMessage;
import com.gm910.goeturgy.messages.Messages.ServerMessageHandler;
import com.gm910.goeturgy.proxy.IProxy;
import com.gm910.goeturgy.spells.spellspaces.SpellSpaces;
import com.gm910.goeturgy.world.DimensionData;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.relauncher.Side;
import scala.reflect.runtime.ThreadLocalStorage.MyThreadLocalStorage;

@EventBusSubscriber
@Mod(modid = Goeturgy.MODID, name = Goeturgy.NAME, version = Goeturgy.VERSION)
public class Goeturgy
{
    public static final String MODID = "goeturgy";
    public static final String NAME = "Goeturgy Mod";
    public static final String VERSION = "1.0";
    public DimensionData temp;
    
    @Instance
    public static Goeturgy instance;
    
    public static final String COMMON_PROXY = "com.gm910.goeturgy.proxy.ServerProxy";

    public static final String CLIENT_PROXY = "com.gm910.goeturgy.proxy.ClientProxy";
    
    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = COMMON_PROXY)
    public static IProxy proxy;
    
    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	proxy.preInit();

    	DimensionData.registerInitialProviders();
        logger = event.getModLog();
        TileInit.registerTileEntities();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init();
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    	proxy.postInit();
    }
    
    @EventHandler
    public void worldLoad(FMLServerStartedEvent event) {
    	MinecraftServer serv = FMLCommonHandler.instance().getMinecraftServerInstance();
    	if (serv == null) return;
    	DimensionData dat = DimensionData.get(serv.getWorld(0));
    	dat.registerInitialDimensions();
    	SpellSpaces.get();
    	temp = dat;
    }
    
    @EventHandler
    public void serverUnload(FMLServerStoppedEvent event) {
    	if (temp != null) {
    		for (DimensionType ty : temp.dimensions.values()) {
    			DimensionManager.unregisterDimension(ty.getId());
    		}
    	}
    }
    
}
