package com.example.dailyLog.service;

public interface FileService {
    public String uploadFile(String uploadPath,String originalFileName,byte[] fileData) throws Exception;
    public void deleteFile(String filePath) throws Exception;
}
