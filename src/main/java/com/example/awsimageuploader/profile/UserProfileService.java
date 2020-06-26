package com.example.awsimageuploader.profile;


import com.example.awsimageuploader.bucket.BucketName;
import com.example.awsimageuploader.filestore.FileStore;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.apache.http.entity.ContentType.*;
import static org.apache.http.entity.ContentType.IMAGE_BMP;

@Service
public class UserProfileService {

    private final UserProfileDataAccessService userProfileDataAccessService;

    private final FileStore fileStore;

    @Autowired
    public UserProfileService(UserProfileDataAccessService userProfileDataAccessService, FileStore fileStore) {
        this.userProfileDataAccessService = userProfileDataAccessService;
        this.fileStore = fileStore;
    }

    List<UserProfile> getUserProfiles() {
        return userProfileDataAccessService.getUserProfiles();
    }

    /*
    check if image is not empty , if the file is an image
    the user is in DB
    grab metadata from file
    store in S3 and update DB with s3 link
     */
    public void uploadUserProfileImage(UUID userProfileId, MultipartFile file) {
        isFileEmpty(file);
        isFileAnImage(file);
        UserProfile user = getUserProfileOrThrow(userProfileId);

        Map<String, String> metadata = extractMetadata(file);

        String path = String.format("%s/%s", BucketName.PROFILE_IMAGE.getBucketName(), userProfileId);
        String fileName = String.format("%s-%s", file.getOriginalFilename(), UUID.randomUUID());
        try {
            fileStore.save(path,
                    fileName,
                    Optional.of(metadata),
                    file.getInputStream());
            user.setUserProfileImageLink(fileName);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

    }

    public byte[] downloadUserProfileImage(UUID userProfileId) {
        UserProfile user = getUserProfileOrThrow(userProfileId);
        String path = String.format("%s/%s",
                BucketName.PROFILE_IMAGE.getBucketName(),
                user.getUserProfileId());
        return user.getUserProfileImageLink().map(key -> fileStore.download(path, key))
                .orElse(new byte[0]);
    }

    private Map<String, String> extractMetadata(MultipartFile file) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type", file.getContentType());
        metadata.put("Content-Length", String.valueOf(file.getSize()));
        return metadata;
    }

    private UserProfile getUserProfileOrThrow(UUID userProfileId) {
        return userProfileDataAccessService
                .getUserProfiles()
                .stream()
                .filter(userProfile -> userProfile.getUserProfileId().equals(userProfileId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("User %s not found in DB", userProfileId)));
    }

    private void isFileAnImage(MultipartFile file) {
        if (!Arrays.asList(IMAGE_BMP.getMimeType(), IMAGE_GIF.getMimeType(), IMAGE_JPEG.getMimeType(), IMAGE_PNG.getMimeType())
                .contains(file.getContentType())) {
            throw new IllegalStateException("Unrecognized image type[" + file.getContentType() + "].Only permitted types are JPEG/GIF/BMP/PNG");
        }
    }

    private void isFileEmpty(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalStateException("Cannot upload Empty image[" + file.getSize() + "]");
        }
    }


}
