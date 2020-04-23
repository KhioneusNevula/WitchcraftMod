package com.gm910.goeturgy.init;

import java.util.ArrayList;
import java.util.List;

import com.gm910.goeturgy.items.ItemBase;
import com.gm910.goeturgy.items.ItemDustBag;

import net.minecraft.item.Item;

public class ItemInit {

	public static final List<Item> ITEMS = new ArrayList<>();
	
	public static final Item CHALK_DUST = new ItemBase("chalk_dust");
	public static final Item PEARL = new ItemBase("pearl");
	public static final Item DUST_BAG = new ItemDustBag("dust_bag");
}