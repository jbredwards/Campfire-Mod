package git.jbredwards.campfire.common.tileentity;

import git.jbredwards.campfire.common.block.AbstractCampfire;
import git.jbredwards.campfire.common.config.CampfireConfigHandler;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 *
 * @author jbred
 *
 */
public abstract class AbstractCampfireTE extends TileEntity implements ITickable
{
    public int color = -1;
    public int forcedSmokeColor = -1;

    public boolean isLit() { return (getBlockMetadata() & 2) != 0; }
    public boolean isSignal() { return (getBlockMetadata() & 4) != 0; }
    public boolean isPowered() { return (getBlockMetadata() & 1) != 0; }

    public static int getColor(@Nullable TileEntity tile) {
        return tile instanceof AbstractCampfireTE ? ((AbstractCampfireTE)tile).color : -1;
    }

    @Nonnull
    public Optional<AbstractCampfire<?>> getBlock() {
        return getBlockType() instanceof AbstractCampfire ? Optional.of((AbstractCampfire<?>)getBlockType()) : Optional.empty();
    }

    @SideOnly(Side.CLIENT)
    public void addParticles() {
        final Optional<AbstractCampfire<?>> block = getBlock();
        if(block.isPresent() && block.get().isSmokey() && world.rand.nextFloat() < 0.11) {
            final int smokeColor = getSmokeColor();
            final int fallbackColor = getFallbackColor();

            for(int i = 0; i < world.rand.nextInt(2) + 2; i++)
                block.get().addParticles(world, pos, smokeColor, fallbackColor, forcedSmokeColor != -1, isSignal(), isPowered(), -1);
        }
    }

    public int getFallbackColor() {
        return !CampfireConfigHandler.doesSmokeFollowDye || forcedSmokeColor != -1 ? forcedSmokeColor : color;
    }

    //returns the map color below, or -1 if this shouldn't use that color
    public int getSmokeColor() {
        if(forcedSmokeColor != -1 || !isPowered() || CampfireConfigHandler.poweredAction != CampfireConfigHandler.PoweredAction.COLOR) return -1;
        final MapColor mapColor = world.getBlockState(pos.down(isSignal() ? 2 : 1)).getMapColor(world, pos.down(isSignal() ? 2 : 1));
        return mapColor != MapColor.AIR ? mapColor.colorValue : -1;
    }

    @Nonnull
    @Override
    public NBTTagCompound getUpdateTag() { return writeToNBT(new NBTTagCompound()); }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() { return new SPacketUpdateTileEntity(pos, 0, getUpdateTag()); }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        super.writeToNBT(compound);
        if(color != -1) compound.setInteger("Color", color);
        if(forcedSmokeColor != -1) compound.setInteger("ForcedSmokeColor", forcedSmokeColor);
        return compound;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);
        color = compound.hasKey("Color", Constants.NBT.TAG_INT) ? compound.getInteger("Color") : -1;
        forcedSmokeColor = compound.hasKey("ForcedSmokeColor", Constants.NBT.TAG_INT) ? compound.getInteger("ForcedSmokeColor") : -1;
    }

    @Override
    public boolean shouldRefresh(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState oldState, @Nonnull IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
}
