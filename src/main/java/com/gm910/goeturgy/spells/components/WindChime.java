package com.gm910.goeturgy.spells.components;

import java.util.ArrayList;
import java.util.List;

import com.gm910.goeturgy.spells.ioflow.MagicIO;
import com.gm910.goeturgy.spells.spellspaces.SpellSpace.Spell;
import com.gm910.goeturgy.spells.util.ISpellComponent;
import com.gm910.goeturgy.tileentities.TileEntityBaseTickable;
import com.gm910.goeturgy.util.NonNullMap;
import com.gm910.goeturgy.util.ServerPos;
import com.google.common.collect.Lists;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class WindChime extends TileEntityBaseTickable implements ISpellComponent {

	public static int DEFAULT_RADIUS = 5;
	
	@Override
	public boolean accepts(EnumFacing facing, NBTTagCompound comp) {
		if (comp.hasKey(MagicIO.INT) && !MagicIO.has(MagicIO.INT, inputs)) {
			return true;
		}
		return false;
	}

	@Override
	public NonNullMap<EnumFacing, NBTTagCompound> getInputs() {
		
		return inputs;
	}

	@Override
	public boolean putInput(NonNullMap<EnumFacing, NBTTagCompound> inputs) {
		this.inputs = inputs;
		return true;
	}

	public int getRadiusFrom(NonNullMap<EnumFacing, NBTTagCompound> inputs) {
		int radius = DEFAULT_RADIUS;
		for (EnumFacing f : inputs.keySet()) {
			if (inputs.get(f).hasKey(MagicIO.INT)) {
				radius = MagicIO.getInteger(inputs.get(f));
			}
		}
		return radius;
	}
	
	@Override
	public NonNullMap<EnumFacing, NBTTagCompound> activate(Spell sp, ServerPos modifiedPos, NonNullMap<EnumFacing, NBTTagCompound> inputs) {
		NonNullMap<EnumFacing, NBTTagCompound> outputs = new NonNullMap<>(NBTTagCompound::new);
		
		int radius = getRadiusFrom(inputs);
		
		System.out.println("Wind chime activating with radius " + radius);
		List<EntityLivingBase> entities = new ArrayList<>();
		{
			final int temp = radius;
			entities = modifiedPos.getWorld().getEntities(EntityLivingBase.class, (e) -> {
				
				return e.getDistanceSq(modifiedPos) <= temp * temp;
			});
		}
		List<ServerPos> poses = new ArrayList<>();
		entities.forEach((e) -> {
			poses.add(new ServerPos(e));
		});
		
		for (EnumFacing f : EnumFacing.VALUES) {
			MagicIO.writePhysicalEntityListToCompound(entities, outputs.get(f));
			//MagicIO.writePosListToCompound(poses, outputs.get(f));
		}
		
		return outputs;
	}

	@Override
	public NonNullMap<EnumFacing, List<String>> getPossibleReturns(NonNullMap<EnumFacing, List<String>> input) {
		NonNullMap<EnumFacing, List<String>> rets = new NonNullMap<>(ArrayList<String>::new);
		for (EnumFacing f : EnumFacing.VALUES) {
			rets.put(f,  Lists.newArrayList(MagicIO.toList(MagicIO.ENTITY)/*, MagicIO.toList(MagicIO.POS)*/));

		}
		return rets;
	}

	@Override
	public int getRequiredPower(NonNullMap<EnumFacing, List<String>> tagsForSide) {
		
		return 2 ;
	}

	@Override
	public int getRequiredPowerFromNBT(NonNullMap<EnumFacing, NBTTagCompound> tagsForSide, ServerPos modifiedPos) {
		// TODO Auto-generated method stub
		return 2 * getRadiusFrom(tagsForSide);
	}

	@Override
	public int getRequiredPower() {
		// TODO Auto-generated method stub
		return 2;
	}
	
	

}
