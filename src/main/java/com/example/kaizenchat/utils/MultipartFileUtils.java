package com.example.kaizenchat.utils;

import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
public class MultipartFileUtils {

    private static final long MAX_BYTES_SIZE = 3_000_000L; // 3MB
    private static final String PNG = "image/png";
    private static final String JPG = "image/jpg";
    private static final String JPEG = "image/jpeg";

    public static String getFileExtension(MultipartFile file) {
        String type = file.getContentType();
        return type == null ? "" : type.substring(type.indexOf("/") + 1);
    }

    public static boolean isNotValidFileType(MultipartFile file) {
        String type = file.getContentType();
        Objects.requireNonNull(type);
        return !(type.equals(PNG) || type.equals(JPG) || type.equals(JPEG));
    }

    public static boolean isNotValidFileSize(MultipartFile file) {
        return file.getSize() > MAX_BYTES_SIZE;
    }
}