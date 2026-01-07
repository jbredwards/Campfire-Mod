/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.tileentity;

/**
 *
 * @author jbred
 *
 */
public class TileEntityBrazier extends AbstractCampfireTE
{
    @Override
    public void update() { if(hasWorld() && isLit()) addParticles(); }
}
