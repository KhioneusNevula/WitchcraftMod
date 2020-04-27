package com.gm910.goeturgy.spells.spellspaces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.gm910.goeturgy.Goeturgy;
import com.gm910.goeturgy.spells.events.space.CreateSpellSpaceEvent;
import com.gm910.goeturgy.spells.events.space.RemoveSpellSpaceEvent;
import com.gm910.goeturgy.spells.util.ISpellObject;
import com.gm910.goeturgy.util.NonNullMap;
import com.gm910.goeturgy.util.ServerPos;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class SpellSpaces extends WorldSavedData implements Collection<SpellSpace> {
	
	private static final String NAME = Goeturgy.MODID + "_spellspaces";
	private long currentID = 0;
	private final Map<Integer, List<SpellSpace>> spellSpacesByWorld = new NonNullMap<Integer, List<SpellSpace>>(() -> new ArrayList<SpellSpace>());
	private int power = 0;
	
	public SpellSpaces(String name) {
		super(name);
	}
	
	public SpellSpaces() {
		super(NAME);
	}
	
	public void addSpellSpace(SpellSpace sp) {
		boolean cannotContinue = MinecraftForge.EVENT_BUS.post(new CreateSpellSpaceEvent(sp));
		if (cannotContinue) {
			System.out.println("SpellSpace creation blocked");
			return;
		}
		for (SpellSpace space : spellSpacesByWorld.get(sp.dimension)) {
			if (space.headPos.equals(sp.headPos)) {
				System.err.println("Spellspace exists!");
				return;
			}
		}
		MinecraftForge.EVENT_BUS.register(sp);
		spellSpacesByWorld.get(sp.getDimension()).add(sp);
		markDirty();
	}
	
	public SpellSpace createSpellSpace(int dimension, int height, BlockPos head, List<BlockPos> outline, List<BlockPos> significantPoints) {
		for (SpellSpace space : spellSpacesByWorld.get(dimension)) {
			if (space.headPos.equals(head)) {
				System.err.println("Spellspace exists!");
				//throw new IllegalArgumentException("SpellSpace exists!");
				//return null;
			}
		}
		System.out.println("Created spellspace");
		SpellSpace created = new SpellSpace(dimension);
		created.init(returnNewID(), height, head, outline, significantPoints);
		this.addSpellSpace(created);
		markDirty();
		return created;
	}
	
	public void removeSpellSpace(SpellSpace sp) {
		boolean canceled = MinecraftForge.EVENT_BUS.post(new RemoveSpellSpaceEvent(sp));
		if (canceled) {
			System.out.println("Canceled removal of spellspace");
			return;
		}
		
		
		
		for (BlockPos pos : sp.getInnerSpace()) {
			ISpellObject part = sp.getWorld().getTileEntity(pos) instanceof ISpellObject ? (ISpellObject)sp.getWorld().getTileEntity(pos) : null;
			if (part == null) continue;
			part.setSpaceID(-1);
		}
		
		for (BlockPos pos : sp.sigPoints.keySet()) {
			ISpellObject part = sp.getWorld().getTileEntity(pos) instanceof ISpellObject ? (ISpellObject)sp.getWorld().getTileEntity(pos) : null;
			if (part == null) continue;
			part.setSpaceID(-1);
		}
		
		long id = sp.id;
		
		spellSpacesByWorld.get(sp.getDimension()).remove(sp);
		sp.id = -1;
		sp.clientTick(new RenderWorldLastEvent(null, 0.1685f));
		MinecraftForge.EVENT_BUS.unregister(sp);
		System.out.println("Removed spellspace " + id);
		markDirty();
	}
	
	public SpellSpace getById(long id) {
		for (List<SpellSpace> ls : this.spellSpacesByWorld.values()) {
			for (SpellSpace sp : ls) {
				if (sp.getID() == id) return sp;
			}
		}
		return null;
	}
	
	public SpellSpace getByPosition(ServerPos position) {
		
		for (SpellSpace sp : spellSpacesByWorld.get(position.d)) {
			if (sp.getInnerSpace().contains(position)) {
				return sp;
			}
		}
		return null;
	}
	
	public long returnNewID() {
		long id = currentID;
		currentID++;
		markDirty();
		return id;
	}
	
	public long getCurrentID() {
		return currentID;
	}
	
	public void setCurrentID(long currentID) {
		this.currentID = currentID;
		markDirty();
	}
	
	public void addPower(int power) {
		this.power += power;
		markDirty();
	}
	
	public int getPower() {
		return power;
	}
	
	public void setPower(int power) {
		this.power = power;
		markDirty();
	}
	
	public int removePower(int power) {
		if (power <= this.power) {
			this.power -= power;
			markDirty();
			return power;
		} else {
			return 0;
		}
	}
	
	public static SpellSpaces get(World world) {
		  
		  MapStorage storage = world.getMapStorage();
		  SpellSpaces instance = (SpellSpaces) storage.getOrLoadData(SpellSpaces.class, NAME);

		  if (instance == null) {
		    instance = new SpellSpaces();
		    storage.setData(NAME, instance);
		  }
		  return instance;
	}
	
	public static SpellSpaces get() {
		if (Goeturgy.proxy.getServer() == null) {
			System.out.println("On client side???");
			return null;
		}
		return get(FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0));
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		System.out.println("Deserializing spell space data");
		this.currentID = nbt.getLong("LastID");
		this.power = nbt.getInteger("Power");
		this.spellSpacesByWorld.clear();
		

		//this.spellSpacesByWorld.get(dim);
		for (NBTBase base : nbt.getTagList("SpellSpaceList", NBT.TAG_COMPOUND)) {
			NBTTagCompound comp = (NBTTagCompound)base;
			int dim = comp.getInteger("Dimension");
			List<SpellSpace> list = new ArrayList<>();
			for (NBTBase base2 : nbt.getTagList("SpellSpaces", NBT.TAG_COMPOUND)) {
				SpellSpace s = new SpellSpace(dim);
				s.deserializeNBT((NBTTagCompound) base2);
				//list.add(s);
				list.add(s);
			}
			this.spellSpacesByWorld.put(dim,  list);
			
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		System.out.println("Serializing spell space data");
		compound.setLong("LastID", currentID);
		compound.setInteger("Power", this.power);
		NBTTagList ls = new NBTTagList();
		for (int dim : this.spellSpacesByWorld.keySet()) {
			NBTTagCompound cmp = new NBTTagCompound();
			cmp.setInteger("Dimension", dim);
			NBTTagList sps = new NBTTagList();
			
			for (SpellSpace m : this.spellSpacesByWorld.get(dim)) {
				sps.appendTag(m.serializeNBT());
			}
			cmp.setTag("SpellSpaces", sps);
			ls.appendTag(cmp);
		}
		compound.setTag("SpellSpaceList", ls);
		return compound;
	}

	@Override
	public Iterator<SpellSpace> iterator() {
		List<SpellSpace> spsp = new ArrayList<>();
		for (int x : this.spellSpacesByWorld.keySet()) {
			spsp.addAll(this.spellSpacesByWorld.get(x));
		}
		return spsp.iterator();
	}

	@Override
	public boolean add(SpellSpace arg0) {
		this.addSpellSpace(arg0);
		markDirty();
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends SpellSpace> c) {
		boolean b = false;
		for (SpellSpace sp : c) {
			b = true;
			this.add((SpellSpace)c);
		}
		markDirty();
		return b;
	}

	@Override
	public void clear() {
		this.spellSpacesByWorld.clear();
		markDirty();
	}
	
	public List<SpellSpace> getAsList() {
		List<SpellSpace > ls = new ArrayList<>();
		for (int x : spellSpacesByWorld.keySet()) {
			ls.addAll(this.spellSpacesByWorld.get(x));
		}
		return ls;
	}

	@Override
	public boolean contains(Object o) {
		
		return getAsList().contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		
		return getAsList().containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		
		return getAsList().isEmpty();
	}

	@Override
	public boolean remove(Object o) {
		if (o instanceof Integer) {
			this.spellSpacesByWorld.remove((Integer)o);
			return true;
		} else if (o instanceof SpellSpace) {
			this.spellSpacesByWorld.get(((SpellSpace)o).getDimension()).remove((SpellSpace)o);
			return true;
		}
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean worked = false;
		for (Object o : c) {
			Object t = this.spellSpacesByWorld.remove(o);
			if (t == null) {
				if (o instanceof SpellSpace) {
					boolean w = this.spellSpacesByWorld.get(((SpellSpace)o).getDimension()).remove((SpellSpace)o);
					worked = w;
				}
			} else {
				worked = true;
			}
		}
		return worked;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean b = false;
		for (Object o : this.getAsList()) {
			if (!c.contains(o)) {
				boolean x = this.remove(o);
				b = x;
			}
		}
		return b;
	}

	@Override
	public int size() {
		
		return getAsList().size();
	}

	@Override
	public Object[] toArray() {
		
		return getAsList().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return getAsList().toArray(a);
	}
	
	public SpellSpace[] toArrayProper() {
		return toArray(new SpellSpace[0]);
	}

}
