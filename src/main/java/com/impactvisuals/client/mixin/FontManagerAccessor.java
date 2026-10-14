package com.impactvisuals.client.mixin;

import net.minecraft.client.font.FontManager;
import net.minecraft.client.font.FontStorage;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * {@code FontManager#getStorage} is private in 1.21.4, and its
 * {@code MISSING_STORAGE_ID} marker is package-private - both unreachable
 * from the mod's packages, but a font registered via
 * assets/impactvisuals/font/*.json can only be reached through them.
 *
 * A shadow (with a public wrapper) plus a static field accessor keep the
 * access version-checked by the Mixin annotation processor - no reflection.
 */
@Mixin(FontManager.class)
public abstract class FontManagerAccessor {

    /** Private in the target class - shadowed so the mod can call it. */
    @Shadow
    private abstract FontStorage getStorage(Identifier id);

    /** Public wrapper around the private shadow, callable from other packages. */
    public FontStorage impactvisuals$getStorage(Identifier id) {
        return getStorage(id);
    }

    /** Package-private in the target class - exposed as a generated static field. */
    @Accessor("MISSING_STORAGE_ID")
    public static Identifier IV_MISSING_STORAGE_ID;
}
