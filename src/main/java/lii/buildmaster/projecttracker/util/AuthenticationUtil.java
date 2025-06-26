package lii.buildmaster.projecttracker.util;

import java.util.UUID;

public class AuthenticationUtil {

    public static String generateTemporaryPassword() {
        return UUID.randomUUID().toString().replace("-", "")
                .substring(0, 12) + "!";
    }
}
