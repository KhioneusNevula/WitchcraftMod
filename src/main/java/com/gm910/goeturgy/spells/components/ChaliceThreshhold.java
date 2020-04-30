package com.gm910.goeturgy.spells.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.gm910.goeturgy.spells.ioflow.MagicIO;
import com.gm910.goeturgy.spells.spellspaces.SpellSpace;
import com.gm910.goeturgy.spells.spellspaces.SpellSpace.Spell;
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

public class ChaliceThreshhold extends TileEntityBaseTickable implements ISpellComponent {
	
	//private int max = Integer.MAX_VALUE;
	//private int min = Integer.MIN_VALUE;
	
	public static enum SideState {
		NUMERIC_INPUT,
		MINIMUM_INPUT,
		MAXIMUM_INPUT,
		GENERIC_INPUT,
		GENERIC_OUTPUT,
		NONE;
		public static SideState fromName(String name) {
			for (SideState state : values()) {
				if (state.name().equals(name)) {
					return state;
				}
			}
			return null;
		}
		
		public static SideState getNext(SideState face) {
			if (face.ordinal() < values().length - 1) {
				return values()[face.ordinal() + 1];
			} else {
				return values()[0];
			}
		}
	}
	
	private Map<EnumFacing, SideState> sides = (new NonNullMap<EnumFacing, SideState>(SideState.NONE)).generateValues(EnumFacing.VALUES);
	
