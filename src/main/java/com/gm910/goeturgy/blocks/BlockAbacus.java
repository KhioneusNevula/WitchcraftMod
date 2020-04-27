package com.gm910.goeturgy.blocks;

import com.gm910.goeturgy.spells.components.Abacus;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockAbacus extends BlockBase {

	public BlockAbacus(String name) {
		super(name, Material.WOOD, true, null, CreativeTabs.REDSTONE);
		this.setTileEntity("abacus", Abacus.class);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
		Abacus abacus = (Abacus) worldIn.getTileEntity(pos);
		if (!playerIn.isSneaking()) {
			abacus.incrementValue();
		} else {
			abacus.setValue(abacus.getValue() - 1);
		}

		System.out.println("Abacus value " + abacus.getValue());
		return true;//super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
	}
	
}
