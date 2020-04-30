package com.gm910.goeturgy.blocks;

import com.gm910.goeturgy.init.ItemInit;
import com.gm910.goeturgy.spells.components.MysticLodestone;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockMysticLodestone extends BlockBase {

	public BlockMysticLodestone(String name) {
		super(name, Material.ROCK);
		this.setTileEntity("mystic_lodestone", MysticLodestone.class);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		MysticLodestone stone = (MysticLodestone)worldIn.getTileEntity(pos);
		if (playerIn.getHeldItem(hand).getItem() == ItemInit.WAYSTONE) {
			if (stone.getItemStack().isEmpty()) {
				stone.setWaystone(playerIn.getHeldItem(hand));
				playerIn.setHeldItem(hand, ItemStack.EMPTY);
				return true;
			}
		} else if (playerIn.getHeldItem(hand).isEmpty()) {
			if (stone.getItemStack().getItem() == ItemInit.WAYSTONE) {
				playerIn.setHeldItem(hand, stone.getItemStack());
				stone.setWaystone(ItemStack.EMPTY);
			}
		}
		return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
	}
	
}
