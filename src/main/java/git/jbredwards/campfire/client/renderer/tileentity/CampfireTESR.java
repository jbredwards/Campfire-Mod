package git.jbredwards.campfire.client.renderer.tileentity;

import git.jbredwards.campfire.common.tileentity.TileEntityCampfire;
import git.jbredwards.campfire.common.tileentity.slot.CampfireSlotInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 * needed to register the items campfires hold
 * @author jbred
 *
 */
@SideOnly(Side.CLIENT)
public class CampfireTESR extends TileEntitySpecialRenderer<TileEntityCampfire>
{
    @Override
    public void render(@Nonnull TileEntityCampfire te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if(te.hasWorld() && !te.slotInfo.isEmpty()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
            te.slotInfo.forEach(CampfireSlotInfo::render);
            GlStateManager.popMatrix();
        }
    }
}
