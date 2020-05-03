package com.gm910.goeturgy.blocks;

import com.gm910.goeturgy.spells.components.Abacus;
import com.gm910.goeturgy.spells.components.PoppetMaker;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockPoppetMaker extends BlockBase {

	public BlockPoppetMaker(String name) {
		super(name, Material.WOOD, true, null, CreativeTabs.REDSTONE);
		this.setTileEntity("poppet_maker", PoppetMaker.class);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		//if (worldIn.isRemote) return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
		PoppetMaker pm = (PoppetMaker) worldIn.getTileEntity(pos);
		if (!playerIn.isSneaking() && !worldIn.isRemote) {
			playerIn.displayGUIChest(pm);
			//playerIn.displayGui(pm);
		}
		return true;//super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
	}
	
}
