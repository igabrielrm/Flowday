package com.flowday.flowday.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Conditional(OAuthConfiguredCondition.class)
public class OAuth2ClientConfig {

    @Bean
    ClientRegistrationRepository clientRegistrationRepository(AppProperties appProperties) {
        List<ClientRegistration> registrations = new ArrayList<>();
        AppProperties.OAuth oauth = appProperties.getOauth();

        if (oauth.hasGoogle()) {
            registrations.add(ClientRegistration.withRegistrationId("google")
                    .clientId(oauth.getGoogleClientId())
                    .clientSecret(oauth.getGoogleClientSecret())
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .scope("openid", "profile", "email")
                    .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                    .tokenUri("https://oauth2.googleapis.com/token")
                    .userInfoUri("https://openidconnect.googleapis.com/v1/userinfo")
                    .userNameAttributeName("sub")
                    .clientName("Google")
                    .build());
        }

        if (oauth.hasMicrosoft()) {
            registrations.add(ClientRegistration.withRegistrationId("microsoft")
                    .clientId(oauth.getMicrosoftClientId())
                    .clientSecret(oauth.getMicrosoftClientSecret())
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .scope("openid", "profile", "email")
                    .issuerUri("https://login.microsoftonline.com/common/v2.0")
                    .build());
        }

        return new InMemoryClientRegistrationRepository(registrations);
    }
}
