package com.gm910.goeturgy.spells.spellspaces;

import java.util.List;
import java.util.Map;

import com.gm910.goeturgy.messages.types.IRunnableTask;
import com.gm910.goeturgy.spells.ioflow.SpellIOMap;
import com.gm910.goeturgy.spells.util.AetherStorage;
import com.gm910.goeturgy.util.GMNBT;
import com.gm910.goeturgy.util.NonNullMap;
import com.gm910.goeturgy.util.ServerPos;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

/**
 * A way to synchronize a spellspace from serverside to clientside by overriding a lot of server methods to do nothing
 * @author borah
 *
 */
public class ClientSpellSpace extends SpellSpace {
	
	private NBTTagCompound from;
	
	public ClientSpellSpace(NBTTagCompound comp) {
		super(comp);
		this.from = comp.copy();
	}
	
	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		this.dimension = nbt.getInteger("Dimension");
		this.shape = GMNBT.createList(nbt.getTagList("Shape", NBT.TAG_COMPOUND), (b) ->  NBTUtil.getPosFromTag((NBTTagCompound)b) );
		this.edges = GMNBT.createList(nbt.getTagList("Edges", NBT.TAG_COMPOUND), (b) ->  NBTUtil.getPosFromTag((NBTTagCompound)b) );
		this.id = nbt.getLong("Id");
		this.headPos = NBTUtil.getPosFromTag(nbt.getCompoundTag("Head"));
		this.height = nbt.getInteger("Height");
		
		(nbt.getTagList("SigPoints", NBT.TAG_COMPOUND)).forEach((b) -> {
			NBTTagCompound cmp = (NBTTagCompound)b;
			sigPoints.put(NBTUtil.getPosFromTag(cmp.getCompoundTag("Pos")), cmp.getCompoundTag("State"));
		});
		
		this.isSolid = nbt.getBoolean("Solid");
		this.energyStorage = new AetherStorage(nbt.getCompoundTag("Power"));
	}

	@Override
	public void decrementPower(int amount) {}

	
	@Override
	public int extractPower(int power) {return 0;}
	
	@Override
	public MinecraftServer getServer() { return null;}
	
	@Override
	public int getPower() { return 0;}
	
	@Override
	public void init(long id, int height, BlockPos head, List<BlockPos> edges, List<BlockPos> sigPoints) {}
	
	@Override
	public void causeEventOnClient(IRunnableTask event) {
		event.run();
	}
	
	@Override
	public boolean forceLoad(boolean justHead) {return false;}
	
	@Override
	public World getWorld() {return Minecraft.getMinecraft().world;}
	
	@Override
	public World getWorldSoft() {return getWorld();}
	
	@Override
	public void initShape() {}
	
	@Override
	public boolean isFullyLoaded() {return false;}
	
	@Override
	public void tick() {}
	
	@Override
	public void update(PlayerTickEvent event) {}
	
	@Override
	public void start(ServerPos runPos, Map<BlockPos, SpellIOMap> mp) {}
	
	@Override
	public void start(Entity en, Map<BlockPos, SpellIOMap> mp) {}
	
	@Override
	public NBTTagCompound serializeNBT() {
		return from;
	}
	
	@Override
	public void rightClick(RightClickBlock event) {}
	
	@Override
	public void partsInit() {}
	
	@Override
	public boolean isLoaded(BlockPos pos) {return false;}
	
	@Override
	public boolean isHeadLoaded() {return false;}
	
}
