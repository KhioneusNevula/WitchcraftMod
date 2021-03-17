package com.gm910.goeturgy.spells.spellspaces;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
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
import com.gm910.goeturgy.messages.Messages;
import com.gm910.goeturgy.messages.Messages.TaskMessage;
import com.gm910.goeturgy.messages.types.IRunnableTask;
import com.gm910.goeturgy.messages.types.TaskIllusion;
import com.gm910.goeturgy.messages.types.TaskParticles;
import com.gm910.goeturgy.spells.SpecialSpellComponentRegistry;
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
import net.minecraft.nbt.NBTTagString;
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
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

/**
 * This class represents a central component to this mod: the spell-space, the space where spells are executed like programs
 * @author borah
 *
 */
@EventBusSubscriber
public class SpellSpace implements ICapabilitySerializable<NBTTagCompound>{

	/**
	 * The dimension this spellspace exists in
	 */
	protected int dimension;
	
	/**
	 * ##Probably not used##
	 */
	protected String specialWorld;
	
	/**
	 * Edges of the spellspace
	 */
	protected List<BlockPos> edges = new ArrayList<>();
	
	/**
	 * Shape of the spellspace (inner area)
	 */
	protected List<BlockPos> shape = new ArrayList<>();
	
	/**
	 * Id of the spellspace in Le Grande Interdimensionale Spell-Space Registry
	 */
	protected long id = -1;
	
	/**
	 * How tall the spellspace is
	 */
	protected int height = 10;
	
	/**
	 * Universal area tolerance for how long the spellspace's flood fill algorithm searches before it decides that it can't make a spellspace
	 */
	public static final int AREA_TOLERANCE = 3000;

	/**
	 * Indicates that the ending of a spell was forced 
	 */
	public static final NonNullMap<EnumFacing, NBTTagCompound> FORCED_END = new SpellIOMap(() -> {NBTTagCompound c = new NBTTagCompound(); c.setBoolean("ForcedEnd", true); return c;});
	
	/**
	 * BlockPos that represents the position acting as 'head' of the spellspace for distinction purposes
	 */
	protected BlockPos headPos;
	
	/**
	 * Energy level of spellspace so that we have a check-and-balance for ultra-powerful magic
	 */
	protected AetherStorage energyStorage = new AetherStorage(Integer.MAX_VALUE);

	/**
	 * Significant points that make up border of spellspace, if these are damaged or changed we destroy the spellspace
	 */
	protected Map<BlockPos, NBTTagCompound> sigPoints = new HashMap<>();
	
	/**
	 * 
	 */
	protected SpellPartStorage spellParts;

	/**
	 * All spells currently being run by the spellspace
	 */
	protected List<Spell> runningInstances = new ArrayList<>();
	
	/**
	 * This is a list of small objects which are used to produce effects on the client side
	 */
	public static List<IRunnableTask> runClients = new ArrayList<>();
	
	/**
	 * This is a list of small objects used to produce effects on server side
	 */
	public static List<IRunnableTask> runServers = new ArrayList<>();
	
	/**
	 * Whether the spellspace's borders are rendered solid by the Salt Effigy
	 */
	protected boolean isSolid = false;
	
	/**
	 * Constructs spellspace in the given dimension
	 * @param dimension
	 */
	public SpellSpace(int dimension) {
		this.dimension = dimension;
	}
	
	/**
	 * Whether the spellspace is removed and should not be considered a real spellspace
	 * @return
	 */
	public boolean isRemoved() { 
		return id == -1;
	}
	
	/**
	 * Generate spellspace from NBT
	 * @param cmp the NBT
	 */
	public SpellSpace(NBTTagCompound cmp) {
		this(cmp.getInteger("Dimension"));
		this.deserializeNBT(cmp);
	}
	
	/**
	 * Creates particle effect indicating spellspace was created
	 */
	public void postInit() {
		System.out.println("Spell space generating particles now");
		for (int i = 0; i < 50; i++) {
			Random r = new Random();
			this.spawnParticles(EnumParticleTypes.FIREWORKS_SPARK, true, (new Vec3d(this.headPos)).add(new Vec3d(0.5, 0, 0.5)), new Vec3d(r.nextDouble() * 2 - 1,r.nextDouble() * 2 - 1,r.nextDouble() * 2 - 1), dimension, 1);
		}
	}
	
	public void storageCreate() {
		this.spellParts = new SpellPartStorage(this);
	}
	
	/**
	 * Initializes inner area of spellspace
	 */
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
			
			
			foundShape = forLoopFloodFill(startPos, shape1, AREA_TOLERANCE*2, edges);//floodFill(0, shape1, startPos, AREA_TOLERANCE );
			
			
			
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
	
