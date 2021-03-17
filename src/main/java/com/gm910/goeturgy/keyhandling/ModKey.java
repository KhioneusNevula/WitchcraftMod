package com.gm910.goeturgy.keyhandling;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import com.gm910.goeturgy.Goeturgy;
import com.gm910.goeturgy.keyhandling.conflict.GodModeKeyConflictContext;
import com.gm910.goeturgy.messages.Messages;
import com.gm910.goeturgy.util.GMReflection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

@EventBusSubscriber
public enum ModKey {
	

	BECOME_GOD,
	EXIT_GOD,
	USE_GOD_POWER,
	CYCLE_LEFT,
	CYCLE_RIGHT;
	
    public static IKeyConflictContext GOD_MODE_CONTEXT = new GodModeKeyConflictContext();
    
    public static IKeyConflictContext UNIVERSAL_CONTEXT = new GodModeKeyConflictContext.GodModeConsiderateUniversal();
    
    public static KeyBinding[] CONFLICT_GOD_MODE;
    
    
    /**
     * Keys
     */
    public static final Map<ModKey, KeyBinding> keys = new HashMap<>();
    
    public static void init() {
    	Minecraft mc = Minecraft.getMinecraft();
    	GameSettings g = mc.gameSettings;
    	CONFLICT_GOD_MODE = new KeyBinding[] {
    		g.keyBindDrop,
    		g.keyBindLoadToolbar,
    		g.keyBindSaveToolbar
    	};
    	
		keys.put(BECOME_GOD, new KeyBinding("god.mode.become", UNIVERSAL_CONTEXT, Keyboard.KEY_G, "god.mode"));
		keys.put(EXIT_GOD, new KeyBinding("god.mode.exit", GOD_MODE_CONTEXT, Keyboard.KEY_X, "god.mode"));
		keys.put(USE_GOD_POWER, new KeyBinding("god.mode.use", GOD_MODE_CONTEXT, Keyboard.KEY_M, "god.mode"));
		keys.put(CYCLE_LEFT, new KeyBinding("god.mode.left", GOD_MODE_CONTEXT, Keyboard.KEY_LBRACKET, "god.mode"));
		keys.put(CYCLE_RIGHT, new KeyBinding("god.mode.right", GOD_MODE_CONTEXT, Keyboard.KEY_RBRACKET, "god.mode"));
		
		configureGameKeys();
		
    }
    
    public static void configureGameKeys() {
    	Minecraft mc = Minecraft.getMinecraft();
    	GameSettings g = mc.gameSettings;
    	List<Field> keyBinds = GMReflection.getFields(GameSettings.class, (field) -> field.getName().startsWith("keyBind") && field.getType().equals(KeyBinding.class));
    	for (int i = 0; i < keyBinds.size() ; i++) {
    		Field f = keyBinds.get(i);
    		KeyBinding kb = GMReflection.accessField(f.getName(), GameSettings.class, g, KeyBinding.class);
    		if (kb.getKeyConflictContext() == KeyConflictContext.UNIVERSAL) {
    			kb.setKeyConflictContext(UNIVERSAL_CONTEXT);
    		}
    	}
    }
    
    @EventBusSubscriber
    public static class ModKeyEventHandler {

	    @SubscribeEvent
	    public static void key(ClientTickEvent event) {
	
			//System.out.println(event + " " + Goeturgy.proxy.getAsClientProxy().isGodMode);
	    	if (Goeturgy.proxy.getAsClientProxy().isGodMode) {
	    		for (KeyBinding k : CONFLICT_GOD_MODE) {
	    			if (k.isKeyDown()) {
	    				System.out.println(k + " unpressed");
	    				GMReflection.runVoidMethod("unpressKey", KeyBinding.class, k);
	    			}
	    		}
	    	}
	    	
	    	keys.values().forEach((key) -> {
	    		if (key.isKeyDown()) {
	    			System.out.println(key.getDisplayName() + " DOWN#$%^ " + Goeturgy.proxy.getAsClientProxy().isGodMode);
	    			Messages.pressKey(key.getKeyCode());
	    		} else {
	    			if (!Minecraft.getMinecraft().isGamePaused()) {
	    				//System.out.println(key.getDisplayName() + " notdown " + Goeturgy.proxy.getAsClientProxy().isGodMode);
	    			}
	    		}
	    	});
	    }
    }
}
