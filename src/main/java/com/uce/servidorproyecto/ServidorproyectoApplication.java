package com.uce.servidorproyecto;

import com.uce.servidorproyecto.config.AppProperties;
import com.uce.servidorproyecto.config.DotenvLoader;
import com.uce.servidorproyecto.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(AppProperties.class)
public class ServidorproyectoApplication implements CommandLineRunner {

    @Autowired
    private UsuarioService usuarioService;

    public static void main(String[] args) {
        DotenvLoader.load();
        SpringApplication.run(ServidorproyectoApplication.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("✅ Aplicación iniciada correctamente!");
        System.out.println("🌐 http://localhost:8080/login");
        usuarioService.crearAdminSiNoExiste();
    }
}