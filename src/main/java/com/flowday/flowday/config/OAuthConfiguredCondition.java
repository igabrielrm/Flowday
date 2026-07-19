package com.flowday.flowday.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Activa OAuth solo cuando hay credenciales completas (Google y/o Microsoft).
 * Usa Environment en lugar de SpEL sobre {@code appProperties} para evitar fallos al arrancar.
 */
public class OAuthConfiguredCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment env = context.getEnvironment();
        boolean google = hasText(env.getProperty("app.oauth.google-client-id"))
                && hasText(env.getProperty("app.oauth.google-client-secret"));
        boolean microsoft = hasText(env.getProperty("app.oauth.microsoft-client-id"))
                && hasText(env.getProperty("app.oauth.microsoft-client-secret"));
        return google || microsoft;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
