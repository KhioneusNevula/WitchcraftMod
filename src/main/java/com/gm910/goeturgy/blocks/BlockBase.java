package com.gm910.goeturgy.blocks;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.gm910.goeturgy.Goeturgy;
import com.gm910.goeturgy.init.BlockInit;
import com.gm910.goeturgy.init.ItemInit;
import com.gm910.goeturgy.init.TileInit;
import com.gm910.goeturgy.util.GMReflection;
import com.gm910.goeturgy.util.IHasModel;
import com.google.common.base.Predicates;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class BlockBase extends Block implements IHasModel {
	
	protected Map<String, Supplier<? extends TileEntity>> tileSups = new HashMap<>();
	protected Map<String, Predicate<? super IBlockState>> tilePreds = new HashMap<>();
	

	public BlockBase(String name, Material materialIn, boolean makeItem, CreativeTabs tab) {
		super(materialIn);
		setUnlocalizedName(name);
		setRegistryName(new ResourceLocation(Goeturgy.MODID, name));
		setCreativeTab(tab);
		
		BlockInit.BLOCKS.add(this);
		if (makeItem) ItemInit.ITEMS.add((new ItemBlock(this)).setRegistryName(this.getRegistryName()));
		
	}

	public BlockBase addTile(Class<? extends TileEntity> clazz, String name) {
		TileInit.TILES.put(name, clazz);
		System.out.println("Added tile entity " + name + " of class " + clazz.getCanonicalName());
		return this;
	}
	
	public BlockBase setTileEntity(String name, Supplier<? extends TileEntity> clazz, Predicate<? super IBlockState> pred) {
		tileSups.put(name, clazz);
		tilePreds.put(name, pred);
		System.out.println("Put tile entity " + name);
		if (!TileInit.TILES.containsKey(name)) {
			addTile(clazz.get().getClass(), name);
		}
		return this;
	}
	
	public BlockBase setTileEntity(String name, Supplier<? extends TileEntity> clazz) {
		return this.setTileEntity(name, clazz, Predicates.alwaysTrue());
	}
	
	public BlockBase setTileEntity(String name, Supplier<? extends TileEntity> clazz, int meta) {
		return this.setTileEntity(name, clazz, (te) -> {return te.getBlock().getMetaFromState(te) == meta;});
	}
	
	public BlockBase setTileEntity(String name, Class<? extends TileEntity> clazz) {
		return this.setTileEntity(name, () -> {
			return GMReflection.construct(clazz);
		});
	}
	
	public BlockBase(String name, Material material, boolean makeItem) {
		this(name, material, makeItem, CreativeTabs.BUILDING_BLOCKS);
	}
	
	public BlockBase(String name, Material material) {
		this(name, material, true);
	}

	@Override
	public void registerModels() {
		// TODO Auto-generated method stub
		Goeturgy.proxy.registerItemRenderer(Item.getItemFromBlock(this), 0, "inventory");
	}
	
	
	@Override
	public boolean hasTileEntity(IBlockState state) {
		//System.out.println("em" + this.tileSups.isEmpty());
		return !this.tileSups.isEmpty();
	}
	
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		for (String sup : this.tilePreds.keySet()) {
			if (tilePreds.get(sup).test(state)) {
				System.out.println("Tile " + sup + " created");
				return tileSups.get(sup).get();
			}
		}
		System.out.println("Tile entity confusion??");
		return super.createTileEntity(world, state);
	}

}
