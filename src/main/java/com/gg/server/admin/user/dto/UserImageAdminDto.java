package com.gg.server.admin.user.dto;

import com.gg.server.domain.user.data.UserImage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserImageAdminDto {
    Long id;
    String userIntraId;
    String imageUri;
    LocalDateTime createdAt;
    LocalDateTime deletedAt;
    Boolean isCurrent;

    public UserImageAdminDto(UserImage userImage) {
        this.id = userImage.getId();
        this.userIntraId = userImage.getUser().getIntraId();
        this.imageUri = userImage.getImageUri();
        this.createdAt = userImage.getCreatedAt();
        this.deletedAt = userImage.getDeletedAt();
        this.isCurrent = userImage.getIsCurrent();
    }
}
