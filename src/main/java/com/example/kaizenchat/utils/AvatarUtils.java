package com.example.kaizenchat.utils;

import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

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

    public static void updateAvatar(Path destination, String encodedContent) throws IOException {
        try (var bufferedWriter = Files.newBufferedWriter(destination)) {
            bufferedWriter.write(encodedContent);
            bufferedWriter.flush();
        }
    }

    public static String downloadAvatar(Path destination) throws IOException {
        try (var bufferedReader = Files.newBufferedReader(destination)) {
            return bufferedReader.lines().collect(Collectors.joining());
        }
    }

}