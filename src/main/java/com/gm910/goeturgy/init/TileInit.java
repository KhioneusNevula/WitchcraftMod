package com.gm910.goeturgy.init;

import java.util.HashMap;
import java.util.Map;

import com.gm910.goeturgy.Goeturgy;
import com.gm910.goeturgy.spells.bordermakers.spell_dust.TileSpellDust;
import com.gm910.goeturgy.spells.bordermakers.spell_dust.TileSpellDustHead;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class TileInit {
	
	public static final Map<String, Class<? extends TileEntity>> TILES = new HashMap<>();

	public static void registerTileEntities() {
		//register(TileSpellDust.class, "spell_dust");
		//register(TileSpellDustHead.class, "spell_dust_head");
		for (String clazz : TILES.keySet()) {
			
			register(TILES.get(clazz), clazz);
		}
	}
	
	public static void register(Class<? extends TileEntity> clazz, String name) {
		System.out.println("Registered " + name);
		GameRegistry.registerTileEntity(clazz, new ResourceLocation(Goeturgy.MODID, name));
	}
}
