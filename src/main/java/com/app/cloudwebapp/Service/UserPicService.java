package com.app.cloudwebapp.Service;







import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.app.cloudwebapp.Model.ProfilePic;
import com.app.cloudwebapp.Model.User;
import com.app.cloudwebapp.Repository.UserPicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;

///import static com.csye6225.Config.AWSConfig.BucketName;


@Service

public class UserPicService {

    @Value("${amazonProperties.bucketName}")
    private String s3BucketName;

    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private UserService userService;

    private UserPicRepository pictureRepository;

   // private StatsDClient statsd = new NonBlockingStatsDClient("statsd", "localhost", 8125);

    //Logger logger = LoggerFactory.getLogger(PictureService.class);

    @Autowired
    UserPicService(UserPicRepository pictureRepository){

        this.pictureRepository = pictureRepository;
    }


    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        //logger.info("Converting multipart file to File");
       // File convertedFile = new File(file.getOriginalFilename());
//        System.out.println("COnvertedfile" + convertedFile);
//        FileInputStream fos = new FileInputStream(convertedFile);
//        fos.read(file.getBytes());
//        fos.close();
        File convertedFile = new File(file.getOriginalFilename());
        convertedFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(convertedFile);
        fos.write(file.getBytes());
        fos.close();
        System.out.println("after COnvertedfile" + convertedFile);
       // logger.info("Returning File");
        return convertedFile;
    }

    public List<ProfilePic> getPictureInfo(){
        return pictureRepository.findAll();
    }

    public boolean checkIfPictureExists(UUID user_id){

        List<ProfilePic> pictures_list = getPictureInfo();
        for(ProfilePic p:pictures_list){
            if(p.getUser().getId().equals(user_id)){

                return true;
            };
        }

        return false;
    }

    @Async
    public ProfilePic uploadPicture(MultipartFile picture, String username,User user) throws IOException {

        System.out.println("File picture get name"  );
        System.out.println("File picture" +picture.getOriginalFilename()  );
        //File newfile = new File(picture);
        File fileObject = null;
//        try {
//            fileObject = convertMultiPartToFile(picture);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        System.out.println("File" );
       // String content_type = picture.getContentType();
       // UserDetails u = userService.loadUserByUsername(username);
        String filename = "";
//        if(content_type.equals("image/png")){
//            filename = picture.getOriginalFilename() +".png";
//        } else if(content_type.equals("image/jpeg")){
//            filename = picture.getOriginalFilename()+".jpeg";
//        } else if(content_type.equals("image/jpg")){
//            filename = picture.getOriginalFilename()+".jpg";
//        }else {
                filename = picture.getOriginalFilename();
//            }

        String foldername = user.getId().toString();

        System.out.println("foldername" + foldername );
        String file_url = s3BucketName+"/"+foldername+"/"+filename;
        ProfilePic p = new ProfilePic(
                UUID.randomUUID(),user,filename,file_url, new Timestamp(System.currentTimeMillis()));

        if(checkIfPictureExists(user.getId())){

            System.out.println("check if pic exist"  );
            ProfilePic p2 = getPictureByUserId(user.getId());
            s3Client.deleteObject(new DeleteObjectRequest(s3BucketName, p2.getUser().getId().toString()+"/"+p2.getFile_name()));
            deletePicture(user);

        }
        System.out.println("File Object "+ fileObject  );
        System.out.println("before put objjct "  );
        ObjectMetadata data = new ObjectMetadata();
        data.setContentType(picture.getContentType());
        data.setContentLength(picture.getSize());
        System.out.println("after contect tye and bofre s3 clinet  put objjct "  );
       s3Client.putObject(s3BucketName, foldername+"/"+picture.getOriginalFilename(), picture.getInputStream(), data);

      //  s3Client.putObject(new PutObjectRequest(s3BucketName,  foldername+"/"+filename,fileObject));
        System.out.println("after put objjct "  );
       // fileObject.delete();
        pictureRepository.save(p);
        return p;
    }


    public ProfilePic getPictureByUserId(UUID user_id){


        List<ProfilePic> pictures_list = getPictureInfo();

        for(ProfilePic p:pictures_list){
            if(p.getUser().getId().equals(user_id)){

                return p;
            };
        }

        return null;
    }

    public ResponseEntity<Object> deletePicture(User user){

       // User u = (User) userService.loadUserByUsername(username);
        UUID user_id = user.getId();
        ProfilePic p = getPictureByUserId(user_id);

        if(p == null){
            return new ResponseEntity<>("User dont have picture", HttpStatus.NOT_FOUND);
        }

        s3Client.deleteObject(new DeleteObjectRequest(s3BucketName, p.getUser().getId().toString()+"/"+p.getFile_name()));

        pictureRepository.deleteById(p.getId());

        String response_body_message = p.getFile_name()+" deleted successfully";
        return new ResponseEntity<>(response_body_message, HttpStatus.NO_CONTENT);
    }


}
