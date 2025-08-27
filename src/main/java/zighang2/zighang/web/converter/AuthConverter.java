package zighang2.zighang.web.converter;

import org.springframework.security.crypto.password.PasswordEncoder;
import zighang2.zighang.web.domain.user.User;
import zighang2.zighang.web.domain.user.UserRole;

public class AuthConverter {

    public static User toUser(String email, String name, String password, PasswordEncoder passwordEncoder) {
        return User.builder()
                .email(email)
                .userRole(UserRole.GENERAL)
                .password(passwordEncoder.encode(password))
                .name(name)
                .build();
    }
}
