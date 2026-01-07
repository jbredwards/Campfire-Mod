/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.asm.transformers;

import git.jbredwards.campfire.api.block.IHasWorldState;
import git.jbredwards.campfire.mod.common.block.AbstractCampfire;
import git.jbredwards.campfire.mod.common.init.CampfireBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Moved from ASMHandler. Adds functionality for IHasWorldState, compat with FutureMC beehives and Thaumcraft crucibles.
 * @author jbred
 *
 */
public final class TransformerMain implements IClassTransformer, Opcodes
{
    @Nullable
    @Override
    public byte[] transform(@Nullable String name, @Nullable String transformedName, @Nullable byte[] basicClass) {
        if(basicClass == null) return null;

        final boolean isFutureMC = "thedarkcolour.futuremc.block.villagepillage.CampfireBlock$Companion".equals(transformedName);
        final boolean isThaumcraft = !isFutureMC && "thaumcraft.common.tiles.crafting.TileCrucible".equals(transformedName);

        if(isFutureMC || isThaumcraft || "net.minecraft.world.World".equals(transformedName)) {
            final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);

            if(isFutureMC) classNode.interfaces.add("git/jbredwards/campfire/api/block/IBeeCalmer");
            all: //find the desired method
            for(MethodNode method : classNode.methods) {
                //compat with future mc mod beehives
                if(isFutureMC) { if(method.name.equals("isLitCampfire")) {
                    for(AbstractInsnNode insn : method.instructions.toArray()) {
                        /*
                         * Old code:
                         * return state.getBlock() instanceof CampfireBlock && state.getValue(LIT);
                         *
                         * New code:
                         * //check for interface instead of class
                         * return state.getBlock() instanceof IBeeCalmer && ((IBeeCalmer)state.getBlock()).canCalmBeeHive(state);
                         */
                        if(insn.getOpcode() == INSTANCEOF) ((TypeInsnNode)insn).desc = "git/jbredwards/campfire/api/block/IBeeCalmer";
                        else if(insn.getOpcode() == INVOKEINTERFACE && ((MethodInsnNode)insn).name.equals("booleanValue")) {
                            final InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 1));
                            list.add(new MethodInsnNode(INVOKEINTERFACE, "net/minecraft/block/state/IBlockState", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "getBlock" : "func_177230_c", "()Lnet/minecraft/block/Block;", true));
                            list.add(new TypeInsnNode(CHECKCAST, "git/jbredwards/campfire/api/block/IBeeCalmer"));
                            list.add(new VarInsnNode(ALOAD, 1));
                            list.add(new MethodInsnNode(INVOKEINTERFACE, "git/jbredwards/campfire/api/block/IBeeCalmer", "canCalmBeeHive", "(Lnet/minecraft/block/state/IBlockState)Z", true));

                            method.instructions.insert(insn, list);
                            for(int i = 0; i < 10; i++) method.instructions.remove(insn.getPrevious());
                            method.instructions.remove(insn);
                            break all;
                        }
                    }
                }}
                //compat with thaumcraft mod crucible
                else if(isThaumcraft) { if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "update" : "func_73660_a")) {
                    for(AbstractInsnNode insn : method.instructions.toArray()) {
                        /*
                         * Old code:
                         * if (block.getMaterial() == Material.LAVA
                         * || block.getMaterial() == Material.FIRE
                         * || BlocksTC.nitor.containsValue(block.getBlock())
                         * || block.getBlock() == Blocks.MAGMA)
                         * {
                         *     ...
                         * }
                         *
                         * New code:
                         * //add check for campfires
                         * if (block.getMaterial() == Material.LAVA
                         * || block.getMaterial() == Material.FIRE
                         * || BlocksTC.nitor.containsValue(block.getBlock())
                         * || Hooks.canLightCrucible(block)
                         * || block.getBlock() == Blocks.MAGMA)
                         * {
                         *     ...
                         * }
                         */
                        if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals("containsValue")) {
                            final InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 2));
                            list.add(new MethodInsnNode(INVOKESTATIC, "git/jbredwards/campfire/mod/asm/transformers/TransformerMain$Hooks", "canLightCrucible", "(Lnet/minecraft/block/state/IBlockState;)Z", false));
                            list.add(new JumpInsnNode(insn.getNext().getOpcode(), ((JumpInsnNode)insn.getNext()).label));

                            method.instructions.insert(insn.getNext(), list);
                            break all;
                        }
                    }
                }}
                // Responsible for adding the functionality of {@link git.jbredwards.campfire.api.block.IHasWorldState IHasWorldState}.
                else if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "setBlockState" : "func_180501_a") && method.desc.endsWith("I)Z")) {
                    for(@Nonnull final AbstractInsnNode insn : method.instructions.toArray()) {
                        /*
                         * setBlockState: (changes are around line 389)
                         * Old code:
                         * public boolean setBlockState(...)
                         * {
                         *     ...
                         * }
                         *
                         * New code:
                         * // Replace state to set with the one defined by IHasWorldState
                         * public boolean setBlockState(...)
                         * {
                         *     newState = Hooks.getStateForWorld(this, pos, newState);
                         *     ...
                         * }
                         */
                        if(insn.getPrevious() == method.instructions.getFirst()) {
                            method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                            method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 1));
                            method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 2));
                            method.instructions.insertBefore(insn, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/campfire/mod/asm/transformers/TransformerMain$Hooks", "getStateForWorld", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Lnet/minecraft/block/state/IBlockState;", false));
                            method.instructions.insertBefore(insn, new VarInsnNode(ASTORE, 2));
                            break all;
                        }
                    }
                }
            }

            //writes the changes
            final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(writer);
            return writer.toByteArray();
        }

        return basicClass;
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        public static boolean canLightCrucible(@Nonnull final IBlockState state) {
            return (state.getBlock() == CampfireBlocks.CAMPFIRE || state.getBlock() == CampfireBlocks.BRAZIER) && state.getValue(AbstractCampfire.LIT);
        }

        @Nonnull
        public static IBlockState getStateForWorld(@Nonnull final World world, @Nonnull final BlockPos pos, @Nonnull final IBlockState state) {
            return state.getBlock() instanceof IHasWorldState ? ((IHasWorldState)state.getBlock()).getStateForWorld(world, pos, state) : state;
        }
    }
}
