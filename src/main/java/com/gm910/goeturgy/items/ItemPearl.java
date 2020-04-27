package com.gm910.goeturgy.items;

import java.util.HashMap;
import java.util.List;

import com.gm910.goeturgy.init.BlockInit;
import com.gm910.goeturgy.spells.spellspaces.SpellSpace;
import com.gm910.goeturgy.spells.spellspaces.SpellSpaces;
import com.gm910.goeturgy.util.ServerPos;
import com.gm910.goeturgy.util.Translate;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemPearl extends ItemBase {
	
	public static final String LINKED_DATA_TAG = "LinkedSpellBlock";

	public ItemPearl(String name) {
		super(name);
	}
	
	public static SpellSpace getSpellSpace(ItemStack stack) {
		return SpellSpaces.get().getById(stack.getSubCompound(LINKED_DATA_TAG) != null ? (stack.getSubCompound(LINKED_DATA_TAG).hasKey("SpellSpace") ? stack.getSubCompound(LINKED_DATA_TAG).getLong("SpellSpace") : -1) : -1);
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
		if (world1.getBlockState(pos).getBlock() == BlockInit.PEARL_BLOCK) {
			SpellSpace space = SpellSpaces.get().getByPosition(pos);
			stack.getOrCreateSubCompound(LINKED_DATA_TAG).setLong("SpellSpace", space != null ? space.getID() : -1);
			stack.getOrCreateSubCompound(LINKED_DATA_TAG).setBoolean("Broken", false);

		}  else {
			stack.getOrCreateSubCompound(LINKED_DATA_TAG).setBoolean("Broken", true);
		}
		
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		
		if (player.isSneaking()) {
			if (worldIn.getBlockState(pos).getBlock() == BlockInit.PEARL_BLOCK) {
				linkToBlock(new ServerPos(pos, worldIn), player.getHeldItem(hand));
			}
		} else {
			
			if (worldIn.isRemote) return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
			SpellSpace space = getSpellSpace(player.getHeldItem(hand));
			if (space != null) {
				if (!space.isFullyLoaded()) {
					space.forceLoad(false);
				}
				space.start(new ServerPos(pos, worldIn), new HashMap<>());
			}
		}
		return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		if (worldIn.isRemote) return super.onItemRightClick(worldIn, playerIn, handIn);
		
		SpellSpace space = getSpellSpace(playerIn.getHeldItem(handIn));
		
		if (space != null && !playerIn.isSneaking()) {
			
			if (!space.isFullyLoaded()) {
				space.forceLoad(false);
			}
			if (space.isFullyLoaded())
				space.start(new ServerPos(playerIn.getPosition(), worldIn), new HashMap<>());
			else
				System.out.println("Cannot load spellspace!");
		}
		
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		
		super.addInformation(stack, worldIn, tooltip, flagIn);
		ServerPos pos = getLinkedBlock(stack);
		if (pos == null) {
			tooltip.add(Translate.translate("pearl.unbound"));
		} else {
			if (stack.getOrCreateSubCompound(LINKED_DATA_TAG).getBoolean("Broken")) {
				tooltip.add(Translate.translate("pearl.broken", TextFormatting.RED, pos));
			} else {
				long id = stack.getOrCreateSubCompound(LINKED_DATA_TAG).getLong("SpellSpace");
				tooltip.add(Translate.translate("pearl.withspellspace", id == -1 ? Translate.translate("null", TextFormatting.RED) : id));
			}
		}
		
	}
	
}
