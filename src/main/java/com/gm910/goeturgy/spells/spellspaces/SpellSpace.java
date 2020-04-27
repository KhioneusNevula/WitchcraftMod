package com.gm910.goeturgy.spells.spellspaces;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.gm910.goeturgy.Goeturgy;
import com.gm910.goeturgy.init.BlockInit;
import com.gm910.goeturgy.messages.types.IRunnableTask;
import com.gm910.goeturgy.messages.types.TaskIllusion;
import com.gm910.goeturgy.messages.types.TaskParticles;
import com.gm910.goeturgy.spells.components.MagicWire;
import com.gm910.goeturgy.spells.events.space.ActivateSpellSpaceEvent;
import com.gm910.goeturgy.spells.events.space.ComponentActivationEvent;
import com.gm910.goeturgy.spells.events.space.EndSpellSpaceMagicEvent;
import com.gm910.goeturgy.spells.events.space.ForceLoadSpellSpace;
import com.gm910.goeturgy.spells.events.space.RemoveSpellSpaceEvent;
import com.gm910.goeturgy.spells.events.space.SpellSpaceTickingEvent;
import com.gm910.goeturgy.spells.ioflow.MagicIO;
import com.gm910.goeturgy.spells.ioflow.SpellIOMap;
import com.gm910.goeturgy.spells.util.AetherStorage;
import com.gm910.goeturgy.spells.util.ISpellBorder;
import com.gm910.goeturgy.spells.util.ISpellChainListener;
import com.gm910.goeturgy.spells.util.ISpellComponent;
import com.gm910.goeturgy.spells.util.ISpellObject;
import com.gm910.goeturgy.tileentities.TileEntityBase;
import com.gm910.goeturgy.util.GMNBT;
import com.gm910.goeturgy.util.NonNullMap;
import com.gm910.goeturgy.util.ServerPos;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;

import akka.japi.Pair;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

@EventBusSubscriber
public class SpellSpace implements ICapabilitySerializable<NBTTagCompound>{

	protected int dimension;
	
	protected String specialWorld;
	
	protected List<BlockPos> edges = new ArrayList<>();
	
	protected List<BlockPos> shape = new ArrayList<>();
	
	
	protected long id = -1;
	
	protected int height = 10;
	
	public static int AREA_TOLERANCE = 3000;
	

	protected BlockPos headPos;
	
	protected AetherStorage energyStorage = new AetherStorage(Integer.MAX_VALUE);

	protected Map<BlockPos, NBTTagCompound> sigPoints = new HashMap<>();
	
	//protected Map<BlockPos, NonNullMap<EnumFacing, NBTTagCompound>> lastTickInputs = new HashMap<>();

	protected List<SpellInstance> runningInstances = new ArrayList<>();
	
	public static List<IRunnableTask> runClients = new ArrayList<>();
	public static List<IRunnableTask> runServers = new ArrayList<>();
	
	protected boolean isSolid = false;
	
	
	public SpellSpace(int dimension) {
		this.dimension = dimension;
	}
	
	public boolean isRemoved() { 
		return id == -1;
	}
	
	public SpellSpace(NBTTagCompound cmp) {
		this(cmp.getInteger("Dimension"));
		this.deserializeNBT(cmp);
	}
	
	public void postInit() {
		System.out.println("Spell space generating particles now");
		for (int i = 0; i < 50; i++) {
			Random r = new Random();
			this.spawnParticles(EnumParticleTypes.FIREWORKS_SPARK, true, (new Vec3d(this.headPos)).add(new Vec3d(0.5, 0, 0.5)), new Vec3d(r.nextDouble() * 2 - 1,r.nextDouble() * 2 - 1,r.nextDouble() * 2 - 1), dimension, 1);
		}
	}
	
	public void initShape() {
		System.out.println("Initting shape");
		BlockPos pos = headPos;//edges.get(((new Random()).nextInt(edges.size())));
		List<Pair<EnumFacing, EnumFacing>> dirs = new ArrayList<>();
		for (EnumFacing face : EnumFacing.HORIZONTALS) {
			BlockPos newPos = pos.offset(face);
			if (!this.edges.contains(newPos)) {
				dirs.add(new Pair<> (face, null));
			} else {
				for (EnumFacing face2 : EnumFacing.HORIZONTALS) {
					if (face2 == face.getOpposite() || face2 == face) continue;
					newPos = (newPos.offset(face2));
					if (!this.edges.contains(newPos)) {
						dirs.add(new Pair<>(face, face2));
					}
				}
			}
		}
		dirs.removeAll(Collections.singleton(null));
		for (Pair<EnumFacing, EnumFacing> dir : dirs) {
			BlockPos startPos = dir.second() == null ? pos.offset(dir.first()) : pos.offset(dir.first()).offset(dir.second());
			List<BlockPos> shape1 = new ArrayList<>();
			boolean foundShape = false;
			
			
			foundShape = experimentalLoopFloodFill(startPos, shape1, AREA_TOLERANCE*2);//floodFill(0, shape1, startPos, AREA_TOLERANCE );
			
			
			
			if (foundShape) {
				this.shape = shape1;
				break;
			} else {
				//System.out.println("Malformed shape");
				continue;
			}
		}
		if (dirs.isEmpty()) {
			System.out.println("No dir pairs???");
		}
		if (shape.isEmpty()) {
			System.out.println("Malformed shape????");
			for (BlockPos pos2e : this.getInnerSpace()) {
				this.getWorld().createExplosion(null, pos2e.getX(), pos2e.getY(), pos2e.getZ(), 1, false);
				
			}
			SpellSpaces.get().removeSpellSpace(this);
		} else {
			System.out.println("Successfully made shape !!!!!!!!!!");
		}
	}
	
