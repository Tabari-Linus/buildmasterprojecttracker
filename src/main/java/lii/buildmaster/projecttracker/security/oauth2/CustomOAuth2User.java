package lii.buildmaster.projecttracker.security.oauth2;

import lii.buildmaster.projecttracker.model.entity.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
public class CustomOAuth2User implements OAuth2User, UserDetails {


    private Long id;
    private String email;
    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;

    public CustomOAuth2User(Long id, String email, String username, String password,
                            Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    public CustomOAuth2User(List<SimpleGrantedAuthority> roleUser, Map<String, Object> attributes, String name) {
        this.authorities = roleUser;
        this.attributes = attributes;
        this.username = name;
    }

    public static CustomOAuth2User create(User user) {
        Collection<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());

        return new CustomOAuth2User(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }

    public static CustomOAuth2User create(User user, Map<String, Object> attributes) {
        CustomOAuth2User customOAuth2User = CustomOAuth2User.create(user);
        customOAuth2User.setAttributes(attributes);
        return customOAuth2User;
    }

    @Override
    public String getName() {
        return String.valueOf(id);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
