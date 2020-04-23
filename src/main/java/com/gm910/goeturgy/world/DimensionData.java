package com.gm910.goeturgy.world;

import java.util.HashMap;
import java.util.Map;

import com.gm910.goeturgy.Goeturgy;
import com.gm910.goeturgy.util.GMNBT;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class DimensionData extends WorldSavedData {
	
	private static final String NAME = Goeturgy.MODID + "_dimensions";

	public DimensionData(String name) {
		super(name);
		
	}
	
	public DimensionData() {
		super(NAME);
	}
	
	public final Map<String, DimensionType> dimensions = new HashMap<>();
	public final Map<Integer, NBTTagCompound> dimData = new HashMap<>();
	public final Map<Integer, Class<? extends WorldProvider>> dimProviders = new HashMap<>();
	public static final Map<Class<? extends WorldProvider>, String> REVERSE_PROVIDERS = new HashMap<>();
	public static final Map<String, Class<? extends WorldProvider>> PROVIDERS = new HashMap<>();
	
	public static void registerInitialProviders() {
		
	}
	
	public void registerInitialDimensions() {
		//register("SHEOL", "Sheol", "_sheol", MagicMod.DIMENSION_SHEOL, DimensionSheol.class, false);
		//register("TYPHON", "Typhon", "_typhon", MagicMod.DIMENSION_TYPHON, DimensionTyphon.class, false);
	}
	
	
	public void register(String dimensionName, String suffix, int id, Class<? extends WorldProvider> clazz, boolean keepLoaded, NBTTagCompound cmp) {
		if (!PROVIDERS.containsValue(clazz)) {
			System.err.println("Not a valid world provider! Does not have a mapping!");
		}
		if (!DimensionManager.isDimensionRegistered(id)) {
			DimensionType type = DimensionType.register(dimensionName, suffix, id, clazz, keepLoaded);
			DimensionManager.registerDimension(id, type);
			dimensions.put(dimensionName, type);
			dimData.put(id, cmp);
			dimProviders.put(id, clazz);
		} else {
			System.out.println("Dimension " + id + " has already been registered, not going to register");
		}
	}
	
	public static Class<? extends WorldProvider> providerFromNBT(NBTTagCompound comp) {
		return WorldProviderHell.class;//TODO figure this out... 
	}
	
	public static void registerWorldProviderType(String name, Class<? extends WorldProvider> clazz) {
		PROVIDERS.put(name, clazz);
		REVERSE_PROVIDERS.put(clazz, name);
	}
	
	public String getNameForProvider(Class<? extends WorldProvider> clazz) {
		return REVERSE_PROVIDERS.get(clazz);
	}
	
	public Class<? extends WorldProvider> getProviderForName(String name) {
		return PROVIDERS.get(name);
	}
	
	public void addDimension(String name, Class<? extends WorldProvider> clazz) {
		addDimension(name, clazz, new NBTTagCompound());
	}
	
	public void register(String name, int id, Class<? extends WorldProvider> clazz, NBTTagCompound cmp) {
		register(name, "_" + name.toLowerCase(), id, clazz, false, cmp);
	}
	
	public void addDimension(String name, Class<? extends WorldProvider> clazz, NBTTagCompound cmp) {
		register(name, DimensionManager.getNextFreeDimId(), clazz, cmp);
	}
	
	public void addDimension(String name, NBTTagCompound settings) {
		addDimension(name, providerFromNBT(settings), settings);
	}
	
	public DimensionType getDimensionType(String name) {
		return dimensions.get(name);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		nbt.getTagList("Dims", NBT.TAG_COMPOUND).forEach((base) -> {
			NBTTagCompound cmp = (NBTTagCompound)base;
			this.register(cmp.getString("Name"),
					cmp.getInteger("ID"), 
					PROVIDERS.get(cmp.getString("Provider")),
					cmp.getCompoundTag("Data"));
			dimensions.put(cmp.getString("Name"), DimensionType.getById(cmp.getInteger("ID")));
			
		});
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("Dims", GMNBT.makeList(dimensions.keySet(), (str) -> {
			DimensionType type = dimensions.get(str);
			NBTTagCompound cmp = new NBTTagCompound();
			cmp.setInteger("ID", type.getId());
			cmp.setString("Name", str);
			cmp.setString("Provider", REVERSE_PROVIDERS.get(dimProviders.get(type.getId())));
			cmp.setTag("Data", dimData.get(type.getId()));
			return cmp;
		}));
		return compound;
	}
	
	public static DimensionData get(World world) {
		  
		  MapStorage storage = world.getMapStorage();
		  DimensionData instance = (DimensionData) storage.getOrLoadData(DimensionData.class, NAME);

		  if (instance == null) {
		    instance = new DimensionData();
		    storage.setData(NAME, instance);
		  }
		  return instance;
	}
	
	public static DimensionData get() {
		return get(FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0));
	}

	/*@SubscribeEvent
	public static void load(WorldEvent.Load event) {
		if (event.getWorld().isRemote) return;
		DimensionData.get(event.getWorld()).registerInitialDimensions();
	}*/
	
}
