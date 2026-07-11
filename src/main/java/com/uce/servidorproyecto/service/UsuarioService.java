package com.uce.servidorproyecto.service;

import com.uce.servidorproyecto.config.AppProperties;
import com.uce.servidorproyecto.model.Usuario;
import com.uce.servidorproyecto.repository.UsuarioRepository;
import com.uce.servidorproyecto.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UsuarioService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AppProperties appProperties;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public boolean correoValido(String correo) {
        return correo != null && EMAIL_PATTERN.matcher(correo.trim()).matches();
    }

    public boolean correoExiste(String correo) {
        return usuarioRepository.findByCorreo(correo).isPresent();
    }

    public Usuario registrar(Usuario usuario) {
        usuario.setContrasena(encoder.encode(usuario.getContrasena()));
        usuario.setFechaRegistro(LocalDateTime.now());
        usuario.setEstado("ACTIVO");
        usuario.setRol("USER");
        usuario.setTema("dark");
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> autenticar(String correo, String contrasenaPlana) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo);
        if (usuarioOpt.isPresent()) {
            Usuario u = usuarioOpt.get();
            if ("ACTIVO".equals(u.getEstado()) && encoder.matches(contrasenaPlana, u.getContrasena())) {
                u.setUltimoAcceso(LocalDateTime.now());
                usuarioRepository.save(u);
                return Optional.of(u);
            }
        }
        return Optional.empty();
    }

    public void guardar(Usuario usuario) {
        usuarioRepository.save(usuario);
    }

    public boolean telefonoValido(String telefono) {
        return telefono != null && telefono.matches("\\d{10}");
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo);
    }

    public String verificarRecuperacion(String correo, String telefono) {
        if (correo == null || correo.isBlank()) {
            return "Ingresa tu correo electrónico.";
        }
        if (telefono == null || !telefonoValido(telefono.trim())) {
            return "Ingresa tu teléfono registrado (10 dígitos).";
        }
        Optional<Usuario> u = usuarioRepository.findByCorreo(correo.trim());
        if (u.isEmpty()) {
            return "No encontramos una cuenta con ese correo.";
        }
        if ("ADMIN".equals(u.get().getRol())) {
            return "Los administradores deben contactar soporte interno para restablecer acceso.";
        }
        String telReg = u.get().getTelefono();
        if (telReg == null || !telReg.equals(telefono.trim())) {
            return "El teléfono no coincide con el registrado en tu perfil.";
        }
        return null;
    }

    public void restablecerContrasena(Long userId, String nuevaPlana) {
        Usuario u = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        u.setContrasena(encoder.encode(nuevaPlana));
        usuarioRepository.save(u);
    }

    public Iterable<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public void eliminar(Long id) {
        usuarioRepository.deleteById(id);
    }

    public void actualizarPerfil(Long id, String nombre, String telefono,
                                 LocalDate fechaNacimiento, String genero) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        u.setNombre(nombre);
        u.setTelefono(telefono);
        u.setFechaNacimiento(fechaNacimiento);
        u.setGenero(genero);
        usuarioRepository.save(u);
    }

    public void actualizarEmergencia(Long id, String nombre, String telefono, String relacion) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        u.setNombreEmergencia(nombre);
        u.setTelefonoEmergencia(telefono);
        u.setRelacionEmergencia(relacion);
        usuarioRepository.save(u);
    }

    public void cambiarTema(Long id, String tema) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        u.setTema(tema);
        usuarioRepository.save(u);
    }

    public String cambiarContrasena(Long id, String actualPlana, String nuevaPlana) {
        if (!SecurityUtils.isPasswordStrongEnough(nuevaPlana)) {
            return "La nueva contraseña debe tener al menos " + SecurityUtils.PASSWORD_MIN_LENGTH + " caracteres.";
        }
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (!encoder.matches(actualPlana, u.getContrasena())) {
            return "La contraseña actual es incorrecta.";
        }
        u.setContrasena(encoder.encode(nuevaPlana));
        usuarioRepository.save(u);
        return null;
    }

    public void desactivarCuenta(Long id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        u.setEstado("INACTIVO");
        usuarioRepository.save(u);
    }

    public void cambiarRol(Long id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        u.setRol("ADMIN".equals(u.getRol()) ? "USER" : "ADMIN");
        usuarioRepository.save(u);
    }

    public long contarUsuarios() {
        return usuarioRepository.count();
    }

    public long contarUsuariosActivos() {
        return usuarioRepository.countUsuariosActivos();
    }

    public long contarAdmins() {
        return usuarioRepository.countAdmins();
    }

    public long contarUsers() {
        return usuarioRepository.countUsers();
    }

    public void crearAdminSiNoExiste() {
        AppProperties.Admin adminConfig = appProperties.getAdmin();
        if (!adminConfig.isSeedEnabled()) {
            return;
        }
        String email = adminConfig.getSeedEmail();
        String password = adminConfig.getSeedPassword();
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return;
        }
        if (usuarioRepository.findByCorreo(email.trim()).isPresent()) {
            return;
        }
        Usuario admin = new Usuario();
        admin.setNombre("Administrador");
        admin.setCorreo(email.trim());
        admin.setContrasena(encoder.encode(password));
        admin.setRol("ADMIN");
        admin.setEstado("ACTIVO");
        admin.setFechaRegistro(LocalDateTime.now());
        usuarioRepository.save(admin);
    }

    public Usuario resolverOAuth(String email, String nombre) {
        if (!correoValido(email)) {
            throw new IllegalStateException("El proveedor OAuth no devolvió un correo válido.");
        }
        String correo = email.trim().toLowerCase();
        Optional<Usuario> existente = usuarioRepository.findByCorreo(correo);
        if (existente.isPresent()) {
            Usuario usuario = existente.get();
            if (!"ACTIVO".equals(usuario.getEstado())) {
                throw new IllegalStateException("Tu cuenta está inactiva. Contacta soporte.");
            }
            if ("ADMIN".equals(usuario.getRol())) {
                throw new IllegalStateException("Los administradores deben usar el acceso interno.");
            }
            usuario.setUltimoAcceso(LocalDateTime.now());
            return usuarioRepository.save(usuario);
        }

        Usuario nuevo = new Usuario();
        nuevo.setCorreo(correo);
        nuevo.setNombre(nombre != null && !nombre.isBlank() ? nombre.trim() : correo.split("@")[0]);
        nuevo.setContrasena(encoder.encode(java.util.UUID.randomUUID().toString()));
        nuevo.setTelefono("");
        return registrar(nuevo);
    }
}
