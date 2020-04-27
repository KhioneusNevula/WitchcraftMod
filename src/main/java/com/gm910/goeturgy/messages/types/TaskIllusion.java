package com.gm910.goeturgy.messages.types;

import com.gm910.goeturgy.util.DrawEffects;
import com.gm910.goeturgy.util.ServerPos;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;

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
	
	@Override
	public void run() {

		if (Minecraft.getMinecraft().world == null) return;
		if (Minecraft.getMinecraft().world.provider.getDimension() != pos.d) return;
		DrawEffects.drawBlock(Minecraft.getMinecraft().getRenderPartialTicks(), block, pos, 1.0f);
	}

	
}
