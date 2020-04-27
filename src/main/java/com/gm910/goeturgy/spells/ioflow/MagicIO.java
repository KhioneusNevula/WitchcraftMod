package com.gm910.goeturgy.spells.ioflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.gm910.goeturgy.Goeturgy;
import com.gm910.goeturgy.util.GMNBT;
import com.gm910.goeturgy.util.NonNullMap;
import com.gm910.goeturgy.util.ServerPos;
import com.google.common.base.Functions;
import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.registries.GameData;

public abstract class MagicIO {

	public static final String STRING = "String";
	public static final String INT = "Integer";
	public static final String POS = "Position";
	public static final String ENTITY = "Entity";
	
	
	public static final String BOOL = "Boolean";
	public static final String LONG = "Long";
	
	public static final String SPECIES = "Species";
	
	public static final String ITEM = "ItemStack";
	public static final String BLOCK = "Block";
	
	public static final String ENTITY_DATA = "EntityData";
	
	public static final Map<String, Class<?>> TYPE_FOR_TAG = new HashMap<>();
	
	public static final Map<String, Function<NBTTagCompound, Object>> GETTERS = new HashMap<>();
	
	public static final Map<String, Function<NBTBase, Object>> DIRECT_GETTERS = new HashMap<>();
	
	public static final Map<String, Function<Object, NBTBase>> WRITERS = new HashMap<>();
	
	public static final Map<String, Integer> LIST_TYPE_FOR_TAG = new HashMap<>();
	
	
	
	private MagicIO() {
		throw new UnsupportedOperationException("Cannot initialize this");
	}
	
	public static final List<String> TAG_TYPES = Lists.newArrayList(
	STRING, INT, POS, ENTITY,
		BOOL, LONG,
		SPECIES,
		ITEM, BLOCK,
		ENTITY_DATA
	);
			
	
	public static String toList(String name) {
		return name + "List";
	}
	
	public static <T> T get(String k, NBTTagCompound cmp, Function<String, T> func) {
		if (cmp.hasKey(k)) {
			return func.apply(k);
		}
		return (T) null;
	}
	
	public static <T> T get(String k, NBTTagCompound cmp, int t, BiFunction<String, Integer, T> func) {
		if (cmp.hasKey(k)) {
			return func.apply(k, t);
		}
		return (T) null;
	}
	
	public static <T> void set(T t, String k, BiConsumer<String, T> func) {
		func.accept(k, t);
	}
	
	public static UUID getEntity(NBTTagCompound cmp) {
		
		return get(ENTITY, cmp, cmp::getUniqueId);
	}
	
	public static <T extends Entity> T getPhysicalEntity(Class<T> clazz, NBTTagCompound cmp) {
		
		return entityfromid(clazz, get(ENTITY, cmp, cmp::getUniqueId));
	}
	
	public static Entity getPhysicalEntity(NBTTagCompound cmp) {
		return getPhysicalEntity(Entity.class, cmp);
	}
	
	public static <T extends Entity> T entityfromid(Class<T> clazz, UUID uu) {
		return (T) Goeturgy.proxy.getServer().getEntityFromUuid(uu);
	}
	
	public static Entity entityfromid(UUID uu) {
		return entityfromid(Entity.class, uu);
	}
	
	public static ResourceLocation getSpecies(NBTTagCompound cmp) {
		if (!cmp.hasKey(SPECIES)) return null;
		return new ResourceLocation(cmp.getString(SPECIES));
	}
	
	public static Class<? extends Entity> getFromRL(ResourceLocation id) {
		return GameData.getEntityRegistry().getValue(id) != null ? GameData.getEntityRegistry().getValue(id).getEntityClass() : null;
	}
	
	public static Entity createFromRL(ResourceLocation l, World world) {
		return GameData.getEntityRegistry().getValue(l) != null ? GameData.getEntityRegistry().getValue(l).newInstance(world) : null;
	}
	
	public static int entityClass(Class<? extends Entity> id) {
		return GameData.getEntityRegistry().getID(GameData.getEntityClassMap().get(id));
	}
	
