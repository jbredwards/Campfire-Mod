package git.jbredwards.campfire.asm;

import net.minecraft.launchwrapper.IClassTransformer;
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
    public static final class Transformer implements IClassTransformer
    {
        @Nonnull
        @Override
        public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
            //compat with future mc mod beehives
            if("thedarkcolour.futuremc.block.villagepillage.CampfireBlock$Companion".equals(transformedName)) {
                final ClassNode classNode = new ClassNode();
                new ClassReader(basicClass).accept(classNode, 0);

                classNode.interfaces.add("git/jbredwards/campfire/common/compat/futuremc/IBeeCalmer");
                all: //find the desired method
                for(MethodNode method : classNode.methods) {
                    if(method.name.equals("isLitCampfire")) {
                        for(AbstractInsnNode insn : method.instructions.toArray()) {
                            /*
                             * Old code:
                             * return state.getBlock() instanceof CampfireBlock && state.getValue(LIT);
                             *
                             * New code:
                             * //check for interface instead of class
                             * return state.getBlock() instanceof IBeeCalmer && state.getValue(LIT);
                             */
                            if(insn.getOpcode() == Opcodes.LDC) {
                                ((LdcInsnNode)insn).cst = "git/jbredwards/campfire/common/compat/futuremc/IBeeCalmer";
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
