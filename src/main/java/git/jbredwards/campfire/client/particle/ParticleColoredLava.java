package git.jbredwards.campfire.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleLava;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.awt.*;

/**
 *
 * @author jbred
 *
 */
@SideOnly(Side.CLIENT)
public class ParticleColoredLava extends ParticleLava
{
    public ParticleColoredLava(@Nonnull World worldIn, double xCoordIn, double yCoordIn, double zCoordIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn);
        setParticleTexture(ModelLoader.defaultTextureGetter().apply(
                new ResourceLocation("campfire", "particles/colored_lava")));
    }

    public static void spawnParticle(@Nonnull World world, double xCoordIn, double yCoordIn, double zCoordIn, int color) {
        final ParticleColoredLava particle = new ParticleColoredLava(world, xCoordIn, yCoordIn, zCoordIn);
        color = new Color(color).brighter().getRGB();

        particle.setRBGColorF((color >> 16 & 255) / 255f, (color >> 8 & 255) / 255f, (color & 255) / 255f);
        Minecraft.getMinecraft().effectRenderer.addEffect(particle);
    }

    @Override
    public int getFXLayer() { return 1; }

    @Override
    public void setParticleTextureIndex(int particleTextureIndex) { }
}
