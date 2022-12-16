package git.jbredwards.campfire.common.compat.hwyla;

import git.jbredwards.campfire.common.tileentity.TileEntityCampfire;
import mcp.mobius.waila.api.*;
import mcp.mobius.waila.overlay.DisplayUtil;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.List;

/**
 *
 * @author jbred
 *
 */
@WailaPlugin
public final class CampfireHwylaPlugin implements IWailaPlugin
{
    @Override
    public void register(@Nonnull IWailaRegistrar registrar) {
        registrar.registerBodyProvider(Provider.INSTANCE, TileEntityCampfire.class);
        registrar.registerNBTProvider(Provider.INSTANCE, TileEntityCampfire.class);

        registrar.registerTooltipRenderer("campfire.stack", Renderer.INSTANCE);
    }

    enum Provider implements IWailaDataProvider
    {
        INSTANCE;

        @Nonnull
        @Override
        public List<String> getWailaBody(@Nonnull ItemStack itemStack, @Nonnull List<String> tooltip, @Nonnull IWailaDataAccessor accessor, @Nonnull IWailaConfigHandler config) {
            final TileEntity tile = accessor.getTileEntity();
            if(tile instanceof TileEntityCampfire) {
                ((TileEntityCampfire)tile).slotInfo.forEach(slot -> {
                    String renderStr = "";
                    if(!slot.output.isEmpty()) {
                        renderStr += SpecialChars.getRenderString("campfire.stack", slot.stack.serializeNBT().toString());
                        if(!slot.output.isEmpty()) {
                            //use nbt since cookTime is only updated serverside
                            final int cookTime = accessor.getNBTData().getTagList("Slots", Constants.NBT.TAG_COMPOUND)
                                    .getCompoundTagAt(slot.slotIndex).getInteger("cookTime");

                            renderStr += SpecialChars.getRenderString("waila.progress", String.valueOf(cookTime), String.valueOf(slot.maxCookTime));
                            renderStr += SpecialChars.getRenderString("campfire.stack", slot.output.serializeNBT().toString());
                        }
                    }

                    if(!renderStr.isEmpty()) tooltip.add(renderStr);
                });
            }

            return tooltip;
        }

        @Nonnull
        @Override
        public NBTTagCompound getNBTData(@Nonnull EntityPlayerMP player, @Nonnull TileEntity te, @Nonnull NBTTagCompound tag, @Nonnull World world, @Nonnull BlockPos pos) {
            return te.writeToNBT(tag);
        }
    }

    //draw correctly serialized stack
    enum Renderer implements IWailaTooltipRenderer
    {
        INSTANCE;

        @Nonnull
        @Override
        public Dimension getSize(@Nonnull String[] params, @Nonnull IWailaCommonAccessor accessor) {
            return new Dimension(20, 16);
        }

        @Override
        public void draw(@Nonnull String[] params, @Nonnull IWailaCommonAccessor accessor) {
            try {
                final ItemStack stack = new ItemStack(JsonToNBT.getTagFromJson(params[0]));
                RenderHelper.enableGUIStandardItemLighting();
                DisplayUtil.renderStack(0, 0, stack);
                RenderHelper.disableStandardItemLighting();
            }
            //should never pass
            catch(NBTException | ArrayIndexOutOfBoundsException ignore) { }
        }
    }
}
