package com.example.dailyLog.service;

import com.example.dailyLog.entity.*;
import com.example.dailyLog.exception.imageException.EmptyFileData;
import com.example.dailyLog.exception.imageException.FileUploadError;
import com.example.dailyLog.exception.imageException.ImageErrorCode;
import com.example.dailyLog.exception.imageException.InvalidFileName;
import com.example.dailyLog.repository.DiaryImageRepository;
import com.example.dailyLog.repository.ProfileImageRepository;
import com.example.dailyLog.repository.ScheduleImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ImageServiceImpl implements ImageService {

    @Value("${ImgLocation}")
    private String imageLocation;

    @Value("${ProfileImgLocation}")
    private String profileImageLocation;

    private final DiaryImageRepository diaryImageRepository;
    private final ScheduleImageRepository scheduleImageRepository;
    private final ProfileImageRepository profileImageRepository;
    private final FileService fileService;


    // 유효성 검사 메소드
    public void validateImageFile(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new EmptyFileData(ImageErrorCode.EMPTY_FILE_DATA);
        }

        String oriImgName = imageFile.getOriginalFilename();
        if (oriImgName == null || !oriImgName.contains(".")) {
            throw new InvalidFileName(ImageErrorCode.INVALID_FILE_NAME);
        }
    }


    @Transactional
    @Override
    public DiaryImage saveDiaryImage(MultipartFile imageFile, Diary diary){

        validateImageFile(imageFile);

        try {
            String oriImgName = imageFile.getOriginalFilename();
            String savedFileName = fileService.uploadFile(imageLocation, oriImgName, imageFile.getBytes());
            String imageUrl = "/images/" + savedFileName;

            // Image 엔티티 생성 및 설정
            DiaryImage diaryImage = new DiaryImage();
            diaryImage.setImgName(savedFileName);
            diaryImage.setOriImgName(oriImgName);
            diaryImage.setImgUrl(imageUrl);
            diaryImage.setDiary(diary);

            return diaryImageRepository.save(diaryImage);
        } catch (Exception e) {
            throw new FileUploadError(ImageErrorCode.FILE_UPLOAD_ERROR);
        }
    }


    @Transactional
    @Override
    public ScheduleImage saveScheduleImage(MultipartFile imageFile, Schedule schedule){

        validateImageFile(imageFile);

        try {
            String oriImgName = imageFile.getOriginalFilename();
            String savedFileName = fileService.uploadFile(imageLocation, oriImgName, imageFile.getBytes());
            String imageUrl = "/images/" + savedFileName;

            // Image 엔티티 생성 및 설정
            ScheduleImage scheduleImage = new ScheduleImage();
            scheduleImage.setImgName(savedFileName);
            scheduleImage.setOriImgName(oriImgName);
            scheduleImage.setImgUrl(imageUrl);
            scheduleImage.setSchedule(schedule);

            return scheduleImageRepository.save(scheduleImage);
        } catch (Exception e) {
            throw new FileUploadError(ImageErrorCode.FILE_UPLOAD_ERROR);
        }
    }


    @Transactional
    @Override
    public ProfileImage saveProfileImage(MultipartFile imageFile, User user) throws Exception {
            try {
                String oriImgName = imageFile.getOriginalFilename();
                String savedFileName = fileService.uploadFile(profileImageLocation, oriImgName, imageFile.getBytes());
                String imageUrl = "/profileImages/" + savedFileName;

                // ProfileImage 엔티티 생성 및 설정
                ProfileImage profileImage = new ProfileImage();
                profileImage.setImgName(savedFileName);
                profileImage.setOriImgName(oriImgName);
                profileImage.setImgUrl(imageUrl);
                profileImage.setUser(user);
                return profileImageRepository.save(profileImage);
            } catch (Exception e) {
                throw new FileUploadError(ImageErrorCode.FILE_UPLOAD_ERROR);
            }

    }
}

