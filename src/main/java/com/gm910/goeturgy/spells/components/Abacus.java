package com.gm910.goeturgy.spells.components;

import java.util.ArrayList;
import java.util.List;

import com.gm910.goeturgy.spells.ioflow.MagicIO;
import com.gm910.goeturgy.spells.spellspaces.SpellSpace.SpellInstance;
import com.gm910.goeturgy.spells.util.ISpellComponent;
import com.gm910.goeturgy.tileentities.TileEntityBaseTickable;
import com.gm910.goeturgy.util.IObjectMouseoverGui;
import com.gm910.goeturgy.util.NonNullMap;
import com.gm910.goeturgy.util.ServerPos;
import com.gm910.goeturgy.util.Translate;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;

public class Abacus extends TileEntityBaseTickable implements ISpellComponent, IObjectMouseoverGui {

	private int value = 0;
	
	public static final int MAX_VALUE = 50;
	
	@Override
	public void update() {
		
		super.update();
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("Val", value);
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		value = compound.getInteger("Val");
		super.readFromNBT(compound);
	}
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int value) {
		
		this.value = value;
		if (value > MAX_VALUE) {
			this.value = value-MAX_VALUE;
		} else if (value < 0) {
			this.value = MAX_VALUE + value;
		}
		sync();
	}
	
	public void incrementValue() {
		setValue(value+1);
	}
	
	@Override
	public boolean accepts(EnumFacing facing, NBTTagCompound comp) {
		
		return false;
	}

	@Override
	public boolean isStatic() {
		return true;
	}
	
	@Override
	public NonNullMap<EnumFacing, NBTTagCompound> getStaticOutput(ServerPos modifiedPos) {
		NonNullMap<EnumFacing, NBTTagCompound> so = new NonNullMap<>( () ->  {
			NBTTagCompound cmp = new NBTTagCompound();
			MagicIO.writeIntToCompound(value, cmp);
			return cmp;
		});
		so.generateValues(EnumFacing.VALUES);
		return so;
	}
	
	@Override
	public NonNullMap<EnumFacing, NBTTagCompound> getInputs() {
		
		return new NonNullMap<>(NBTTagCompound::new);
	}

	@Override
	public boolean putInput(NonNullMap<EnumFacing, NBTTagCompound> inputs) {
		
		return false;
	}


	@Override
	public NonNullMap<EnumFacing, NBTTagCompound> activate(SpellInstance sp, ServerPos modifiedPos, NonNullMap<EnumFacing, NBTTagCompound> map) {
		System.out.println("Abacus should not be activated, it is static");
		return new NonNullMap<>(NBTTagCompound::new);
	}

	@Override
	public NonNullMap<EnumFacing, List<String>> getPossibleReturns(NonNullMap<EnumFacing, List<String>> input) {
		NonNullMap<EnumFacing, List<String>> so = new NonNullMap<>( () ->  {
			List<String> cmp = new ArrayList<String>();
			cmp.add(MagicIO.INT);
			return cmp;
		});
		so.generateValues(EnumFacing.VALUES);
		return so;
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
	public void drawGuiOverlays(Pre event, Minecraft mc, Gui gui, TextureManager tex, ScaledResolution res,
			EnumFacing sideHit, IBlockState state, Vec3d hitVec, BlockPos position, TileEntity tile, Object object) {
		gui.drawCenteredString(mc.fontRenderer, Translate.translate("abacus.value", this.value), res.getScaledWidth() / 2 , res.getScaledHeight() / 2, 0xFF0000);
	}
	

}
