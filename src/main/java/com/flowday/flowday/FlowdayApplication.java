package com.flowday.flowday;

import com.flowday.flowday.config.AppProperties;
import com.flowday.flowday.config.DotenvLoader;
import com.flowday.flowday.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(AppProperties.class)
public class FlowdayApplication implements CommandLineRunner {

    @Autowired
    private UsuarioService usuarioService;

    public static void main(String[] args) {
        DotenvLoader.load();
        SpringApplication.run(FlowdayApplication.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("✅ Aplicación iniciada correctamente!");
        System.out.println("🌐 http://localhost:8080/login");
        usuarioService.crearAdminSiNoExiste();
    }
}
