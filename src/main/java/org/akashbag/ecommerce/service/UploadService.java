package org.akashbag.ecommerce.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.akashbag.ecommerce.exception.customException.UploadSizeExceededException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadService {
    private final Cloudinary cloudinary;

    public String upload(MultipartFile file, String email) {
        try{
            Map params = ObjectUtils.asMap(
                    "public_id", email,
                    "overwrite", true,
                    "resource_type", "image"
            );

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

            return uploadResult.get("secure_url").toString();
        }catch(Exception e){
            log.error("Image upload failed: {}", e.getMessage());
            throw new IllegalArgumentException("Failed to upload image " + e.getMessage());
        }
    }

    public String uploadImage(MultipartFile file) {
        String uniqueFileName = UUID.randomUUID().toString();

        return upload(file, uniqueFileName);
    }

    public String uploadImageBytes(byte[] file, String fileName) {
        try {
            Map params = ObjectUtils.asMap(
                    "public_id", fileName,
                    "overwrite", true,
                    "resource_type", "raw",
                    "content_type", "application/pdf",
                    "type", "upload"
            );

            Map uploadResult = cloudinary.uploader().upload(file, params);
            return uploadResult.get("secure_url").toString();

        } catch (Exception e) {
            log.error("PDF upload failed: {}", e.getMessage());
            throw new IllegalArgumentException("Failed to upload PDF " + e.getMessage());
        }
    }

    public String getPdfViewUrl(String publicId) {
        return cloudinary.url()
                .resourceType("raw") // Use "raw" if uploaded as a raw file
                .generate(publicId);
    }
}
