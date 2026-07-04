package com.uce.servidorproyecto;

import com.uce.servidorproyecto.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ServidorproyectoApplication implements CommandLineRunner {

    @Autowired
    private UsuarioService usuarioService;

    public static void main(String[] args) {
        SpringApplication.run(ServidorproyectoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("✅ Aplicación iniciada correctamente!");
        System.out.println("🔑 Admin: admin@uce.edu.ec / admin123");
        System.out.println("🌐 http://localhost:8080/login");

        // Crear admin por defecto
        usuarioService.crearAdminSiNoExiste();
    }
}