	public static ItemStack getItemStack(NBTTagCompound cmp) {
		return get(ITEM, cmp, cmp::getCompoundTag) == null ? ItemStack.EMPTY : new ItemStack(get(ITEM, cmp, cmp::getCompoundTag)); 
	}
	
	public static BlockStack getBlockStack(NBTTagCompound cmp) {
		return BlockStack.fromNBT(get(BLOCK, cmp, cmp::getCompoundTag));
	}
	
	public static NBTTagCompound getEntityAsData(NBTTagCompound cmp) {
		return get(ENTITY_DATA, cmp, cmp::getCompoundTag);
	}
	
	public static Entity getPhysicalEntityFromData(NBTTagCompound cmp) {
		return createEntityFromData(get(ENTITY_DATA, cmp, cmp::getCompoundTag));
	}
	
	public static Entity createEntityFromData(NBTTagCompound cmp) {
		return EntityList.createEntityFromNBT(cmp, Goeturgy.proxy.getWorld(cmp.getInteger("Dimension")));
	}
	
	
	public static long getLong(NBTTagCompound cmp) {
		return get(LONG, cmp, cmp::getLong);
	}
	
	public static boolean getBool(NBTTagCompound cmp) {
		
		return get(BOOL, cmp, cmp::getBoolean);
	}
	
	public static String getString(NBTTagCompound cmp) {
		
		return get(STRING, cmp, cmp::getString);
	}
	
	public static int getInteger(NBTTagCompound cmp) {
		
		return get(INT, cmp, cmp::getInteger);
	}
	
	public static ServerPos getPos(NBTTagCompound cmp) {
		NBTTagCompound cm = get(POS, cmp, cmp::getCompoundTag);
		if (cm == null) return null;
		return ServerPos.fromNBT(cm);
	}
	
	
	public static <T, K extends NBTBase> List<T> getList(String tag, int tagType, NBTTagCompound cmp, Class<K> nbtClazz, Function<K, T> getter) {
		NBTTagList ls =get(tag, cmp, tagType, cmp::getTagList);
		if (ls == null) return null;
		return GMNBT.createList(ls, (v) -> { return getter.apply(((K)v));});
	}
	
	public static List<ServerPos> getPosList(NBTTagCompound cmp) {
		
		return getList(toList(POS), NBT.TAG_COMPOUND, cmp, NBTTagCompound.class, (t) ->  {return ServerPos.fromNBT(t);});
	}
	
	public static List<UUID> getEntityList(NBTTagCompound cmp) {
		
		return getList(toList(ENTITY), NBT.TAG_COMPOUND, cmp, NBTTagCompound.class, (t) ->  {return t.getUniqueId(ENTITY);});
	}
	
	public static <T extends Entity> List<T> getPhysicalEntityList(NBTTagCompound cmp) {
		List<T> ls = new ArrayList<>();
		for (UUID uu : getEntityList(cmp)) {
			ls.add(MagicIO.<T>entityfromid(null, uu));
		}
		return ls;
	}
	
	public static List<Entity> getPhysicalEntityListFromEntityData(NBTTagCompound cmp) {
		List<Entity> ls = new ArrayList<>();
		for (NBTTagCompound uu : getEntityDataList(cmp)) {
			ls.add(createEntityFromData(uu));
		}
		return ls;
	}
	
	public static List<ResourceLocation> getSpeciesList(NBTTagCompound cmp) {
		
		return getList(toList(SPECIES), NBT.TAG_STRING, cmp, NBTTagString.class, (t) ->  {return new ResourceLocation(t.getString());});
	}
	
	public static List<String> getStringList(NBTTagCompound cmp) {
		
		return getList(toList(STRING), NBT.TAG_STRING, cmp, NBTTagString.class, (t) ->  {return t.getString();});
	}
	
	public static List<Integer> getIntList(NBTTagCompound cmp) {
		
		return getList(toList(INT), NBT.TAG_INT, cmp, NBTTagInt.class, (t) ->  {return t.getInt();});
	}
	
