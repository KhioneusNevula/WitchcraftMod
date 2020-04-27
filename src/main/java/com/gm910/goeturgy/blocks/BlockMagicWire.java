package com.gm910.goeturgy.blocks;

import com.gm910.goeturgy.init.BlockInit;
import com.gm910.goeturgy.spells.components.MagicWire;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockMagicWire extends BlockBase {
	
	public static final PropertyDirection SIDE_PLACED = PropertyDirection.create("side_placed");

	public BlockMagicWire(String name) {
		super(name, Material.ANVIL, true, new MagicWireItemBlock(), CreativeTabs.MISC);
		this.setTileEntity("magic_wire", MagicWire.class);
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
		MagicWire wire = (MagicWire)worldIn.getTileEntity(pos);
		if (!playerIn.isSneaking()) {
			wire.toggleAsOutput(facing);
		} else {
			wire.toggleAsInput(facing);
		}
		return true;
	}
	
	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer) {
		
		IBlockState state = super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
		state = state.withProperty(SIDE_PLACED, facing.getOpposite());
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
		MagicWire wire = (MagicWire) worldIn.getTileEntity(pos);
		wire.resetInputSide();
		wire.makeInput(state.getValue(SIDE_PLACED));
	}
	
	public static class MagicWireItemBlock extends ItemBlock {

		public MagicWireItemBlock() {
			super(BlockInit.MAGIC_WIRE);
		}
		
		@Override
		public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side,
				float hitX, float hitY, float hitZ, IBlockState newState) {
			
			return super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
		}
		
	}

}
