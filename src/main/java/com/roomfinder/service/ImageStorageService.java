package com.roomfinder.service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ImageStorageService {
    List<String> saveImages(List<MultipartFile> images)throws IOException;
    void deleteImage(String fileName)throws IOException ;
}
