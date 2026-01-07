/*
 * Copyright (c) 2024. jbredwards
 * All rights reserved.
 */

package git.jbredwards.campfire.mod.common.compat.groovyscript;

import com.cleanroommc.groovyscript.api.documentation.annotations.Example;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.registry.ForgeRegistryWrapper;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 *
 * @author jbred
 *
 */
public abstract class AbstractRecipeRegistry<T extends IForgeRegistryEntry<T>> extends ForgeRegistryWrapper<T>
{
    public AbstractRecipeRegistry(@Nonnull final IForgeRegistry<T> registry) { this(registry, null); }
    public AbstractRecipeRegistry(@Nonnull final IForgeRegistry<T> registry, @Nullable final Collection<String> aliases) {
        super(registry, aliases);
    }

    @MethodDescription(example = @Example("campfire"))
    public boolean removeByMod(@Nullable final String modid) {
        return modid != null && streamRecipes().removeIf(recipe -> modid.equals(recipe.getRegistryName().getNamespace()));
    }

    public boolean removeByRegex(@Nullable final String regex) {
        if(regex == null) return false;

        @Nonnull final Pattern p = Pattern.compile(regex);
        return streamRecipes().removeIf(recipe -> p.matcher(String.valueOf(recipe.getRegistryName())).matches());
    }
}
