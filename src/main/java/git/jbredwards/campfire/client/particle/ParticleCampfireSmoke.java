package git.jbredwards.campfire.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
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
public class ParticleCampfireSmoke extends Particle
{
    public ParticleCampfireSmoke(@Nonnull World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, boolean longLivingEmber) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn);
        multipleParticleScaleBy(3);
        setSize(0.25f, 0.25f);

        particleMaxAge = rand.nextInt(50) + (longLivingEmber ? 280 : 80);
        particleGravity = 3.0E-6F;
        motionX = xSpeedIn;
        motionY = ySpeedIn + rand.nextFloat() / 500;
        motionZ = zSpeedIn;

        setParticleTexture(ModelLoader.defaultTextureGetter().apply(
                new ResourceLocation("campfire", String.format("particles/big_smoke_%d", rand.nextInt(12)))));
    }

    public static void spawnParticle(@Nonnull World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, boolean longLivingEmber, boolean makeBrighter, int color) {
        final ParticleCampfireSmoke particle = new ParticleCampfireSmoke(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, longLivingEmber);
        if(color != -1) {
            if(makeBrighter) color = new Color(color).brighter().getRGB();
            particle.setRBGColorF((color >> 16 & 255) / 255f, (color >> 8 & 255) / 255f, (color & 255) / 255f);
        }

        else particle.setRBGColorF(129 / 255f, 123 / 255f, 116 / 255f); //default particle color
        particle.setAlphaF(longLivingEmber ? 0.95f : 0.9f);
        Minecraft.getMinecraft().effectRenderer.addEffect(particle);
    }

    @Override
    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        if(particleAge++ < particleMaxAge && !(particleAlpha <= 0)) {
            motionX += rand.nextFloat() / 5000 * (rand.nextBoolean() ? 1 : -1);
            motionZ += rand.nextFloat() / 5000 * (rand.nextBoolean() ? 1 : -1);
            motionY -= particleGravity;

            move(motionX, motionY, motionZ);
            if(particleAge >= particleMaxAge - 60 && particleAlpha > 0.01) particleAlpha -= 0.015;

        }

        else setExpired();
    }

    @Override
    public int getFXLayer() { return 1; }

    @Override
    public void setParticleTextureIndex(int particleTextureIndex) { }
}
