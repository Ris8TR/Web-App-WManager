package com.myTesi.aloisioUmberto.data.services.interfaces;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public interface ImageService {

    public String processImage(MultipartFile img, String userId, Integer act)throws IOException;

    public Resource getImage(String UserId, String imagePath);

    Resource getSensorImage(String userId, String imagePath);

    Resource getAreaImage(String userId );
}
