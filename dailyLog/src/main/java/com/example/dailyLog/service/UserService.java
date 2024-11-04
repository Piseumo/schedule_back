package com.example.dailyLog.service;

import com.example.dailyLog.dto.request.UserRequestInsertDto;
import com.example.dailyLog.dto.request.UserRequestUpdateDto;
import com.example.dailyLog.entity.ProfileImage;
import com.example.dailyLog.entity.User;
import com.example.dailyLog.security.CustomUserDetails;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    User createUser(UserRequestInsertDto userRequestInsertDto);
    User findUserById(Long id);
    void updateUserName(Long id, UserRequestUpdateDto userRequestUpdateDto);
    void updateProfileImage(Long id, MultipartFile imageFile);
}
