package com.gm910.goeturgy.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.gm910.goeturgy.Goeturgy;
import com.gm910.goeturgy.messages.types.IRunnableTask;
import com.gm910.goeturgy.messages.types.TaskChangeGodMode;
import com.gm910.goeturgy.messages.types.TaskKeyPress;
import com.gm910.goeturgy.spells.spellspaces.SpellSpace;
import com.google.common.base.Charsets;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Messages {

	private static int id = 0;
	
	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Goeturgy.MODID);

	public static class TaskMessage implements IMessage {
		  // A default constructor is always required
		  public TaskMessage(){}

		  private String toSend;
		  /**
		   * Please send runnables by casting them to (Runnable & Serializable)
		   * @param toSend
		   */
		  public TaskMessage(IRunnableTask toSend) {
		    this.toSend = toSend.serializeNBT().toString();
		  }

		  @Override public void toBytes(ByteBuf buf) {
			  
			buf.writeInt(toSend.length());
			buf.writeCharSequence(toSend, Charsets.UTF_8);
			
		  }

		  @Override public void fromBytes(ByteBuf buf) {
		    // Reads the int back from the buf. Note that if you have multiple values, you must read in the same order you wrote.
		     
			  int len = buf.readInt();
			 toSend = ""+buf.readCharSequence(len, Charsets.UTF_8);
			 //System.out.println(toSend);
			 
		  }
		}
	
	public static class ServerMessageHandler implements IMessageHandler<TaskMessage, IMessage> {
		  // Do note that the default constructor is required, but implicitly defined in this case

		  @Override public IMessage onMessage(TaskMessage message, MessageContext ctx) {
		    EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
		    NBTTagCompound nbt = null; 
		    //System.out.println(message.toSend);
		    try {
		    	nbt = JsonToNBT.getTagFromJson(message.toSend);
		    } catch (NBTException e) {

				  System.err.println("Problem sending message to server : " + e);
				  return null;
		    }
		    IRunnableTask check = IRunnableTask.getFromNBT(nbt);
		    if (check == null) {

				  System.err.println("Problem sending message to server: " + nbt);
		    	return null;
		    }
		    
		    serverPlayer.getServerWorld().addScheduledTask(() -> SpellSpace.runServers.add(check));
		    // No response packet
		    return null;
		  }
	}
	
	public static class ClientMessageHandler implements IMessageHandler<TaskMessage, IMessage> {
		  // Do note that the default constructor is required, but implicitly defined in this case

		  @Override public IMessage onMessage(TaskMessage message, MessageContext ctx) {
			  
			  NBTTagCompound nbt = null;
			    try {
			    	nbt = JsonToNBT.getTagFromJson(message.toSend);
			    } catch (NBTException e) {

					  System.err.println("Problem sending message to server : " + e);
					  return null;
			    }
			    IRunnableTask check = IRunnableTask.getFromNBT(nbt);
			    if (check == null) {

					  System.err.println("Problem sending message to server");
			    	return null;
			    }
			  Minecraft.getMinecraft().addScheduledTask(() -> SpellSpace.runClients.add(check));
		    // No response packet
		    return null;
		  }
		}
	
	public static String serialize(Object obj) {
	      ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
	      try(ObjectOutputStream outputStream = new ObjectOutputStream(stream1)) {
	         outputStream.writeObject(obj);
	      } catch (IOException e) {
			System.out.println("Serializing " + obj + " threw " + e);
		}
	      
	      return stream1.toString();
	 }
	
	 public static Object deserialize(String from) {
	     ByteArrayInputStream stream1 = new ByteArrayInputStream(from.getBytes());
	      try(ObjectInputStream inputStream = new ObjectInputStream(stream1)) {
	         return inputStream.readObject();
	      } catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			System.err.println("Deserializing " + from + " threw " + e);
		} 
	      return null;
	 }
	 
	 public static int getID() {
		 return id;
	 }
	 
	 public static int returnNewID() {
		 return id++;
	 }
	 
	 public static void pressKey(int key) {
		 INSTANCE.sendToServer(new TaskMessage(new TaskKeyPress(Minecraft.getMinecraft().player.getUniqueID(), key)));
	 }
	 
	 public static void changeGodMode(EntityPlayerMP player, boolean yes) {
		 INSTANCE.sendTo(new TaskMessage(new TaskChangeGodMode(yes)), player);
	 }
}

