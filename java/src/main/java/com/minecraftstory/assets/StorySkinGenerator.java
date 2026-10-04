package com.minecraftstory.assets;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Generates original 64x64 Minecraft-compatible humanoid skin textures.
 * The build places these under generated resources so the finished jar is self-contained.
 */
public final class StorySkinGenerator {
    private static final int SIZE = 64;

    private StorySkinGenerator() {}

    public static void generateAll(Path output) throws IOException {
        Files.createDirectories(output);
        Map<String, Skin> skins = Map.of(
                "wanderer", new Skin(new Color(38, 42, 49), new Color(198, 154, 112), new Color(82, 55, 38), new Color(38, 84, 110)),
                "mara", new Skin(new Color(61, 66, 72), new Color(184, 138, 99), new Color(90, 45, 31), new Color(71, 86, 101)),
                "elias", new Skin(new Color(113, 78, 50), new Color(219, 174, 126), new Color(53, 34, 25), new Color(55, 103, 72)),
                "cael", new Skin(new Color(53, 53, 58), new Color(202, 160, 115), new Color(96, 73, 49), new Color(92, 92, 98)),
                "sera", new Skin(new Color(43, 48, 56), new Color(205, 160, 112), new Color(57, 34, 28), new Color(112, 73, 48)),
                "bram", new Skin(new Color(118, 73, 42), new Color(211, 165, 116), new Color(76, 45, 26), new Color(167, 112, 47)),
                "nessa", new Skin(new Color(48, 52, 57), new Color(191, 145, 103), new Color(39, 28, 24), new Color(102, 109, 116)),
                "pip", new Skin(new Color(95, 58, 111), new Color(224, 179, 128), new Color(76, 49, 36), new Color(91, 155, 113)),
                "toma", new Skin(new Color(42, 81, 111), new Color(220, 177, 129), new Color(68, 42, 31), new Color(189, 159, 61)),
                "old_renn", new Skin(new Color(91, 74, 61), new Color(173, 128, 92), new Color(120, 99, 77), new Color(73, 82, 87)),
                "lio", new Skin(new Color(37, 80, 78), new Color(212, 168, 119), new Color(47, 32, 25), new Color(72, 122, 125)),
                "sera_expedition", new Skin(new Color(62, 66, 60), new Color(205, 160, 112), new Color(57, 34, 28), new Color(126, 92, 51))
        );

        for (Map.Entry<String, Skin> entry : skins.entrySet()) {
            write(output.resolve(entry.getKey() + ".png"), entry.getValue());
        }
    }

    private static void write(Path file, Skin skin) throws IOException {
        BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.setColor(skin.cloak);
        g.fillRect(0, 0, SIZE, SIZE);

        // Vanilla 64x64 skin layout: head, body, arms and legs.
        paintHead(g, skin);
        paintBody(g, skin);
        paintArm(g, skin, 40, 16, false);
        paintArm(g, skin, 32, 48, true);
        paintLeg(g, skin, 16, 16);
        paintLeg(g, skin, 0, 16);

        // Keep unused outer-layer regions transparent.
        g.dispose();
        ImageIO.write(image, "PNG", file.toFile());
    }

    private static void paintHead(Graphics2D g, Skin s) {
        fill(g, s.skin, 8, 8, 8, 8);
        fill(g, s.hair, 8, 0, 8, 8);
        fill(g, s.hair, 0, 8, 8, 8);
        fill(g, s.skin, 16, 8, 8, 8);
        fill(g, s.skin, 8, 16, 8, 8);
        fill(g, s.hair, 8, 0, 8, 3);
        fill(g, Color.BLACK, 10, 10, 1, 1);
        fill(g, Color.BLACK, 13, 10, 1, 1);
    }

    private static void paintBody(Graphics2D g, Skin s) {
        fill(g, s.shirt, 20, 20, 8, 12);
        fill(g, s.shirt.darker(), 20, 32, 8, 4);
        fill(g, s.shirt, 28, 20, 8, 12);
        fill(g, s.skin, 20, 16, 8, 4);
        fill(g, s.shirt.brighter(), 20, 20, 8, 3);
    }

    private static void paintArm(Graphics2D g, Skin s, int x, int y, boolean mirrored) {
        fill(g, s.skin, x, y, 4, 12);
        fill(g, s.shirt, x, y + 4, 4, 8);
        fill(g, s.shirt.darker(), x, y + 10, 4, 2);
    }

    private static void paintLeg(Graphics2D g, Skin s, int x, int y) {
        fill(g, s.pants, x, y, 4, 12);
        fill(g, s.pants.darker(), x, y + 8, 4, 4);
        fill(g, s.boots, x, y + 12, 4, 4);
    }

    private static void fill(Graphics2D g, Color c, int x, int y, int w, int h) {
        g.setColor(c);
        g.fillRect(x, y, w, h);
    }

    private record Skin(Color shirt, Color skin, Color hair, Color pants) {
        Color boots() { return shirt.darker(); }
        Color cloak() { return pants; }
    }
}
