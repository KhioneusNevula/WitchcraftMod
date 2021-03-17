package com.gm910.goeturgy.spells.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.gm910.goeturgy.spells.ioflow.MagicIO;
import com.gm910.goeturgy.spells.spellspaces.SpellSpace.Spell;
import com.gm910.goeturgy.spells.util.ISpellComponent;
import com.gm910.goeturgy.tileentities.TileEntityBaseTickable;
import com.gm910.goeturgy.util.DrawEffects;
import com.gm910.goeturgy.util.DrawEffects.RenderBlockShape;
import com.gm910.goeturgy.util.GMNBT;
import com.gm910.goeturgy.util.IObjectMouseoverGui;
import com.gm910.goeturgy.util.NonNullMap;
import com.gm910.goeturgy.util.ServerPos;
import com.gm910.goeturgy.util.Translate;

import akka.japi.Pair;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.common.util.Constants.NBT;

public class MagicalSieve extends TileEntityBaseTickable implements ISpellComponent, IObjectMouseoverGui {
	
	//private int max = Integer.MAX_VALUE;
	//private int min = Integer.MIN_VALUE;
	
	private boolean removeOnly = true;
	
	public static enum SideState {
		FILTER_INPUT,
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

	public Pair<String, NBTTagList> getSignificantList(NonNullMap<EnumFacing, NBTTagCompound> inputs, EnumFacing face) {
		
		NBTTagCompound sigComp = inputs.get(face);
		for (String tag : MagicIO.TAG_TYPES_AS_LISTS) {
			if (sigComp.hasKey(tag, NBT.TAG_LIST)) {
				return new Pair<>(MagicIO.notList(tag), sigComp.getTagList(tag, MagicIO.LIST_TYPE_FOR_TAG.get(MagicIO.notList(tag))));
			}
		}
		
		return null;
	}
	
