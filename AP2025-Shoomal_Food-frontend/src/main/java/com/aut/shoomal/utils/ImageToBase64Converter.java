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
            imageInFile.read(imageData);
            return Base64.getEncoder().encodeToString(imageData);
        }
    }

    public static void main(String[] args) {
        String imagePath = "path/to/your/image.png";
        try {
            String base64Image = convertImageFileToBase64(imagePath);
            System.out.println("Base64 Encoded Image:\n" + base64Image);
        } catch (IOException e) {
            System.err.println("Error converting image to Base64: " + e.getMessage());
        }
    }
}