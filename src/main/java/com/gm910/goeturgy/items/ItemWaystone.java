package com.gm910.goeturgy.items;

import java.util.List;

import com.gm910.goeturgy.util.ServerPos;
import com.gm910.goeturgy.util.Translate;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemWaystone extends ItemBase {
	
	public static final String LINKED_DATA_TAG = "LinkedLocation";

	public ItemWaystone(String name) {
		super(name);
	}
	

	public static ItemStack linkToBlock(ServerPos pos, ItemStack stack) {
		NBTTagCompound tag = stack.getOrCreateSubCompound(LINKED_DATA_TAG);
		
		if (pos != null) {
			tag.setTag("Pos", pos.toNBT());
		}
		
		return stack;
	}
	
	public static ServerPos getLinkedBlock(ItemStack stack) {
		if (!stack.hasTagCompound()) return null;
		//if (!stack.getTagCompound().hasKey(LINKED_DATA_TAG)) return null;
		return ServerPos.fromNBT(stack.getOrCreateSubCompound(LINKED_DATA_TAG).getCompoundTag("Pos"));
	}
	
	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		if (worldIn.isRemote) return;
		if (!isSelected) return;
		if (worldIn.rand.nextInt(14) >= 2) return;
		
		ServerPos pos = getLinkedBlock(stack);
		
		if (pos == null) {
			return;
		}
		
		World world1 = pos.getWorld();
		stack.getOrCreateSubCompound(LINKED_DATA_TAG).setTag("BlockState", NBTUtil.writeBlockState(new NBTTagCompound(), world1.getBlockState(pos)));
		
	}
	
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		
		if (player.isSneaking()) {
			linkToBlock(new ServerPos(pos, worldIn), player.getHeldItem(hand));
			return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
		}
		return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
	}
	
	@Override
	public int getItemStackLimit() {
		// TODO Auto-generated method stub
		return 1;
	}

	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		
		super.addInformation(stack, worldIn, tooltip, flagIn);
		ServerPos pos = getLinkedBlock(stack);
		if (pos == null) {
			tooltip.add(Translate.translate("pearl.unbound"));
		} else {
			IBlockState state = NBTUtil.readBlockState(stack.getOrCreateSubCompound(LINKED_DATA_TAG).getCompoundTag("BlockState"));
			Item item = Item.getItemFromBlock(state.getBlock());
			if (item == Items.AIR) {
				tooltip.add(Translate.translate("waystone.bound", state.getBlock().getLocalizedName(), pos));
			} else {
				ItemStack stack2 = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
				tooltip.add(Translate.translate("waystone.bound", stack2.getDisplayName(), pos));
			}
		}
		
	}
	
}
