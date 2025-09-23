package com.aut.shoomal.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;

public class ImageToBase64Converter {
    public static String convertImageFileToBase64(String imagePath) throws IOException {
        File file = new File(imagePath);
        try (FileInputStream imageInFile = new FileInputStream(file)) {
            byte[] imageData = new byte[(int) file.length()];
            int ignored = imageInFile.read(imageData);
            return Base64.getEncoder().encodeToString(imageData);
        }
    }
}