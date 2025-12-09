package com.tiago_lima.social_media_app.controllers;

import com.tiago_lima.social_media_app.domain.dtos.EditUserDto;
import com.tiago_lima.social_media_app.domain.dtos.UserDto;
import com.tiago_lima.social_media_app.domain.entities.User;
import com.tiago_lima.social_media_app.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable UUID id) {
        User user = userService.findById(id);

        UserDto dto = UserDto.builder()
                .username(user.getUsername())
                .bio(user.getBio())
                .profilePicture(user.getProfilePicture())
                .createdAt(user.getSignupDate())
                .build();

        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable UUID id,
            @RequestPart("user") EditUserDto userDto,
            @RequestPart(value = "file", required = false) MultipartFile file,
            Principal principal) {

        UserDto updated = userService.updateUser(id, userDto, file, principal);
        return ResponseEntity.ok(updated);
    }


    @GetMapping("/{id}/profile-picture")
    public ResponseEntity<Resource> getProfilePicture(@PathVariable UUID id) throws MalformedURLException {
        User user = userService.findById(id);
        Path file = Paths.get("uploads/profile-pictures/").resolve(Paths.get(user.getProfilePicture()).getFileName());
        Resource resource = new UrlResource(file.toUri());

        // Determine content type dynamically
        String filename = file.getFileName().toString().toLowerCase();
        MediaType mediaType = MediaType.IMAGE_JPEG; // default

        if (filename.endsWith(".png")) {
            mediaType = MediaType.IMAGE_PNG;
        } else if (filename.endsWith(".gif")) {
            mediaType = MediaType.IMAGE_GIF;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))
                .body(resource);
    }


}