	public static List<Long> getLongList(NBTTagCompound cmp) {
		
		return getList(toList(LONG), NBT.TAG_LONG, cmp, NBTTagLong.class, (t) ->  {return t.getLong();});
	}
	
	public static List<Boolean> getBoolList(NBTTagCompound cmp) {
		
		return getList(toList(BOOL), NBT.TAG_BYTE, cmp, NBTTagByte.class, (t) ->  {return t.getByte() != 0;});
	}
	
	public static List<ItemStack> getItemStackList(NBTTagCompound cmp) {
		return getList(toList(ITEM), NBT.TAG_COMPOUND, cmp, NBTTagCompound.class, ItemStack::new);
	}
	
	public static List<BlockStack> getBlockList(NBTTagCompound cmp) {
		return getList(toList(BLOCK), NBT.TAG_COMPOUND, cmp, NBTTagCompound.class, BlockStack::fromNBT);
	}
	
	public static List<NBTTagCompound> getEntityDataList(NBTTagCompound cmp) {
		return getList(toList(ENTITY_DATA), NBT.TAG_COMPOUND, cmp, NBTTagCompound.class, Functions.identity());
	}
	
	public static <T> boolean has(String key, NonNullMap<T, NBTTagCompound> map) {
		for (NBTTagCompound v : map.values()) {
			if (v.hasKey(key)) {
				return true;
			}
		}
		return false;
		
	}
	
