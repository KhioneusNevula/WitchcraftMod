package com.gm910.goeturgy.spells.components;

import java.util.ArrayList;
import java.util.List;

import com.gm910.goeturgy.spells.spellspaces.SpellSpace.SpellInstance;
import com.gm910.goeturgy.spells.util.ISpellComponent;
import com.gm910.goeturgy.tileentities.TileEntityBaseTickable;
import com.gm910.goeturgy.util.NonNullMap;
import com.gm910.goeturgy.util.ServerPos;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

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
	public NonNullMap<EnumFacing, NBTTagCompound> activate(SpellInstance sp, ServerPos modifiedPos, NonNullMap<EnumFacing, NBTTagCompound> map) {
		System.out.println("Pearl block activated");
		return new NonNullMap<>(NBTTagCompound::new);
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
