package net.togogo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.togogo.entity.User;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
//隔绝实现类，达到安全防护的效果
public class UserDTO {

    private Long id;
    private String username;
    private String email;
    private LocalDateTime createTime;
    private String phoneNumber;
    private User.Role role;

}
