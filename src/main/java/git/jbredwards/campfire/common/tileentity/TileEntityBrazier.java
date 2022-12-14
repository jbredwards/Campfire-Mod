package git.jbredwards.campfire.common.tileentity;

/**
 *
 * @author jbred
 *
 */
public class TileEntityBrazier extends AbstractCampfireTE
{
    @Override
    public void update() {
        if(hasWorld() && isLit()) addParticles();
    }
}
