package com.gm910.goeturgy.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;

import com.gm910.goeturgy.Goeturgy;
import com.gm910.goeturgy.messages.types.IRunnableTask;
import com.gm910.goeturgy.spells.spellspaces.SpellSpace;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class Messages {

	private static int id = 0;
	
	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Goeturgy.MODID);

	public static class StringMessage implements IMessage {
		  // A default constructor is always required
		  public StringMessage(){}

		  private String toSend;
		  /**
		   * Please send runnables by casting them to (Runnable & Serializable)
		   * @param toSend
		   */
		  public StringMessage(Serializable toSend) {
		    this.toSend = serialize(toSend);
		  }

		  @Override public void toBytes(ByteBuf buf) {
			buf.writeInt(toSend.length());
		    buf.writeCharSequence(toSend, Charset.defaultCharset());
		  }

		  @Override public void fromBytes(ByteBuf buf) {
		    // Reads the int back from the buf. Note that if you have multiple values, you must read in the same order you wrote.
		    int len = buf.readInt();
			 toSend = buf.readCharSequence(len, Charset.defaultCharset()).toString();
			 
		  }
		}
	
	public static class ServerMessageHandler implements IMessageHandler<StringMessage, IMessage> {
		  // Do note that the default constructor is required, but implicitly defined in this case

		  @Override public IMessage onMessage(StringMessage message, MessageContext ctx) {
		    EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
		    String amount = message.toSend.toString();
		    Object check = deserialize(amount);
		    if (!(check instanceof IRunnableTask)) {

				  System.err.println("Problem sending message to server");
		    	return null;
		    }
		    
		    serverPlayer.getServerWorld().addScheduledTask(() -> SpellSpace.runServers.add((IRunnableTask)check));
		    // No response packet
		    return null;
		  }
	}
	
	public static class ClientMessageHandler implements IMessageHandler<StringMessage, IMessage> {
		  // Do note that the default constructor is required, but implicitly defined in this case

		  @Override public IMessage onMessage(StringMessage message, MessageContext ctx) {
			  
			  
			  String amount = message.toSend.toString();
			  Object check = deserialize(amount);
			  if (!(check instanceof IRunnableTask)) {
				  System.err.println("Problem sending message to client");
				  return null;
			  }
			  IRunnableTask toRun = (IRunnableTask)deserialize(amount);
			  Minecraft.getMinecraft().addScheduledTask(() -> SpellSpace.runClients.add(toRun));
		    // No response packet
		    return null;
		  }
		}
	
	public static String serialize(Serializable obj) {
	      ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
	      try(ObjectOutputStream outputStream = new ObjectOutputStream(stream1)) {
	         outputStream.writeObject(obj);
	      } catch (IOException e) {
			System.out.println("Problem serializing " + obj + " threw " + e);
		}
	      
	      return stream1.toString();
	 }
	
	 public static Object deserialize(String from) {
	     ByteArrayInputStream stream1 = new ByteArrayInputStream(from.getBytes());
	      try(ObjectInputStream inputStream = new ObjectInputStream(stream1)) {
	         return inputStream.readObject();
	      } catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			System.err.println("Problem deserializing " + from + " threw " + e);
		} 
	      return null;
	 }
	 
	 public static int getID() {
		 return id;
	 }
	 
	 public static int returnNewID() {
		 return id++;
	 }
}

