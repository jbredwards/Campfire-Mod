package git.jbredwards.campfire.client.renderer.tileentity;

import git.jbredwards.campfire.common.tileentity.CampfireSlotInfo;
import git.jbredwards.campfire.common.tileentity.TileEntityCampfire;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public class CampfireTESR extends TileEntitySpecialRenderer<TileEntityCampfire>
{
    @Override
    public void render(@Nonnull TileEntityCampfire te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
        te.slotInfo.forEach(CampfireSlotInfo::render);
        GlStateManager.popMatrix();
    }
}
