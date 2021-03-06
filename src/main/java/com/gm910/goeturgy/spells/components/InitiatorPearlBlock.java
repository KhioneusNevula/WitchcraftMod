package com.gm910.goeturgy.spells.components;

import java.util.ArrayList;
import java.util.List;

import com.gm910.goeturgy.spells.ioflow.MagicIO;
import com.gm910.goeturgy.spells.spellspaces.SpellSpace.Spell;
import com.gm910.goeturgy.spells.util.ISpellComponent;
import com.gm910.goeturgy.tileentities.TileEntityBaseTickable;
import com.gm910.goeturgy.util.NonNullMap;
import com.gm910.goeturgy.util.ServerPos;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

/**
 * A pearl block initiates the execution of a spell and is a basic starter component. Can be linked to a pearl item to execute the spell where the pearl is used
 * @author borah
 *
 */
public class InitiatorPearlBlock extends TileEntityBaseTickable implements ISpellComponent {

	@Override
	public void update() {
		//System.out.println("Tile pearl block");
		
		super.update();
	}
	
	@Override
	public boolean accepts(EnumFacing facing, NBTTagCompound comp) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean isOutput(EnumFacing face) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public NonNullMap<EnumFacing, NBTTagCompound> getInputs() {
		// TODO Auto-generated method stub
		return new NonNullMap<>(NBTTagCompound::new);
	}

	@Override
	public boolean putInput(NonNullMap<EnumFacing, NBTTagCompound> inputs) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public NonNullMap<EnumFacing, NBTTagCompound> activate(Spell sp, ServerPos modifiedPos, NonNullMap<EnumFacing, NBTTagCompound> map) {
		System.out.println("Pearl block activated");
		
		return new NonNullMap<EnumFacing, NBTTagCompound>(() -> {
			NBTTagCompound cmp = new NBTTagCompound();
			MagicIO.writePosToCompound(modifiedPos, cmp);
			
			return cmp;
		}).generateValues(EnumFacing.VALUES);
	}
	
	@Override
	public NonNullMap<EnumFacing, NBTTagCompound> activate(Spell spell, Entity modifiedEntity,
			NonNullMap<EnumFacing, NBTTagCompound> inputs) {
		System.out.println("Pearl block activated");
		return new NonNullMap<EnumFacing, NBTTagCompound>(() -> {
			NBTTagCompound cmp = new NBTTagCompound();
			MagicIO.writeEntityToCompound(modifiedEntity, cmp);
			
			return cmp;
		}).generateValues(EnumFacing.VALUES);
	}

	@Override
	public NonNullMap<EnumFacing, List<String>> getPossibleReturns(NonNullMap<EnumFacing, List<String>> input) {
		// TODO Auto-generated method stub
		return new NonNullMap<>(ArrayList<String>::new);
	}

	@Override
	public int getRequiredPower(NonNullMap<EnumFacing, List<String>> tagsForSide) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getRequiredPowerFromNBT(NonNullMap<EnumFacing, NBTTagCompound> tagsForSide, ServerPos modifiedPos) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getRequiredPower() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isStarter() {
		// TODO Auto-generated method stub
		return true;
	}
	

}
