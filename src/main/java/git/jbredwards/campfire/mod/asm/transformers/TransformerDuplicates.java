/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.asm.transformers;

import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Prevent duplicate blocks and items from being registered.
 * @author jbred
 *
 */
public final class TransformerDuplicates implements IClassTransformer, Opcodes
{
    @Nullable
    @Override
    public byte[] transform(@Nullable final String name, @Nullable final String transformedName, @Nullable final byte[] basicClass) {
        if(basicClass == null) return null;

        // -----------------------------------
        // FutureMC (campfire, chain, lantern)
        // -----------------------------------

        else if("thedarkcolour.futuremc.registry.FBlocks".equals(transformedName)) return removeMapping(basicClass, "registerBlocks", 1, "CAMPFIRE", "CHAIN", "LANTERN");
        else if("thedarkcolour.futuremc.registry.FItems".equals(transformedName)) return removeMapping(basicClass, "registerItems", 1, "CAMPFIRE", "CHAIN", "LANTERN");
        return basicClass;
    }

    @Nonnull
    private static byte[] removeMapping(@Nonnull final byte[] basicClass, @Nonnull final String registerMethod, final int offset, @Nonnull final String... toRemove) {
        @Nonnull final ClassNode classNode = new ClassNode();
        new ClassReader(basicClass).accept(classNode, 0);
        for(@Nonnull final MethodNode method : classNode.methods) {
            if(method.name.equals(registerMethod)) {
                for(@Nonnull final AbstractInsnNode insn : method.instructions.toArray()) {
                    if(insn instanceof MethodInsnNode && ((MethodInsnNode)insn).name.equals("register")) {
                        @Nonnull AbstractInsnNode entry = insn.getPrevious();
                        for(int i = 0; i < offset; i++) entry = entry.getPrevious();
                        if(entry instanceof FieldInsnNode && ArrayUtils.contains(toRemove, ((FieldInsnNode)entry).name)) {
                            method.instructions.insert(insn, new MethodInsnNode(INVOKESTATIC, "git/jbredwards/campfire/mod/asm/transformers/TransformerDuplicates$Hooks", "dummy", "(Ljava/lang/Object;Ljava/lang/Object;)V", false));
                            method.instructions.remove(insn);
                        }
                    }
                }

                break;
            }
        }

        @Nonnull final ClassWriter writer = new ClassWriter(0);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        public static void dummy(@Nonnull final Object registry, @Nonnull final Object registryEntry) {}
    }
}
