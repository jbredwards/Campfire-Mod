package git.jbredwards.campfire.common.message;

import git.jbredwards.campfire.common.tileentity.CampfireSlotInfo;
import git.jbredwards.campfire.common.tileentity.TileEntityCampfire;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

/**
 *
 * @author jbred
 *
 */
public class MessageSyncCampfireSlot implements IMessage
{
    protected BlockPos pos;
    protected NBTTagCompound serializedSlot;
    protected int slotIndex;
    protected boolean isValid;

    public MessageSyncCampfireSlot() {}
    public MessageSyncCampfireSlot(@Nonnull CampfireSlotInfo slotIn) {
        serializedSlot = slotIn.serializeNBT();
        pos = slotIn.tile.getPos();
        slotIndex = slotIn.slotIndex;
        isValid = true;
    }

    @Override
    public void fromBytes(@Nonnull ByteBuf buf) {
        isValid = buf.readBoolean();
        if(isValid) {
            final PacketBuffer wrapper = new PacketBuffer(buf);
            pos = wrapper.readBlockPos();
            slotIndex = wrapper.readVarInt();
            try { serializedSlot = wrapper.readCompoundTag(); }
            //should never pass
            catch(IOException e) { serializedSlot = new NBTTagCompound(); }
        }
    }

    @Override
    public void toBytes(@Nonnull ByteBuf buf) {
        buf.writeBoolean(isValid);
        if(isValid) {
            final PacketBuffer wrapper = new PacketBuffer(buf);
            wrapper.writeBlockPos(pos);
            wrapper.writeVarInt(slotIndex);
            wrapper.writeCompoundTag(serializedSlot);
        }
    }

    public enum Handler implements IMessageHandler<MessageSyncCampfireSlot, IMessage>
    {
        INSTANCE;

        @Nullable
        @Override
        public IMessage onMessage(@Nonnull MessageSyncCampfireSlot message, @Nonnull MessageContext ctx) {
            if(message.isValid && ctx.side.isClient()) handleMessage(message);
            return null;
        }

        @SideOnly(Side.CLIENT)
        static void handleMessage(@Nonnull MessageSyncCampfireSlot message) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                final TileEntity tile = Minecraft.getMinecraft().world.getTileEntity(message.pos);
                if(tile instanceof TileEntityCampfire) ((TileEntityCampfire)tile).slotInfo
                        .get(message.slotIndex).deserializeNBT(message.serializedSlot);
            });
        }
    }
}
