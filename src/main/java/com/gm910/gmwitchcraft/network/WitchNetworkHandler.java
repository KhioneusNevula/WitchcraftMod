package com.gm910.gmwitchcraft.network;

import java.nio.charset.Charset;

import com.gm910.gmwitchcraft.Reference;
import com.gm910.gmwitchcraft.network.networkcap.MessageProvider;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class WitchNetworkHandler {
	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MODID);
	
	public static class WitchMessage implements IMessage {
		  // A default constructor is always required
		  public WitchMessage(){}

		  private CharSequence toSend;
		  public WitchMessage(CharSequence toSend) {
		    this.toSend = toSend;
		  }

		  @Override 
		  public void toBytes(ByteBuf buf) {
			  buf.writeInt(toSend.length());
			  buf.writeCharSequence(toSend, Charset.defaultCharset());
		  }

		  @Override public void fromBytes(ByteBuf buf) {
		    // Reads the int back from the buf. Note that if you have multiple values, you must read in the same order you wrote.
			int length = buf.readInt();
		    toSend = buf.readCharSequence(length, Charset.defaultCharset());
		  }
	}
	
	// The params of the IMessageHandler are <REQ, REPLY>
	// This means that the first param is the packet you are receiving, and the second is the packet you are returning.
	// The returned packet can be used as a "response" from a sent packet.
	public class WitchMessageHandler implements IMessageHandler<WitchMessage, IMessage> {
	  // Do note that the default constructor is required, but implicitly defined in this case

		
		
	  @Override public IMessage onMessage(WitchMessage message, MessageContext ctx) {
	    // This is the player the packet was sent to the server from
	    EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
	    // The value that was sent
	    String action = (String) message.toSend;
	    // Execute the action on the main server thread by adding it as a scheduled task
	    serverPlayer.getServerWorld().addScheduledTask(() -> {
	      serverPlayer.getCapability(MessageProvider.NET_CAP, null).sendMessage(action);
	    });
	    // No response packet
	    return null;
	  }
	}
}
