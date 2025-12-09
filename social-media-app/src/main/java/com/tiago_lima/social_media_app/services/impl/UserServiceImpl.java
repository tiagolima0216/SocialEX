package com.tiago_lima.social_media_app.services.impl;

import com.tiago_lima.social_media_app.domain.dtos.EditUserDto;
import com.tiago_lima.social_media_app.domain.dtos.UserDto;
import com.tiago_lima.social_media_app.domain.entities.User;
import com.tiago_lima.social_media_app.kafka.events.UserEvent;
import com.tiago_lima.social_media_app.kafka.producers.UserEventProducer;
import com.tiago_lima.social_media_app.repositories.UserRepository;
import com.tiago_lima.social_media_app.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;
import java.nio.file.*;
import java.security.Principal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserEventProducer userEventProducer;
    private final UserRepository userRepository;

    @Override
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Override
    public String saveProfilePicture(UUID userId, MultipartFile file) {
        try {
            // Generate unique filename
            String filename = userId + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get("uploads/profile-pictures/");
            Files.createDirectories(uploadPath);

            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Build URL to access the picture (if served statically)
            String url = "/uploads/profile-pictures/" + filename;

            // Save URL in DB
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setProfilePicture(url);
            userRepository.save(user);

            return url;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Override
    public UserDto updateUser(UUID id, EditUserDto userDto, MultipartFile file, Principal principal) {
        // Prevent editing other people's profiles
        UUID loggedInId = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new EntityNotFoundException("Logged in user doesn't exist"))
                .getId();

        if (!loggedInId.equals(id)) {
            throw new AccessDeniedException("You are not allowed to update this profile");
        }

        try {
            String timestamp = String.valueOf(System.currentTimeMillis()); // or use UUID.randomUUID()
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setBio(userDto.getBio());

            if (user.getProfilePicture() != null) {
                Path oldFile = Paths.get("uploads/profile-pictures/").resolve(Paths.get(user.getProfilePicture()).getFileName());
                Files.deleteIfExists(oldFile);
            }
            if (file != null && !file.isEmpty()) {
                String filename = id + "_" + timestamp + "_" + file.getOriginalFilename();
                Path uploadPath = Paths.get("uploads/profile-pictures/");
                Files.createDirectories(uploadPath);
                Path filePath = uploadPath.resolve(filename);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                user.setProfilePicture("/uploads/profile-pictures/" + filename);
            }

            User savedUser = userRepository.save(user);

            userEventProducer.publish(UserEvent.builder()
                    .id(savedUser.getId().toString())
                    .username(savedUser.getUsername())
                    //.displayName(user.getUsername()) // or a separate displayName
                    .bio(savedUser.getBio())
                    .profilePicture(savedUser.getProfilePicture())
                    .type(UserEvent.Type.UPDATED)
                    .build());

            return UserDto.builder()
                    .username(savedUser.getUsername())
                    .bio(savedUser.getBio())
                    .profilePicture(savedUser.getProfilePicture())
                    .createdAt(savedUser.getSignupDate())
                    .build();
        }catch (IOException e){
            throw new RuntimeException("Failed to store file", e);
        }

    }


}
