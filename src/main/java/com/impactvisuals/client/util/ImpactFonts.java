package com.impactvisuals.client.util;

import com.impactvisuals.client.mixin.FontManagerAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.FontManager;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.util.Identifier;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * The mod's own rounded UI font (Nunito, SIL OFL) - registered through
 * assets/impactvisuals/font/impact_font*.json so every HUD card, menu and
 * readout renders with a real typeface instead of Minecraft's pixel font.
 *
 * In 1.21.4 a font can only be drawn through a {@link TextRenderer} bound to a
 * {@link FontStorage}, and {@code FontManager#getStorage} is private - hence the
 * tiny {@link FontManagerAccessor} mixin.
 *
 * Everything here degrades gracefully: if the custom font fails to load for any
 * reason (corrupted jar entry, unsupported FreeType build, ...), we fall back to
 * the vanilla pixel font instead of showing tofu boxes. That is exactly the bug
 * the old JetBrains Mono bundle suffered from, so the fallback is non-negotiable.
 */
public final class ImpactFonts {

    public static final Identifier REGULAR_ID = Identifier.of("impactvisuals", "impact_font");
    public static final Identifier SEMI_BOLD_ID = Identifier.of("impactvisuals", "impact_font_semibold");
    public static final Identifier BOLD_ID = Identifier.of("impactvisuals", "impact_font_bold");

    /** Per-FontManager cache: [0] = regular, [1] = semi bold, [2] = bold. */
    private static final Map<FontManager, TextRenderer[]> CACHE = new IdentityHashMap<>();

    private ImpactFonts() {}

    /** Regular weight - body text, HUD labels, values. */
    public static TextRenderer hud(MinecraftClient client) {
        return get(client, REGULAR_ID, 0);
    }

    /** Semi bold weight - sub-headings, emphasized values. */
    public static TextRenderer hudSemiBold(MinecraftClient client) {
        return get(client, SEMI_BOLD_ID, 1);
    }

    /** Bold weight - titles and names. */
    public static TextRenderer hudBold(MinecraftClient client) {
        return get(client, BOLD_ID, 2);
    }

    private static TextRenderer get(MinecraftClient client, Identifier id, int slot) {
        if (client == null) return null;
        FontManager manager = client.fontManager;
        if (manager == null) return client.textRenderer;

        TextRenderer[] cache = CACHE.computeIfAbsent(manager, m -> new TextRenderer[3]);
        TextRenderer renderer = cache[slot];
        if (renderer == null) {
            renderer = create(client, manager, id);
            cache[slot] = renderer;
        }
        return renderer;
    }

    private static TextRenderer create(MinecraftClient client, FontManager manager, Identifier id) {
        try {
            FontStorage storage = ((FontManagerAccessor) manager).impactvisuals$invokeGetStorage(id);
            Identifier missing = FontManagerAccessor.impactvisuals$missingStorageId();
            if (storage == null || storage.getId() == null || missing != null && storage.getId().equals(missing)) {
                return client.textRenderer;
            }
            // The accessor resolves the storage lazily on every draw, so the
            // renderer stays correct across resource reloads without rebuilding.
            return new TextRenderer(ignored -> ((FontManagerAccessor) manager).impactvisuals$invokeGetStorage(id), false);
        } catch (Throwable t) {
            return client.textRenderer;
        }
    }
}