	public boolean experimentalLoopFloodFill(BlockPos starter, List<BlockPos> shape, int tolerance) {

		List<BlockPos> nextIteration = new ArrayList<>();
		nextIteration.add(starter);
		for (int i = 0; i < tolerance; i++) {
			
			List<BlockPos> nextIter = new ArrayList<>(nextIteration);
			nextIteration.clear();
			for (BlockPos checking : nextIter) {

				//getWorld().setBlockState(checking, Blocks.FIRE.getDefaultState());
				shape.add(checking);
				
				//boolean r = false;
				for (EnumFacing face : EnumFacing.HORIZONTALS) {
					if (!edges.contains(checking.offset(face)) && !shape.contains(checking.offset(face)) && !nextIteration.contains(checking.offset(face))) {
						//r = true;//floodFill(index+1, shape, starter.offset(face), tolerance);
						
						nextIteration.add(checking.offset(face));
					}
				}
				
			}
			if (nextIteration.size() > tolerance || shape.size() > tolerance) {
				
				return false;
			}
			if (nextIteration.isEmpty()) {
				break;
			}
			
		}
		
		return nextIteration.isEmpty();
	}
	
	
	public boolean floodFill(int index, List<BlockPos> shape, BlockPos starter, int tolerance) {
		if (index > tolerance) {
			System.out.println("Exceeded tolerance");
			return false;
		}
		//System.out.println("flood filling " + index);
		shape.add(starter);
		boolean r = true;
		for (EnumFacing face : EnumFacing.HORIZONTALS) {
			if (!edges.contains(starter.offset(face)) && !shape.contains(starter.offset(face))) {
				r = floodFill(index+1, shape, starter.offset(face), tolerance);
				
				if (!r) {
					return false;
				}
			}
		}
		return r;
	}
	
	public <T> List<BlockPos> getPositionsOfInstancesInSpace(Class<T> toCheck, Predicate<? super T> pred) {
		List<BlockPos> ls = new ArrayList<>();
		for (BlockPos pos : this.getInnerSpace()) {
			
			if (getWorld().getTileEntity(pos) != null && toCheck.isAssignableFrom(this.getWorld().getTileEntity(pos).getClass()) && pred.test((T) getWorld().getTileEntity(pos))) {
				ls.add(pos);
			}
		}
		return ls;
	}
	
	public <T> List<BlockPos> getPositionsOfInstancesInSpace(Class<T> toCheck) {
		return this.getPositionsOfInstancesInSpace(toCheck, Predicates.alwaysTrue());
	}
	
	public <T> List<T> getInstancesInSpace(Class<T> toCheck, Predicate<? super T> pred) {
		List<T> ls = new ArrayList<>();
		for (BlockPos pos : this.getInnerSpace()) {
			if (getWorld().getTileEntity(pos) != null && toCheck.isAssignableFrom(this.getWorld().getTileEntity(pos).getClass()) && pred.test((T)this.getWorld().getTileEntity(pos))) {
				ls.add((T) getWorld().getTileEntity(pos));
			}
		}
		return ls;
	}
	
	public BlockPos getHeadPos() {
		return headPos;
	}
	
	public void setHeadPos(BlockPos headPos) {
		this.headPos = headPos;
	}

