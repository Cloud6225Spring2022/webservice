package com.app.cloudwebapp.Repository;


import java.nio.file.Path;
import java.util.stream.Stream;

import com.app.cloudwebapp.Model.User;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FIleStorageRepository {

        public void init();
        public void save(MultipartFile file, User user);
        public Resource load(String filename);
        public void deleteAll();
        public Stream<Path> loadAll();

}
