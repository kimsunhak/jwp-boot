package com.ksh.jwpboot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
//@EnableConfigurationProperties(AppProperties.class)
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private final Auth auth = new Auth();
    private final OAuth2 oAuth2 = new OAuth2();
    private final S3 s3 = new S3();

    @Getter
    @Setter
    public static class Auth {
        private String tokenSecret;
        private long refreshTokenExpirationMsec;
        private long accessTokenExpirationMsec;
    }

    public static final class OAuth2 {
        private List<String> authorizedRedirectUris = new ArrayList<>();

        public List<String> getAuthorizedRedirectUris() {
            return authorizedRedirectUris;
        }

        public OAuth2 authorizedRedirectUris(List<String> authorizedRedirectUris) {
            this.authorizedRedirectUris = authorizedRedirectUris;
            return this;
        }
    }

    @Getter
    @Setter
    public static final class S3 {
        private String defaultImageUri;
        private Integer defaultNumber;
        private String defaultExtension;
    }
}