	public void setHeight(int height) {
		this.height = height;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	
	public <T> List<T> getInstancesInSpace(Class<T> toCheck) {
		return this.getInstancesInSpace(toCheck, Predicates.alwaysTrue());
	}
	
	public List<BlockPos> getPositionsOfComponents() {
		return getPositionsOfInstancesInSpace(ISpellComponent.class);
	}
	
	public List<ISpellComponent> getComponents() {
		return getInstancesInSpace(ISpellComponent.class);
	}
	
	public ISpellComponent getComponent(BlockPos pos) {
		return this.getWorld().getTileEntity(pos) instanceof ISpellComponent ? (ISpellComponent)this.getWorld().getTileEntity(pos) : null;
	}
	
	public ISpellBorder getBorderPiece(BlockPos pos) {
		return this.getWorld().getTileEntity(pos) instanceof ISpellBorder ? (ISpellBorder) this.getWorld().getTileEntity(pos) : null;
	}
	
	public SpellSpaces getSpellSpaceRegistry() {
		return SpellSpaces.get();
	}
	
	public void decrementPower(int amount) {
		getSpellSpaceRegistry().addPower(this.extractPower(amount));
	}
	
	public void incrementPower(int amount) {
		getSpellSpaceRegistry().removePower(this.putPower(amount));
	}
	

		
	public List<BlockPos> getAdjacents(BlockPos start, boolean includeVertical) {
		List<EnumFacing> f = Lists.newArrayList(includeVertical ? EnumFacing.VALUES : EnumFacing.HORIZONTALS);
		List<BlockPos> poses = new ArrayList<>();
		for (EnumFacing e : f) {
			poses.add(start.offset(e));
		}
		return poses;
	}
	
	public List<ISpellComponent> getSpellComponentsFor(List<BlockPos> poses ) {
		List<ISpellComponent> ls = new ArrayList<>();
		for (BlockPos p : poses) {
			if (getComponent(p) != null) ls.add(this.getComponent(p));
		}
		return ls;
	}

		
	public int getPower() {
		return this.energyStorage.getEnergyStored();
	}
	
	public int putPower(int power) {
		return energyStorage.receiveEnergy(power, false);
	}
	
	public int extractPower(int power) {
		return energyStorage.extractEnergy(power, false);
	}
	

	
	public ISpellChainListener getChainListener(BlockPos pos) {
		
		return this.getComponent(pos) instanceof ISpellChainListener ? (ISpellChainListener)this.getComponent(pos) : null;
	}
	

	
	public void makeBordersSolid(boolean solid) {
		this.isSolid = solid;
		for (BlockPos pos : this.getOutline()) {
			if (getSpellObject(pos) instanceof ISpellBorder) {
				((ISpellBorder)getSpellObject(pos)).setSolid(solid);
			} 
			if (getWorld().getBlockState(pos).getMaterial().isReplaceable()) {
				if (solid) {
					getWorld().setBlockState(pos, BlockInit.MAGICAL_BARRIER.getDefaultState());
					getSpellObject(pos).setSpaceID(this.id);
					getSpellObject(pos).setSpellSpace(this);
				}
			}
			
		}
	}
	
	public void causeEventOnClient(IRunnableTask event) {
		//Messages.INSTANCE.sendToAll(new StringMessage(event));
		runClients.add(event);
	}
	
	public ISpellObject getSpellObject(BlockPos pos) {
		return getWorld().getTileEntity(pos) instanceof ISpellObject ? (ISpellObject) getWorld().getTileEntity(pos) : null;
	}
	
	public boolean areBordersSolid() {
		
		return isSolid;
		
	}
	
	
	
	public void start() {
		start((ServerPos)null, new HashMap<>());
	}
	
	public void start(Entity en, Map<BlockPos, SpellIOMap> mp) {
		if (isRemoved()) {
			System.out.println("Spell space cannot run because it has been removed");
		}
		SpellInstance runner = new SpellInstance(en);
		this.runningInstances.add(runner);
		runner.start(mp);
	}
	
	public void start(ServerPos runPos, Map<BlockPos, SpellIOMap> mp) {
		if (isRemoved()) {
			System.out.println("Spell space cannot run because it has been removed");
		}
		SpellInstance runner = new SpellInstance(runPos);
		this.runningInstances.add(runner);
		runner.start(mp);
	}
	
	public void tick() {
		if (isRemoved()) {
			return;
		}
	
		List<SpellInstance> runners = new ArrayList<>(this.runningInstances);
		for (SpellInstance run : runners) {
			run.tick();
		}
		if (this.shape.isEmpty() && !this.edges.isEmpty()) {
			this.initShape();
		}
		if (this.edges.isEmpty()) {
			System.out.println("SpellSpace has no edges!");
		}
		for (BlockPos pos : this.sigPoints.keySet()) {
			if (!NBTUtil.writeBlockState(new NBTTagCompound(), getWorld().getBlockState(pos)).equals(sigPoints.get(pos))) {
				SpellSpaces.get().removeSpellSpace(this);
			}
		}
		
		
	}
	
	public boolean isRunning() {
		return !this.runningInstances.isEmpty();
	}
	
	public void end(SpellInstance runner) {
		this.runningInstances.remove(runner);
	}
	
	
	
	public void init(long id, int height, BlockPos head, List<BlockPos> edges, List<BlockPos> sigPoints) {
		this.edges = edges;
		this.headPos = head;
		System.out.println("%% Spellspace init %%");
		for (BlockPos pos : sigPoints) {
			IBlockState b = getWorld().getBlockState(pos);
			
			this.sigPoints.put(pos, NBTUtil.writeBlockState(new NBTTagCompound(), b));
		}
		this.id = id;
		this.height = height;

		
		this.initShape();

		partsInit();
		postInit();
	}
	
	public void partsInit() {
		System.out.println("Adding all parts to space");
		for (BlockPos pos : this.getInnerSpace()) {
			if (getWorld().getTileEntity(pos) instanceof ISpellObject) {
				
				//System.out.println(""+ getWorld().getTileEntity(pos).getClass() + " "+getWorld().getTileEntity(pos).hashCode());
				ISpellObject part = (ISpellObject) getWorld().getTileEntity(pos);
				if (part.getSpellSpace() == null) {
					part.setSpellSpace(this);
					part.setSpaceID(id);
				} else {
					SpellSpace space = part.getSpellSpace();
					if (space == this) {
						//System.out.println("Why am I trying to add my own part to myself...?");
						continue;
					} else {
						if (part.getSpellSpace().shape.size() > this.shape.size()) {
							part.setSpellSpace(this);
							part.setSpaceID(id);
						} else if (part.getSpellSpace().shape.size() < this.shape.size()) {
							System.out.println("Found a smaller spellspace inside bigger one, and the smaller spellspace will be left as it is");
						} else {
							System.out.println("HOW ARE TWO IDENTICAL SPELLSPACES IN THE SAME PLACE???");
							SpellSpaces.get().removeSpellSpace(this);
						}
					}
				}
			}
		}
	}
	
	public World getWorld() {
		
		return FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dimension);
	}
	
	public World getWorldSoft() {
		return DimensionManager.getWorld(this.dimension);
	}
	
	public MinecraftServer getServer() {
		if (Minecraft.getMinecraft() != null) {
			return null;
		}
		return FMLCommonHandler.instance().getMinecraftServerInstance();
	}
	
	public long getID() {
		return id;
	}
	
	public void setID(long id) {
		this.id = id;
	}
	
	public int getDimension() {
		return dimension;
	}
	
	public List<BlockPos> getEdges() {
		return new ArrayList<>(edges);
	}
	
	public void addEdge(BlockPos pos) {
		this.edges.add(pos);
		System.out.println("We just added a block to the outline...?");
	}
	
	public List<BlockPos> getShape() {
		return new ArrayList<>(shape);
	}
	
	public List<BlockPos> getTotalShape() {
		List<BlockPos> sh = new ArrayList<>(shape);
		sh.addAll(edges);
		return sh;
	}

	public List<BlockPos> getInnerSpace() {
		List<BlockPos> sh = new ArrayList<>(getTotalShape());
		for (BlockPos pos : getTotalShape()) {
			for (int i = 1; i < this.height; i++) {
				sh.add(new BlockPos(pos.getX(), pos.getY() + i, pos.getZ()));
			}
		}
		return sh;
	}

