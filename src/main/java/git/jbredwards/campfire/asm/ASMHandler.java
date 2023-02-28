package git.jbredwards.campfire.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 *
 * @author jbred
 *
 */
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("Campfire Plugin")
public final class ASMHandler implements IFMLLoadingPlugin
{
    /**
     * This class exists because the launcher don't allow {@link IClassTransformer IClassTransformers}
     * to be the same class as {@link IFMLLoadingPlugin IFMLLoadingPlugins}
     */
    @SuppressWarnings("unused")
    public static final class Transformer implements IClassTransformer, Opcodes
    {
        @Nonnull
        @Override
        public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
            final boolean isFutureMC = "thedarkcolour.futuremc.block.villagepillage.CampfireBlock$Companion".equals(transformedName);
            if(isFutureMC || "thaumcraft.common.tiles.crafting.TileCrucible".equals(transformedName)) {
                final ClassNode classNode = new ClassNode();
                new ClassReader(basicClass).accept(classNode, 0);

                if(isFutureMC) classNode.interfaces.add("git/jbredwards/campfire/common/compat/futuremc/IBeeCalmer");
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
                            if(insn.getOpcode() == INSTANCEOF) ((TypeInsnNode)insn).desc = "git/jbredwards/campfire/common/compat/futuremc/IBeeCalmer";
                            else if(insn.getOpcode() == INVOKEINTERFACE && ((MethodInsnNode)insn).name.equals("booleanValue")) {
                                final InsnList list = new InsnList();
                                list.add(new VarInsnNode(ALOAD, 1));
                                list.add(new MethodInsnNode(INVOKEINTERFACE, "net/minecraft/block/state/IBlockState", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "getBlock" : "func_177230_c", "()Lnet/minecraft/block/Block;", true));
                                list.add(new TypeInsnNode(CHECKCAST, "git/jbredwards/campfire/common/compat/futuremc/IBeeCalmer"));
                                list.add(new VarInsnNode(ALOAD, 1));
                                list.add(new MethodInsnNode(INVOKEINTERFACE, "git/jbredwards/campfire/common/compat/futuremc/IBeeCalmer", "canCalmBeeHive", "(Lnet/minecraft/block/state/IBlockState)Z", true));

                                method.instructions.insert(insn, list);
                                for(int i = 0; i < 10; i++) method.instructions.remove(insn.getPrevious());
                                method.instructions.remove(insn);
                                break all;
                            }
                        }
                    }}
                    //compat with thaumcraft mod crucible
                    else if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "update" : "func_73660_a")) {
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
                             * || block.getBlock() instanceof AbstractCampfire
                             * || block.getBlock() == Blocks.MAGMA)
                             * {
                             *     ...
                             * }
                             */
                            if(insn.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode)insn).name.equals("containsValue")) {
                                final InsnList list = new InsnList();
                                list.add(new VarInsnNode(ALOAD, 2));
                                list.add(new MethodInsnNode(INVOKEINTERFACE, "net/minecraft/block/state/IBlockState", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "getBlock" : "func_177230_c", "()Lnet/minecraft/block/Block;", true));
                                list.add(new TypeInsnNode(INSTANCEOF, "git/jbredwards/campfire/common/block/AbstractCampfire"));
                                list.add(new JumpInsnNode(IFNE, ((JumpInsnNode)insn.getNext()).label));

                                method.instructions.insert(insn.getNext(), list);
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
    }

    @Nonnull
    @Override
    public String[] getASMTransformerClass() {
        return new String[] {"git.jbredwards.campfire.asm.ASMHandler$Transformer"};
    }

    @Nullable
    @Override
    public String getModContainerClass() { return null; }

    @Nullable
    @Override
    public String getSetupClass() { return null; }

    @Override
    public void injectData(@Nonnull Map<String, Object> map) { }

    @Nullable
    @Override
    public String getAccessTransformerClass() { return null; }
}
