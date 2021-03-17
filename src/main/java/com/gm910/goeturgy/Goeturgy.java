package com.gm910.goeturgy;

import org.apache.logging.log4j.Logger;

import com.gm910.goeturgy.capability.CapabilityHandler;
import com.gm910.goeturgy.init.TileInit;
import com.gm910.goeturgy.keyhandling.ModKey;
import com.gm910.goeturgy.messages.Messages;
import com.gm910.goeturgy.messages.Messages.ClientMessageHandler;
import com.gm910.goeturgy.messages.Messages.ServerMessageHandler;
import com.gm910.goeturgy.messages.Messages.TaskMessage;
import com.gm910.goeturgy.proxy.IProxy;
import com.gm910.goeturgy.spells.spellspaces.SpellSpaces;
import com.gm910.goeturgy.world.DimensionData;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber
@Mod(modid = Goeturgy.MODID, name = Goeturgy.NAME, version = Goeturgy.VERSION)
public class Goeturgy
{
    public static final String MODID = "goeturgy";
    public static final String NAME = "Goeturgy Mod";
    public static final String VERSION = "1.0";
    public DimensionData dimensionData;
    public SpellSpaces spellSpaces;
    
    public int tickCount = 0;
    
    @Instance
    public static Goeturgy instance;
    
    public static final String COMMON_PROXY = "com.gm910.goeturgy.proxy.ServerProxy";

    public static final String CLIENT_PROXY = "com.gm910.goeturgy.proxy.ClientProxy";
    
    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = COMMON_PROXY)
    public static IProxy proxy;
    
    private static Logger logger;
    
    public Goeturgy() {

        Messages.INSTANCE.registerMessage(ClientMessageHandler.class, TaskMessage.class, Messages.returnNewID(), Side.CLIENT);
        Messages.INSTANCE.registerMessage(ServerMessageHandler.class, TaskMessage.class, Messages.returnNewID(), Side.SERVER);
	}
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	proxy.preInit();

    	DimensionData.registerInitialProviders();
        logger = event.getModLog();
        TileInit.registerTileEntities();
        MinecraftForge.EVENT_BUS.register(this);
        CapabilityHandler.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
		ModKey.init();
        proxy.init();
        
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    	proxy.postInit();
    }
    
    @EventHandler
    public void serverLoad(FMLServerStartedEvent event) {
    	MinecraftServer serv = FMLCommonHandler.instance().getMinecraftServerInstance();
    	if (serv == null) return;
    	DimensionData dat = DimensionData.get(serv.getWorld(0));
    	dat.registerInitialDimensions();
    	spellSpaces = SpellSpaces.get();
    	MinecraftForge.EVENT_BUS.register(spellSpaces);
    	MinecraftForge.EVENT_BUS.register(dat);
    	dimensionData = dat;
    	tickCount = 0;
    }
    
    @SubscribeEvent
    public void update(ServerTickEvent event) {
    	tickCount++;
    	if (proxy.isServer()) proxy.tick();
    }
    
    @SubscribeEvent
    public void update(ClientTickEvent event) {
    	tickCount++;
    	
    }
    
    
    @EventHandler
    public void serverUnload(FMLServerStoppedEvent event) {
    	if (dimensionData != null) {
    		for (DimensionType ty : dimensionData.dimensions.values()) {
    			DimensionManager.unregisterDimension(ty.getId());
    		}
    		dimensionData = null;
    	}
    	spellSpaces = null;
    	tickCount = 0;
    }
    
    
}
