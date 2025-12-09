package com.tiago_lima.social_media_app.services;

import com.tiago_lima.social_media_app.domain.dtos.EditUserDto;
import com.tiago_lima.social_media_app.domain.dtos.UserDto;
import com.tiago_lima.social_media_app.domain.entities.User;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.UUID;

public interface UserService {
    User findById(UUID id);

    String saveProfilePicture(UUID id, MultipartFile file);

    UserDto updateUser(UUID id, EditUserDto userDto, MultipartFile file, Principal principal);
}
