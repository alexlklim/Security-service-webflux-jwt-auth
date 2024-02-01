package com.alex.inventory.entity;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("refresh_token")
public class RefreshToken {
    @Id
    private Long id;
    private String refreshToken;
    private LocalDateTime expiresAt;
    private Long userId;
}
