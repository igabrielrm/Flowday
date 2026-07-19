package com.flowday.flowday.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Admin admin = new Admin();
    private final Brand brand = new Brand();
    private final OAuth oauth = new OAuth();

    public Admin getAdmin() {
        return admin;
    }

    public Brand getBrand() {
        return brand;
    }

    public OAuth getOauth() {
        return oauth;
    }

    public static class Admin {
        private String pathPrefix = "/internal";
        private boolean seedEnabled = false;
        private String seedEmail = "";
        private String seedPassword = "";

        public String getPathPrefix() {
            return pathPrefix;
        }

        public void setPathPrefix(String pathPrefix) {
            this.pathPrefix = pathPrefix;
        }

        public String getLoginPath() {
            String prefix = pathPrefix == null ? "/internal" : pathPrefix.replaceAll("/$", "");
            return prefix + "/login";
        }

        public boolean isSeedEnabled() {
            return seedEnabled;
        }

        public void setSeedEnabled(boolean seedEnabled) {
            this.seedEnabled = seedEnabled;
        }

        public String getSeedEmail() {
            return seedEmail;
        }

        public void setSeedEmail(String seedEmail) {
            this.seedEmail = seedEmail;
        }

        public String getSeedPassword() {
            return seedPassword;
        }

        public void setSeedPassword(String seedPassword) {
            this.seedPassword = seedPassword;
        }
    }

    public static class OAuth {
        private String googleClientId = "";
        private String googleClientSecret = "";
        private String microsoftClientId = "";
        private String microsoftClientSecret = "";

        public String getGoogleClientId() {
            return googleClientId;
        }

        public void setGoogleClientId(String googleClientId) {
            this.googleClientId = googleClientId;
        }

        public String getGoogleClientSecret() {
            return googleClientSecret;
        }

        public void setGoogleClientSecret(String googleClientSecret) {
            this.googleClientSecret = googleClientSecret;
        }

        public String getMicrosoftClientId() {
            return microsoftClientId;
        }

        public void setMicrosoftClientId(String microsoftClientId) {
            this.microsoftClientId = microsoftClientId;
        }

        public String getMicrosoftClientSecret() {
            return microsoftClientSecret;
        }

        public void setMicrosoftClientSecret(String microsoftClientSecret) {
            this.microsoftClientSecret = microsoftClientSecret;
        }

        public boolean hasGoogle() {
            return hasText(googleClientId) && hasText(googleClientSecret);
        }

        public boolean hasMicrosoft() {
            return hasText(microsoftClientId) && hasText(microsoftClientSecret);
        }

        public boolean isConfigured() {
            return hasGoogle() || hasMicrosoft();
        }

        private static boolean hasText(String value) {
            return value != null && !value.isBlank();
        }
    }

    public static class Brand {
        private String name = "Flowday";
        private String tagline = "Plan your day, own your flow.";
        private String primaryColor = "#5082ef";
        private String logoUrl = "/images/flowday-logo.svg";
        private String supportEmail = "support@flowday.app";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTagline() {
            return tagline;
        }

        public void setTagline(String tagline) {
            this.tagline = tagline;
        }

        public String getPrimaryColor() {
            return primaryColor;
        }

        public void setPrimaryColor(String primaryColor) {
            this.primaryColor = primaryColor;
        }

        public String getLogoUrl() {
            return logoUrl;
        }

        public void setLogoUrl(String logoUrl) {
            this.logoUrl = logoUrl;
        }

        public String getSupportEmail() {
            return supportEmail;
        }

        public void setSupportEmail(String supportEmail) {
            this.supportEmail = supportEmail;
        }
    }
}
