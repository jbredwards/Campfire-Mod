package git.jbredwards.campfire.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@SideOnly(Side.CLIENT)
public class ParticleCampfireSmoke extends Particle
{
    protected final double yAccel;
    public ParticleCampfireSmoke(@Nonnull World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0, 0, 0);
        yAccel = 0.004;
        motionX *= 0.1;
        motionY *= 0.1;
        motionZ *= 0.1;
        motionX += xSpeedIn;
        motionY += ySpeedIn;
        motionZ += zSpeedIn;

        final float color = world.rand.nextFloat() * 0.3f;
        particleRed = color;
        particleGreen = color;
        particleBlue = color;

        particleScale *= 0.75;
        particleMaxAge = (int)(particleMaxAge / (world.rand.nextFloat() * 0.8 + 0.2));
        particleMaxAge = (int)(particleMaxAge * 0.75);
        particleMaxAge = Math.max(particleMaxAge, 1);
        canCollide = true;
    }

    @Override
    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        if(particleAge++ >= particleMaxAge) {
            setExpired();
            return;
        }

        setParticleTexture(textures[7 - particleAge * 8 / particleMaxAge]);
        motionY += yAccel;
        move(motionX, motionY, motionZ);
        if(posY == prevPosY) {
            motionX *= 1.1;
            motionZ *= 1.1;
        }

        motionX *= 0.96;
        motionY *= 0.96;
        motionZ *= 0.96;
        if(onGround) {
            motionX *= 0.7;
            motionZ *= 0.7;
        }
    }

    @Override
    public int getFXLayer() { return 1; }

    @Override
    public void setParticleTextureIndex(int particleTextureIndex) { }
}
