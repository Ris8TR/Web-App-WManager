package com.myTesi.aloisioUmberto.config;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public class FileUtil {
 //TODO BISOGNA VEDERE SE SERVE
    public static String assignProgressiveName(MultipartFile file) {

        if (file.getOriginalFilename() != null) {
            String originalFileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);
            UUID uid = UUID.randomUUID();
            return "file_" + uid.toString() + "." + fileExtension;
        }
        return "Errore";
    }

    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1);
    }

}
