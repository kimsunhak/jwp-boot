package com.ksh.portfolio.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.ksh.portfolio.exception.NotSupportedException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class S3Uploader {

    private final Logger logger = LoggerFactory.getLogger(S3Uploader.class);

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String s3BucketName;

    @Value("${api.s3.profile.dir}")
    private String profileDir;

    public String upload(MultipartFile multipartFile, String dirName) throws IOException {

        File uploadFile = covert(multipartFile).orElseThrow(() -> new IllegalArgumentException("MultipartFile -> File로 전환 실패"));

        return upload(uploadFile, dirName);
    }

    private String upload(File uploadFile, String dirName) {
        String fileName = uploadFile.getName();
        String uuid = UUID.randomUUID().toString();
        String fileNameOnly = fileName.substring(0, fileName.lastIndexOf("."));
        String newFileName = dirName + "/" + fileNameOnly + "_" + uuid + ".jpg";
        String imageUrl = putS3(uploadFile, newFileName);

        removeNewFile(uploadFile);

        return imageUrl;
    }

    private String putS3(File uploadFile, String fileName) {
        amazonS3.putObject(new PutObjectRequest(s3BucketName, fileName, uploadFile).withCannedAcl(CannedAccessControlList.PublicRead));
        return amazonS3.getUrl(s3BucketName, fileName).toString();
    }

    private void removeFile(String path, String name) {
        amazonS3.deleteObject(s3BucketName, profileDir + (path != null ? File.separator + path : "") + File.separator + name);
    }

    private void removeNewFile(File file) {
        if (file.delete()) {
            logger.info("파일이 삭제되었습니다.");
        } else {
            logger.info("파일이 삭제되지 않았습니다.");
        }
    }

    public String uploadImageFile(MultipartFile multipartFile) throws IOException {
        String fileName = multipartFile.getOriginalFilename();
        String extension = fileName.substring(fileName.lastIndexOf("."));

        List<String> supportedExtension = Arrays.asList(".jpg", ".png", ".jpeg");
        if (!supportedExtension.contains(extension)) {
            throw new NotSupportedException(extension + "은 지원하지 않는 확장자 입니다. jpg, jpeg, png만 지원합니다.");
        }

        return upload(multipartFile, profileDir);
    }

    public String changeImageUrl(String imageUrl, String size) {
        return imageUrl
                .replace("ifcommunity-s3.s3.ap-northeast-2.amazonaws.com", "d3ztqaagptbg1.cloudfront.net")
                .replace(".jpg", ".jpg" + size);
    }

    private Optional<File> covert(MultipartFile multipartFile) throws IOException {
        File covertFile = new File(multipartFile.getOriginalFilename());

        if (covertFile.createNewFile()) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(covertFile)) {
                fileOutputStream.write(multipartFile.getBytes());
            }
            return Optional.of(covertFile);
        }

        return Optional.empty();
    }


}
