package ru.itmo.cs.service;

import io.minio.*;
import io.minio.errors.*;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponseException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class MinIOService {

    @Getter
    private final MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    public MinIOService(@Value("${minio.url}") String minioUrl,
                        @Value("${minio.accessKey}") String accessKey,
                        @Value("${minio.secretKey}") String secretKey) {
        this.minioClient = MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(accessKey, secretKey)
                .build();
    }

    public void uploadFile(String fileName, byte[] data) {
        try {
            ensureBucketExists();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(new ByteArrayInputStream(data), data.length, -1)
                            .build()
            );
        } catch (ErrorResponseException | InsufficientDataException | InternalException |
                 InvalidKeyException | InvalidResponseException | IOException |
                 NoSuchAlgorithmException | XmlParserException | ServerException e) {
            throw new RuntimeException("Ошибка при загрузке файла в MinIO: " + e.getMessage(), e);
        } catch (io.minio.errors.ErrorResponseException e) {
            throw new RuntimeException(e);
        }
    }

    private void ensureBucketExists() {
        try {
            boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | XmlParserException | ServerException e) {
            throw new RuntimeException("Ошибка при проверке/создании корзины в MinIO: " + e.getMessage(), e);
        } catch (io.minio.errors.ErrorResponseException e) {
            throw new RuntimeException(e);
        }
    }
}