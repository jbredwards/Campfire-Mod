package git.jbredwards.campfire.common.tileentity;

import git.jbredwards.campfire.common.config.CampfireConfigHandler;

/**
 *
 * @author jbred
 *
 */
public class TileEntityBrazier extends AbstractCampfireTE
{
    @Override
    public void update() {
        if(CampfireConfigHandler.brazierEmitsSmoke && hasWorld() && world.isRemote && isLit()) addParticles();
    }
}
