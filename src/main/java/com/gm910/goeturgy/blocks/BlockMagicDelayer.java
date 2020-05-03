package com.gm910.goeturgy.blocks;

import com.gm910.goeturgy.spells.components.MagicDelayer;
import com.gm910.goeturgy.spells.components.MagicWire.SideState;
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

public class BlockMagicDelayer extends BlockBase {
	
	public static final PropertyDirection SIDE_PLACED = PropertyDirection.create("side_placed");

	public BlockMagicDelayer(String name) {
		super(name, Material.ANVIL, true, null, CreativeTabs.MISC);
		this.setTileEntity("magic_delayer", MagicDelayer.class);
		this.setDefaultState(this.blockState.getBaseState().withProperty(SIDE_PLACED, EnumFacing.UP));
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		// TODO Auto-generated method stub
		return new BlockStateContainer(this, new IProperty[] {SIDE_PLACED} );
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		MagicDelayer wire = (MagicDelayer)worldIn.getTileEntity(pos);
		if (!playerIn.isSneaking()) {
			if (wire.getStateForSide(facing) == SideState.NONE) {
				wire.setStateForSide(facing, SideState.OUTPUT);
			} else if (wire.getStateForSide(facing) == SideState.OUTPUT) {
				wire.setStateForSide(facing, SideState.INPUT);
			} else {
				wire.setStateForSide(facing, SideState.NONE);
			}
		} else {
			
			wire.setDelay(wire.getDelay() + 1);

			System.out.println("Delayer value " + wire.getDelay());
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
		MagicDelayer wire = (MagicDelayer) worldIn.getTileEntity(pos);
		wire.resetInputSide();
		wire.makeInput(state.getValue(SIDE_PLACED));
	}
	

}
