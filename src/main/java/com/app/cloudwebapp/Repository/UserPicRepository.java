package com.app.cloudwebapp.Repository;



import com.app.cloudwebapp.Model.ProfilePic;
import com.app.cloudwebapp.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

public interface UserPicRepository extends JpaRepository<ProfilePic, UUID> {

    //@Transactional

    Optional<ProfilePic> findByUser(User user);

}

