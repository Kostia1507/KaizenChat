package com.example.kaizenchat.utils;

import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AvatarUtils {

    private AvatarUtils() {}

    public static Path getImageDestination(String filename) {
        return Path.of("src", "main", "resources", "images", filename);
    }

    public static byte[] getImage(String dest) throws IOException {
        return Files.readAllBytes(Path.of(dest));
    }

    public static MediaType getImageType(String path) {
        String ext = path.substring(path.lastIndexOf(".") + 1);
        return MediaType.valueOf("images/" + ext);
    }

}