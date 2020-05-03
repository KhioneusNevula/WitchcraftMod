package com.gm910.goeturgy.items;

import java.util.List;

import com.gm910.goeturgy.spells.components.MagicTypeFilterBlock;
import com.gm910.goeturgy.util.Translate;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemTypeFilterSettingChanger extends ItemBase {
	
	public static enum UsageState {
		DATA_TAG("datatag"),
		LIST_OR_NOT("list"),
		RESTRICT_ONLY_TAG_OR_NOT("restrict"),
		IO_SIDE("ioside");
		String name;
		private UsageState(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
		
		public UsageState getNext() {
			int ord = this.ordinal();
			if (ord >= values().length - 1) {
				return values()[0];
			} else {
				return values()[ord+1];
			}
		}
		
		public static UsageState fromNameField(String name) {
			for (UsageState state : values()) {
				if (state.name.equals(name)) {
					return state;
				}
			}
			return null;
		}
	}

	public ItemTypeFilterSettingChanger(String name) {
		super(name);
	}
	
	public static ItemStack setState(ItemStack stack, UsageState state) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		stack.getTagCompound().setString("TypeNameUsage", state.name);
		return stack;
	}
	
	public static ItemStack cycleState(ItemStack stack) {
		return setState(stack, getState(stack).getNext());
	}
	
	public static UsageState getState(ItemStack stack) {
		if (stack.getTagCompound() == null)
		{
			stack.setTagCompound(new NBTTagCompound());
		}
		return UsageState.fromNameField(stack.getTagCompound().getString("TypeNameUsage"));
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!player.isSneaking() && worldIn.getTileEntity(pos) instanceof MagicTypeFilterBlock) {
			MagicTypeFilterBlock block = (MagicTypeFilterBlock)worldIn.getTileEntity(pos);
			
			switch(getState(player.getHeldItem(hand))) {
				case DATA_TAG: {
					
					block.cycleDataType();
					return EnumActionResult.SUCCESS;
				}
				case LIST_OR_NOT: {

					block.cycleListOrNot();
					return EnumActionResult.SUCCESS;
				}
				case RESTRICT_ONLY_TAG_OR_NOT: {
					block.cycleRestrictOnly();
					
					return EnumActionResult.SUCCESS;
				} case IO_SIDE: {
					block.cycleSide(facing);
					
					return EnumActionResult.SUCCESS;
				}
				default: {
					return EnumActionResult.FAIL;
				}
			}
		}
		return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		if (playerIn.isSneaking()) {
			if (getState(playerIn.getHeldItem(handIn)) == null) {
				System.out.println("Confused item...");
			}
			playerIn.setHeldItem(handIn, cycleState(playerIn.getHeldItem(handIn)));
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if (getState(stack) == null) {
			setState(stack, UsageState.IO_SIDE);
		}
		tooltip.add(Translate.translate("typefilteritem." + getState(stack).name));
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}
	
}