	/**
	 * A flood-fill algorithm using a for loop to initialize inside of spellspace. Intended so that we don't create stack overflow errors, but honestly isn't that great
	 * @param starter the position to start filling from
	 * @param shape the list to be used as a shape to add blocks to
	 * @param tolerance the iteration at which to consider flood-filling failed, because if you can't find the edge you gotta stop somewhere
	 * @param edges the edges of the shape to be filled
	 * @return whether floodFill was successful
	 */
	public boolean forLoopFloodFill(BlockPos starter, List<BlockPos> shape, int tolerance, List<BlockPos> edges) {

		List<BlockPos> nextIteration = new ArrayList<>();
		nextIteration.add(starter);
		for (int i = 0; i < tolerance; i++) {
			
			List<BlockPos> nextIter = new ArrayList<>(nextIteration);
			nextIteration.clear();
			for (BlockPos checking : nextIter) {

				if (!getWorld().isValid(checking)) {
					return false;
				}
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
	
	/**
	 * Older algorithm using recursion, scrapped because we want to avoid stackOverflowErrors and loops are always more efficient
	 * @param index the index we're recursing on
	 * @param shape the shape we're adding blocks to
	 * @param starter the position to start flood-filling from
	 * @param tolerance the point at which we have to stop flood-filling and consider flood-fill-ment failed because otherwise the game would crash
	 * @return
	 */
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
	
	/**
	 * Returns positions of all instances of tile entities extending/implementing this class in spellSpace which satisfy the predicate
	 * @param <T> the class to look for as a Type Parameter
	 * @param toCheck the class to look for--you still need to define this... 
	 * @param pred the predicate used to further check each instance of this class
	 * @return
	 */
	public <T> List<BlockPos> getPositionsOfInstancesInSpace(Class<T> toCheck, Predicate<? super T> pred) {
		if (!this.isFullyLoaded()) {
			System.out.println("NotFullyLoaded in PositionsOFInstancesChecker");
			return spellParts.getPositionsOfInstancesInSpace(toCheck, pred);
		}
		List<BlockPos> ls = new ArrayList<>();
		for (BlockPos pos : this.getInnerSpace()) {
			
			if (getWorld().getTileEntity(pos) != null && toCheck.isAssignableFrom(this.getWorld().getTileEntity(pos).getClass()) && pred.test((T) getWorld().getTileEntity(pos))) {
				ls.add(pos);
			}
		}
		return ls;
	}
	
	/**
	 * Returns positions of all instances of tile entities extending/implementing this class in spellSpace
	 * @param <T> the class to look for as a Type Parameter
	 * @param toCheck the class to look for--you still need to define this... 
	 * @return
	 */
	public <T> List<BlockPos> getPositionsOfInstancesInSpace(Class<T> toCheck) {
		
		return this.getPositionsOfInstancesInSpace(toCheck, Predicates.alwaysTrue());
	}
	
	/**
	 * Returns all instances of tile entities extending/implementing this class in spellSpace which satisfy the predicate as the class defined in the {@code toCheck} parameter
	 * @param <T> the class to look for as a Type Parameter
	 * @param toCheck the class to look for--you still need to define this... 
	 * @param pred the predicate used to further check each instance of this class
	 * @return
	 */
	public <T> List<T> getInstancesInSpace(Class<T> toCheck, Predicate<? super T> pred) {
		if (!this.isFullyLoaded()) {
			System.out.println("NotFullyLoaded in InstancesChecker");
			return spellParts.getInstancesInSpace(toCheck, pred);
		}
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
	
	/**
	 * Returns all instances of tile entities extending/implementing this class in spellSpace
	 * @param <T> the class to look for as a Type Parameter
	 * @param toCheck the class to look for--you still need to define this... 
	 * @return
	 */
	public <T> List<T> getInstancesInSpace(Class<T> toCheck) {
		return this.getInstancesInSpace(toCheck, Predicates.alwaysTrue());
	}
	
	/**
	 * 
	 * Returns positions of all spellComponents
	 * @return
	 */
	public List<BlockPos> getPositionsOfComponents() {
		return getPositionsOfInstancesInSpace(ISpellComponent.class);
	}
	
	/**
	 * Returns all spellComponents
	 * @return
	 */
	public List<ISpellComponent> getComponents() {
		return getInstancesInSpace(ISpellComponent.class);
	}
	
	/**
	 * Returns the component at this pos, or null if it is not a component
	 * @param pos
	 * @return
	 */
	public ISpellComponent getComponent(BlockPos pos) {
		if (!this.isLoaded(pos)) {
			return spellParts.getComponent(pos);
		}
		if (this.getWorld().getTileEntity(pos) != null) {
			if (SpecialSpellComponentRegistry.get(this.getWorld().getTileEntity(pos).getClass()) != null) {
				return SpecialSpellComponentRegistry.get(this.getWorld().getTileEntity(pos).getClass());
			}
		}
		return this.getWorld().getTileEntity(pos) instanceof ISpellComponent ? (ISpellComponent)this.getWorld().getTileEntity(pos) : null;
	}
	
	/**
	 * Returns the tile entity acting as a border-maker at this position, or null if no such tile entity exists
	 * @param pos
	 * @return
	 */
	public ISpellBorder getBorderPiece(BlockPos pos) {
		if (!this.isLoaded(pos)) return spellParts.getBorderPart(pos);
		return this.getWorld().getTileEntity(pos) instanceof ISpellBorder ? (ISpellBorder) this.getWorld().getTileEntity(pos) : null;
	}
	
	/**
	 * Returns the SpellSpaces instance that governs all the spellspaces of this universe
	 * @return
	 */
	public SpellSpaces getSpellSpaceRegistry() {
		return SpellSpaces.get();
	}
	
	/**
	 * Decrements the power of this spellspace by sending the power to the <em>living god of magic itself</em>, the spellspace registry
	 * @param amount
	 */
	public void decrementPower(int amount) {
		getSpellSpaceRegistry().addPower(this.extractPower(amount));
	}
	
	/**
	 * Increments the power of this spellspace by taking the power from the <em>living god of magic itself</em>, the spellspace registry
	 * @param amount
	 */
	public void incrementPower(int amount) {
		getSpellSpaceRegistry().removePower(this.putPower(amount));
	}
	
	/**
	 * Returns all blockPositions adjacent to this one
	 * @param start
	 * @param includeVertical whether to check up and down, too
	 * @return
	 */
	public List<BlockPos> getAdjacents(BlockPos start, boolean includeVertical) {
		List<EnumFacing> f = Lists.newArrayList(includeVertical ? EnumFacing.VALUES : EnumFacing.HORIZONTALS);
		List<BlockPos> poses = new ArrayList<>();
		for (EnumFacing e : f) {
			poses.add(start.offset(e));
		}
		return poses;
	}
	
	/**
	 * Turns a list of BlockPos's into a list of the spell components at those positions (does not add null values)
	 * @param poses
	 * @return
	 */
	public List<ISpellComponent> getSpellComponentsFor(List<BlockPos> poses ) {
		
		List<ISpellComponent> ls = new ArrayList<>();
		for (BlockPos p : poses) {
			if (getComponent(p) != null) ls.add(this.getComponent(p));
		}
		return ls;
	}

	/**
	 * Returns the amount of energy in this spellspace
	 * @return
	 */
	public int getPower() {
		return this.energyStorage.getEnergyStored();
	}
	
	/**
	 * Adds energy
	 * @param power
	 * @return
	 */
	public int putPower(int power) {
		return energyStorage.receiveEnergy(power, false);
	}
	
	/**
	 * Removes energy
	 * @param power
	 * @return
	 */
	public int extractPower(int power) {
		return energyStorage.extractEnergy(power, false);
	}
	

	/**
	 * Gets anything which checks for spellspace chain executions at this position
	 * @param pos
	 * @return
	 */
	public ISpellChainListener getChainListener(BlockPos pos) {
		if (!this.isLoaded(pos)) {
			return spellParts.getChainListener(pos);
		}
		return this.getComponent(pos) instanceof ISpellChainListener ? (ISpellChainListener)this.getComponent(pos) : null;
	}
	
	/**
	 * Makes the borders of this space solid or not
	 * TODO
	 * @param canPass the uuid's which can pass through
	 * @param solid whether to be solid or not
	 */
	public void makeBordersSolid(boolean solid, UUID...canPass) {
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
	
	/**
	 * Intended to allow a server spellspace to run an event on client, but is currently <em> very</em> broken
	 * @param event
	 */
	public void causeEventOnClient(IRunnableTask event) {
		Messages.INSTANCE.sendToAll(new TaskMessage(event));
		//runClients.add(event);
	}
	
	/**
	 * Returns the tile entity of type spell object at this pos
	 * @param pos
	 * @return
	 */
	public ISpellObject getSpellObject(BlockPos pos) {
		if (!this.isLoaded(pos)) {
			return spellParts.getSpellObject(pos);
		}
		return getWorld().getTileEntity(pos) instanceof ISpellObject ? (ISpellObject) getWorld().getTileEntity(pos) : null;
	}
	
	/**
	 * Whether the borders of this spellspace are currently solid
	 * @return
	 */
	public boolean areBordersSolid() {
		
		return isSolid;
		
	}
	
	/**
	 * Cast a spell using this spellspace! 
	 */
	public void start() {
		start((ServerPos)null, new HashMap<>());
	}
	
	/**
	 * Cast a spell using this spellspace!
	 * @param en The entity to use as the focal point of the spell
	 * @param mp the map representing inputs to use initially, can be empty but not null (check?) TODO
	 */
	public void start(Entity en, Map<BlockPos, SpellIOMap> mp) {
		if (isRemoved()) {
			System.out.println("Spell space cannot run because it has been removed");
		}
		if (this.isFullyLoaded()) {
			this.storageCreate();
		}
		Spell runner = new Spell(en);
		this.runningInstances.add(runner);
		runner.start(mp);
	}
	
	/**
	 * Cast a spell using this spellspace!
	 * @param en The position to use as the focal point of the spell
	 * @param mp the map representing inputs to use initially, can be empty but not null (check?) TODO
	 */
	public void start(ServerPos runPos, Map<BlockPos, SpellIOMap> mp) {
		if (isRemoved()) {
			System.out.println("Spell space cannot run because it has been removed");
		}
		if (this.isFullyLoaded()) {
			this.storageCreate();
		}
		Spell runner = new Spell(runPos);
		this.runningInstances.add(runner);
		runner.start(mp);
	}
	
	/**
	 * Runs a tick where the spellspace does lovely stuff like continue the execution of each spell running under it and yell angrily at the client about how it's not initialized properly
	 * and, of course, remove itself from the world if any of its significant border pieces is broken
	 */
	public void tick() {
		for (BlockPos pos : this.sigPoints.keySet()) {
			if (!this.isLoaded(pos)) continue;
			if (!NBTUtil.writeBlockState(new NBTTagCompound(), getWorld().getBlockState(pos)).equals(sigPoints.get(pos))) {
				SpellSpaces.get().removeSpellSpace(this);
			}
		}
		if (isRemoved()) {
			return;
		}
	
		
		if (this.shape.isEmpty() && !this.edges.isEmpty()) {
			this.initShape();
		}
		if (this.edges.isEmpty()) {
			System.out.println("SpellSpace has no edges!");
		}
		
		
	}
	
	/**
	 * Whether the spellspace is running
	 * @return
	 */
	public boolean isRunning() {
		for (Spell spell : runningInstances) {
			if (spell.isRunning()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * removes Spell from the list of running spells
	 * @param runner
	 */
	public void removeSpell(Spell runner) {
		this.runningInstances.remove(runner);
	}
	
	/**
	 * Initializes spellspace
	 * @param id the id the spellspace is to be registered under
	 * @param height how tall the spell space is
	 * @param head the position which is considered the 'head' of the spellspace
	 * @param edges the edges of the spellspace
	 * @param sigPoints the positions where specific blocks used to detect the integrity of the spellspace are (if they break, the entire spellspace does)
	 */
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

		storageCreate();
	}
	
	/**
	 * This registers all spell objects within the spellspace as parts of Le Empire of This SpellSpace
	 */
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
	
	/**
	 * Forcefully obtains world instance from server
	 * @return
	 */
	public World getWorld() {
		
		return FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dimension);
	}
	
	/**
	 * Gets a world instance from server, but only if it exists, of course
	 * @return
	 */
	public World getWorldSoft() {
		return DimensionManager.getWorld(this.dimension);
	}
	
	/**
	 * Gets the server, used so that we don't always have to write out Goeturgy.proxy.getServer() or FMLCommonHandler.instance().getMinecraftServerInstance();
	 * @return
	 */
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
	
	/**
	 * Adds a block to the "edge" list of the spellspace
	 * @param pos
	 */
	public void addEdge(BlockPos pos) {
		if (getWorld().getTileEntity(pos) instanceof ISpellBorder) {
			this.sigPoints.put(pos, NBTUtil.writeBlockState(new NBTTagCompound(), getWorld().getBlockState(pos)));
		}
		this.edges.add(pos);
		System.out.println("We just added a block to the outline...?");
	}
	
	public List<BlockPos> getShape() {
		return new ArrayList<>(shape);
	}
	
	/**
	 * Gets both shape and edges as one list
	 * @return
	 */
	public List<BlockPos> getTotalShape() {
		List<BlockPos> sh = new ArrayList<>(shape);
		sh.addAll(edges);
		return sh;
	}

	/**
	 * Gets the entire inner spatial zone of the spellspace, commonly used
	 * @return
	 */
	public List<BlockPos> getInnerSpace() {
		List<BlockPos> sh = new ArrayList<>(getTotalShape());
		for (BlockPos pos : getTotalShape()) {
			for (int i = 1; i < this.height; i++) {
				sh.add(new BlockPos(pos.getX(), pos.getY() + i, pos.getZ()));
			}
		}
		return sh;
	}

	/**
	 * Gets the outlines of the spellspace, in three-dimensions, of course
	 * @return
	 */
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
		this.forceLoad(false);
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
			Spell runner = this.new Spell(comp);
			return runner;
		});
		for (BlockPos pos : this.sigPoints.keySet()) {
			ISpellObject comp = this.getSpellObject(pos);
			if (comp != null) {
				comp.setSpaceID(this.id);
				comp.setSpellSpace(this);
			} else {
				System.out.println("Missing spell component for spellspace " + id + " at " + pos);
			}
		}
		//this.partsInit();
		//this.lastTickInputs.clear();;
		/*
		 * GMNBT.forEach(nbt.getTagList("LastTickInputs", NBT.TAG_COMPOUND), (c) -> {
		 * NBTTagCompound cmp = (NBTTagCompound)c; BlockPos p = ServerPos.fromNBT(cmp);
		 * NonNullMap<EnumFacing, NBTTagCompound> ims =
		 * MagicIO.nonNullMapFromNBT(cmp.getTagList("Inps", NBT.TAG_COMPOUND));
		 * lastTickInputs.put(p, ims); });
		 */
		//if (nbt.hasKey("MagickingPos")) magickingPos = NBTUtil.getPosFromTag(nbt.getCompoundTag("MagickingPos"));
		storageCreate();
	}
	
	@SubscribeEvent
	public void unload(ChunkEvent.Unload event) {
		
		if (event.getWorld().provider.getDimension() != this.dimension) {
			return;
		}
		ChunkPos headChunk = new ChunkPos(this.headPos);
		if (event.getChunk().isAtLocation(headChunk.x, headChunk.z)) {
			System.out.println("Creating storage for unloading spellspace");
			storageCreate();
		}
	}
	
	/**
	 * This runs server ticks for the spellspace, should any be needed. Most spellspace logic is run using player ticks, so that unnecessary world-loading is not performed
	 * @param event
	 */
	@SubscribeEvent
	public static void server(ServerTickEvent event) {
		for (IRunnableTask task : runServers) {
			task.run();
		}
		runServers.clear();
		
		List<SpellSpace> spaces = new ArrayList<>(SpellSpaces.get().getAsList());
		for (SpellSpace space : spaces) {
			if (space.isRemoved()) {
				SpellSpaces.get().removeSpellSpace(space);
				SpellSpaces.get().remove(space);
				continue;
			}
			List<Spell> runners = new ArrayList<>(space.runningInstances);
			for (Spell run : runners) {
				
				/*if (run.isRunning() && !space.isFullyLoaded()) {
					space.forceLoad(false);
				}*/
				run.tick();
			}
		}
	}
	
	/**
	 * This runs spell-space ticks if a player is present in the dimension of the spellspace
	 * @param event
	 */
	@SubscribeEvent
	public void update(PlayerTickEvent event) {
		if (event.player.world.isRemote) return;
		if (event.player.world.provider.getDimension() != this.dimension) return;
		
		this.tick();
		//causeEventOnClient((e) -> EntityCelestialBeam.summonCelestialBeam(event.player.world, headPos.getX(), headPos.getY(), headPos.getZ(), 2, false));
		if (isRemoved()) {
			SpellSpaces.get().removeSpellSpace(this);
			return;
		}
		for (BlockPos pos : this.getOutline()) {
			//renderIllusion(Blocks.LEAVES2.getDefaultState(), new ServerPos(pos, dimension));
			
			spawnParticles(EnumParticleTypes.TOWN_AURA, true, (new Vec3d(pos)).add(new Vec3d(0.5, 0.5, 0.5)), new Vec3d(1, 1, 1), dimension, 1);
		}
		if (!this.runningInstances.isEmpty()) {
			renderIllusion(Blocks.FIRE.getDefaultState(), new ServerPos(headPos, dimension));
		}
		/*for (BlockPos pos : this.getOutline()) {
			//renderIllusion(Blocks.LEAVES2.getDefaultState(), new ServerPos(pos, dimension));
			spawnParticles(EnumParticleTypes.TOWN_AURA, true, (new Vec3d(pos)).add(new Vec3d(0.5, 0.5, 0.5)), new Vec3d(1, 1, 1));
		}*/
		//SpellSpaces sp = SpellSpaces.get();
		SpellSpace space = this;
		//if (sp != null) {
			//for (SpellSpace space : sp.getAsList()) {
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
	
	
	/**
	 * Used to check when starter components or the head-block is/are right-clicked
	 * @param event
	 */
	@SubscribeEvent
	public void rightClick(RightClickBlock event) {
		if (event.getWorld().isRemote) return;
		if (event.getWorld() != this.getWorldSoft()) {
			return;
		}
		if (isRemoved()) {
			System.out.println("Spellspace has been removed");
			return;
		}
		if (event.getPos().equals(headPos) || this.getComponent(event.getPos()) != null && this.getComponent(event.getPos()).isStarter() && !event.getEntityPlayer().isSneaking()) {
			this.start();
		}
	}
	
	/**
	 * Does rendering for spellspaces on client
	 * @param event
	 */
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
	
	/**
	 * Spawn particles
	 * @param type Type of particle
	 * @param thatDistanceThing I honestly have no clue, really
	 * @param pos position of particles
	 * @param sped 3d speed vector
	 * @param d dimension of particle effect
	 * @param count how many to spawn
	 * @param args the args, only used for particles .redstone and blockcrack, really
	 */
	public void spawnParticles(EnumParticleTypes type, boolean thatDistanceThing, Vec3d pos, Vec3d sped, int d, int count, int...args) {
		this.causeEventOnClient(new TaskParticles(type, thatDistanceThing, pos, sped, d, count, args));
	}
	
	/**
	 * Render an  i <strong>m</strong> a <strong>g</strong> i <strong>n</strong> a <strong>r</strong> y  block in the world... 
	 * @param state
	 * @param pos
	 */
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
	
	/**
	 * Whether this position is loaded in the world
	 * @param pos
	 * @return
	 */
	public boolean isLoaded(BlockPos pos) {
		if (FMLCommonHandler.instance().getMinecraftServerInstance() == null) {
			return Minecraft.getMinecraft().world.isBlockLoaded(pos);
		} else {
			World world = this.getWorldSoft();
			if (world == null) {
				return false;
			} else {
				//ChunkPos pos1 = new ChunkPos(pos);
				if (world.isBlockLoaded(pos)) {
					return true && !isRemoved();
				}
			}
		}
		return false;
	}
	
	/**
	 * Whether this spellspace's headPos is loaded
	 * @return
	 */
	public boolean isHeadLoaded() {
		return isLoaded(this.headPos) && !isRemoved();
	}
	
	/**
	 * Whether the entire spellspace is loaded
	 * @return
	 */
	public boolean isFullyLoaded() {
		boolean loaded = true;
		for (BlockPos pos : this.getTotalShape()) {
			loaded = loaded && isLoaded(pos);
		}
		//System.out.println("Spelspace " + id + " loaded? " + loaded);
		return loaded && !isRemoved();
	}
	
	/**
	 * Force-load the spell-space's chunks
	 * @param justHead whether to just load the head of the spellspace (is this ever useful?)
	 * @return True if successfully loaded, false if otherwise, and True if already loaded
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
	
	/**
	 * Runs when a spell-space is removed, before it is removed (to allow for cancelation)
	 * @param event
	 */
	@SubscribeEvent
	public static void spellSpace(RemoveSpellSpaceEvent event) {
		System.out.println(event.getSpellSpace().id + " listening for removal");
		for (int i = 0; i < 50; i++) {
			Random r = new Random();
			event.getSpellSpace().spawnParticles(EnumParticleTypes.LAVA, true, (new Vec3d(event.getSpellSpace().headPos)).add(new Vec3d(0.5, 0, 0.5)), new Vec3d(r.nextDouble() * 2 - 1,r.nextDouble() * 2 - 1,r.nextDouble() * 2 - 1), event.getSpellSpace().dimension, 1);
				
		}

	}
	
	/**
	 * A class representing a Spell, implicitly linked to the SpellSpace class. A spell is just "something" that causes an effect in the world
	 * @author borah
	 *
	 */
	public class Spell implements INBTSerializable<NBTTagCompound> {
		
		/**
		 * The position where mystical stuff is currently happening in the spell
		 */
		public BlockPos magickingPos = null;
		
		/**
		 * The position which the spell is focused at, might be null (must be null if runEntity is not null)
		 */
		protected ServerPos runPos;
		
		/**
		 * The entity which the spell is focused at, might be null (must be null if runPos is not null)
		 */
		protected Entity runEntity;
		
		/**
		 * The positions in the spellspace which are going to be interpreted next tick by the Spell
		 */
		protected List<BlockPos> nextPositions = new ArrayList<>();
		
		/**
		 * These components are to be run after a certain delay
		 */
		protected Map<BlockPos, Integer> delayedPositions = new NonNullMap<>(-1);
		
		/**
		 * The tick the spellspace is currently on, 0 if the spellspace is not running
		 */
		protected int spellTick = 0;
		
		/**
		 * Used to prevent spell from running too quickly and overloading game
		 */
		protected int worldTicks = 0;
		
		/**
		 * The positions in the spellspace which the Spell interpreted last tick, so that we don't make infinite loops by going back and forth
		 */
		protected List<BlockPos> prevPositions = new ArrayList<>();
		
		/**
		 * Inputs used for the next execution stored in each individual spell to avoid two spells trying to activate something simultaneously and interfering with each other
		 */
		protected Map<BlockPos, NonNullMap<EnumFacing, NBTTagCompound>> inputsForTick = new NonNullMap<>(() -> new NonNullMap<>(NBTTagCompound::new));

		/**
		 * All spellspaces within the spellspace that this Spell is attached to
		 */
		protected List<SpellSpace> miniSpaces = new ArrayList<>();

		/**
		 * All headPositions of smaller spellspaces within the spellspace that this spell is attached to
		 */
		protected List<ISpellBorder> miniPositions = new ArrayList<>();
		

		protected String logger = "";
		
		protected OutputStream LOG = new OutputStream() {
			
			@Override
			public void write(int b) throws IOException {
				logger = logger + (char)b;
			}
		};
		
		protected PrintStream printer = new PrintStream(LOG);
		
		
		/**
		 * Create Spell from NBT
		 * @param comp
		 */
		public Spell(NBTTagCompound comp) {
			this.deserializeNBT(comp);
		}
		
		/**
		 * Create a new spell which is not focused on a specific position
		 */
		public Spell() {
			this((ServerPos)null);
		}
		
		/**
		 * Create a spell focused at the given position, which may be null
		 * @param runPos
		 */
		public Spell(@Nullable ServerPos runPos) {
			this.magickingPos = SpellSpace.this.headPos;
			this.runPos = runPos;
		}
		
		/**
		 * Create a spell focused at the given entity, which may be null
		 * @param entity
		 */
		public Spell(@Nullable Entity entity) {
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
			
			cmp.setInteger("Ticks", spellTick);
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
			
			cmp.setTag("Delayed", GMNBT.makeList(delayedPositions.keySet(), (pos) -> {
				NBTTagCompound e = new NBTTagCompound();
				e.setTag("Pos", NBTUtil.createPosTag(pos));
				e.setInteger("Delay", delayedPositions.get(pos));
				return e;
			}));
			
			cmp.setTag("MiniSpaces", GMNBT.makeList(miniSpaces, (sp )-> {return new NBTTagLong(sp.id);} )); 
			cmp.setTag("Log", GMNBT.makeList(Lists.newArrayList(logger.split("\n")), (e) ->  new NBTTagString(e)));
			//cmp.setString("Log", this.logger);
			return cmp;
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt) {

			SpellSpace.this.forceLoad(false);
			nextPositions = GMNBT.createPosList(nbt.getTagList("NextPositions", NBT.TAG_COMPOUND));
			
			miniPositions = GMNBT.createList(nbt.getTagList("NextPositions", NBT.TAG_COMPOUND), (n) -> {
				NBTTagCompound comp = (NBTTagCompound)n;
				return getBorderPiece(NBTUtil.getPosFromTag(comp));
			});
			
			prevPositions = GMNBT.createPosList(nbt.getTagList("PrevPositions", NBT.TAG_COMPOUND));
			this.miniSpaces = GMNBT.createList(nbt.getTagList("MiniSpaces", NBT.TAG_LONG), ( tg) -> {return SpellSpaces.get().getById(((NBTTagLong)tg).getLong());} );
			spellTick = nbt.getInteger("Ticks");
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
			
			this.delayedPositions = GMNBT.createMap(nbt.getTagList("Delayed", NBT.TAG_COMPOUND), ( base) -> {
				
				NBTTagCompound cmp = (NBTTagCompound)base;
				
				return new Pair<BlockPos, Integer>(NBTUtil.getPosFromTag(cmp.getCompoundTag("Pos")), cmp.getInteger("Delay"));
			});
			logger = String.join("\n", GMNBT.createList(nbt.getTagList("Log", NBT.TAG_STRING), (e) -> {
				NBTTagString str = (NBTTagString)e;
				return str.getString();
			}));
		}
		

		public void setTicks(int ticks) {
			this.spellTick = ticks;
		}
		
		public int getTicks() {
			return spellTick;
		}
		
		public boolean isRunning() {
			return spellTick != 0;
		}
		
		/**
		 * Activates a spellspace within the spellspace of this spell
		 * @param pos
		 * @param cmp
		 * @return
		 */
		public boolean putInputIntoMiniSpellSpace(BlockPos pos, NonNullMap<EnumFacing, NBTTagCompound> cmp) {
			ISpellBorder bord = getBorderPiece(pos);
			if (bord == null) return false;
			SpellSpace sp = bord.getSpellSpace();
			if (sp == SpellSpace.this || sp == null) return false;
			List<ISpellComponent> comps = sp.getInstancesInSpace(ISpellComponent.class, (e) -> e.isStarter());
			Map<BlockPos, SpellIOMap> map = new HashMap<>();
			comps.forEach( (c) -> {
				map.put(c.getPos(), new SpellIOMap((e) -> cmp.get(e).copy()));
				printer.println(cmp);
			});
			
			if (runPos != null) {
				sp.start(this.runPos, map);
			} else {
				printer.println("SubSpellSpace activation");
				sp.start(this.runEntity, map);
			}
			return true;
		}
			
		/**
		 * Magic wires are not considered actual components. As such, they are handled so that any input put into one end automatically is spit out the other end of a chain of magic wires
		 * @param wire1 the first wire
		 * @return a list of positions at the ends of wires and the directions which they would receive input from
		 */
		public Map<BlockPos,EnumFacing> findEndsOfMagicWireChain(MagicWire wire1) {
			Map<BlockPos,EnumFacing> foundPositions = new HashMap<>();
			List<BlockPos> visited = new ArrayList<>();
			List<BlockPos> checkPoses = new ArrayList<>();
			checkPoses.add(wire1.getPos());
			for (int i = 0; i <= SpellSpace.this.shape.size(); i++) {
				List<BlockPos> checkings = new ArrayList<>(checkPoses);
				checkPoses.clear();
				if (checkings.isEmpty()) break;
				for (BlockPos curPos : checkings) {
					MagicWire wire = (MagicWire)getComponent(curPos);
					printer.println("Magic wire chain at " + curPos);
					for (int j = 0; j < 100; j++) {
						Random r = new Random();
						spawnParticles(EnumParticleTypes.CRIT_MAGIC, false, (new Vec3d(curPos)).add(new Vec3d(0.5, 0.5, 0.5)), new Vec3d(2*r.nextDouble()-1, 2*r.nextDouble() -1, 2*r.nextDouble() - 1), dimension, 1);
					}
					List<EnumFacing> outputs = wire.getOutputSides();
					for (EnumFacing f : outputs) {
						BlockPos otherPos = curPos.offset(f);
						ISpellComponent otherComp = getComponent(otherPos);
						if (otherComp instanceof MagicWire) {
							MagicWire otherWire = (MagicWire) otherComp;
							if (otherWire.isInput(f.getOpposite())) {
								checkPoses.add(otherPos);
							}
						} else {
							if (otherComp != null) {
								printer.println("Found spell component " + otherComp.getClass() + " at " + curPos + " in magic wire chain");
								foundPositions.put(otherPos, f.getOpposite());
							}
						}
					}
					visited.add(curPos);
				}
				
			}
			return foundPositions;
		}
		
		/**
		 * Sends the output of a static component to adjacent components
		 * @param pos position of static component
		 * @param vertical whether to send them out veritcally (this is always true, so it doesn't really matter... but we'll keep it just in case)
		 */
		public void disseminateStaticOutput(BlockPos pos, boolean vertical) {
			ISpellComponent comp = getComponent(pos);
			if (comp == null) return;
			ServerPos modPos = comp.getServerPos();
			if (this.runPos != null) {
				modPos = runPos;
			}
			NonNullMap<EnumFacing, NBTTagCompound> returns = runEntity != null ? comp.getStaticOutput(runEntity, modPos) : comp.getStaticOutput(modPos);
			if (returns == null) {
				//this.end(false, pos);
				return;
			}
			for (EnumFacing face : (vertical ? EnumFacing.VALUES : EnumFacing.HORIZONTALS)) {
				ISpellComponent other = getComponent(pos.offset(face));
				if (other == null) {
					//putInputIntoMiniSpellSpace(pos.offset(face), returns);
					continue;
				}
				
				NBTTagCompound cmp = returns.get(face);
				if (other instanceof MagicWire) {
					Map<BlockPos,EnumFacing> toPut = this.findEndsOfMagicWireChain((MagicWire)other);
					for (BlockPos putting : toPut.keySet()) {
						if (getComponent(putting).accepts(toPut.get(putting), cmp)) {
							this.putInputs(getComponent(putting), toPut.get(putting), cmp);
						}
					}
				}
				else if (other.accepts(face.getOpposite(), cmp)) {
					//NonNullMap<EnumFacing, NBTTagCompound> inps = new NonNullMap<>(NBTTagCompound::new);
					//inps.put(face.getOpposite(), cmp);
					//other.getInputs().put(face.getOpposite(), cmp);
					this.putInputs(other, face.getOpposite(), cmp);
					//this.nextPositions.add(other.getPos());
					
				}
			}
		}
		
		/**
		 * Activates one component in the spellspace and sends out the appropriate event
		 * @param <T> The type of component being activated
		 * @param inputs the inputs to give to this component
		 * @param comp the component to be activated
		 * @param statique whether the component is being activated statically
		 * @return outputs or null if unsuccessful activation
		 */
		public <T extends TileEntity> NonNullMap<EnumFacing, NBTTagCompound> activate(NonNullMap<EnumFacing, NBTTagCompound> inputs, ISpellComponent comp, boolean statique) {
			if (MinecraftForge.EVENT_BUS.post(new ComponentActivationEvent<T>(this, comp))) {
				printer.println("Activation of " + comp.getString() + " canceled");
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
			
			printer.println("Activation of " + comp.getString() + (returns == null ? " unsuccessful" : " successful"));
			printer.println("Done with inputs "+ inputs);
			if (returns != null) printer.println("Returned " + returns);
			return returns;
		}
		
		/**
		 * The other activate, but allowing you to put a tile entity to save the trouble of writing (ISpellComponent)c
		 * @param <T>
		 * @param inputs
		 * @param c
		 * @param statique
		 * @return
		 * @see activate(NonNullMap<EnumFacing, NBTTagCompound>, ISpellComponent, boolean)
		 */
		public <T extends TileEntity> NonNullMap<EnumFacing, NBTTagCompound> activate(NonNullMap<EnumFacing, NBTTagCompound> inputs, T c, boolean statique) {
			
			return this.<T>activate(inputs, (ISpellComponent)c, statique);
		}
		
		/**
		 * Marks a component to be activated on the next tick of the spell
		 * @param toActivate position of component to be activated
		 * @param inputs inputs to give the component (may be null)
		 */
		public void markForActivationNextTick(BlockPos toActivate, @Nullable NonNullMap<EnumFacing, NBTTagCompound> inputs) {
			ISpellComponent comp = getComponent(toActivate);
			
			if (comp == null) {
				return;
			}
			
			this.nextPositions.add(toActivate);
			prevPositions.remove(toActivate);
			if (inputs != null) {
				inputsForTick.put(comp.getPos(), inputs);
			}
		}
		
		public void markForActivationAfterDelay(BlockPos toActivate, int delay, @Nullable NonNullMap<EnumFacing, NBTTagCompound> inputs) {
			//TODO
			ISpellComponent comp = getComponent(toActivate);
			
			if (comp == null) {
				return;
			}
			
			this.delayedPositions.put(toActivate, delay);
			prevPositions.remove(toActivate);
			if (inputs != null) {
				inputsForTick.put(comp.getPos(), inputs);
			}
		}
		
		/**
		 * Puts inputs into component
		 * @param comp component
		 * @param f side
		 * @param inputs inputs as a nbttagcompound
		 */
		public void putInputs(ISpellComponent comp, EnumFacing f, NBTTagCompound inputs) {
			NonNullMap<EnumFacing, NBTTagCompound> inps = comp.getInputs();
			inps.put(f, inputs);

			inputsForTick.put(comp.getPos(), inps);
		}
		
		/**
		 * Activates this component and puts its output values into the adjacent components
		 * @param pos the position to activate
		 * @param starter whether this component is starting the spell
		 * @param inputs the inputs to give this component
		 * @param prevPositions the previous positions activated to avoid reactivation
		 * @return 0 for continuation, 1 for successful termination, 2 for failed termination
		 */
		public int activateAndDisseminateOutput(BlockPos pos, boolean starter, NonNullMap<EnumFacing, NBTTagCompound> inputs, List<BlockPos> prevPositions) {
			
			ISpellComponent comp = getComponent(pos);
			List<BlockPos> prevPos = new ArrayList<>(prevPositions);
			prevPositions.clear();
			if (comp == null) return 2;
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
				if (returns == FORCED_END) {
					this.end(true, pos);
					return 0;
				}
			} else {
				//this.end(false, pos);
				for (ISpellChainListener lis : listeners) {
					if (lis.isPartOfChain(this, pos)) {
						lis.finished(this, pos, false);
					}
				}
				return 2;
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
				if (!prevPos.contains(other.getPos())) {
					if (other instanceof MagicWire) {
						Map<BlockPos,EnumFacing> toPut = this.findEndsOfMagicWireChain((MagicWire)other);
						for (BlockPos putting : toPut.keySet()) {

							printer.println("Magic wire chain end at " + putting);
							if (starter || getComponent(putting).accepts(toPut.get(putting), cmp)) {
								didSomething = true;
								didSomethingHere = true;
								this.putInputs(getComponent(putting), toPut.get(putting), cmp);
								nextPositions.add(putting);
							}
						}
					}
					else if (other.accepts(face.getOpposite(), cmp) || cmp.hasNoTags() && other.acceptsEmpty(face.getOpposite())) {
						didSomething = true;
						didSomethingHere = true;
						this.putInputs(other, face.getOpposite(), cmp);
						
						//other.getInputs().put(face.getOpposite(), cmp);
						nextPositions.add(other.getPos());
						printer.println(other + " marked for activation after " + comp + " with input at " + face + " : " + cmp);
					} else {
						printer.println("Output of " + comp + " not accepted by " + other);
						printer.println("For clarification, output is " + cmp);
					}
				}
				
				if (listener != null) {
					if (didSomethingHere) {
						listener.addToChain(this, pos.offset(face));
					} 
				} 
				
			}
			

			//if (listener != null) {
				if (!didSomething) {
					for (ISpellChainListener lis : listeners) {
						if (lis.isPartOfChain(this, pos)) {
							lis.finished(this, pos, true);
						}
					}
				} else {
					prevPositions.add(pos);
				}
			//}
			return 0;
		}
		
		
		/**
		 * Starts the spell
		 */
		public void start() {
			start(new HashMap<>());
		}
		
		/**
		 * Used to accumulate power into the spellspace from all power sources TODO
		 */
		public void gatherPower() {
			//TODO
			//TODO
			putPower(energyStorage.getMaxEnergyStored());
			//TODO
			//TODO
		}
		
		/**
		 * Starts spell
		 * @param initialWithInputs this map represents the initially given input values
		 */
		public void start(Map<BlockPos, SpellIOMap> initialWithInputs) {
			
			
			if (isRunning()) {
				printer.println("Running");
				return;
			}
			
			this.worldTicks = 0;

			gatherPower();
			
			if (SpellSpace.this.isFullyLoaded()) {
				SpellSpace.this.partsInit();//(SpellSpace.this.id, SpellSpace.this.height, SpellSpace.this.headPos, SpellSpace.this.edges, new ArrayList<>(SpellSpace.this.sigPoints.keySet()));
			} else {
				System.out.println("Not initializing parts");
			}
			System.out.println("Is fully loaded? " + SpellSpace.this.isFullyLoaded());
			
			if (MinecraftForge.EVENT_BUS.post(new ActivateSpellSpaceEvent(this))) {
				printer.println("Stopped activation of spellspace");
				return;
			}
			printer.println("Activating...");
			nextPositions.clear();
			//lastTickInputs.clear();
			List<BlockPos> statics = getPositionsOfInstancesInSpace(ISpellComponent.class, (c) -> c.isStatic() && c.getSpaceID() == SpellSpace.this.id);
			//List<BlockPos> nonstatics = getPositionsOfInstancesInSpace(ISpellComponent.class, (o) -> !o.isStatic());
			List<BlockPos> starters = getPositionsOfInstancesInSpace(ISpellComponent.class, (c) -> c.isStarter() && c.getSpaceID() == SpellSpace.this.id);
			List<ISpellComponent> comps = getInstancesInSpace(ISpellComponent.class);
			List<ISpellBorder> miniPositions = getInstancesInSpace(ISpellBorder.class, (c) -> c.getSpaceID() != SpellSpace.this.id && c.getSpellSpace() != SpellSpace.this && c.getSpellSpace() != null && c.isHeadPiece());
			List<SpellSpace> miniSpaces = new ArrayList<>();
			printer.println(comps);
			printer.println(starters);
			printer.println(statics);
			printer.println(miniPositions);
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

				magickingPos = pos;
				ISpellComponent comp = getComponent(pos);
				
				ServerPos modPos = comp.getServerPos();
				if (this.runPos != null) {
					modPos = runPos;
				}
				
				NonNullMap<EnumFacing, NBTTagCompound> returns = comp.getStaticOutput(modPos);
				printer.println("Static " + getComponent(pos) + " returns " + returns);
				this.disseminateStaticOutput(pos, true);
				
			}
			for (BlockPos pos : starters) {
				printer.println("Starter " + getComponent(pos));
				magickingPos = pos;
				int status = this.activateAndDisseminateOutput(pos, true, new NonNullMap<EnumFacing, NBTTagCompound>(NBTTagCompound::new), new ArrayList<>());
				if (status == 1 || status == 2) {
					end(status == 1, pos);
					return;
				}
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
			spellTick++;
			
		}
		/**
		 * Ticks the spell
		 */
		public void tick() {
			worldTicks++;
			if (worldTicks % 5 != 0) return;
			if (this.spellTick != 0) {
				if (MinecraftForge.EVENT_BUS.post(new SpellSpaceTickingEvent(this, spellTick))) {
					printer.println("Spell space casting ticking was suspended");
					return;
				}
				List<BlockPos> dels = new ArrayList<>(this.delayedPositions.keySet());
				for (BlockPos delPos : dels) {
					this.delayedPositions.put(delPos, this.delayedPositions.get(delPos) - 1);
					if (this.delayedPositions.get(delPos) <= 0) {
						nextPositions.add(delPos);
						delayedPositions.remove(delPos);
					}
				}
				
				printer.println("Ticking " + spellTick);
				boolean didSomething = false;
				
				List<BlockPos> pp = new ArrayList<>(nextPositions);
				prevPositions.addAll(nextPositions);
				nextPositions.clear();
				for (BlockPos pos : pp) {
					
					magickingPos = pos;
					didSomething = true;
					if (getComponent(pos) == null) {
						printer.println("Why is " + pos + " null uhhjjj");
						getWorld().setBlockState(pos, Blocks.FIRE.getDefaultState());
						continue;
					}
					//NonNullMap<EnumFacing, NBTTagCompound> cmp = lastTickInputs.get(pos);
					//if (cmp == null) continue;
					int status = this.activateAndDisseminateOutput(pos, false, inputsForTick.get(pos), prevPositions);
					if (status == 1 || status == 2) {
						end(status == 1, pos);
						return;
					}
				}
				
				if (didSomething || !nextPositions.isEmpty() || !delayedPositions.isEmpty()) {
					spellTick++;
					
					
					if (didSomething) {
						for (int i = 0; i < 80; i++) {
							Random r = new Random();
							spawnParticles(EnumParticleTypes.CRIT_MAGIC, false, (new Vec3d(magickingPos)).add(new Vec3d(0.5, 0.5, 0.5)), new Vec3d(2*r.nextDouble()-1, 2*r.nextDouble() -1, 2*r.nextDouble() - 1), dimension, 1);
						}
					}
				} else {
					
					end(true, headPos);
					return;
				}
			}
		}
		
		/**
		 * Ends the spell
		 * @param success whether it ended successfully
		 * @param endPos where it ended
		 */
		public void end(boolean success, BlockPos endPos) {
			MinecraftForge.EVENT_BUS.post(new EndSpellSpaceMagicEvent(this, success));
			printer.println("Spell ended " + (success ? "successfully" : "unsuccessfully") + " at " + endPos);
			//this.getWorld().setBlockState(endPos.up(), Blocks.END_GATEWAY.getDefaultState());
			
			nextPositions.clear();

			prevPositions.clear();
			
			inputsForTick.clear();
			//lastTickInputs.clear();
			spellTick = 0;
			
			for (ISpellComponent comp : getComponents()) {
				comp.end(this, success, endPos);
			}
			
			for (int i = 0; i < 100; i++) {
				Random r = new Random();
				SpellSpace.this.spawnParticles(success ? EnumParticleTypes.ENCHANTMENT_TABLE : EnumParticleTypes.LAVA, false, (new Vec3d(endPos)).add(new Vec3d(0.5, 0.5, 0.5)), new Vec3d(2*r.nextDouble()-1, 2*r.nextDouble() -1, 2*r.nextDouble() - 1), dimension, 1);
				if (runPos != null) {
					SpellSpace.this.spawnParticles(success ? EnumParticleTypes.ENCHANTMENT_TABLE : EnumParticleTypes.LAVA, false, (new Vec3d(runPos)).add(new Vec3d(0.5, 0.5, 0.5)), new Vec3d(2*r.nextDouble()-1, 2*r.nextDouble() -1, 2*r.nextDouble() - 1), dimension, 1);
				}
				if (runEntity != null) {
					SpellSpace.this.spawnParticles(success ? EnumParticleTypes.ENCHANTMENT_TABLE : EnumParticleTypes.LAVA, false, (runEntity).getPositionVector().add(new Vec3d(0.5, 0.5, 0.5)), new Vec3d(2*r.nextDouble()-1, 2*r.nextDouble() -1, 2*r.nextDouble() - 1), dimension, 1);
					
				}
				
			}
			SpellSpace.this.removeSpell(this);
			String[] loggings = this.logger.split("\n");
			for (String s : loggings) {
				//System.out.print(s);
			}
			System.out.println("Is still loaded? " + SpellSpace.this.isHeadLoaded());
		}
		
		/**
		 * Returns the spellspace this spell is connected to. Since a spell is an inner class, it just returns the outer class
		 * @return
		 */
		public SpellSpace getSpellSpace() {
			return SpellSpace.this;
		}
		
	}
	
}
