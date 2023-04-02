package org.demo.core.service;

import io.minio.errors.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface MinioService {

    /**
     * @param bucket 桶
     * @param srcPath   源文件路径，本地路径
     * @param destPath  目标文件路径，Minio路径
     */
    void upload(String bucket, String srcPath, String destPath) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;

    /**
     * @param bucket 桶
     * @param in   输入流
     * @param destPath  目标文件路径，Minio路径
     */
    void upload(String bucket, InputStream in, String destPath, String contentType) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;

    /**
     * @param bucket 桶
     * @param srcPath   源文件路径，Minio路径
     * @param os  目标输出流
     */
    void download(String bucket, String srcPath, OutputStream os) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;

    InputStream getDownloadInputStream(String bucket, String srcPath) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;

    /**
     * @param bucket    桶
     * @param path  上传路径
     */
    void remove(String bucket, String path) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;

    /**
     *
     * @param path 路径
     * @param name 名字 = 文件名 + 后缀
     * @param maxIndex 分块最大下标
     */
    void merge(String path, String name, int maxIndex) throws Exception;
}
