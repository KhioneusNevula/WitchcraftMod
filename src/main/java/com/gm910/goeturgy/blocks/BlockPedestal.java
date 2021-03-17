package com.gm910.goeturgy.blocks;

import com.gm910.goeturgy.spells.components.Pedestal;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

public class BlockPedestal extends BlockBase {

	public BlockPedestal(String name) {
		super(name, Material.IRON);
		this.setTileEntity("pedestal", Pedestal.class);
	}
	
	@Override
	public boolean canSustainLeaves(IBlockState state, IBlockAccess world, BlockPos pos) {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction,
			IPlantable plantable) {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public boolean isFertile(World world, BlockPos pos) {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public boolean isFireSource(World world, BlockPos pos, EnumFacing side) {
		
		return true;
	}

}
