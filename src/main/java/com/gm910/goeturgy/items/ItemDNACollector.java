package com.gm910.goeturgy.items;

import java.util.List;
import java.util.Set;

import com.gm910.goeturgy.util.Translate;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
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
	/**
	 * DNA Format
	 * {Abilities: <ai list>, EntityData: <entity data>, Stats: <stats on health, etc>}
	 */
	public static final String LINKED_DATA_TAG = "StoredEntityData";
	

	public ItemDNACollector(String name) {
		super(name);
	}
	

	public static ItemStack storeDNA(EntityLivingBase en, ItemStack stack) {
		NBTTagCompound tag = stack.getOrCreateSubCompound(LINKED_DATA_TAG);
		
		if (en != null) {
			/*if (en instanceof EntityLiving) {
				EntityLiving enl = (EntityLiving )en;
				Set<EntityAITaskEntry> ais = enl.tasks.taskEntries;
				for (EntityAITaskEntry ent : ais) {
					
				}
			}*/
		}
		
		return stack;
	}
	
	public static NBTTagCompound getDNAData(ItemStack stack) {
		return stack.getSubCompound(LINKED_DATA_TAG);
	}
	
	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target,
			EnumHand hand) {
		if (!playerIn.isSneaking()) {
			ItemDNACollector.storeDNA(target, stack);
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
			ItemDNACollector.storeDNA(player, stack);
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
