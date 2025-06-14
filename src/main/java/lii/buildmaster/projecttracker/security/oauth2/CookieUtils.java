package lii.buildmaster.projecttracker.security.oauth2;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.SerializationUtils;

import java.util.Base64;
import java.util.Optional;

public class CookieUtils {


    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge, boolean httpOnly, String sameSite) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(false);

        if (sameSite != null && !sameSite.isEmpty()) {
            response.setHeader("Set-Cookie", String.format("%s=%s; Path=/; Max-Age=%d;%s%s",
                    name, value, maxAge,
                    (httpOnly ? " HttpOnly;" : ""),
                    (sameSite != null && !sameSite.isEmpty() ? " SameSite=" + sameSite + ";" : "")
            ));
        } else {

            response.addCookie(cookie);
        }
    }


    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge, boolean httpOnly, boolean secure, String domain) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(secure);
        if (domain != null && !domain.isEmpty()) {
            cookie.setDomain(domain);
        }
        response.addCookie(cookie);
    }


    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie: cookies) {
                if (cookie.getName().equals(name)) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }
    }

    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return Optional.of(cookie);
                }
            }
        }
        return Optional.empty();
    }

    public static String serialize(Object object) {
        return Base64.getUrlEncoder()
                .encodeToString(SerializationUtils.serialize(object));
    }

    public static <T> T deserialize(Cookie cookie, Class<T> cls) {

        try {
            return cls.cast(SerializationUtils.deserialize(
                    Base64.getUrlDecoder().decode(cookie.getValue())));
        } catch (Exception e) {

            System.err.println("Error deserializing cookie: " + e.getMessage());
            return null;
        }
    }
}
