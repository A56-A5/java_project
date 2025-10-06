package com.auction.util;

import com.auction.database.Database;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public final class ImageStorage {
    private ImageStorage() {}

    public static String storeImage(String sourcePath) throws IOException {
        Path src = Paths.get(sourcePath);
        String ext = getExtension(src.getFileName().toString());
        if (ext == null) ext = "png";
        String filename = UUID.randomUUID().toString() + "." + ext;
        Path target = Paths.get(Database.getImagesDirectory(), filename);
        Files.copy(src, target);
        return target.toString();
    }

    public static ImageIcon loadScaled(String path, int w, int h) throws IOException {
        BufferedImage img = ImageIO.read(new File(path));
        if (img == null) return null;
        Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private static String getExtension(String name) {
        int i = name.lastIndexOf('.');
        if (i < 0) return null;
        return name.substring(i + 1).toLowerCase();
    }
}


