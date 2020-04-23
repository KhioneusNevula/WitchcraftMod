package com.gm910.goeturgy.init;

import java.util.ArrayList;
import java.util.List;

import com.gm910.goeturgy.blocks.BlockAbacus;
import com.gm910.goeturgy.blocks.BlockBase;
import com.gm910.goeturgy.blocks.BlockMagicalBarrier;
import com.gm910.goeturgy.blocks.BlockSpellDust;
import com.gm910.goeturgy.spells.components.InitiatorPearlBlock;
import com.gm910.goeturgy.spells.components.LightningSummoner;
import com.gm910.goeturgy.spells.components.MagicWire;
import com.gm910.goeturgy.spells.components.Pedestal;
import com.gm910.goeturgy.spells.components.PhilosophicCrystal;
import com.gm910.goeturgy.spells.components.WindChime;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockInit {

	public static final List<Block> BLOCKS = new ArrayList<>();
	
	/**SpellSpaceBorder**/
	public static final Block SPELL_DUST = new BlockSpellDust("spell_dust");
	
	/**SpellSpaceComponents**/
	public static final Block PEARL_BLOCK = (new BlockBase("pearl_block", Material.CORAL)).setTileEntity("pearl_block", InitiatorPearlBlock::new);
	public static final Block LIGHTNING_SUMMONER = (new BlockBase("lightning_summoner", Material.IRON)).setTileEntity("lightning_summoner", LightningSummoner::new);
	public static final Block WIND_CHIME = (new BlockBase("wind_chime", Material.CLOTH)).setTileEntity("wind_chime", WindChime::new);
	public static final Block ABACUS = (new BlockAbacus("abacus"));
	public static final Block MAGIC_WIRE = (new BlockBase("magic_wire", Material.ANVIL)).setTileEntity("magic_wire", MagicWire::new);
	public static final Block PHILOSOPHIC_CRYSTAL = (new BlockBase("philosophic_crystal", Material.ROCK)).setTileEntity("philosophic_crystal", PhilosophicCrystal.class);
	public static final Block PEDESTAL = (new BlockBase("pedestal", Material.ROCK)).setTileEntity("pedestal", Pedestal.class);
	
	/**Misc**/
	public static final Block MAGICAL_BARRIER = (new BlockMagicalBarrier("magical_barrier"));
	public static final Block CHALK_BLOCK = new BlockBase("chalk_block", Material.GROUND);
}
