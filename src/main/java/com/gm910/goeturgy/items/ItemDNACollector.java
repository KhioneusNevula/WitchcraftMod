package com.gm910.goeturgy.items;

import java.util.List;
import java.util.UUID;

import com.gm910.goeturgy.Goeturgy;
import com.gm910.goeturgy.util.Translate;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class ItemDNACollector extends ItemBase {
	
	public static final String LINKED_DATA_TAG = "LinkedEntity";

	public ItemDNACollector(String name) {
		super(name);
	}
	

	public static ItemStack linkToEntity(UUID en, ItemStack stack) {
		NBTTagCompound tag = stack.getSubCompound(LINKED_DATA_TAG);
		
		if (en != null && tag != null) {
			tag.setUniqueId("Entity", en);
		}
		
		return stack;
	}
	
	public static UUID getLinkedUUID(ItemStack stack) {
		if (stack.getSubCompound(LINKED_DATA_TAG) == null) return null;
		//if (!stack.getTagCompound().hasKey(LINKED_DATA_TAG)) return null;
		return stack.getSubCompound(LINKED_DATA_TAG).getUniqueId("Entity");
	}
	
	public static Entity getEntity(ItemStack stack) {
		if (getLinkedUUID(stack) == null) return null;
		return Goeturgy.proxy.getServer().getEntityFromUuid(getLinkedUUID(stack));
	}
	
	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		if (worldIn.isRemote) return;
		if (!isSelected) return;
		if (worldIn.rand.nextInt(14) >= 2) return;
		
		Entity en = getEntity(stack);
		
		if (en == null) {
			return;
		}
		
		stack.getOrCreateSubCompound(LINKED_DATA_TAG).setString("Name", en.getDisplayName().getFormattedText());
		
	}
	
	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target,
			EnumHand hand) {
		if (!playerIn.isSneaking()) {
			ItemDNACollector.linkToEntity(target.getUniqueID(), stack);
			playerIn.setHeldItem(hand, stack);
			return true;
		}
		return super.itemInteractionForEntity(stack, playerIn, target, hand);
	}

	@Override
	public int getItemStackLimit() {
		// TODO Auto-generated method stub
		return 1;
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (player.isSneaking()) {
			ItemStack stack = player.getHeldItem(hand);
			ItemDNACollector.linkToEntity(player.getUniqueID(), stack);
			player.setHeldItem(hand, stack);
			return EnumActionResult.SUCCESS;
		}
		return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		
		super.addInformation(stack, worldIn, tooltip, flagIn);
		if (stack.getOrCreateSubCompound(LINKED_DATA_TAG).hasKey("Name", NBT.TAG_STRING)) {
			tooltip.add(Translate.translate("dnacollect.bound", stack.getOrCreateSubCompound(LINKED_DATA_TAG).getString("Name")));
		} else {
			tooltip.add(Translate.translate("pearl.unbound"));
		}
	}
	
}
