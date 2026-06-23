package com.myTesi.aloisioUmberto.data.services;

import com.myTesi.aloisioUmberto.config.FileUtil;
import com.myTesi.aloisioUmberto.config.JwtTokenProvider;
import com.myTesi.aloisioUmberto.data.dao.InterestAreaRepository;
import com.myTesi.aloisioUmberto.data.dao.UserRepository;
import com.myTesi.aloisioUmberto.data.entities.InterestArea;
import com.myTesi.aloisioUmberto.data.entities.User;
import com.myTesi.aloisioUmberto.data.services.interfaces.ImageService;
import com.myTesi.aloisioUmberto.dto.enumetation.Role;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;
import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final InterestAreaRepository interestAreaRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userDao;

    private final String relativePathToUploadsSensor = "src\\main\\resources\\images\\sensor\\";
    private final String relativePathToUploadsArea = "src\\main\\resources\\images\\area\\";




    public String processImage(MultipartFile img, String id, Integer act) throws IOException {

        if (act.equals(1)) {
            try {
                String realPathToUploads = System.getProperty("user.dir") + File.separator + relativePathToUploadsSensor + id;
                if (!new File(realPathToUploads).exists()) {
                    new File(realPathToUploads).mkdirs();
                }
                String orgName = FileUtil.assignProgressiveName(img);
                String filePath = realPathToUploads + File.separator + orgName;
                File dest = new File(filePath);
                img.transferTo(dest);
                return (orgName);

            } catch (Exception e) {
                return null;
            }
        }
        if (act.equals(2)) {
            try {
                String realPathToUploads = System.getProperty("user.dir") + File.separator + relativePathToUploadsArea + id;

                if (!new File(realPathToUploads).exists()) {
                    new File(realPathToUploads).mkdirs();
                }
                String filePath = realPathToUploads + File.separator + "preview.jpg";
                System.out.println(filePath);
                File dest = new File(filePath);
                img.transferTo(dest);
                return (filePath);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }


    @Override
    public Resource getImage(String userId, String imagePath) {

        String fullPath = relativePathToUploadsSensor + userId + File.separator + imagePath;
        Resource resource = new FileSystemResource(fullPath);

        if (resource.exists()) {
            return resource;
        }
        throw new EntityNotFoundException();
    }


    @Override
    public Resource getSensorImage(String userId, String imagePath) {
        String fullPath = relativePathToUploadsSensor + userId + File.separator + imagePath;
        return getImageResource(fullPath);
    }

    @Override
    public Resource getAreaImage(String areaId) {
        InterestArea area = interestAreaRepository.findById(areaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Area not found"));

        String fullPath = relativePathToUploadsArea
                + areaId
                + File.separator
                + "preview.jpg";

        return getImageResource(fullPath);
    }




    public Resource getImageResource(String fullPath) {
        Resource resource = new FileSystemResource(fullPath);
        if (resource.exists()) {
            return resource;
        }
        throw new EntityNotFoundException("Image not found: " + fullPath);
    }
}

