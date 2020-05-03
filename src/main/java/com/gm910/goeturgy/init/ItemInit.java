package com.gm910.goeturgy.init;

import java.util.ArrayList;
import java.util.List;

import com.gm910.goeturgy.items.ItemBase;
import com.gm910.goeturgy.items.ItemDNACollector;
import com.gm910.goeturgy.items.ItemDustBag;
import com.gm910.goeturgy.items.ItemPearl;
import com.gm910.goeturgy.items.ItemTypeFilterSettingChanger;
import com.gm910.goeturgy.items.ItemWaystone;

import net.minecraft.item.Item;

public class ItemInit {

	public static final List<Item> ITEMS = new ArrayList<>();
	
	public static final Item CHALK_DUST = new ItemBase("chalk_dust");
	public static final Item PEARL = new ItemPearl("pearl");
	public static final Item DUST_BAG = new ItemDustBag("dust_bag");
	public static final Item WAYSTONE = new ItemWaystone("waystone");
	public static final Item DNA_COLLECTOR = new ItemDNACollector("dna_collector");
	public static final Item TYPE_FILTER_CYCLER = new ItemTypeFilterSettingChanger("type_filter_cycler");
}