	@Override
	public boolean accepts(EnumFacing facing, NBTTagCompound comp) {
		if ((this.isNumericInput(facing) || this.isMaximumInput(facing) || this.isMinimumInput(facing))) {
			if (comp.hasKey(MagicIO.INT) && !inputs.get(facing).hasKey(MagicIO.INT)) {
				return true;
			}
			return false;
		} else if (this.isGenericInput(facing) && ((ISpellComponent)world.getTileEntity(pos.offset(facing))).isOutput(facing.getOpposite())) {
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

	@Override
	public boolean acceptsEmpty(EnumFacing face) {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public boolean isOutput(EnumFacing face) {
		// TODO Auto-generated method stub
		return this.getOutputSides().contains(face);
	}
	
	@Override
	public NonNullMap<EnumFacing, NBTTagCompound> activate(Spell sp, ServerPos modifiedPos, NonNullMap<EnumFacing, NBTTagCompound> inputs) {
		
		EnumFacing minSide = this.getMinInputSide();
		EnumFacing numericInputSide = this.getNumericInputSide();
		EnumFacing maxSide = this.getMaxInputSide();
		if (minSide == null || numericInputSide == null || maxSide == null) {
			return null;
		}
		
		int min = inputs.get(minSide).hasKey(MagicIO.INT) ? MagicIO.getInteger(inputs.get(minSide)) : Integer.MAX_VALUE;
		int max = inputs.get(maxSide).hasKey(MagicIO.INT) ? MagicIO.getInteger(inputs.get(maxSide)) : Integer.MAX_VALUE;
		int checker = inputs.get(numericInputSide).hasKey(MagicIO.INT) ? MagicIO.getInteger(inputs.get(numericInputSide)) : Integer.MAX_VALUE;
		
		if (min == Integer.MAX_VALUE && max == Integer.MAX_VALUE || checker == Integer.MAX_VALUE) return null;
		boolean conditio = (min != Integer.MIN_VALUE ? checker >= min : true) && (max != Integer.MAX_VALUE ? checker <= max : true);
		
		if (conditio) {
			if (!this.getOutputSides().isEmpty() && this.getGenericInputSide() != null) {
				NonNullMap<EnumFacing, NBTTagCompound> outputs = new NonNullMap<>(() -> {
					NBTTagCompound c1 = inputs.get(this.getGenericInputSide());
					return c1;
				});
				outputs.generateValues(this.getOutputSides().toArray(new EnumFacing[0]));

				return outputs;
			} else {
				return null;
			}
		} else {
			return SpellSpace.FORCED_END;
		}
	}


	@Override
	public NonNullMap<EnumFacing, List<String>> getPossibleReturns(NonNullMap<EnumFacing, List<String>> input) {
		NonNullMap<EnumFacing, List<String>> rets = new NonNullMap<>(ArrayList<String>::new);
		
		return rets;
	}

	@Override
	public int getRequiredPower(NonNullMap<EnumFacing, List<String>> tagsForSide) {
		
		return 1;
	}

	@Override
	public int getRequiredPowerFromNBT(NonNullMap<EnumFacing, NBTTagCompound> tagsForSide, ServerPos modifiedPos) {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public int getRequiredPower() {
		// TODO Auto-generated method stub
		return 1;
	}
	
	public boolean setStateForSide(EnumFacing f, SideState state) {
		if (state == SideState.NUMERIC_INPUT || state == SideState.MAXIMUM_INPUT || state == SideState.MINIMUM_INPUT || state == SideState.GENERIC_INPUT) {
			if (!sides.containsValue(state)) {
				this.sides.put(f, state);
			}
		} else {
			this.sides.put(f, state);
		}
		
		sync();
		return getStateForSide(f) == state;
	}
	
	public SideState getStateForSide(EnumFacing f) {
		return this.sides.get(f);
	}
	
	public boolean isNumericInput(EnumFacing f) {
		return this.getStateForSide(f) == SideState.NUMERIC_INPUT;
	}
	
	public boolean isGenericInput(EnumFacing f) {
		return this.getStateForSide(f) == SideState.GENERIC_INPUT;
	}
	
	public boolean isMinimumInput(EnumFacing f) {
		return this.getStateForSide(f) == SideState.MINIMUM_INPUT;
	}
	
	public boolean isMaximumInput(EnumFacing f) {
		return this.getStateForSide(f) == SideState.MAXIMUM_INPUT;
	}
	
	public boolean isGenericOutput(EnumFacing f) {
		return this.getStateForSide(f) == SideState.GENERIC_OUTPUT;
	}
	
	public boolean doesSideHaveNoSignificance(EnumFacing f) {
		return this.getStateForSide(f) == SideState.NONE;
	}
	
	public boolean makeNumericInput(EnumFacing f) {
		this.setStateForSide(f, SideState.NUMERIC_INPUT);
		return this.getStateForSide(f) == SideState.NUMERIC_INPUT;
	}
	
	public boolean makeMinInput(EnumFacing f) {
		this.setStateForSide(f, SideState.MINIMUM_INPUT);

		return this.getStateForSide(f) == SideState.MINIMUM_INPUT;
	}
	
	public boolean makeMaxInput(EnumFacing f) {
		this.setStateForSide(f, SideState.MAXIMUM_INPUT);
		
		return this.getStateForSide(f) == SideState.MAXIMUM_INPUT;
	}
	
	public boolean makeGenericInput(EnumFacing f) {
		this.setStateForSide(f, SideState.GENERIC_INPUT);
		return this.getStateForSide(f) == SideState.GENERIC_OUTPUT;
	}
	

	public boolean makeGenericOutput(EnumFacing f) {
		this.setStateForSide(f, SideState.GENERIC_OUTPUT);
		return this.getStateForSide(f) == SideState.GENERIC_OUTPUT;
	}
	
	public void resetNumericInputSide() {
		if (this.getNumericInputSide() != null) {
			this.setStateForSide(this.getNumericInputSide(), SideState.NONE);
		}
	}
	
	public void resetGenericInputSide() {
		if (this.getGenericInputSide() != null) {
			this.setStateForSide(this.getGenericInputSide(), SideState.NONE);
		}
	}
	
	public void resetMinInputSide() {
		if (this.getMinInputSide() != null) {
			this.setStateForSide(this.getMinInputSide(), SideState.NONE);
		}
	}
	
	public void resetMaxInputSide() {
		if (this.getMaxInputSide() != null) {
			this.setStateForSide(this.getMaxInputSide(), SideState.NONE);
		}
	}
	
	
	public boolean makeSideInsignificant(EnumFacing f) {
		this.setStateForSide(f, SideState.NONE);
		return this.getStateForSide(f) == SideState.NONE;
	}
	
	public SideState cycle(EnumFacing f) {
		SideState fromState = this.getStateForSide(f);
		SideState toState = SideState.getNext(fromState);
		if (this.setStateForSide(f, toState)) {
			return toState;
		} else {
			int i = 0;
			do {
				fromState = toState;
				toState = SideState.getNext(fromState);
			} while(!setStateForSide(f, toState) && i++ < SideState.values().length);
		}
		return this.getStateForSide(f);
	}
	
	public EnumFacing getNumericInputSide() {
		for (EnumFacing facing : EnumFacing.VALUES) {
			if (this.isNumericInput(facing)) {
				return facing;
			}
		}
		return null;
	}
	
	public EnumFacing getGenericInputSide() {
		for (EnumFacing facing : EnumFacing.VALUES) {
			if (this.isGenericInput(facing)) {
				return facing;
			}
		}
		return null;
	}
	
	public EnumFacing getMinInputSide() {
		for (EnumFacing facing : EnumFacing.VALUES) {
			if (this.isMinimumInput(facing)) {
				return facing;
			}
		}
		return null;
	}
	
	public EnumFacing getMaxInputSide() {
		for (EnumFacing facing : EnumFacing.VALUES) {
			if (this.isMaximumInput(facing)) {
				return facing;
			}
		}
		return null;
	}
	
	public List<EnumFacing> getNonSpecialSides() {
		List<EnumFacing> list = new ArrayList<>();
		for (EnumFacing f : EnumFacing.VALUES) {
			if (this.doesSideHaveNoSignificance(f)) {
				list.add(f);
			}
		}
		return list;
	}
	
	public List<EnumFacing> getOutputSides() {
		List<EnumFacing> list = new ArrayList<>();
		for (EnumFacing f : EnumFacing.VALUES) {
			if (this.isGenericOutput(f)) {
				list.add(f);
			}
		}
		return list;
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		//compound.setInteger("Min", min);
		//compound.setInteger("Max", max);
		NBTTagList ls = new NBTTagList();
		sides.forEach((f, s) -> {
			NBTTagCompound cmp = new NBTTagCompound();
			cmp.setInteger("Face", f.getIndex());
			cmp.setString("State", s.name());
			ls.appendTag(cmp);
		});
		compound.setTag("Sides", ls);
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		//this.min = compound.getInteger("Min");
		//this.max = compound.getInteger("Max");
		sides = GMNBT.createMap(compound.getTagList("Sides", NBT.TAG_COMPOUND), (base) -> {
			NBTTagCompound cmp = (NBTTagCompound)base;
			return new Pair<EnumFacing, SideState>(EnumFacing.VALUES[cmp.getInteger("Face")], SideState.fromName(cmp.getString("State")));
		});
		super.readFromNBT(compound);
	}

	public static class ThreshRenderer extends TileEntitySpecialRenderer<ChaliceThreshhold> {
		
		@Override
		public void render(ChaliceThreshhold te, double x, double y, double z, float partialTicks, int destroyStage,
				float alpha) {
			
			EnumFacing minIn = te.getMinInputSide();
			EnumFacing maxIn = te.getMaxInputSide();
			EnumFacing input = te.getGenericInputSide();
			EnumFacing numericInput = te.getNumericInputSide();
			List<EnumFacing> genericOutputs = te.getOutputSides();
			
			RenderBlockShape inputRender = new DrawEffects.RenderBlockShape(te.world, Minecraft.getMinecraft().player, te.pos, 255, 255, 255, "magicwire/input.png");
			RenderBlockShape maxInRenderer = new DrawEffects.RenderBlockShape(te.world, Minecraft.getMinecraft().player, te.pos, 255, 255, 255, "threshhold_math/maximum.png");
			RenderBlockShape numericInputRenderer = new DrawEffects.RenderBlockShape(te.world, Minecraft.getMinecraft().player, te.pos, 255, 255, 255, "threshhold_math/numeric_input.png");
			RenderBlockShape outputRender = new DrawEffects.RenderBlockShape(te.world, Minecraft.getMinecraft().player, te.pos, 255, 255, 255, "magicwire/output.png");
			
			RenderBlockShape minInRenderer = new DrawEffects.RenderBlockShape(te.world, Minecraft.getMinecraft().player, te.pos, 255, 255, 255, "threshhold_math/minimum.png");

			if (input != null) {
				inputRender.render(partialTicks, input);
			}
			if (!genericOutputs.isEmpty()) {
				outputRender.render(partialTicks, genericOutputs.toArray(new EnumFacing[0]));
			}
			if (maxIn != null) {
				maxInRenderer.render(partialTicks, maxIn);
			}
			if (minIn != null) {
				minInRenderer.render(partialTicks, minIn);
			}
			if (numericInput != null) {
				numericInputRenderer.render(partialTicks, numericInput);
			}
			
			super.render(te, x, y, z, partialTicks, destroyStage, alpha);
		}
		
	}
}
