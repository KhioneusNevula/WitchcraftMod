package com.gm910.goeturgy.blocks;

import com.gm910.goeturgy.spells.components.PotionDisperser;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemSpectralArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockPotionDispenser extends BlockBase {

	public BlockPotionDispenser(String name) {
		super(name, Material.ROCK);
		this.setTileEntity("potion_dispenser", PotionDisperser.class);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		PotionDisperser stone = (PotionDisperser)worldIn.getTileEntity(pos);
		if (playerIn.getHeldItem(hand).getItem() instanceof ItemPotion || 
				playerIn.getHeldItem(hand).getItem() instanceof ItemSpectralArrow) {
			if (stone.getPotion().isEmpty()) {
				stone.setPotion(playerIn.getHeldItem(hand));
				playerIn.setHeldItem(hand, ItemStack.EMPTY);
				return true;
			}
		} else if (playerIn.getHeldItem(hand).isEmpty()) {
				playerIn.setHeldItem(hand, stone.getPotion());
				stone.setPotion(ItemStack.EMPTY);
				return true;
		}
		return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
	}
	
}