	public static <T> boolean hasList(NonNullMap<T, NBTTagCompound> map) {
		for (String s : TAG_TYPES) {
			if (has(toList(s), map)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean hasList(NBTTagCompound cmp) {
		for (String s : TAG_TYPES) {
			if (cmp.hasKey(toList(s))) {
				return true;
			}
		}
		return false;
	}
	
	public static NonNullMap<EnumFacing, NBTTagCompound> makeMap() {
		return makeMap(NBTTagCompound::new);
	}
	
	public static NonNullMap<EnumFacing, NBTTagCompound> makeMap(Supplier<NBTTagCompound> sup) {
		return new NonNullMap<EnumFacing, NBTTagCompound>(sup);
	}
	
	public static NonNullMap<EnumFacing, NBTTagCompound> makeMap(EnumFacing f, final NBTTagCompound c) {
		return new NonNullMap<>((f1) -> { if (f1 == f) return c.copy(); else return new NBTTagCompound();});
	}
	
	public static NonNullMap<EnumFacing, NBTTagCompound> makeMap(final NBTTagCompound cmp) {
		return makeMap(cmp::copy);
	}
	
	public static NBTTagList nonNullMapToNBT(NonNullMap<EnumFacing, NBTTagCompound> map) {
		return GMNBT.makeList(map.keySet(), (k) -> {
			NBTTagCompound cmp = new NBTTagCompound();
			cmp.setInteger("Facing", k.getIndex());
			cmp.setTag("Data", map.get(k));
			return cmp;
		});
	}
	
	public static NonNullMap<EnumFacing, NBTTagCompound> nonNullMapFromNBT(NBTTagList ls) {
		NonNullMap<EnumFacing, NBTTagCompound> it = NonNullMap.create(NBTTagCompound::new);
		GMNBT.forEach(ls, (n ) -> {
			NBTTagCompound cmp = (NBTTagCompound)n;
			it.put(EnumFacing.VALUES[cmp.getInteger("Facing")], cmp.getCompoundTag("Data"));
		});
		return it;
	}
	
	
	
	public static NBTTagCompound write(String type, Object info, NBTTagCompound cmp) {
		if (info instanceof NBTBase) {
			cmp.setTag(type, (NBTBase)info);
			return cmp;
		}
		cmp.setTag(type, WRITERS.get(type).apply(info));
		
		return cmp;
	}
	
	public static NBTBase writeInt(int in) {
		return new NBTTagInt(in);
	}
	
	public static NBTBase writeBool(boolean in) {
		return new NBTTagByte((in ? (byte)1 : (byte)0));
	}
	
	public static NBTBase writeLong(long in) {
		return new NBTTagLong(in);
	}
	
	public static NBTBase writeString(String str) {
		return new NBTTagString(str);
	}
	
	public static NBTBase writeEntity(UUID str) {
		NBTTagCompound cmp = new NBTTagCompound(); cmp.setUniqueId(ENTITY, str); return cmp; 
	}
	
	public static <T extends Entity> NBTBase writeEntity(T str) {
		NBTTagCompound cmp = new NBTTagCompound(); cmp.setUniqueId(ENTITY, str.getUniqueID()); return cmp; 
	}
	
	
	
	public static NBTBase writeSpecies(ResourceLocation in) {
		return new NBTTagString(in.toString());
	}
	

	public static NBTBase writePos(ServerPos str) {
		return str.toNBT();
	}
	
	public static NBTBase writeItemStack(ItemStack str) {
		return str.writeToNBT(new NBTTagCompound());
	}
	
	public static NBTBase writeBlock(BlockStack str) {
		return str.writeToNBT(new NBTTagCompound());
	}
	
	public static NBTBase writeEntityData(NBTTagCompound str) {
		return str;
	}
	
	public static NBTBase writeEntityToData(Entity str) {
		return str.serializeNBT();
	}
	
	public static NBTTagList writePosList(List<ServerPos> ls) {
		return GMNBT.makeList(ls, (e) -> {return writePos(e);});
	}
	
	public static NBTTagList writeStringList(List<String> ls) {
		return GMNBT.makeList(ls, (e) -> {return writeString(e);});
	}
	
	public static NBTTagList writeIntList(List<Integer> ls) {
		return GMNBT.makeList(ls, (e) -> {return writeInt(e);});
	}
	
	public static NBTTagList writeIntList(int[] ls) {
		List<Integer> l = new ArrayList<>();
		for (int e : ls) {
			l.add(e);
		}
		return GMNBT.makeList(l, (e) -> {return writeInt(e);});
	}
	
	public static NBTTagList writeLongList(List<Long> ls) {
		return GMNBT.makeList(ls, (e) -> {return writeLong(e);});
	}
	
	public static NBTTagList writeLongList(long[] ls) {
		List<Long> l = new ArrayList<>();
		for (long e : ls) {
			l.add(e);
		}
		return GMNBT.makeList(l, (e) -> {return writeLong(e);});
	}
	
	public static NBTTagList writeBoolList(List<Boolean> ls) {
		return GMNBT.makeList(ls, (e) -> {return writeBool(e);});
	}
	
	public static NBTTagList writeBoolList(boolean[] ls) {
		List<Boolean> l = new ArrayList<>();
		for (boolean e : ls) {
			l.add(e);
		}
		return GMNBT.makeList(l, (e) -> {return writeBool(e);});
	}
	
	public static NBTTagList writeEntityList(List<UUID> ls) {
		return GMNBT.makeList(ls, (e) ->  writeEntity(e));
	}
	
	public static <T extends Entity> NBTTagList writePhysicalEntityList(List<T> ls) {
		return GMNBT.makeList(ls, (e) ->  writeEntity(e));
	}
	
	public static NBTTagList writeSpeciesList(List<ResourceLocation> ls) {
		return GMNBT.makeList(ls, (e) -> {return writeSpecies(e);});
	}
	
	public static NBTTagList writeItemList(List<ItemStack> ls) {
		return GMNBT.makeList(ls, (e) -> {return writeItemStack(e);});
	}
	
	public static NBTTagList writeBlockList(List<BlockStack> ls) {
		return GMNBT.makeList(ls, (e) -> {return writeBlock(e);});
	}
	
	public static NBTTagList writeEntityDataList(List<NBTTagCompound> ls) {
		return GMNBT.makeList(ls, (e) -> {return writeEntityData(e);});
	}
	
	public static NBTTagList writeEntityListToEntityDataList(List<Entity> ls) {
		return GMNBT.makeList(ls, (e) -> {return writeEntityToData(e);});
	}
	
	public static NBTTagCompound writeEntityToCompound(UUID en, NBTTagCompound nbt) {
		nbt.setTag(ENTITY, writeEntity(en));
		return nbt;
	}
	
	public static <T extends Entity> NBTTagCompound writeEntityToCompound(T en, NBTTagCompound nbt) {
		return writeEntityToCompound(en.getUniqueID(), nbt);
	}
	
	public static NBTTagCompound writeSpeciesToCompound(ResourceLocation en, NBTTagCompound nbt) {
		nbt.setTag(SPECIES, writeSpecies(en));
		return nbt;
	}
	
	public static NBTTagCompound writeIntToCompound(int en, NBTTagCompound nbt) {
		nbt.setTag(INT, writeInt(en));
		return nbt;
	}
	
	public static NBTTagCompound writeLongToCompound(long en, NBTTagCompound nbt) {
		nbt.setTag(LONG, writeLong(en));
		return nbt;
	}
	
	public static NBTTagCompound writeBoolToCompound(boolean en, NBTTagCompound nbt) {
		nbt.setTag(BOOL, writeBool(en));
		return nbt;
	}
	
	public static NBTTagCompound writeStringToCompound(String en, NBTTagCompound nbt) {
		nbt.setTag(STRING, writeString(en));
		return nbt;
	}
	
	public static NBTTagCompound writePosToCompound(ServerPos en, NBTTagCompound nbt) {
		nbt.setTag(POS, writePos(en));
		return nbt;
	}
	
	public static NBTTagCompound writeItemToCompound(ItemStack en, NBTTagCompound nbt) {
		nbt.setTag(ITEM, writeItemStack(en));
		return nbt;
	}
	
	public static NBTTagCompound writeBlockToCompound(BlockStack en, NBTTagCompound nbt) {
		nbt.setTag(BLOCK, writeBlock(en));
		return nbt;
	}
	
	public static NBTTagCompound writeEntityDataToCompound(NBTTagCompound en, NBTTagCompound nbt) {
		nbt.setTag(ENTITY_DATA, writeEntityData(en));
		return nbt;
	}
	
	public static NBTTagCompound writeEntityDataToCompound(Entity en, NBTTagCompound nbt) {
		nbt.setTag(ENTITY_DATA, writeEntityToData(en));
		return nbt;
	}
	
	public static NBTTagCompound writeEntityListToCompound(List<UUID> en, NBTTagCompound nbt) {
		nbt.setTag(toList(ENTITY), writeEntityList(en));
		return nbt;
	}
	
	public static <T extends Entity> NBTTagCompound writePhysicalEntityListToCompound(List<T> en, NBTTagCompound nbt) {
		nbt.setTag(toList(ENTITY), writePhysicalEntityList(en));
		return nbt;
	}
	
	public static NBTTagCompound writePosListToCompound(List<ServerPos> en, NBTTagCompound nbt) {
		nbt.setTag(toList(POS), writePosList(en));
		return nbt;
	}
	
	public static NBTTagCompound writeIntListToCompound(List<Integer> en, NBTTagCompound nbt) {
		nbt.setTag(toList(INT), writeIntList(en));
		return nbt;
	}
	
	public static NBTTagCompound writeIntListToCompound(int[] en, NBTTagCompound nbt) {
		nbt.setTag(toList(INT), writeIntList(en));
		return nbt;
	}
	
	public static NBTTagCompound writeLongListToCompound(List<Long> en, NBTTagCompound nbt) {
		nbt.setTag(toList(LONG), writeLongList(en));
		return nbt;
	}
	
	public static NBTTagCompound writeLongListToCompound(long[] en, NBTTagCompound nbt) {
		nbt.setTag(toList(LONG), writeLongList(en));
		return nbt;
	}
	
	public static NBTTagCompound writeBoolListToCompound(List<Boolean> en, NBTTagCompound nbt) {
		nbt.setTag(toList(BOOL), writeBoolList(en));
		return nbt;
	}
	
	public static NBTTagCompound writeBoolListToCompound(boolean[] en, NBTTagCompound nbt) {
		nbt.setTag(toList(BOOL), writeBoolList(en));
		return nbt;
	}
	
	public static NBTTagCompound writeStringListToCompound(List<String> en, NBTTagCompound nbt) {
		nbt.setTag(toList(STRING), writeStringList(en));
		return nbt;
	}
	
	public static NBTTagCompound writeItemListToCompound(List<ItemStack> en, NBTTagCompound nbt) {
		nbt.setTag(toList(ITEM), writeItemList(en));
		return nbt;
	}
	
	public static NBTTagCompound writeBlockListToCompound(List<BlockStack> en, NBTTagCompound nbt) {
		nbt.setTag(toList(BLOCK), writeBlockList(en));
		return nbt;
	}
	
	public static NBTTagCompound writeEntityDataListToCompound(List<NBTTagCompound> en, NBTTagCompound nbt) {
		nbt.setTag(toList(ENTITY_DATA), writeEntityDataList(en));
		return nbt;
	}
	
	public static NBTTagCompound writeEntityListToEntityDataListToCompound(List<Entity> en, NBTTagCompound nbt) {
		nbt.setTag(toList(ENTITY_DATA), writeEntityListToEntityDataList(en));
		return nbt;
	}
	
	public static NBTTagCompound writeSpeciesListToCompound(List<ResourceLocation> en, NBTTagCompound nbt) {
		nbt.setTag(toList(SPECIES), writeSpeciesList(en));
		return nbt;
	}
	
	static {
		TYPE_FOR_TAG.put(STRING, String.class);
		TYPE_FOR_TAG.put(INT, int.class);
		TYPE_FOR_TAG.put(POS, ServerPos.class);
		TYPE_FOR_TAG.put(ENTITY, UUID.class);
		TYPE_FOR_TAG.put(BOOL, boolean.class);
		TYPE_FOR_TAG.put(LONG, long.class);
		TYPE_FOR_TAG.put(SPECIES, ResourceLocation.class);
		TYPE_FOR_TAG.put(ITEM, ItemStack.class);
		TYPE_FOR_TAG.put(BLOCK, BlockStack.class);
		TYPE_FOR_TAG.put(ENTITY_DATA, NBTTagCompound.class);
		
		LIST_TYPE_FOR_TAG.put(STRING, NBT.TAG_STRING);
		LIST_TYPE_FOR_TAG.put(INT, NBT.TAG_INT);
		LIST_TYPE_FOR_TAG.put(POS, NBT.TAG_COMPOUND);
		LIST_TYPE_FOR_TAG.put(ENTITY, NBT.TAG_COMPOUND);
		LIST_TYPE_FOR_TAG.put(BOOL, NBT.TAG_BYTE);
		LIST_TYPE_FOR_TAG.put(LONG, NBT.TAG_LONG);
		LIST_TYPE_FOR_TAG.put(SPECIES, NBT.TAG_STRING);
		LIST_TYPE_FOR_TAG.put(ITEM, NBT.TAG_COMPOUND);
		LIST_TYPE_FOR_TAG.put(BLOCK, NBT.TAG_COMPOUND);
		LIST_TYPE_FOR_TAG.put(ENTITY_DATA, NBT.TAG_COMPOUND);
		
		GETTERS.put(STRING, MagicIO::getString);
		GETTERS.put(INT, MagicIO::getInteger);
		GETTERS.put(POS, MagicIO::getPos);
		GETTERS.put(ENTITY, MagicIO::getEntity);
		GETTERS.put(BOOL, MagicIO::getBool);
		GETTERS.put(LONG, MagicIO::getLong);
		GETTERS.put(SPECIES, MagicIO::getSpecies);
		GETTERS.put(ITEM, MagicIO::getItemStack);
		GETTERS.put(BLOCK, MagicIO::getBlockStack);
		GETTERS.put(ENTITY_DATA, MagicIO::getEntityAsData);
		
		GETTERS.put(toList(STRING), MagicIO::getStringList);
		GETTERS.put(toList(INT), MagicIO::getIntList);
		GETTERS.put(toList(POS), MagicIO::getPosList);
		GETTERS.put(toList(ENTITY), MagicIO::getEntityList);
		GETTERS.put(toList(BOOL), MagicIO::getBoolList);
		GETTERS.put(toList(LONG), MagicIO::getLongList);
		GETTERS.put(toList(SPECIES), MagicIO::getSpeciesList);
		GETTERS.put(toList(ITEM), MagicIO::getItemStackList);
		GETTERS.put(toList(BLOCK), MagicIO::getBlockList);
		GETTERS.put(toList(ENTITY_DATA), MagicIO::getEntityDataList);
		
		DIRECT_GETTERS.put(STRING, (n) -> {
			return ((NBTTagString)n).getString();
		});
		DIRECT_GETTERS.put(INT, (n) -> ((NBTTagInt)n).getInt());
		DIRECT_GETTERS.put(POS, (n) -> ServerPos.fromNBT((NBTTagCompound)n));
		DIRECT_GETTERS.put(ENTITY, (n) -> ((NBTTagCompound)n).getUniqueId(ENTITY));
		DIRECT_GETTERS.put(BOOL, (n) -> ((NBTTagByte)n).getByte() == 0 ? false : true);
		DIRECT_GETTERS.put(LONG, (n) -> ((NBTTagLong)n).getLong());
		DIRECT_GETTERS.put(SPECIES, (n) -> new ResourceLocation(((NBTTagString)n).getString()));
		DIRECT_GETTERS.put(ITEM, (n) -> { ItemStack st = new ItemStack((NBTTagCompound)n); if (st.isEmpty()) return null; return st;});
		DIRECT_GETTERS.put(BLOCK, (n) -> BlockStack.fromNBT((NBTTagCompound)n));
		DIRECT_GETTERS.put(ENTITY_DATA, NBTTagCompound.class::cast);
		
		/*DIRECT_GETTERS.put(toList(STRING), MagicIO::getStringList);
		DIRECT_GETTERS.put(toList(INT), MagicIO::getIntList);
		DIRECT_GETTERS.put(toList(POS), MagicIO::getPosList);
		DIRECT_GETTERS.put(toList(ENTITY), MagicIO::getEntityList);
		DIRECT_GETTERS.put(toList(BOOL), MagicIO::getBoolList);
		DIRECT_GETTERS.put(toList(LONG), MagicIO::getLongList);
		DIRECT_GETTERS.put(toList(SPECIES), MagicIO::getSpeciesList);
		DIRECT_GETTERS.put(toList(ITEM), MagicIO::getItemStackList);
		DIRECT_GETTERS.put(toList(BLOCK), MagicIO::getBlockList);
		DIRECT_GETTERS.put(toList(ENTITY_DATA), MagicIO::getEntityDataList);*/
		
		WRITERS.put(STRING, (E) -> MagicIO.writeString((String)E));
		WRITERS.put(INT, (E) -> MagicIO.writeInt((int)E));
		WRITERS.put(POS, (E) -> MagicIO.writePos((ServerPos)E));
		WRITERS.put(ENTITY, (E) -> {
			boolean w = false;
			try {
				UUID info2 = (UUID)E;
				w = true;
			} catch (ClassCastException c) {
				try {
					Entity info2 = (Entity)E;
					
				} catch (ClassCastException e) {
					throw new IllegalArgumentException("Type mismatches");
				}
			}
			if (w)
			return MagicIO.writeEntity((UUID)E);
			else
			return MagicIO.writeEntity((Entity)E);
		});
		WRITERS.put(BOOL, (E) -> MagicIO.writeBool((boolean)E));
		WRITERS.put(LONG, (E) -> MagicIO.writeLong((long)E));
		WRITERS.put(SPECIES, (E) -> MagicIO.writeSpecies((ResourceLocation)E));
		WRITERS.put(ITEM, (E) -> MagicIO.writeItemStack((ItemStack)E));
		WRITERS.put(BLOCK, (E) -> MagicIO.writeBlock((BlockStack)E));
		WRITERS.put(ENTITY_DATA, (E) -> {
			boolean w = false;
			try {
				NBTTagCompound info2 = (NBTTagCompound)E;
				w = true;
			} catch (ClassCastException c) {
				try {
					Entity info2 = (Entity)E;
					
				} catch (ClassCastException e) {
					throw new IllegalArgumentException("Type mismatches");
				}
			}
			if (w)
			return MagicIO.writeEntityData((NBTTagCompound)E);
			else
			return MagicIO.writeEntityToData((Entity)E);
		});
		
		WRITERS.put(toList(STRING), (E) -> MagicIO.writeStringList((List<String>)E));
		WRITERS.put(toList(INT), (E) -> {
			boolean w = false;
			try {
				int[] info2 = (int[])E;
				w = true;
			} catch (ClassCastException c) {
				try {
					List<Integer> info2 = (List<Integer>)E;
					
				} catch (ClassCastException e) {
					throw new IllegalArgumentException("Type mismatches");
				}
			}
			if (w)
			return MagicIO.writeIntList((int[])E);
			else
			return MagicIO.writeIntList((List<Integer>)E);
		});
		WRITERS.put(toList(POS), (E) -> MagicIO.writePosList((List<ServerPos>)E));
		WRITERS.put(toList(ENTITY), (E) -> {
			boolean w = false;
			try {
				List<UUID> info2 = (List<UUID>)E;
				w = true;
			} catch (ClassCastException c) {
				try {
					List<Entity> info2 = (List<Entity>)E;
					
				} catch (ClassCastException e) {
					throw new IllegalArgumentException("Type mismatches");
				}
			}
			if (w)
			return MagicIO.writeEntityList((List<UUID>)E);
			else
			return MagicIO.writePhysicalEntityList((List<Entity>)E);
		});
		WRITERS.put(toList(BOOL), (E) -> {
			boolean w = false;
			try {
				boolean[] info2 = (boolean[])E;
				w = true;
			} catch (ClassCastException c) {
				try {
					List<Boolean> info2 = (List<Boolean>)E;
					
				} catch (ClassCastException e) {
					throw new IllegalArgumentException("Type mismatches");
				}
			}
			if (w)
			return MagicIO.writeBoolList((boolean[])E);
			else
			return MagicIO.writeBoolList((List<Boolean>)E);
		});
		WRITERS.put(toList(LONG), (E) -> {
			boolean w = false;
			try {
				long[] info2 = (long[])E;
				w = true;
			} catch (ClassCastException c) {
				try {
					List<Long> info2 = (List<Long>)E;
					
				} catch (ClassCastException e) {
					throw new IllegalArgumentException("Type mismatches");
				}
			}
			if (w)
			return MagicIO.writeLongList((long[])E);
			else
			return MagicIO.writeLongList((List<Long>)E);
		});
		WRITERS.put(toList(SPECIES), (E) -> MagicIO.writeSpeciesList((List<ResourceLocation>)E));
		WRITERS.put(toList(ITEM), (E) -> MagicIO.writeItemList((List<ItemStack>)E));
		WRITERS.put(toList(BLOCK), (E) -> MagicIO.writeBlockList((List<BlockStack>)E));
		WRITERS.put(toList(ENTITY_DATA), (E) -> {
			boolean w = false;
			try {
				List<Entity> info2 = (List<Entity>)E;
				w = true;
			} catch (ClassCastException c) {
				try {
					List<NBTTagCompound> info2 = (List<NBTTagCompound>)E;
					
				} catch (ClassCastException e) {
					throw new IllegalArgumentException("Type mismatches");
				}
			}
			if (w)
			return MagicIO.writeEntityListToEntityDataList((List<Entity>)E);
			else
			return MagicIO.writeEntityDataList((List<NBTTagCompound>)E);
		});
		
	}
	
}
