package org.demo.service;

import io.minio.*;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.constant.EntityConstant;
import org.demo.util.ContentTypeUtil;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static org.demo.constant.EntityConstant.CHUNK_SUFFIX;

@Slf4j
@RequiredArgsConstructor
@Service
public class MinioService {

    private final MinioClient minioClient;

    public void upload(String bucket, String srcPath, String destPath) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String contentType = ContentTypeUtil.getContentType(srcPath);
        minioClient.uploadObject(
                UploadObjectArgs.builder()
                        .bucket(bucket)
                        .object(srcPath)
                        .filename(destPath)
                        .contentType(contentType)
                        .build());

    }

    public void upload(String bucket, InputStream in, String destPath, String contentType) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucket)
                .object(destPath)
                .stream(in, in.available(), -1)
                .contentType(contentType)
                .build());

    }

    public void download(String bucket, String srcPath, OutputStream os) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        GetObjectResponse response = minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(srcPath).build());
        response.transferTo(os);
        os.flush();
    }

    public InputStream getDownloadInputStream(String bucket, String srcPath) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        return minioClient.getObject(GetObjectArgs
                .builder()
                .bucket(bucket)
                .object(srcPath)
                .build());
    }

    public void remove(String bucket, String path) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucket)
                        .object(path)
                        .bypassGovernanceMode(true)
                        .build());
    }

    public void merge(String path, String name, int maxIndex) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        List<ComposeSource> list = new ArrayList<>();
        for (int i = 1; i <= maxIndex; i++) {
            list.add(
                    ComposeSource.builder()
                            .bucket(EntityConstant.VIDEO_BUCKET)
                            .object(path + CHUNK_SUFFIX + i)
                            .build()
            );
        }

        minioClient.composeObject(
                ComposeObjectArgs.builder()
                        .bucket(EntityConstant.VIDEO_BUCKET)
                        .object(path + name)
                        .sources(list)
                        .build()
        );
    }
}
