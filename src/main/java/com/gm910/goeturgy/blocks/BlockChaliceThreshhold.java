package com.gm910.goeturgy.blocks;

import com.gm910.goeturgy.spells.components.ChaliceThreshhold;
import com.gm910.goeturgy.spells.components.ChaliceThreshhold.SideState;
import com.gm910.goeturgy.spells.util.ISpellComponent;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockChaliceThreshhold extends BlockBase {
	
	public static final PropertyDirection SIDE_PLACED = PropertyDirection.create("side_placed");

	public BlockChaliceThreshhold(String name) {
		super(name, Material.ANVIL, true, null, CreativeTabs.MISC);
		this.setTileEntity("chalice_threshhold", ChaliceThreshhold.class);
		this.setDefaultState(this.blockState.getBaseState().withProperty(SIDE_PLACED, EnumFacing.DOWN));
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		// TODO Auto-generated method stub
		return new BlockStateContainer(this, new IProperty[] {SIDE_PLACED} );
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ChaliceThreshhold wire = (ChaliceThreshhold)worldIn.getTileEntity(pos);
		if (!playerIn.isSneaking()) {
			System.out.println("Side cycled to " + wire.cycle(facing));
		} else {
			
		}
		return true;
	}
	
	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer) {
		
		IBlockState state = super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
		if (worldIn.getTileEntity(pos.offset(facing.getOpposite())) instanceof ISpellComponent) {

			state = state.withProperty(SIDE_PLACED, facing.getOpposite());
		}
		return state;
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		// TODO Auto-generated method stub
		return state.getValue(SIDE_PLACED).getIndex();
	}
	
	public IBlockState getStateFromMeta(int meta) {
		// TODO Auto-generated method stub
		return this.getDefaultState().withProperty(SIDE_PLACED, EnumFacing.VALUES[meta]);
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer,
			ItemStack stack) {
		ChaliceThreshhold wire = (ChaliceThreshhold) worldIn.getTileEntity(pos);
		wire.resetGenericInputSide();
		wire.makeGenericInput(state.getValue(SIDE_PLACED));
	}
	

}
