package com.example.awsimageuploader.datastore;

import com.example.awsimageuploader.profile.UserProfile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class FakeUserProfileDataStore {
    private static final List<UserProfile> USER_PROFILES = new ArrayList<>();
    static {
        USER_PROFILES.add(new UserProfile(UUID.fromString("919eb7e7-7cc1-428d-8068-079a095a2229"),"janetjones",null));
        USER_PROFILES.add(new UserProfile(UUID.fromString("8d21544e-2d11-4dfe-920b-b4621f255b4d"),"antoniojunior",null));
    }

    public List<UserProfile> getUserProfiles(){
        return USER_PROFILES;
    }
}
