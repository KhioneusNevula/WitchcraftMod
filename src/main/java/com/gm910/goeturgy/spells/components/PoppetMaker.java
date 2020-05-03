package com.gm910.goeturgy.spells.components;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.gm910.goeturgy.init.ItemInit;
import com.gm910.goeturgy.items.ItemDNACollector;
import com.gm910.goeturgy.spells.ioflow.MagicIO;
import com.gm910.goeturgy.spells.spellspaces.SpellSpace;
import com.gm910.goeturgy.spells.spellspaces.SpellSpace.Spell;
import com.gm910.goeturgy.spells.spellspaces.SpellSpaces;
import com.gm910.goeturgy.spells.util.ISpellComponent;
import com.gm910.goeturgy.tileentities.ISyncableTile;
import com.gm910.goeturgy.util.GMNBT;
import com.gm910.goeturgy.util.NonNullMap;
import com.gm910.goeturgy.util.ServerPos;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants.NBT;

public class PoppetMaker extends TileEntityChest implements ISpellComponent, ISyncableTile {

	//private NonNullList<ItemStack> handler = NonNullList.withSize(27, ItemStack.EMPTY);
	
	protected NonNullMap<EnumFacing, NBTTagCompound> inputs = new NonNullMap<EnumFacing, NBTTagCompound>(NBTTagCompound::new);
	protected NonNullMap<EnumFacing, NBTTagCompound> outputs = new NonNullMap<EnumFacing, NBTTagCompound>(NBTTagCompound::new);
	protected long figure;
	
	@Override
	public boolean accepts(EnumFacing facing, NBTTagCompound comp) {
		//System.out.println("Poppetmaker does not accept any compound, especially not compound " + comp);
		if (comp.hasKey(MagicIO.toList(MagicIO.ITEM))) {
			for (ItemStack stack : MagicIO.getItemStackList(comp)) {
				if (ItemDNACollector.getLinkedUUID(stack) == null) {
					return false;
				}
			}
			return true;
		} else if (comp.hasKey(MagicIO.ITEM)) {
			if (ItemDNACollector.getLinkedUUID(MagicIO.getItemStack(comp)) == null) {
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean isStatic() {
		return true;
	}
	
	@Override
	public boolean isOutput(EnumFacing face) {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public NonNullMap<EnumFacing, NBTTagCompound> getStaticOutput(ServerPos modifiedPos) {
		
		NonNullMap<EnumFacing, NBTTagCompound> so = new NonNullMap<>( () ->  {
			NBTTagCompound cmp = new NBTTagCompound();
			List<UUID> ens = new ArrayList<>();
			for (ItemStack stack : super.getItems()) {
				if (ItemDNACollector.getLinkedUUID(stack) != null) {
					ens.add(ItemDNACollector.getLinkedUUID(stack));
				}
			}
			//super.getItems().forEach((e) -> ens.add(ItemDNACollector.getLinkedUUID(e)) );
			MagicIO.writeEntityListToCompound(ens, cmp);
			return cmp;
		});

		so.generateValues(EnumFacing.VALUES);
		System.out.print("Poppetmaker output " + so);
		return so;
	}
	
	@Override
	public NonNullMap<EnumFacing, NBTTagCompound> getInputs() {
		
		return inputs;
	}

	@Override
	public boolean putInput(NonNullMap<EnumFacing, NBTTagCompound> inputs) {
		this.inputs = inputs;
		
		return true;
	}

	@Override
	public NonNullMap<EnumFacing, NBTTagCompound> activate(Spell sp, ServerPos modifiedPos, NonNullMap<EnumFacing, NBTTagCompound> map) {
		
		List<ItemStack> stacks = new ArrayList<>();
		for (EnumFacing face : map.keySet()) {
			List<ItemStack> ls1 = (MagicIO.getItemStackList(map.get(face)));
			if (ls1 == null || ls1.isEmpty()) {
				continue;
			}
			stacks.addAll(ls1);
			ItemStack st1 = MagicIO.getItemStack(map.get(face));
			if (st1 != null && !st1.isEmpty()) {
				stacks.add(st1);
			}
		}
		List<UUID> uuids = new ArrayList<>();
		stacks.forEach((e) -> uuids.add(ItemDNACollector.getLinkedUUID(e)) );

		NonNullMap<EnumFacing, NBTTagCompound> so = new NonNullMap<>(() -> {
			NBTTagCompound cmp = new NBTTagCompound();
			MagicIO.writeEntityListToCompound(uuids, cmp);
			return cmp;
		});
		so.generateValues(EnumFacing.VALUES);
		return so;
	}

	@Override
	public NonNullMap<EnumFacing, List<String>> getPossibleReturns(NonNullMap<EnumFacing, List<String>> input) {
		NonNullMap<EnumFacing, List<String>> so = new NonNullMap<>( () ->  {
			List<String> cmp = new ArrayList<String>();
			cmp.add(MagicIO.toList(MagicIO.ENTITY));
			return cmp;
		});
		so.generateValues(EnumFacing.VALUES);
		return so;
	}

	@Override
	public int getRequiredPower(NonNullMap<EnumFacing, List<String>> tagsForSide) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getRequiredPowerFromNBT(NonNullMap<EnumFacing, NBTTagCompound> tagsForSide, ServerPos modifiedPos) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getRequiredPower() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		//compound.setTag("Inv", GMNBT.makeList(handler, (e) -> e.serializeNBT() ));
		compound.setLong("Space", figure);
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		//NBTTagList list = compound.getTagList("Inv", NBT.TAG_COMPOUND);
		//for (int i = 0; i < list.tagCount(); i++) {
		//	handler.set(i, new ItemStack((NBTTagCompound)list.get(i)));
		//}
		figure = compound.getLong("Space");
		//GMNBT.forEach(compound.getTagList("Inv", NBT.TAG_COMPOUND), (e) -> {handler.add(new ItemStack((NBTTagCompound)e));});
		super.readFromNBT(compound);
	}

	/*@Override
	public String getName() {
		
		return "container.poppetmaker";
	}

	@Override
	public boolean hasCustomName() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getSizeInventory() {
		return handler.size();
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return handler.isEmpty();
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		// TODO Auto-generated method stub
		return handler.get(index);
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		ItemStack st = handler.get(index);
		st.shrink(count);
		return st;
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		return handler.remove(index);
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		handler.set(index, stack);
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		
		return player.getDistanceSq(pos) <= 25;
	}

	@Override
	public void openInventory(EntityPlayer player) {
		
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		
	}*/

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		
		return stack.getItem() == ItemInit.DNA_COLLECTOR;
	}

	/*@Override
	public int getField(int id) {
		
		return 0;
	}

	@Override
	public void setField(int id, int value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getFieldCount() {
		
		return 0;
	}

	@Override
	public void clear() {
		this.handler.clear();
	}

	@Override
	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
		
		return new ContainerChest(playerInventory, this, playerIn);
	}

	@Override
	public String getGuiID() {
		
		return "minecraft:chest";
	}*/


	@Override
	public SpellSpace getSpellSpace() {
		// TODO Auto-generated method stub
		return SpellSpaces.get().getById(this.getSpaceID());
	}

	@Override
	public long getSpaceID() {
		// TODO Auto-generated method stub
		return this.figure;
	}

	@Override
	public void setSpaceID(long id) {
		this.figure = id;
	}

	@Override
	public void resetInputs() {
		inputs.clear();
	}

	@Override
	public void resetOutputs() {
		outputs.clear();
	}
	
	
}
