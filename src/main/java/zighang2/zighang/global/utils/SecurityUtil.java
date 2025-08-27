package zighang2.zighang.global.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import zighang2.zighang.global.auth.UserPrincipal;
import zighang2.zighang.global.payload.code.status.ErrorStatus;
import zighang2.zighang.global.payload.exception.GeneralException;

public class SecurityUtil {
    private SecurityUtil() {}

    public static long getCurrentUserId() {

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new GeneralException(ErrorStatus.EMPTY_TOKEN);
        }

        long userId;
        if (authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            userId = userPrincipal.getId();
        } else {
            throw new GeneralException(ErrorStatus._BAD_REQUEST);
        }

        return userId;
    }
}
