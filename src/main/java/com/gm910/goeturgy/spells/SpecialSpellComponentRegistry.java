package com.gm910.goeturgy.spells;

import java.util.HashMap;
import java.util.Map;

import com.gm910.goeturgy.spells.util.ISpellComponent;

import net.minecraft.tileentity.TileEntity;

public class SpecialSpellComponentRegistry {
	
	public static final Map<Class<? extends TileEntity>,  ISpellComponent> MAP = new HashMap<>();

	public static void register(Class<? extends TileEntity> clazz, ISpellComponent comp) {
		MAP.put(clazz, comp);
	}
	
	public static ISpellComponent get(Class<? extends TileEntity> clazz) {
		return MAP.get(clazz);
	}

}
