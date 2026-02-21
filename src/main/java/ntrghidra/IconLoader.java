package ntrghidra;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;

import javax.imageio.ImageIO;

public class IconLoader {

    // tiny 1x1 red png
    private static final String OVERLAY_PNG_BASE64 =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVQYV2NgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII=";

    public static Icon getOverlayIcon() {
        try {
            byte[] bytes = Base64.getDecoder().decode(OVERLAY_PNG_BASE64);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            BufferedImage img = ImageIO.read(bais);
            return new ImageIcon(img);
        } catch (Exception e) {
            return null;
        }
    }
}
