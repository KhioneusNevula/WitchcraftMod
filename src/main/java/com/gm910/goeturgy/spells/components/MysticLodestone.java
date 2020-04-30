package com.gm910.goeturgy.spells.components;

import java.util.ArrayList;
import java.util.List;

import com.gm910.goeturgy.items.ItemWaystone;
import com.gm910.goeturgy.spells.ioflow.MagicIO;
import com.gm910.goeturgy.spells.spellspaces.SpellSpace.Spell;
import com.gm910.goeturgy.spells.util.ISpellComponent;
import com.gm910.goeturgy.tileentities.TileEntityBaseTickable;
import com.gm910.goeturgy.util.IObjectMouseoverGui;
import com.gm910.goeturgy.util.NonNullMap;
import com.gm910.goeturgy.util.ServerPos;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;

public class MysticLodestone extends TileEntityBaseTickable implements ISpellComponent, IObjectMouseoverGui {

	private ItemStack waystone = ItemStack.EMPTY;
	
	@Override
	public void update() {
		//if (world.isRemote) {
			//DrawEffects.drawBlock(0.1825f, this.getBlock().getBlock(), pos, 1);
		//}
		super.update();
	}
	
	
	public ServerPos getPosition(ItemStack waystone) {
		
		return waystone.isEmpty() ? null : ItemWaystone.getLinkedBlock(waystone);
	}
	
	@Override
	public boolean accepts(EnumFacing facing, NBTTagCompound comp) {
		ItemStack stack = MagicIO.getItemStack(comp);
		if (!stack.isEmpty()) {
			if (stack.getItem() instanceof ItemWaystone && getPosition(stack) != null && !MagicIO.has(MagicIO.ITEM, inputs) && waystone.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isStatic() {
		return true;
	}
	
	@Override
	public boolean isOutput(EnumFacing face) {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public NonNullMap<EnumFacing, NBTTagCompound> getStaticOutput(ServerPos modifiedPos) {
		if (getPosition(waystone) == null) return null;
		NonNullMap<EnumFacing, NBTTagCompound> so = new NonNullMap<>( () ->  {
			NBTTagCompound cmp = new NBTTagCompound();
			MagicIO.writePosToCompound(this.getPosition(waystone), cmp);
			return cmp;
		});

		so.generateValues(EnumFacing.VALUES);
		System.out.print("MysticLodestone output ");
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

	public ItemStack getWaystone(NonNullMap<EnumFacing, NBTTagCompound> map) {
		for (EnumFacing face : map.keySet()) {
			ItemStack stack = MagicIO.getItemStack(map.get(face));
			if (!stack.isEmpty()) {
				if (stack.getItem() instanceof ItemWaystone) {
					return stack;
				}
			}
		}
		return waystone;
	}
	
	public void setWaystone(ItemStack waystone) {
		 this.waystone = waystone;
		 sync();
	}
	
	public ItemStack getItemStack() {
		return waystone;
	}

	@Override
	public NonNullMap<EnumFacing, NBTTagCompound> activate(Spell sp, ServerPos modifiedPos, NonNullMap<EnumFacing, NBTTagCompound> map) {
		ItemStack stack = getWaystone(map);
		if (stack.isEmpty()) {
			return null;
		}
		NonNullMap<EnumFacing, NBTTagCompound> so = new NonNullMap<>( () ->  {
			NBTTagCompound cmp = new NBTTagCompound();
			MagicIO.writePosToCompound(this.getPosition(stack), cmp);
			return cmp;
		});
		so.generateValues(EnumFacing.VALUES);
		return so;
	}

	@Override
	public NonNullMap<EnumFacing, List<String>> getPossibleReturns(NonNullMap<EnumFacing, List<String>> input) {
		NonNullMap<EnumFacing, List<String>> so = new NonNullMap<>( () ->  {
			List<String> cmp = new ArrayList<String>();
			cmp.add(MagicIO.POS);
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
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		if (!waystone.isEmpty()) compound.setTag("Way", waystone.serializeNBT());
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		waystone = compound.hasKey("Way") ? new ItemStack(compound.getCompoundTag("Way")) : waystone;
		super.readFromNBT(compound);
	}


	@Override
	public void drawGuiOverlays(Pre event, Minecraft mc, Gui gui, TextureManager tex, ScaledResolution res,
			EnumFacing sideHit, IBlockState state, Vec3d hitVec, BlockPos position, TileEntity tile, Object object) {
		
		//EntityFallingBlock e = new EntityFallingBlock(mc.world, position.getX()+0.5, position.getY() + 1, position.getZ()+0.5, this.getBlock().getBlock());
		//e.fallTime = 1000;
		//DrawEffects.drawEntity(event.getPartialTicks(), e, 0.3f);
		//e.setDead();
		//world.spawnEntity(e);
		gui.drawCenteredString(mc.fontRenderer, "" + this.getPosition(waystone), res.getScaledWidth()/ 2, res.getScaledHeight() / 2, 0xFFFFFF);
	}
	
	
	
}
