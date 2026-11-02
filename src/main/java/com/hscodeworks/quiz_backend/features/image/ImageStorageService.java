package com.hscodeworks.quiz_backend.features.image;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.hscodeworks.quiz_backend.common.exception.BadRequestException;
import com.hscodeworks.quiz_backend.common.exception.ImageStorageException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageStorageService {

    private static final long MAX_SIZE_BYTES = 4L * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final Cloudinary cloudinary;

    public StoredImage upload(MultipartFile file) {
        validate(file);
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                                    file.getBytes(),
                                    ObjectUtils.asMap("resource_type", "image")
                                );
                                            
            return new StoredImage(
                (String) result.get("secure_url"), 
                (String) result.get("public_id")
            );
        } 
        catch (IOException | RuntimeException ex) {
            log.error("Image upload to Cloudinary failed", ex);
            throw new ImageStorageException("Image upload failed", ex);
        }
    }


    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("An image file is required");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new BadRequestException("Image must be at most 4 MB");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("Only JPEG, PNG and WebP images are allowed");
        }
    }

    
    /** Deletes the image from Cloudinary only if the surrounding database transaction commits. */
    public void deleteAfterCommit(String publicId) {
        deleteWhen(publicId, TransactionSynchronization.STATUS_COMMITTED);
    }


    /** Deletes the image from Cloudinary only if the surrounding database transaction rolls back. */
    public void deleteIfRolledBack(String publicId) {
        deleteWhen(publicId, TransactionSynchronization.STATUS_ROLLED_BACK);
    }


    private void deleteWhen(String publicId, int expectedStatus) {
        if (publicId == null) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == expectedStatus) {
                    delete(publicId);
                }
            }
        });
    }


    private void delete(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } 
        catch (IOException | RuntimeException ex) {
            log.warn("Could not delete image {} from Cloudinary", publicId, ex);
        }
    }

}