	public List<BlockPos> getOutline() {
		List<BlockPos> sh = new ArrayList<>(getEdges());
		for (BlockPos pos : new ArrayList<>(sh)) {
			for (int i = 1; i < this.height; i++) {
				sh.add(new BlockPos(pos.getX(), pos.getY() + i, pos.getZ()));
			}
		}
		sh.addAll(this.getShape());
		for (BlockPos pos : this.getShape()) {
			sh.add(pos.up(height - 1));
		}
		return sh;
	}
	
	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger ("Dimension", this.dimension);
		nbt.setTag("Shape", GMNBT.makePosList(shape));
		nbt.setTag("Edges", GMNBT.makePosList(edges));
		nbt.setLong("Id", this.id);
		nbt.setTag("Head", NBTUtil.createPosTag(headPos));
		//nbt.setInteger("Ticks", this.ticks);
		nbt.setInteger("Height", this.height);
		nbt.setBoolean("Solid", isSolid);
		/*
		 * nbt.setTag("LastTickInputs", GMNBT.makeList(lastTickInputs.keySet(), (pos) ->
		 * { NBTTagCompound cmp = new NBTTagCompound(); cmp.setTag("Pos",
		 * ServerPos.toNBT(pos)); cmp.setTag("Inps",
		 * MagicIO.nonNullMapToNBT(lastTickInputs.get(pos))); return cmp; }));
		 */
		if (specialWorld != null) nbt.setString("SpecialWorld", specialWorld);
		//nbt.setTag("PrevPos", GMNBT.makeList(nextPositions, (pos) -> NBTUtil.createPosTag(pos)));
		nbt.setTag("Power", energyStorage.serializeNBT());
		nbt.setTag("SigPoints", GMNBT.makeList(sigPoints.keySet(), (pos) -> {
			NBTTagCompound cmp = new NBTTagCompound();
			cmp.setTag("State", sigPoints.get(pos));
			cmp.setTag("Pos", NBTUtil.createPosTag(pos));
			return cmp;
		}));
		runningInstances.removeAll(Collections.singleton(null));
		nbt.setTag("Runners", GMNBT.makeList(this.runningInstances, (run) -> {
			return run.serializeNBT();
		}));
		//if (magickingPos != null) nbt.setTag("MagickingPos", NBTUtil.createPosTag(magickingPos));
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		this.dimension = nbt.getInteger("Dimension");
		this.shape = GMNBT.createPosList(nbt.getTagList("Shape", NBT.TAG_COMPOUND));
		this.edges = GMNBT.createPosList(nbt.getTagList("Edges", NBT.TAG_COMPOUND));
		this.id = nbt.getLong("Id");
		this.headPos = NBTUtil.getPosFromTag(nbt.getCompoundTag("Head"));
		//this.ticks = nbt.getInteger("Ticks");
		this.height = nbt.getInteger("Height");
		this.isSolid = nbt.getBoolean("Solid");
		if (nbt.hasKey("SpecialWorld")) specialWorld = nbt.getString("SpecialWorld");
		//this.nextPositions = GMNBT.createList(nbt.getTagList("PrevPos", NBT.TAG_COMPOUND), (b) ->  NBTUtil.getPosFromTag((NBTTagCompound)b) );
		this.energyStorage = new AetherStorage(nbt.getCompoundTag("Power"));
		(nbt.getTagList("SigPoints", NBT.TAG_COMPOUND)).forEach((b) -> {
			NBTTagCompound cmp = (NBTTagCompound)b;
			sigPoints.put(NBTUtil.getPosFromTag(cmp.getCompoundTag("Pos")), cmp.getCompoundTag("State"));
		});
		this.runningInstances = GMNBT.createList(nbt.getTagList("Runners", NBT.TAG_COMPOUND), (base) -> {
			NBTTagCompound comp = (NBTTagCompound)base;
			SpellInstance runner = this.new SpellInstance(comp);
			return runner;
		});
		this.partsInit();
		//this.lastTickInputs.clear();;
		/*
		 * GMNBT.forEach(nbt.getTagList("LastTickInputs", NBT.TAG_COMPOUND), (c) -> {
		 * NBTTagCompound cmp = (NBTTagCompound)c; BlockPos p = ServerPos.fromNBT(cmp);
		 * NonNullMap<EnumFacing, NBTTagCompound> ims =
		 * MagicIO.nonNullMapFromNBT(cmp.getTagList("Inps", NBT.TAG_COMPOUND));
		 * lastTickInputs.put(p, ims); });
		 */
		//if (nbt.hasKey("MagickingPos")) magickingPos = NBTUtil.getPosFromTag(nbt.getCompoundTag("MagickingPos"));
	}
	
	@SubscribeEvent
	public static void server(ServerTickEvent event) {
		for (IRunnableTask task : runServers) {
			task.run();
		}
	}
	
	@SubscribeEvent
	public void update(PlayerTickEvent event) {
		if (event.player.world.isRemote) return;
		if (event.player.world.provider.getDimension() != this.dimension) return;
		if (isRemoved()) return;
		for (BlockPos pos : this.getOutline()) {
			//renderIllusion(Blocks.LEAVES2.getDefaultState(), new ServerPos(pos, dimension));
			spawnParticles(EnumParticleTypes.TOWN_AURA, true, (new Vec3d(pos)).add(new Vec3d(0.5, 0.5, 0.5)), new Vec3d(1, 1, 1), dimension, 1);
		}
		renderIllusion(Blocks.FIRE.getDefaultState(), new ServerPos(headPos, dimension));
		/*for (BlockPos pos : this.getOutline()) {
			//renderIllusion(Blocks.LEAVES2.getDefaultState(), new ServerPos(pos, dimension));
			spawnParticles(EnumParticleTypes.TOWN_AURA, true, (new Vec3d(pos)).add(new Vec3d(0.5, 0.5, 0.5)), new Vec3d(1, 1, 1));
		}*/
		//SpellSpaces sp = SpellSpaces.get();
		SpellSpace space = this;
		//if (sp != null) {
			//for (SpellSpace space : sp.getAsList()) {
		space.tick();
		if (space.shape.isEmpty()) {
			space.initShape();
		}
		/*for (BlockPos pos : this.getPositionsOfInstancesInSpace(ISpellComponent.class, (e) -> e.isStarter() && e.getSpellSpace() == this)) {
			if (event.player.world.isBlockPowered(pos)) {
				System.out.println("Starter power activation");
				
				//space.start();
			}
		}*/
		
			//}
		//} else {
			//System.out.println("World SpellSpace data broken or missing");
		//}
	}
	
	
	
	
	@SubscribeEvent
	public void rightClick(RightClickBlock event) {
		if (event.getWorld().isRemote) return;
		if (event.getWorld() != this.getWorldSoft()) {
			return;
		}
		if (isRemoved()) return;
		if (this.getComponent(event.getPos()) != null && this.getComponent(event.getPos()).isStarter() && !event.getEntityPlayer().isSneaking()) {
			this.start();
		}
	}
	
	@SubscribeEvent
	public static void clientTick(RenderWorldLastEvent event) {
		
		try {
			runClients.removeAll(Collections.singleton(null));
		} catch (Throwable e) {
			return;
		}
		try {
			List<IRunnableTask> ls = new ArrayList<>(runClients);
			List<IRunnableTask> delegate = new ArrayList<>(runClients);
	
			
			for (IRunnableTask runn : ls) {
				runn.run();
				delegate.remove(runn);
			}
			runClients.retainAll(delegate);
		} catch (Throwable e) {
			return;
		}
	}
	
	public void spawnParticles(EnumParticleTypes type, boolean thatDistanceThing, Vec3d pos, Vec3d sped, int d, int count, int...args) {
		this.causeEventOnClient(new TaskParticles(type, thatDistanceThing, pos, sped, d, count, args));
	}
	
	public void renderIllusion(IBlockState state, ServerPos pos) {
		
		this.causeEventOnClient(new TaskIllusion(pos, state));
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		
		return capability == CapabilityEnergy.ENERGY;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		// TODO Auto-generated method stub
		return hasCapability(capability, facing) ? (T) energyStorage : null;
	}
	
	public boolean isLoaded(BlockPos pos) {
		if (FMLCommonHandler.instance().getMinecraftServerInstance() == null) {
			return Minecraft.getMinecraft().world.isBlockLoaded(pos);
		} else {
			World world = this.getWorldSoft();
			if (world == null) {
				return false && !isRemoved();
			} else {
				ChunkPos pos1 = new ChunkPos(pos);
				if (world.getChunkProvider().isChunkGeneratedAt(pos1.x, pos1.z)) {
					return true && !isRemoved();
				}
			}
		}
		return false && !isRemoved();
	}
	
	public boolean isHeadLoaded() {
		return isLoaded(this.headPos) && !isRemoved();
	}
	
	public boolean isFullyLoaded() {
		boolean loaded = true;
		for (BlockPos pos : this.getTotalShape()) {
			loaded = loaded && isLoaded(pos);
		}
		return loaded && !isRemoved();
	}
	
	/**
	 * True if can load, false if otherwise
	 * True if already loaded
	 * @param justHead
	 * @return
	 */
	public boolean forceLoad(boolean justHead) {
		if (justHead ? isHeadLoaded() : isFullyLoaded()) {
			return true;
		}
		if (isRemoved()) return false;
		if (MinecraftForge.EVENT_BUS.post(new ForceLoadSpellSpace(this, justHead))) {
			System.out.println("Force loading of spellspace canceled");
			return false;
		}
		System.out.println("Forceloading " + (justHead ? "spell space head at " + headPos : "spell space at " + headPos) );
		if (justHead) {
			ChunkPos pos = new ChunkPos(headPos);
			return this.getWorld().getChunkProvider().provideChunk(pos.x, pos.z) != null;
		} else {
			boolean success = true;
			for (BlockPos bpos : this.getTotalShape()) {
				ChunkPos pos = new ChunkPos(headPos);
				success = success && this.getWorld().getChunkProvider().provideChunk(pos.x, pos.z) != null;
			}
			return success;
		}
	}
	
	@SubscribeEvent
	public static void spellSpace(RemoveSpellSpaceEvent event) {
		System.out.println(event.getSpellSpace().id + " listening for removal");
		for (int i = 0; i < 50; i++) {
			Random r = new Random();
			event.getSpellSpace().spawnParticles(EnumParticleTypes.LAVA, true, (new Vec3d(event.getSpellSpace().headPos)).add(new Vec3d(0.5, 0, 0.5)), new Vec3d(r.nextDouble() * 2 - 1,r.nextDouble() * 2 - 1,r.nextDouble() * 2 - 1), event.getSpellSpace().dimension, 1);
				
		}

	}
	
	public class SpellInstance implements INBTSerializable<NBTTagCompound> {
		
		public BlockPos magickingPos = null;
		
		protected ServerPos runPos;
		
		protected Entity runEntity;
		
		protected List<BlockPos> nextPositions = new ArrayList<>();
		
		protected int ticks = 0;
		
		protected List<BlockPos> prevPositions = new ArrayList<>();
		
		protected Map<BlockPos, NonNullMap<EnumFacing, NBTTagCompound>> inputsForTick = new NonNullMap<>(() -> new NonNullMap<>(NBTTagCompound::new));

		protected List<SpellSpace> miniSpaces = new ArrayList<>();

		protected List<ISpellBorder> miniPositions = new ArrayList<>();
		
		public SpellInstance(NBTTagCompound comp) {
			this.deserializeNBT(comp);
		}
		
		public SpellInstance(@Nullable ServerPos runPos) {
			this.magickingPos = SpellSpace.this.headPos;
			this.runPos = runPos;
		}
		
		public SpellInstance(@Nullable Entity entity) {
			this((ServerPos)null);
			this.runEntity = entity;
		}
		
		public ServerPos getRunPos() {
			return runPos;
		}
		
		public Entity getRunEntity() {
			return runEntity;
		}

		@Override
		public NBTTagCompound serializeNBT() {
			NBTTagCompound cmp = new NBTTagCompound();
			cmp.setTag("NextPositions", GMNBT.makePosList(nextPositions));

			cmp.setTag("PrevPositions", GMNBT.makePosList(prevPositions));
			
			cmp.setTag("MiniPositions", GMNBT.makeList(miniPositions, (p) -> {return NBTUtil.createPosTag(p.getPos());}));
			
			cmp.setInteger("Ticks", ticks);
			if (runPos != null) {
				cmp.setTag("RunPos", runPos.toNBT());
			}
			
			if (runEntity != null) {
				cmp.setUniqueId("RunEntity", runEntity.getUniqueID());
			}
			if (magickingPos != null) {
				cmp.setTag("MagickingPos", NBTUtil.createPosTag(magickingPos));
			}
			
			cmp.setTag("Inputs", GMNBT.makeList(inputsForTick.keySet(), (pos) -> {
				NBTTagCompound e = new NBTTagCompound();
				e.setTag("Pos", NBTUtil.createPosTag(pos));
				e.setTag("Map", MagicIO.nonNullMapToNBT(inputsForTick.get(pos)));
				return e;
			}));
			cmp.setTag("MiniSpaces", GMNBT.makeList(miniSpaces, (sp )-> {return new NBTTagLong(sp.id);} )); 
			return cmp;
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt) {
			nextPositions = GMNBT.createPosList(nbt.getTagList("NextPositions", NBT.TAG_COMPOUND));
			
			miniPositions = GMNBT.createList(nbt.getTagList("NextPositions", NBT.TAG_COMPOUND), (n) -> {
				NBTTagCompound comp = (NBTTagCompound)n;
				return getBorderPiece(NBTUtil.getPosFromTag(comp));
			});
			
			prevPositions = GMNBT.createPosList(nbt.getTagList("PrevPositions", NBT.TAG_COMPOUND));
			this.miniSpaces = GMNBT.createList(nbt.getTagList("MiniSpaces", NBT.TAG_LONG), ( tg) -> {return SpellSpaces.get().getById(((NBTTagLong)tg).getLong());} );
			ticks = nbt.getInteger("Ticks");
			if (nbt.hasKey("MagickingPos")) {
				magickingPos = NBTUtil.getPosFromTag(nbt.getCompoundTag("MagickingPos"));
			}
			if (nbt.hasKey("RunPos")) {
				this.runPos = ServerPos.fromNBT(nbt.getCompoundTag("RunPos"));
			}
			if (!nbt.getUniqueId("RunEntity").equals(new UUID(0,0))) {
				this.runEntity = Goeturgy.proxy.getServer().getEntityFromUuid(nbt.getUniqueId("RunEntity"));
			}
			this.inputsForTick = GMNBT.createMap(nbt.getTagList("Inputs", NBT.TAG_COMPOUND), ( base) -> {
				
				NBTTagCompound cmp = (NBTTagCompound)base;
				
				return new Pair<BlockPos, NonNullMap<EnumFacing, NBTTagCompound>>(NBTUtil.getPosFromTag(cmp.getCompoundTag("Pos")), MagicIO.nonNullMapFromNBT(cmp.getTagList("Map", NBT.TAG_COMPOUND)));
			});
		}
		

		public void setTicks(int ticks) {
			this.ticks = ticks;
		}
		
		public int getTicks() {
			return ticks;
		}
		
		public boolean isRunning() {
			return ticks != 0;
		}
		
		public boolean putInputIntoMiniSpellSpace(BlockPos pos, NonNullMap<EnumFacing, NBTTagCompound> cmp) {
			ISpellBorder bord = getBorderPiece(pos);
			if (bord == null) return false;
			SpellSpace sp = bord.getSpellSpace();
			if (sp == SpellSpace.this || sp == null) return false;
			List<ISpellComponent> comps = sp.getInstancesInSpace(ISpellComponent.class, (e) -> e.isStarter());
			Map<BlockPos, SpellIOMap> map = new HashMap<>();
			comps.forEach( (c) -> {
				map.put(c.getPos(), new SpellIOMap((e) -> cmp.get(e).copy()));
				System.out.println(cmp);
			});
			
			if (runPos != null) {
				sp.start(this.runPos, map);
			} else {
				System.out.println("SubSpellSpace activation");
				sp.start(this.runEntity, map);
			}
			return true;
		}
			
		public void disseminateStaticOutput(BlockPos pos, boolean vertical) {
			ISpellComponent comp = getComponent(pos);
			if (comp == null) return;
			ServerPos modPos = comp.getServerPos();
			if (this.runPos != null) {
				modPos = runPos;
			}
			NonNullMap<EnumFacing, NBTTagCompound> returns = runEntity != null ? comp.getStaticOutput(runEntity, modPos) : comp.getStaticOutput(modPos);
			for (EnumFacing face : (vertical ? EnumFacing.VALUES : EnumFacing.HORIZONTALS)) {
				ISpellComponent other = getComponent(pos.offset(face));
				if (other == null) {
					//putInputIntoMiniSpellSpace(pos.offset(face), returns);
					continue;
				}
				
				NBTTagCompound cmp = returns.get(face);
				if (other.accepts(face.getOpposite(), cmp)) {
					//NonNullMap<EnumFacing, NBTTagCompound> inps = new NonNullMap<>(NBTTagCompound::new);
					//inps.put(face.getOpposite(), cmp);
					//other.getInputs().put(face.getOpposite(), cmp);
					this.putInputs(other, face.getOpposite(), cmp);
					//this.nextPositions.add(other.getPos());
					if (other instanceof MagicWire) {
						this.markForActivationNextTick(other.getPos(), null);
					}
				}
			}
		}
		public <T extends TileEntity> NonNullMap<EnumFacing, NBTTagCompound> activate(NonNullMap<EnumFacing, NBTTagCompound> inputs, ISpellComponent comp, boolean statique) {
			if (MinecraftForge.EVENT_BUS.post(new ComponentActivationEvent<T>(this, comp))) {
				System.out.println("Activation of " + comp.getString() + " canceled");
				return null;
			}
			
			ServerPos modPos = comp.getServerPos();
			if (this.runPos != null) {
				modPos = runPos;
			}
			
			NonNullMap<EnumFacing, NBTTagCompound> returns = statique ? comp.activate(this, modPos) : (runEntity != null ? comp.activate(this, runEntity, inputs) : comp.activate(this, modPos, inputs));
			
			if (comp instanceof TileEntityBase) {
				((TileEntityBase)comp).sync();
			}
			
			System.out.println("Activation of " + comp.getString() + (returns == null ? " unsuccessful" : " successful"));
			return returns;
		}
		
		public <T extends TileEntity> NonNullMap<EnumFacing, NBTTagCompound> activate(NonNullMap<EnumFacing, NBTTagCompound> inputs, T c, boolean statique) {
			
			return this.<T>activate(inputs, (ISpellComponent)c, statique);
		}
		
		
		public void markForActivationNextTick(BlockPos toActivate, NonNullMap<EnumFacing, NBTTagCompound> inputs) {
			ISpellComponent comp = getComponent(toActivate);
			
			if (comp == null) {
				return;
			}
			
			this.nextPositions.add(toActivate);
			if (inputs != null) {
				inputsForTick.put(comp.getPos(), inputs);
			}
		}
		
		public void putInputs(ISpellComponent comp, EnumFacing f, NBTTagCompound inputs) {
			NonNullMap<EnumFacing, NBTTagCompound> inps = comp.getInputs();
			inps.put(f, inputs);

			inputsForTick.put(comp.getPos(), inps);
		}
		
		public void activateAndDisseminateOutput(BlockPos pos, boolean starter, NonNullMap<EnumFacing, NBTTagCompound> inputs, List<BlockPos> prevPositions) {
			
			ISpellComponent comp = getComponent(pos);
			if (comp == null) return;
			int energy = comp.getRequiredPowerFromNBT(inputs, comp.getServerPos());
			
			ISpellChainListener listener = getChainListener(pos);
			
			List<ISpellChainListener> listeners = getInstancesInSpace(ISpellChainListener.class);
			
			for (BlockPos pos4 : getInnerSpace()) {
				if (getComponent(pos4) instanceof ISpellChainListener && !listeners.contains((ISpellChainListener)getComponent(pos4))) {
					listeners.add(((ISpellChainListener)getComponent(pos4)));
				}
			}
			
			NonNullMap<EnumFacing, NBTTagCompound> returns = this.activate(inputs, comp, starter);//starter ? comp.activateStatic() : comp.activate(inputs);
			if (returns != null) {
				decrementPower(energy);

				for (ISpellChainListener lis : listeners) {
					if (lis.isPartOfChain(this, pos)) {
						lis.activated(this, pos);
					}
				}
			} else {
				this.end(false, pos);
				for (ISpellChainListener lis : listeners) {
					if (lis.isPartOfChain(this, pos)) {
						lis.finished(this, pos, false);
					}
				}
				return;
			}
			
			
			boolean didSomething = false;
			for (EnumFacing face : (EnumFacing.VALUES)) {
				boolean didSomethingHere = false;
				ISpellComponent other = getComponent(pos.offset(face));
				if (other == null) {
					if (putInputIntoMiniSpellSpace(pos.offset(face), returns)) {
						didSomethingHere = true;
					}
					
					continue;
				}
				NBTTagCompound cmp = returns.get(face);
				if (!prevPositions.contains(other.getPos()) && other.accepts(face.getOpposite(), cmp)) {
					didSomething = true;
					didSomethingHere = true;
					this.putInputs(other, face.getOpposite(), cmp);
					
					//other.getInputs().put(face.getOpposite(), cmp);
					nextPositions.add(other.getPos());
				}
				
				if (listener != null) {
					if (didSomethingHere) {
						listener.addToChain(this, pos.offset(face));
					} 
				} 
				
			}
			

			prevPositions.clear();
			//if (listener != null) {
				if (!didSomething) {
					for (ISpellChainListener lis : listeners) {
						if (lis.isPartOfChain(this, pos)) {
							lis.finished(this, pos, true);
						}
					}
				}
			//}
		}
		
		public void start() {
			start(new HashMap<>());
		}
		
		public void gatherPower() {
			//TODO
			//TODO
			putPower(energyStorage.getMaxEnergyStored());
			//TODO
			//TODO
		}
		
		public void start(Map<BlockPos, SpellIOMap> initialWithInputs) {
			
			
			if (isRunning()) {
				System.out.println("Running");
				return;
			}
			

			gatherPower();
			
			SpellSpace.this.partsInit();//(SpellSpace.this.id, SpellSpace.this.height, SpellSpace.this.headPos, SpellSpace.this.edges, new ArrayList<>(SpellSpace.this.sigPoints.keySet()));
			
			if (MinecraftForge.EVENT_BUS.post(new ActivateSpellSpaceEvent(this))) {
				System.out.println("Stopped activation of spellspace");
				return;
			}
			System.out.println("Activating...");
			nextPositions.clear();
			//lastTickInputs.clear();
			List<BlockPos> statics = getPositionsOfInstancesInSpace(ISpellComponent.class, (c) -> c.isStatic() && c.getSpaceID() == SpellSpace.this.id);
			//List<BlockPos> nonstatics = getPositionsOfInstancesInSpace(ISpellComponent.class, (o) -> !o.isStatic());
			List<BlockPos> starters = getPositionsOfInstancesInSpace(ISpellComponent.class, (c) -> c.isStarter() && c.getSpaceID() == SpellSpace.this.id);
			List<ISpellComponent> comps = getInstancesInSpace(ISpellComponent.class);
			List<ISpellBorder> miniPositions = getInstancesInSpace(ISpellBorder.class, (c) -> c.getSpaceID() != SpellSpace.this.id && c.getSpellSpace() != SpellSpace.this && c.getSpellSpace() != null && c.isHeadPiece());
			List<SpellSpace> miniSpaces = new ArrayList<>();
			miniPositions.forEach((e) -> {
				

				miniSpaces.add(e.getSpellSpace());
			});
			this.miniSpaces  = miniSpaces;
			this.miniPositions  = miniPositions;
			comps.forEach((e) ->  {
				e.resetInputs();
				e.resetOutputs();
			});
			
			for (BlockPos pos : statics) {

				System.out.println("Static " + getComponent(pos));
				magickingPos = pos;
				ISpellComponent comp = getComponent(pos);
				
				ServerPos modPos = comp.getServerPos();
				if (this.runPos != null) {
					modPos = runPos;
				}
				
				NonNullMap<EnumFacing, NBTTagCompound> returns = comp.getStaticOutput(modPos);
				this.disseminateStaticOutput(pos, true);
				
			}
			for (BlockPos pos : starters) {
				System.out.println("Starter " + getComponent(pos));
				magickingPos = pos;
				this.activateAndDisseminateOutput(pos, true, new NonNullMap<EnumFacing, NBTTagCompound>(NBTTagCompound::new), new ArrayList<>());
				for (EnumFacing f : EnumFacing.VALUES) {
					if (getComponent(pos.offset(f)) != null && !nextPositions.contains(pos.offset(f))) {
						nextPositions.add(pos.offset(f));
					}
					if (initialWithInputs.containsKey(pos)) {
						NonNullMap<EnumFacing, NBTTagCompound> mp = inputsForTick.get(pos.offset(f));
						mp.put(f, initialWithInputs.get(pos).get(f));
						inputsForTick.put(pos.offset(f), mp);
					}
				}
			}
			ticks++;
			
		}
		
		public void tick() {
			if (this.ticks != 0) {
				if (MinecraftForge.EVENT_BUS.post(new SpellSpaceTickingEvent(this, ticks))) {
					System.out.println("Spell space casting ticking was suspended");
					return;
				}
				
				
				System.out.println("Ticking " + ticks);
				boolean didSomething = false;
				
				List<BlockPos> pp = new ArrayList<>(nextPositions);
				prevPositions.addAll(nextPositions);
				nextPositions.clear();
				for (BlockPos pos : pp) {
					
					magickingPos = pos;
					didSomething = true;
					if (getComponent(pos) == null) {
						System.out.println("Why is " + pos + " null uhhjjj");
						getWorld().setBlockState(pos, Blocks.FIRE.getDefaultState());
						continue;
					}
					//NonNullMap<EnumFacing, NBTTagCompound> cmp = lastTickInputs.get(pos);
					//if (cmp == null) continue;
					this.activateAndDisseminateOutput(pos, false, inputsForTick.get(pos), prevPositions);
				}
				
				if (didSomething || !nextPositions.isEmpty()) {
					ticks++;
	
					for (int i = 0; i < 100; i++) {
						Random r = new Random();
						spawnParticles(EnumParticleTypes.CRIT_MAGIC, false, (new Vec3d(magickingPos)).add(new Vec3d(0.5, 0.5, 0.5)), new Vec3d(2*r.nextDouble()-1, 2*r.nextDouble() -1, 2*r.nextDouble() - 1), dimension, 1);
					}
				} else {
					
					end(true, headPos);
					return;
				}
			}
		}
		
		public void end(boolean success, BlockPos endPos) {
			MinecraftForge.EVENT_BUS.post(new EndSpellSpaceMagicEvent(this, success));
			System.out.println("Spell ended " + (success ? "successfully" : "unsuccessfully") + " at " + endPos);
			//this.getWorld().setBlockState(endPos.up(), Blocks.END_GATEWAY.getDefaultState());
			
			nextPositions.clear();

			prevPositions.clear();
			
			inputsForTick.clear();
			//lastTickInputs.clear();
			ticks = 0;
			
			for (int i = 0; i < 100; i++) {
				Random r = new Random();
				SpellSpace.this.spawnParticles(success ? EnumParticleTypes.ENCHANTMENT_TABLE : EnumParticleTypes.LAVA, false, (new Vec3d(endPos)).add(new Vec3d(0.5, 0.5, 0.5)), new Vec3d(2*r.nextDouble()-1, 2*r.nextDouble() -1, 2*r.nextDouble() - 1), dimension, 1);
			}
			SpellSpace.this.end(this);
		}
		
		public SpellSpace getSpellSpace() {
			return SpellSpace.this;
		}
		
	}
	
}
