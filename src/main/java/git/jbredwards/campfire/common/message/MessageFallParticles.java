package git.jbredwards.campfire.common.message;

import git.jbredwards.campfire.common.capability.ICampfireType;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * handle landing block particles
 * @author jbred
 *
 */
public class MessageFallParticles implements IMessage
{
    protected BlockPos pos;
    protected double x, y, z;
    protected int amount;
    protected boolean isValid;

    public MessageFallParticles() {}
    public MessageFallParticles(@Nonnull BlockPos posIn, double xIn, double yIn, double zIn, int amountIn) {
        pos = posIn;
        x = xIn;
        y = yIn;
        z = zIn;
        amount = amountIn;
        isValid = true;
    }

    @Override
    public void fromBytes(@Nonnull ByteBuf buf) {
        isValid = buf.readBoolean();
        if(isValid) {
            pos = BlockPos.fromLong(buf.readLong());
            x = buf.readDouble();
            y = buf.readDouble();
            z = buf.readDouble();
            amount = buf.readInt();
        }
    }

    @Override
    public void toBytes(@Nonnull ByteBuf buf) {
        buf.writeBoolean(isValid);
        if(isValid) buf.writeLong(pos.toLong()).writeDouble(x).writeDouble(y).writeDouble(z).writeInt(amount);
    }

    public enum Handler implements IMessageHandler<MessageFallParticles, IMessage>
    {
        INSTANCE;

        @Nullable
        @Override
        public IMessage onMessage(@Nonnull MessageFallParticles message, @Nonnull MessageContext ctx) {
            if(message.isValid && ctx.side.isClient()) handleMessage(message);
            return null;
        }

        @SideOnly(Side.CLIENT)
        static void handleMessage(@Nonnull MessageFallParticles message) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                final WorldClient world = Minecraft.getMinecraft().world;
                final ICampfireType type = ICampfireType.get(world.getTileEntity(message.pos));
                if(type != null) for(int i = 0; i < message.amount; i++) {
                    final double speedX = world.rand.nextGaussian() * 0.15;
                    final double speedY = world.rand.nextGaussian() * 0.15;
                    final double speedZ = world.rand.nextGaussian() * 0.15;

                    final ParticleManager manager = Minecraft.getMinecraft().effectRenderer;
                    final Particle particle = manager.particleTypes.get(EnumParticleTypes.BLOCK_DUST.getParticleID())
                            .createParticle(EnumParticleTypes.BLOCK_DUST.getParticleID(), world, message.x, message.y, message.z, speedX, speedY, speedZ, 1);

                    if(particle != null) {
                        final IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(type.get());
                        particle.setParticleTexture(model.getOverrides().handleItemState(model, type.get(), null, null).getParticleTexture());
                        manager.addEffect(particle);
                    }
                }
            });
        }
    }
}
