package com.gm910.goeturgy.messages.types;

import com.gm910.goeturgy.util.DrawEffects;
import com.gm910.goeturgy.util.ServerPos;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.server.command.TextComponentHelper;

public class TaskIllusion implements IRunnableTask {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2838461114698523867L;
	
	private ServerPos pos;
	private IBlockState block;
	
	
	public TaskIllusion(ServerPos pos, IBlockState block) {
		this.pos = pos;
		this.block = block;
	}
	
	public TaskIllusion() {}
	
	@Override
	public void run() {

		if (Minecraft.getMinecraft().world == null) return;
		if (Minecraft.getMinecraft().world.provider.getDimension() != pos.d) return;
		DrawEffects.drawBlock(Minecraft.getMinecraft().getRenderPartialTicks(), block, pos, 1.0f);
	}

	@Override
	public void writeToNBT(NBTTagCompound comp) {
		comp.setTag("Pos", this.pos.toNBT());
		comp.setTag("State", NBTUtil.writeBlockState(new NBTTagCompound(), this.block));
	}

	@Override
	public void readFromNBT(NBTTagCompound comp) {
		pos = ServerPos.fromNBT(comp.getCompoundTag("Pos"));
		block = NBTUtil.readBlockState(comp.getCompoundTag("State"));
	}

}
