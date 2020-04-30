package com.gm910.goeturgy.spells.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.gm910.goeturgy.spells.components.MagicWire.SideState;
import com.gm910.goeturgy.spells.ioflow.SpellIOMap;
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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.common.util.Constants.NBT;

public class MagicDelayer extends TileEntityBaseTickable implements ISpellComponent, IObjectMouseoverGui {
	

	private int value = 0;
	
	public static final int MAX_VALUE = 50;
	
	private Map<EnumFacing, SideState> sides = (new NonNullMap<EnumFacing, SideState>(SideState.NONE)).generateValues(EnumFacing.VALUES);
	
	
	@Override
	public boolean accepts(EnumFacing facing, NBTTagCompound comp) {
		boolean finalReturn = true;
		if (comp.hasNoTags()) {
			if (world.getTileEntity(pos.offset(facing)) instanceof ISpellComponent) {
				finalReturn = ((ISpellComponent)world.getTileEntity(pos.offset(facing))).isOutput(facing.getOpposite());
			} else {
				finalReturn = false;
			}
		}
		return finalReturn && this.isInput(facing);
	}
	
	@Override
	public boolean acceptsEmpty(EnumFacing face) {
		// TODO Auto-generated method stub
		return sides.get(face) == SideState.INPUT;
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
	public NonNullMap<EnumFacing, NBTTagCompound> activate(Spell sp, ServerPos modifiedPos, NonNullMap<EnumFacing, NBTTagCompound> inputs) {
		NonNullMap<EnumFacing, NBTTagCompound> outputs = new NonNullMap<>(NBTTagCompound::new);
		
		EnumFacing inputSide = this.getInputSide();
		
		List<EnumFacing> outputSides = this.getOutputSides();
		
		if (inputSide == null) return null;
		
		if (outputSides.isEmpty()) return null;
		
		for (EnumFacing f : outputSides) {
			
			//outputs.put(f, inputs.get(inputSide));
			EnumFacing other = f.getOpposite();
			SpellIOMap map = new SpellIOMap();
			map.put(other, inputs.get(inputSide));
			sp.markForActivationAfterDelay(pos.offset(f), this.getDelay(), map);
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
	
	public int getDelay() {
		return value;
	}
	
	public void setDelay(int value) {
		
		this.value = value;
		if (value > MAX_VALUE) {
			this.value = value-MAX_VALUE;
		} else if (value < 0) {
			this.value = MAX_VALUE + value;
		}
		sync();
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
		compound.setInteger("Val", value);
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		sides = GMNBT.createMap(compound.getTagList("Sides", NBT.TAG_COMPOUND), (base) -> {
			NBTTagCompound cmp = (NBTTagCompound)base;
			return new Pair<EnumFacing, SideState>(EnumFacing.VALUES[cmp.getInteger("Face")], SideState.fromName(cmp.getString("State")));
		});
		value = compound.getInteger("Val");
		super.readFromNBT(compound);
	}
	

	@Override
	public void drawGuiOverlays(Pre event, Minecraft mc, Gui gui, TextureManager tex, ScaledResolution res,
			EnumFacing sideHit, IBlockState state, Vec3d hitVec, BlockPos position, TileEntity tile, Object object) {
		gui.drawCenteredString(mc.fontRenderer, Translate.translate("abacus.value", this.value), res.getScaledWidth() / 2 , res.getScaledHeight() / 2, 0xFF0000);
	}

	public static class DelayerRenderer extends TileEntitySpecialRenderer<MagicDelayer> {
		
		@Override
		public void render(MagicDelayer te, double x, double y, double z, float partialTicks, int destroyStage,
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
