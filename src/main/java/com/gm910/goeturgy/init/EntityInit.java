package com.gm910.goeturgy.init;

import com.gm910.goeturgy.Goeturgy;
import com.gm910.goeturgy.entity.EntityCelestialBeam;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public class EntityInit {
	
	public static final int CELESTIAL_BEAM_ID = 120;
	
	public static void registerEntities() {
		registerEntity("celestial_beam", EntityCelestialBeam.class, CELESTIAL_BEAM_ID, 50);
	}
	
	public static void registerEntity(String name, Class<? extends Entity> ent, int id, int range, int color1, int color2) {
		EntityRegistry.registerModEntity(new ResourceLocation(Goeturgy.MODID + ":" + name), ent, name, id, Goeturgy.instance, range, 1, true, color1, color2);
	}
	
	public static void registerEntity(String name, Class<? extends Entity> ent, int id, int range) {
		EntityRegistry.registerModEntity(new ResourceLocation(Goeturgy.MODID + ":" + name), ent, name, id, Goeturgy.instance, range, 1, true);
	}
}
