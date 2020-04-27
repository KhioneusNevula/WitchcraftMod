package com.gm910.goeturgy.spells.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.gm910.goeturgy.spells.spellspaces.SpellSpace.SpellInstance;
import com.gm910.goeturgy.spells.util.ISpellComponent;
import com.gm910.goeturgy.tileentities.TileEntityBaseTickable;
import com.gm910.goeturgy.util.DrawEffects;
import com.gm910.goeturgy.util.DrawEffects.RenderBlockShape;
import com.gm910.goeturgy.util.GMNBT;
import com.gm910.goeturgy.util.NonNullMap;
import com.gm910.goeturgy.util.ServerPos;

import akka.japi.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.Constants.NBT;

public class MagicWire extends TileEntityBaseTickable implements ISpellComponent {
	
	public static enum SideState {
		INPUT,
		OUTPUT,
		NONE;
		public static SideState fromName(String name) {
			for (SideState state : values()) {
				if (state.name().equals(name)) {
					return state;
				}
			}
			return null;
		}
	}

	private Map<EnumFacing, SideState> sides = (new NonNullMap<EnumFacing, SideState>(SideState.NONE)).generateValues(EnumFacing.VALUES);
	
	@Override
	public boolean accepts(EnumFacing facing, NBTTagCompound comp) {
		boolean finalReturn = true;
		if (comp.hasNoTags()) {
			if (world.getTileEntity(pos.offset(facing)) instanceof ISpellComponent) {
				finalReturn = ((ISpellComponent)world.getTileEntity(pos.offset(facing))).isStarter();
			} else if (world.getTileEntity(pos.offset(facing)) instanceof MagicWire && 
					((MagicWire)world.getTileEntity(pos.offset(facing))).isOutput(facing.getOpposite())) {
				
				finalReturn = true;
			} else {
				finalReturn = false;
			}
		}
		return finalReturn && this.isInput(facing);
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

	
	@Override
	public NonNullMap<EnumFacing, NBTTagCompound> activate(SpellInstance sp, ServerPos modifiedPos, NonNullMap<EnumFacing, NBTTagCompound> inputs) {
		NonNullMap<EnumFacing, NBTTagCompound> outputs = new NonNullMap<>(NBTTagCompound::new);
		
		EnumFacing inputSide = this.getInputSide();
		
		List<EnumFacing> outputSides = this.getOutputSides();
		
		if (inputSide == null) return null;
		
		if (outputSides.isEmpty()) return null;
		
		for (EnumFacing f : outputSides) {
			
			outputs.put(f, inputs.get(inputSide));
		}
		
		/*for (EnumFacing f : inputs.keySet()) {
			outputs.put(f.getOpposite(), inputs.get(f).copy());
		}*/
		
		return outputs;
	}

	@Override
	public NonNullMap<EnumFacing, List<String>> getPossibleReturns(NonNullMap<EnumFacing, List<String>> input) {
		NonNullMap<EnumFacing, List<String>> rets = new NonNullMap<>(ArrayList<String>::new);
		
		return rets;
	}

	@Override
	public int getRequiredPower(NonNullMap<EnumFacing, List<String>> tagsForSide) {
		
		return 0 ;
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
	
	public void setStateForSide(EnumFacing f, SideState state) {
		if (state == SideState.INPUT) {
			if (!sides.containsValue(state)) {
				this.sides.put(f, state);
			}
		} else {
			this.sides.put(f, state);
		}
		sync();
	}
	
	public SideState getStateForSide(EnumFacing f) {
		return this.sides.get(f);
	}
	
	public boolean isInput(EnumFacing f) {
		return this.getStateForSide(f) == SideState.INPUT;
	}
	
	public boolean isOutput(EnumFacing f) {
		return this.getStateForSide(f) == SideState.OUTPUT;
	}
	
	public boolean isNeitherInputNorOutput(EnumFacing f) {
		return this.getStateForSide(f) == SideState.NONE;
	}
	
	public void makeInput(EnumFacing f) {
		this.setStateForSide(f, SideState.INPUT);
	}
	
	public void resetInputSide() {
		if (this.getInputSide() != null) {
			this.setStateForSide(this.getInputSide(), SideState.NONE);
		}
	}
	
	public void makeOutput(EnumFacing f) {
		this.setStateForSide(f, SideState.OUTPUT);
	}
	
	public void makeNeitherInputNorOutput(EnumFacing f) {
		this.setStateForSide(f, SideState.NONE);
	}
	
	public SideState toggle(EnumFacing f, SideState otherVal) {
		if (this.getStateForSide(f) == otherVal) {
			this.setStateForSide(f, SideState.NONE);
			
		} else if (this.getStateForSide(f) == SideState.NONE) {
			this.setStateForSide(f, otherVal);
		} else {
			this.setStateForSide(f, otherVal);
		}
		return this.getStateForSide(f);
	}
	
	public SideState toggleAsInput(EnumFacing f) {
		return toggle(f, SideState.INPUT);
	}
	
	public SideState toggleAsOutput(EnumFacing f) {
		return toggle(f, SideState.OUTPUT);
	}
	
	public EnumFacing getInputSide() {
		for (EnumFacing facing : EnumFacing.VALUES) {
			if (this.isInput(facing)) {
				return facing;
			}
		}
		return null;
	}
	
	public List<EnumFacing> getOutputSides() {
		List<EnumFacing> list = new ArrayList<>();
		for (EnumFacing f : EnumFacing.VALUES) {
			if (this.isOutput(f)) {
				list.add(f);
			}
		}
		return list;
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagList list = new NBTTagList();
		sides.forEach((f, s) -> {
			NBTTagCompound cmp = new NBTTagCompound();
			cmp.setInteger("Face", f.getIndex());
			cmp.setString("State", s.name());
			list.appendTag(cmp);
		});
		compound.setTag("Sides", list);
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		sides = GMNBT.createMap(compound.getTagList("Sides", NBT.TAG_COMPOUND), (base) -> {
			NBTTagCompound cmp = (NBTTagCompound)base;
			return new Pair<EnumFacing, SideState>(EnumFacing.VALUES[cmp.getInteger("Face")], SideState.fromName(cmp.getString("State")));
		});
		super.readFromNBT(compound);
	}

	public static class WireRenderer extends TileEntitySpecialRenderer<MagicWire> {
		
		@Override
		public void render(MagicWire te, double x, double y, double z, float partialTicks, int destroyStage,
				float alpha) {
			
			List<EnumFacing> outputs = te.getOutputSides();
			EnumFacing input = te.getInputSide();
			
			RenderBlockShape inputRender = new DrawEffects.RenderBlockShape(te.world, Minecraft.getMinecraft().player, te.pos, 255, 255, 255, "magicwire/input.png");
			RenderBlockShape outputRender = new DrawEffects.RenderBlockShape(te.world, Minecraft.getMinecraft().player, te.pos, 255, 255, 255, "magicwire/output.png");
			if (input != null) {
				inputRender.render(partialTicks, input);
			}
			if (!outputs.isEmpty()) {
				outputRender.render(partialTicks, outputs.toArray(new EnumFacing[0]));
			}
			
			super.render(te, x, y, z, partialTicks, destroyStage, alpha);
		}
		
	}
	
}
