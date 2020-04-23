package com.gm910.goeturgy.blocks;

import java.util.Random;

import com.gm910.goeturgy.init.ItemInit;
import com.gm910.goeturgy.spells.bordermakers.spell_dust.TileSpellDust;
import com.gm910.goeturgy.spells.bordermakers.spell_dust.TileSpellDustHead;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockSpellDust extends BlockBase implements ITileEntityProvider {

	public static final PropertyBool IS_HEAD = PropertyBool.create("ishead");
	public static final int SYMBOL_MAX = 7;
	public static final PropertyInteger SYMBOL = PropertyInteger.create("symbol", 0, SYMBOL_MAX-1);
	
	public static final int HEAD_META = 1;
	
	public BlockSpellDust(String name) {
		super(name, Material.CIRCUITS, false);
		this.setDefaultState(this.blockState.getBaseState().withProperty(IS_HEAD, false));
		this.addTile(TileSpellDust.class, "spell_dust");
		this.addTile(TileSpellDustHead.class, "spell_dust_head");
	}

	
	@Override
	public boolean hasTileEntity(IBlockState state) {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		// TODO Auto-generated method stub
		return this.getDefaultState().withProperty(IS_HEAD, meta == HEAD_META);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		// TODO Auto-generated method stub
		return state.getValue(IS_HEAD) ? HEAD_META : 0;
	}
	
	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		// TODO Auto-generated method stub
		return super.getActualState(state, worldIn, pos).withProperty(SYMBOL, (int)((new Random()).nextInt(SYMBOL_MAX)));
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		// TODO Auto-generated method stub
		return new BlockStateContainer(this, new IProperty[] {IS_HEAD, SYMBOL});
	}
	
	public boolean isHead(IBlockState state) {
		return state.getValue(IS_HEAD);
	}
	
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		if (state.getValue(IS_HEAD)) {
			return new TileSpellDustHead(); 
		} else {
			return new TileSpellDust();
		}
	}


	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		// TODO Auto-generated method stub
		return createTileEntity(worldIn, this.getStateFromMeta(meta));
	}
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		// TODO Auto-generated method stub
		return EnumBlockRenderType.MODEL;
	}
	
	@Override
	public BlockRenderLayer getBlockLayer() {
		
		return BlockRenderLayer.CUTOUT;
	}
	
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		// TODO Auto-generated method stub
		return super.getBoundingBox(state, source, pos).contract(0, 10, 0);
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		// TODO Auto-generated method stub
		return NULL_AABB;
	}

	/**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render
     */
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    public boolean isFullCube(IBlockState state)
    {
        return false;
    }
    
    @Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
    	// TODO Auto-generated method stub
    	return true;
    }

    /**
     * Checks if this block can be placed exactly at the given position.
     */
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
    {
        IBlockState downState = worldIn.getBlockState(pos.down());
        return downState.isTopSolid() || downState.getBlockFaceShape(worldIn, pos.down(), EnumFacing.UP) == BlockFaceShape.SOLID || worldIn.getBlockState(pos.down()).getBlock() == Blocks.GLOWSTONE;
    }
    
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        if (!worldIn.isRemote)
        {
            if (this.canPlaceBlockAt(worldIn, pos))
            {

            }
            else
            {
                this.dropBlockAsItem(worldIn, pos, state, 0);
                worldIn.setBlockToAir(pos);
            }
        }
    }
    
    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
    		int fortune) {
    }
    
    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
    	// TODO Auto-generated method stub
    	return new ItemStack(ItemInit.DUST_BAG);
    }
    
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face)
    {
        return BlockFaceShape.UNDEFINED;
    }
    
    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
    	// TODO Auto-generated method stub
    	return false;
    }
    
    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
    	// TODO Auto-generated method stub
    	return layer == BlockRenderLayer.CUTOUT || layer == BlockRenderLayer.TRANSLUCENT;
    }
    
}
