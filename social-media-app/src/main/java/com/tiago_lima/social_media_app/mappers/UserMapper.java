package com.tiago_lima.social_media_app.mappers;

import com.tiago_lima.social_media_app.domain.dtos.LoginUserDto;
import com.tiago_lima.social_media_app.domain.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    @Mapping(target = "username", source = "username")
    @Mapping(target = "password", source = "password")
    User fromUserDto(LoginUserDto loginUserDto);

}
