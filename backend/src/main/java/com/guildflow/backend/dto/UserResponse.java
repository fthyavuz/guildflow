package com.guildflow.backend.dto;

import com.guildflow.backend.model.User;
import com.guildflow.backend.model.enums.LanguagePreference;
import com.guildflow.backend.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Role role;
    private LanguagePreference languagePref;
    private boolean active;
    private LocalDateTime createdAt;

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole())
                .languagePref(user.getLanguagePref())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
