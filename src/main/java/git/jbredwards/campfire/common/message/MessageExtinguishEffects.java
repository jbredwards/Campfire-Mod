package git.jbredwards.campfire.common.message;

import git.jbredwards.campfire.common.tileentity.AbstractCampfireTE;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author jbred
 *
 */
public class MessageExtinguishEffects implements IMessage
{
    protected BlockPos pos;
    protected double extraSmokeOffset;
    protected boolean isValid;

    public MessageExtinguishEffects() {}
    public MessageExtinguishEffects(@Nonnull BlockPos posIn, double extraSmokeOffsetIn) {
        pos = posIn;
        extraSmokeOffset = extraSmokeOffsetIn;
        isValid = true;
    }

    @Override
    public void fromBytes(@Nonnull ByteBuf buf) {
        isValid = buf.readBoolean();
        if(isValid) {
            pos = BlockPos.fromLong(buf.readLong());
            extraSmokeOffset = buf.readDouble();
        }
    }

    @Override
    public void toBytes(@Nonnull ByteBuf buf) {
        buf.writeBoolean(isValid);
        if(isValid) {
            buf.writeLong(pos.toLong());
            buf.writeDouble(extraSmokeOffset);
        }
    }

    public enum Handler implements IMessageHandler<MessageExtinguishEffects, IMessage>
    {
        INSTANCE;

        @Nullable
        @Override
        public IMessage onMessage(@Nonnull MessageExtinguishEffects message, @Nonnull MessageContext ctx) {
            if(message.isValid && ctx.side.isClient()) handleMessage(message);
            return null;
        }

        @SideOnly(Side.CLIENT)
        static void handleMessage(@Nonnull MessageExtinguishEffects message) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                final WorldClient world = Minecraft.getMinecraft().world;
                final TileEntity tile = world.getTileEntity(message.pos);

                if(tile instanceof AbstractCampfireTE) ((AbstractCampfireTE)tile).getBlock().ifPresent(campfire ->
                        campfire.playExtinguishEffects(world, message.pos,(AbstractCampfireTE)tile, message.extraSmokeOffset));
            });
        }
    }
}
