package ru.itmo.cs.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
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

    public void renameObject(String sourceObject, String targetObject) {
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucketName)
                            .object(targetObject)
                            .source(CopySource.builder()
                                    .bucket(bucketName)
                                    .object(sourceObject)
                                    .build())
                            .build());
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucketName).object(sourceObject).build());
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при переименовании файла в MinIO: " + e.getMessage(), e);
        }
    }

    public void deleteObject(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при удалении файла в MinIO: " + e.getMessage(), e);
        }
    }

    public String getPresignedUrl(String fileName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .method(Method.GET)
                            .build()
            );
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | XmlParserException | ServerException e) {
            throw new RuntimeException("Ошибка при получении ссылки на файл из MinIO: " + e.getMessage(), e);
        } catch (io.minio.errors.ErrorResponseException e) {
            throw new RuntimeException(e);
        }
    }
}