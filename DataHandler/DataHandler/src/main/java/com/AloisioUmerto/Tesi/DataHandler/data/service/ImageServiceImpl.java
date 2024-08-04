package com.AloisioUmerto.Tesi.DataHandler.data.service;


import com.AloisioUmerto.Tesi.DataHandler.config.FileUtil;
import lombok.AllArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;


@Service
@AllArgsConstructor
public class ImageServiceImpl {

    private final String relativePathToUploads = "src/main/resources/images/";

    public String insertInsertionImage(MultipartFile img, String userId) throws IOException {


        try {
            String realPathToUploads = System.getProperty("user.dir") + File.separator + relativePathToUploads+ userId;

            if (!new File(realPathToUploads).exists()) { //If the directory "image" is not existent
                new File(realPathToUploads).mkdirs();     //Create a directory
            }


            String orgName = FileUtil.assignProgressiveName(img);
            String filePath = realPathToUploads + File.separator + orgName;

            File dest = new File(filePath);
            img.transferTo(dest);

            return (orgName);

        }catch (Exception e){ return null; }
    }



    public Resource getImage(String userId, String imagePath) {


            // Costruisci il percorso completo del file utilizzando il filepath ricevuto
            String fullPath = relativePathToUploads + userId+ File.separator+ imagePath;
            // Crea file utilizzando il percorso completo del file
            Resource resource = new FileSystemResource(fullPath);

            if(resource.exists()){
                return resource;
            }
            throw new RuntimeException();
        }
    }

