package com.gm910.goeturgy.spells.ioflow;

import java.util.function.Function;
import java.util.function.Supplier;

import com.gm910.goeturgy.util.NonNullMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class SpellIOMap extends NonNullMap<EnumFacing, NBTTagCompound> {


	/**
	 * 
	 */
	private static final long serialVersionUID = -6284701729591819782L;

	public SpellIOMap(Function<EnumFacing, NBTTagCompound> sup) {
		super(sup);
		// TODO Auto-generated constructor stub
	}
	
	public SpellIOMap(Supplier<NBTTagCompound> sup) {
		super(sup);
		// TODO Auto-generated constructor stub
	}
	
	public SpellIOMap(NBTTagCompound sup) {
		super(sup);
		// TODO Auto-generated constructor stub
	}
	
	public SpellIOMap() {
		super(NBTTagCompound::new);
	}
	
	public SpellIOMap(NonNullMap<EnumFacing, NBTTagCompound> map) {
		super(map.supplier);
		this.putAll(map);
	}

}