	@Override
	public boolean accepts(EnumFacing facing, NBTTagCompound comp) {
		if (this.isFilterInput(facing)) {
			if ((comp.hasKey(MagicIO.SPECIES))) {
				return true;
			}
			
			return MagicIO.hasList(comp);
		} else if (this.isGenericInput(facing)) {
			
			return MagicIO.hasList(comp);
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
	public boolean isOutput(EnumFacing face) {
		// TODO Auto-generated method stub
		return this.getOutputSides().contains(face);
	}
	
	@Override
	public NonNullMap<EnumFacing, NBTTagCompound> activate(Spell sp, ServerPos modifiedPos, NonNullMap<EnumFacing, NBTTagCompound> inputs) {
		
		EnumFacing inputSide = this.getGenericInputSide();
		EnumFacing filterSide = this.getFilterInputSide();
		if (filterSide == null || inputSide == null) {
			System.out.println("Sieve missing sides, filterSide: " + filterSide + ", inputSide: " + inputSide);
			return null;
		}
		
		Pair<String, NBTTagList> filterPair = this.getSignificantList(inputs, filterSide);
		ResourceLocation species = null;
		List<ResourceLocation> speciesList = new ArrayList<>();
		if (filterPair == null) {
			if (inputs.get(filterSide).hasKey(MagicIO.SPECIES)) {
				species = MagicIO.getSpecies(inputs.get(filterSide));
			} else {
				System.out.println("Sieve missing filter list and/or tag");
				return null;
			}
		}
		String sigTag = filterPair.first();
		NBTTagList filterList = filterPair.second();
		
		Pair<String, NBTTagList> inputPair = this.getSignificantList(inputs, inputSide);
		
		if (inputPair == null) {
			System.out.println("Sieve missing input list and/or tag");
			return null;
		}
		if (!inputPair.first().equals(sigTag)) {
			if (sigTag.equals(MagicIO.SPECIES) && inputPair.first().equals(MagicIO.ENTITY)) {
				speciesList = MagicIO.getSpeciesList(inputs.get(filterSide));
			} else {
				System.out.println("Conflicting filter and input data types: filter--" + sigTag + ", input--" + inputPair.first());
				return null;
			}
		}
		NBTTagList realList = inputPair.second();
		ArrayList<NBTBase> delegateRealList = new ArrayList<NBTBase>();
		realList.forEach((e) -> delegateRealList.add(e) );
		if (species != null || !speciesList.isEmpty()) {
			List<ResourceLocation> allSpec = new ArrayList<>(speciesList);
			if (species != null) allSpec.add(species);
			List<NBTBase> secDelegate = new ArrayList<>(delegateRealList);
			for (NBTBase bs : secDelegate) {
				Entity e = MagicIO.entityfromid((UUID)MagicIO.DIRECT_GETTERS.get(MagicIO.ENTITY).apply(bs));
				if (e == null) {delegateRealList.remove(bs);continue;}
				if (MagicIO.getSpeciesRL(e) != null && allSpec.contains(MagicIO.getSpeciesRL(e))) {
					if (removeOnly) {
						delegateRealList.remove(bs);
					}
				} else {
					if (!removeOnly) {
						delegateRealList.remove(bs);
					}
				}
			}
		}
		else {ArrayList<NBTBase> delegateFilterList = new ArrayList<NBTBase>();
			filterList.forEach((e) -> delegateFilterList.add(e));
			if (removeOnly) {
				delegateRealList.removeAll(delegateFilterList);
			} else {
				delegateRealList.retainAll(delegateFilterList);
			}
		}
		
		NBTTagList realList2 = new NBTTagList();
		delegateRealList.forEach((e) -> realList2.appendTag(e));

		if (!this.getOutputSides().isEmpty()) {
			NonNullMap<EnumFacing, NBTTagCompound> outputs = new NonNullMap<>(() -> {
				NBTTagCompound c1 = inputs.get(this.getGenericInputSide());
				c1.setTag(MagicIO.toList(sigTag), realList2);
				return c1;
			});
			outputs.generateValues(this.getOutputSides().toArray(new EnumFacing[0]));

			return outputs;
		} else {
			System.out.println("Sieve has no outputs");
			return null;
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
		if (state == SideState.FILTER_INPUT || state == SideState.GENERIC_INPUT) {
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
	
	
	public boolean isGenericInput(EnumFacing f) {
		return this.getStateForSide(f) == SideState.GENERIC_INPUT;
	}
	
	public boolean isFilterInput(EnumFacing f) {
		return this.getStateForSide(f) == SideState.FILTER_INPUT;
	}
	
	public boolean isGenericOutput(EnumFacing f) {
		return this.getStateForSide(f) == SideState.GENERIC_OUTPUT;
	}
	
	public boolean doesSideHaveNoSignificance(EnumFacing f) {
		return this.getStateForSide(f) == SideState.NONE;
	}
	
	
	public boolean makeFilterInput(EnumFacing f) {
		this.setStateForSide(f, SideState.FILTER_INPUT);
		
		return this.getStateForSide(f) == SideState.FILTER_INPUT;
	}
	
	public boolean makeGenericInput(EnumFacing f) {
		this.setStateForSide(f, SideState.GENERIC_INPUT);
		return this.getStateForSide(f) == SideState.GENERIC_OUTPUT;
	}
	

	public boolean makeGenericOutput(EnumFacing f) {
		this.setStateForSide(f, SideState.GENERIC_OUTPUT);
		return this.getStateForSide(f) == SideState.GENERIC_OUTPUT;
	}
	
	public void resetFilterInputSide() {
		if (this.getFilterInputSide() != null) {
			this.setStateForSide(this.getFilterInputSide(), SideState.NONE);
		}
	}
	
	public void resetGenericInputSide() {
		if (this.getGenericInputSide() != null) {
			this.setStateForSide(this.getGenericInputSide(), SideState.NONE);
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
	
	public EnumFacing getFilterInputSide() {
		for (EnumFacing facing : EnumFacing.VALUES) {
			if (this.isFilterInput(facing)) {
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
	
	public void setRemoveOnly(boolean removeOnly) {
		this.removeOnly = removeOnly;
	}
	
	public boolean isRemoveOnly() {
		return removeOnly;
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
		compound.setBoolean("RO", this.removeOnly);
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
		this.removeOnly = compound.getBoolean("RO");
		super.readFromNBT(compound);
	}

	public static class SieveRenderer extends TileEntitySpecialRenderer<MagicalSieve> {
		
		@Override
		public void render(MagicalSieve te, double x, double y, double z, float partialTicks, int destroyStage,
				float alpha) {
			
			EnumFacing filterIn = te.getFilterInputSide();
			EnumFacing input = te.getGenericInputSide();
			List<EnumFacing> genericOutputs = te.getOutputSides();
			
			RenderBlockShape inputRender = new DrawEffects.RenderBlockShape(te.world, Minecraft.getMinecraft().player, te.pos, 255, 255, 255, "magicwire/input.png");
			RenderBlockShape filterRenderer = new DrawEffects.RenderBlockShape(te.world, Minecraft.getMinecraft().player, te.pos, 255, 255, 255, "threshhold_math/numeric_input.png");
			RenderBlockShape outputRender = new DrawEffects.RenderBlockShape(te.world, Minecraft.getMinecraft().player, te.pos, 255, 255, 255, "magicwire/output.png");
			
			
			if (input != null) {
				inputRender.render(partialTicks, input);
			}
			if (!genericOutputs.isEmpty()) {
				outputRender.render(partialTicks, genericOutputs.toArray(new EnumFacing[0]));
			}
			
			if (filterIn != null) {
				filterRenderer.render(partialTicks, filterIn);
			}
			
			
			super.render(te, x, y, z, partialTicks, destroyStage, alpha);
		}
		
	}
	
	@Override
	public void drawGuiOverlays(Pre event, Minecraft mc, Gui gui, TextureManager tex, ScaledResolution res,
			EnumFacing sideHit, IBlockState state, Vec3d hitVec, BlockPos position, TileEntity tile, Object object) {
		gui.drawCenteredString(mc.fontRenderer, Translate.translate("sieve.restrict"+this.removeOnly), res.getScaledWidth() / 2, res.getScaledHeight() / 2, 0xFFFFFF);
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Sieve at " + pos;
	}
}
