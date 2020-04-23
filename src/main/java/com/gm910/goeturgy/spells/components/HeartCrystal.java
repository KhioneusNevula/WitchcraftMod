package com.gm910.goeturgy.spells.components;

import java.util.ArrayList;
import java.util.List;

import com.gm910.goeturgy.spells.ioflow.MagicIO;
import com.gm910.goeturgy.spells.spellspaces.SpellSpace;
import com.gm910.goeturgy.spells.util.ISpellChainListener;
import com.gm910.goeturgy.spells.util.ISpellComponent;
import com.gm910.goeturgy.tileentities.TileEntityBaseTickable;
import com.gm910.goeturgy.util.GMNBT;
import com.gm910.goeturgy.util.NonNullMap;

import akka.japi.Pair;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;

public class HeartCrystal extends TileEntityBaseTickable implements ISpellComponent, ISpellChainListener {

	private NBTTagList data = null;
	private int index = -1;
	private String tagType = null;
	private List<BlockPos> listening = new ArrayList<BlockPos>();
	boolean waitingToActivate = false;
	
	@Override
	public boolean accepts(EnumFacing facing, NBTTagCompound comp) {
		if (MagicIO.hasList(comp) && !MagicIO.hasList(inputs)) {
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

	public Pair<String, NBTTagList> getListToIterate(NonNullMap<EnumFacing, NBTTagCompound> inputs) {
	
		for (String s : MagicIO.TAG_TYPES) {
			for (EnumFacing f : EnumFacing.VALUES) {
				if (!inputs.get(f).hasKey(s)) continue;
				NBTTagList ls = inputs.get(f).getTagList(s, MagicIO.LIST_TYPE_FOR_TAG.get(s));
				return new Pair<>(s, ls);
				
			}
		}
		return new Pair<>(null, null);
	}
	
	public void end() {
		index = -1;
		data = null;
		tagType = null;
	}
	
	@Override
	public NonNullMap<EnumFacing, NBTTagCompound> activate(NonNullMap<EnumFacing, NBTTagCompound> inputs) {
		NonNullMap<EnumFacing, NBTTagCompound> outputs = new NonNullMap<>(NBTTagCompound::new);
		if (index == -1) {
			Pair<String, NBTTagList> pa = getListToIterate(inputs);
			this.tagType = pa.first();
			this.data = pa.second();
			if (data == null || tagType == null) {
				end();
				return null;
			} else if (data.hasNoTags()){
				end();
				return outputs;
			}
			
			makeOutputs(0, outputs);
			waitingToActivate = true;
			//this.getSpellSpace().markForActivationNextTick(this.pos, null);
			index = 1;
		} else {
			if (this.data == null || this.tagType == null) {
				System.err.println("Data is " + data + " and tagType is " + tagType + ", this is a problem");
				end();
				return outputs;
			}
			if (this.data.tagCount() <= this.index) {
				end();
				return outputs;
			}
			
			makeOutputs(index, outputs);
			//this.getSpellSpace().markForActivationNextTick(pos, null);
			waitingToActivate = true;
			index++;
			
		}
		
		return outputs;
	}
	
	public NonNullMap<EnumFacing, NBTTagCompound> makeOutputs(int index, NonNullMap<EnumFacing, NBTTagCompound> outputs) {
		outputs.forEach((f) -> {
			NBTTagCompound cmp = new NBTTagCompound();
			MagicIO.write(tagType, data.get(index), cmp);
			
			return cmp;
		}, EnumFacing.VALUES);
		return outputs;
	}

	@Override
	public NonNullMap<EnumFacing, List<String>> getPossibleReturns(NonNullMap<EnumFacing, List<String>> input) {
		NonNullMap<EnumFacing, List<String>> rets = new NonNullMap<>(ArrayList<String>::new);
		for (EnumFacing f : EnumFacing.VALUES) {
			List<String> ls = new ArrayList<>();
			for (String tag : MagicIO.TAG_TYPES) {
				ls.add(MagicIO.toList(tag));
			}
			rets.put(f,  ls);
		}
		return rets;
	}

	@Override
	public int getRequiredPower(NonNullMap<EnumFacing, List<String>> tagsForSide) {
		
		return 2 ;
	}

	@Override
	public int getRequiredPowerFromNBT(NonNullMap<EnumFacing, NBTTagCompound> tagsForSide) {
		// TODO Auto-generated method stub
		return 2 * (this.getListToIterate(tagsForSide).second() != null ? this.getListToIterate(tagsForSide).second().tagCount() : 0);
	}

	@Override
	public int getRequiredPower() {
		// TODO Auto-generated method stub
		return 2;
	}
	
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		
		compound.setInteger("index", index);
		if (tagType != null) {
			compound.setString("tagType", tagType);
			if (data != null) {
				compound.setTag("data", data);
			}
		}
		compound.setBoolean("dirty", waitingToActivate);
		compound.setTag("Listening", GMNBT.makePosList(listening));
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		if (compound.hasKey("tagType")) {
			tagType = compound.getString("tagType");
			if (compound.hasKey("data")) {
				this.data = compound.getTagList("data", MagicIO.LIST_TYPE_FOR_TAG.get(tagType));
			}
		}
		this.index = compound.getInteger("index");
		this.waitingToActivate = compound.getBoolean("dirty");
		this.listening = GMNBT.createPosList(compound.getTagList("Listening", NBT.TAG_COMPOUND));
		super.readFromNBT(compound);
	}

	@Override
	public void activated(SpellSpace space, BlockPos pos) {
		
	}

	@Override
	public void finished(SpellSpace space, BlockPos pos, boolean success) {
		if (success && waitingToActivate) {
			space.markForActivationNextTick(pos, null);
			this.waitingToActivate = false;
		}
	}

	@Override
	public void addToChain(SpellSpace space, BlockPos pos) {
		this.listening.add(pos);
	}

	@Override
	public boolean isPartOfChain(SpellSpace space, BlockPos pos) {
		// TODO Auto-generated method stub
		return listening.contains(pos);
	}
	

}
