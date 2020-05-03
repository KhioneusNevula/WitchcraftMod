package com.gm910.goeturgy.spells.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.common.util.Constants.NBT;

public class MagicTypeFilterBlock extends TileEntityBaseTickable implements ISpellComponent, IObjectMouseoverGui {
	
	private String onlyTag = MagicIO.INT;
	private boolean restrictList = false;
	
	private boolean restrictOnlyTag = false;
	
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
		
		return this.isInput(facing);
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
		return this.isInput(face);
	}
	
	
	
	@Override
	public NonNullMap<EnumFacing, NBTTagCompound> activate(Spell sp, ServerPos modifiedPos, NonNullMap<EnumFacing, NBTTagCompound> inputs) {
		NonNullMap<EnumFacing, NBTTagCompound> outputs = new NonNullMap<>(NBTTagCompound::new);
		String onlyTag = restrictList ? MagicIO.toList(this.onlyTag) : this.onlyTag;
		
		if (this.getInputSide() == null) return null;
		if (this.getOutputSides().isEmpty()) return null; 
		
		if (inputs.get(this.getInputSide()).hasKey(onlyTag)) {
			
				NBTTagCompound comp = inputs.get(this.getInputSide());
				if (restrictOnlyTag) {
					comp.removeTag(onlyTag);
				} else {
					NBTBase b = comp.getTag(onlyTag);
					comp = new NBTTagCompound();
					comp.setTag(onlyTag, b);
				}
				for (EnumFacing f : this.getOutputSides()) {
					outputs.put(f, comp);
				}
		}
		
		return outputs;
	}

	@Override
	public NonNullMap<EnumFacing, List<String>> getPossibleReturns(NonNullMap<EnumFacing, List<String>> input) {
		NonNullMap<EnumFacing, List<String>> rets = new NonNullMap<>(ArrayList<String>::new);
		
		return rets;
	}

	@Override
	public int getRequiredPower(NonNullMap<EnumFacing, List<String>> tagsForSide) {
		
		return 1 ;
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
	
	public SideState cycleSide(EnumFacing f) {
		if (getStateForSide(f) == SideState.INPUT) {
			setStateForSide(f, SideState.OUTPUT);
		} else if (getStateForSide(f) == SideState.OUTPUT) {
			setStateForSide(f, SideState.NONE);
		} else {
			setStateForSide(f, SideState.INPUT);
			if (getStateForSide(f) != SideState.INPUT) {
				setStateForSide(f, SideState.OUTPUT);
			}
		}
		return getStateForSide(f);
	}
	
	public void cycleRestrictOnly() {
		this.restrictOnlyTag = !this.restrictOnlyTag;
	}
	
	public void cycleListOrNot() {
		this.restrictList = !this.restrictList;
	}
	
	public String cycleDataType() {
		int index = MagicIO.TAG_TYPES.indexOf(onlyTag);
		if (index >= MagicIO.TAG_TYPES.size() - 1) {
			this.onlyTag = MagicIO.TAG_TYPES.get(0);
		} else {
			this.onlyTag = MagicIO.TAG_TYPES.get(index+1);
		}
		return this.onlyTag;
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
		compound.setString("OnlyTag", this.onlyTag);
		compound.setBoolean("RestrictOnlyTag", this.restrictOnlyTag);
		compound.setBoolean("RestrictList", this.restrictList);
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		sides = GMNBT.createMap(compound.getTagList("Sides", NBT.TAG_COMPOUND), (base) -> {
			NBTTagCompound cmp = (NBTTagCompound)base;
			return new Pair<EnumFacing, SideState>(EnumFacing.VALUES[cmp.getInteger("Face")], SideState.fromName(cmp.getString("State")));
		});
		this.onlyTag = compound.getString("OnlyTag");
		this.restrictOnlyTag = compound.getBoolean("RestrictOnlyTag");
		this.restrictList = compound.getBoolean("RestrictList");
		super.readFromNBT(compound);
	}

	public static class DataBlockRenderer extends TileEntitySpecialRenderer<MagicTypeFilterBlock> {
		
		@Override
		public void render(MagicTypeFilterBlock te, double x, double y, double z, float partialTicks, int destroyStage,
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

	@Override
	public void drawGuiOverlays(Pre event, Minecraft mc, Gui gui, TextureManager tex, ScaledResolution res,
			EnumFacing sideHit, IBlockState state, Vec3d hitVec, BlockPos position, TileEntity tile, Object object) {
		String ot = this.restrictList ? MagicIO.toList(onlyTag) : onlyTag;
		gui.drawCenteredString(mc.fontRenderer, 
				Translate.translate("datablock.restrict"+restrictOnlyTag, MagicIO.translate(ot)), 
				res.getScaledWidth() / 2, res.getScaledHeight() / 2, 0xFFFFFF);
	}
	
}
