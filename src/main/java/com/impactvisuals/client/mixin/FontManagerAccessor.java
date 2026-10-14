package com.impactvisuals.client.mixin;

import net.minecraft.client.font.FontManager;
import net.minecraft.client.font.FontStorage;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.invocation.Invoker;

/**
 * {@code FontManager#getStorage} is private in 1.21.4, and its
 * {@code MISSING_STORAGE_ID} marker is package-private - both unreachable from
 * the mod's packages, but a font registered via assets/impactvisuals/font/*.json
 * can only be reached through them. Mixin accessors keep the access
 * version-checked by the mixin compiler (no reflection).
 */
@Mixin(FontManager.class)
public interface FontManagerAccessor {

    /** Private in the target class - invoked instead of reflected. */
    @Invoker("getStorage")
    FontStorage impactvisuals$invokeGetStorage(Identifier id);

    /** Package-private in the target class - exposed as a generated static getter. */
    @Accessor("MISSING_STORAGE_ID")
    static Identifier impactvisuals$missingStorageId();
}
