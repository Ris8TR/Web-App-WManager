package com.myTesi.aloisioUmberto.data.services.SensorDataHandler;

import com.myTesi.aloisioUmberto.config.FileUtil;
import com.myTesi.aloisioUmberto.data.entities.User;
import com.myTesi.aloisioUmberto.data.services.SensorDataHandler.interfaces.SensorDataHandler;
import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDataDto;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;


import javax.persistence.EntityNotFoundException;
import java.io.File;
import java.io.IOException;

public class ImageSensorDataHandler implements SensorDataHandler {

    private final String relativePathToUploads = "src/main/resources/image/";

    @Override
    public void handle(SensorData data, NewSensorDataDto newSensorDataDTO) throws IOException {
        data.setPayloadType("image");
        data.setPayload(insertInsertionImage((MultipartFile) newSensorDataDTO.getPayload(),newSensorDataDTO.getUserId()));
    }

    public String insertInsertionImage(MultipartFile img, String userId) throws IOException {


        String realPathToUploads = System.getProperty("user.dir") + File.separator + relativePathToUploads + File.separator + userId;

        if (!new File(realPathToUploads).exists()) { //If the directory "image" is not existent
            new File(realPathToUploads).mkdir();     //Create a directory
        }

        String orgName = FileUtil.assignProgressiveName(img);
        String filePath = realPathToUploads + orgName;

        File dest = new File(filePath);
        img.transferTo(dest);
        return filePath;

    }


    public Resource getImage(String imagePath) {

        Resource resource = new FileSystemResource(imagePath);

        if (resource.exists()) {
            return resource;
        }
        throw new EntityNotFoundException();
    }
}



