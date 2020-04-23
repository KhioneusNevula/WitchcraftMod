package com.gm910.goeturgy.items;

import com.gm910.goeturgy.Goeturgy;
import com.gm910.goeturgy.init.ItemInit;
import com.gm910.goeturgy.util.IHasModel;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public class ItemBase extends Item implements IHasModel {

	public ItemBase(String name, CreativeTabs tab) {
		setUnlocalizedName(name);
		setRegistryName(new ResourceLocation(Goeturgy.MODID, name));
		
		this.setCreativeTab(tab);
		ItemInit.ITEMS.add(this);
	}
	
	public ItemBase(String name) {
		this(name, CreativeTabs.MISC);
	}

	@Override
	public void registerModels() {
		// TODO Auto-generated method stub
		Goeturgy.proxy.registerItemRenderer(this, 0, "inventory");
	}
}
