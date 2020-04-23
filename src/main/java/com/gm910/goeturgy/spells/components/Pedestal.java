package com.gm910.goeturgy.spells.components;

import java.util.ArrayList;
import java.util.List;

import com.gm910.goeturgy.spells.ioflow.BlockStack;
import com.gm910.goeturgy.spells.ioflow.MagicIO;
import com.gm910.goeturgy.spells.util.ISpellComponent;
import com.gm910.goeturgy.tileentities.TileEntityBaseTickable;
import com.gm910.goeturgy.util.DrawEffects;
import com.gm910.goeturgy.util.IObjectMouseoverGui;
import com.gm910.goeturgy.util.NonNullMap;
import com.gm910.goeturgy.util.ServerPos;
import com.gm910.goeturgy.util.Translate;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;

public class Pedestal extends TileEntityBaseTickable implements ISpellComponent, IObjectMouseoverGui {

	
	@Override
	public void update() {
		
		super.update();
	}
	
	
	public BlockStack getBlock() {
		BlockStack stack = null;
		ServerPos sigPos = null;
		if (this.getModifiedPos().equals(pos)) {
			sigPos = getServerPos().sUp();
		} else {
			sigPos = getModifiedPos();
		}
		for (EnumFacing f : EnumFacing.VALUES) {
			if (!MagicIO.getItemStack(inputs.get(f)).isEmpty()) {
				if (MagicIO.getItemStack(inputs.get(f)).getItem() instanceof ItemBlock) {
					ItemStack itemplacer = MagicIO.getItemStack(inputs.get(f));
					ItemBlock itemblock = ((ItemBlock)itemplacer.getItem());
					IBlockState state = itemblock.getBlock().getStateFromMeta(itemplacer.getItem().getMetadata(itemplacer.getMetadata()));
					
					NBTTagCompound tiledata = itemplacer.getSubCompound("BlockEntityTag");
					TileEntity tile = itemblock.getBlock().createTileEntity(getModifiedWorld(), state);
					if (tile != null) {
						NBTTagCompound nbttagcompound1 = tile.writeToNBT(new NBTTagCompound());
	                    NBTTagCompound nbttagcompound2 = nbttagcompound1.copy();
	                    nbttagcompound1.merge(tiledata);
	
	                    if (!nbttagcompound1.equals(nbttagcompound2))
	                    {
	                        tile.readFromNBT(nbttagcompound1);
	                    }
					}
                    stack = new BlockStack(state, tile);
				}
			}
		}
		
		if (stack == null) {
			stack = new BlockStack(sigPos);
		}
		
		return stack;
	}
	
	@Override
	public boolean accepts(EnumFacing facing, NBTTagCompound comp) {
		ItemStack stack = MagicIO.getItemStack(comp);
		if (!stack.isEmpty()) {
			if (stack.getItem() != null) {
				if (stack.getItem() instanceof ItemBlock) {
					return true;
				}
			}
		} 
		return false;
	}

	@Override
	public boolean isStatic() {
		return true;
	}
	
	@Override
	public NonNullMap<EnumFacing, NBTTagCompound> getStaticOutput() {
		NonNullMap<EnumFacing, NBTTagCompound> so = new NonNullMap<>( () ->  {
			NBTTagCompound cmp = new NBTTagCompound();
			MagicIO.writeBlockToCompound(getBlock(), cmp);
			return cmp;
		});
		so.generateValues(EnumFacing.VALUES);
		return so;
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
	public NonNullMap<EnumFacing, NBTTagCompound> activate(NonNullMap<EnumFacing, NBTTagCompound> map) {
		this.putInput(map);
		return this.getStaticOutput();
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
	public int getRequiredPowerFromNBT(NonNullMap<EnumFacing, NBTTagCompound> tagsForSide) {
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
		EntityFallingBlock e = new EntityFallingBlock(mc.world, position.getX(), position.getY() + 0.5, position.getZ(), this.getBlock().getBlock());
		e.turn(45, 45);
		DrawEffects.drawEntity(event.getPartialTicks(), e, 0.3f);
	}
	

}
