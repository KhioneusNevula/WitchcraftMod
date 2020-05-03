package com.gm910.goeturgy.tileentities;

import com.gm910.goeturgy.spells.ioflow.MagicIO;
import com.gm910.goeturgy.spells.spellspaces.SpellSpace;
import com.gm910.goeturgy.spells.spellspaces.SpellSpaces;
import com.gm910.goeturgy.util.NonNullMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * Includes useful input and output list for spellspace magics and stuff
 * @author borah
 *
 */
public abstract class TileEntityBase extends TileEntity implements ISyncableTile {
	

	
	protected NonNullMap<EnumFacing, NBTTagCompound> inputs = new NonNullMap<EnumFacing, NBTTagCompound>(NBTTagCompound::new);
	protected NonNullMap<EnumFacing, NBTTagCompound> outputs = new NonNullMap<EnumFacing, NBTTagCompound>(NBTTagCompound::new);
	protected long figure;
	
	//protected SpellComponent personalComponent;
	
	public TileEntityBase() {
		
	}
	
	/*public TileEntityBase(SpellComponent personalComponent) {
		this.personalComponent = personalComponent;
	}*/
	
	@Override
	public void onLoad() {
		// TODO Auto-generated method stub
		super.onLoad();
	}
	
	/*public SpellComponent getProvidedComponent() {
		return personalComponent;
	}*/
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		
		NBTTagCompound cmp = super.writeToNBT(compound);
		cmp.setTag("Inp", MagicIO.nonNullMapToNBT(inputs));
		cmp.setTag("Outp", MagicIO.nonNullMapToNBT(outputs));
		cmp.setLong("Figure", this.figure);
		/*if (personalComponent != null) {
			cmp.setTag("Component", personalComponent.writeToNBT(new NBTTagCompound()));
		}*/
		return cmp;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		inputs = MagicIO.nonNullMapFromNBT(compound.getTagList("Inp", NBT.TAG_COMPOUND));
		outputs = MagicIO.nonNullMapFromNBT(compound.getTagList("Outp", NBT.TAG_COMPOUND));
		this.figure = compound.getLong("Figure");
		super.readFromNBT(compound);
		/*if (compound.hasKey("Component")) {
			this.personalComponent = SpellComponent.createForName(compound.getString("Type"));
			personalComponent.readFromNBT(compound);
		}*/
	}
	

	public void setSpellSpace(SpellSpace space) {
		if (space != null) {
			this.figure = space.getID();
		} else {
			this.figure = -1;
		}
	}
	

	public void setSpaceID(long id) {
		this.figure = id;
	}
	public SpellSpace getSpellSpace() {
		return SpellSpaces.get().getById(figure);
	}
	public long getSpaceID() {
		return figure;
	}
	
	public TileEntity getTile() {
		return this.world.getTileEntity(pos);
	}
	
	public void resetInputs() {
		this.inputs.clear();
	}
	
	public void resetOutputs() {
		this.outputs.clear();
	}
	
	
	
	
}
