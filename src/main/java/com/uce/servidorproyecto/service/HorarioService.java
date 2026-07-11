package com.uce.servidorproyecto.service;

import com.uce.servidorproyecto.model.BloqueRecurrente;
import com.uce.servidorproyecto.model.Usuario;
import com.uce.servidorproyecto.repository.BloqueRecurrenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
public class HorarioService {

    public static final int HORA_GRID_INICIO = 7;
    public static final int HORA_GRID_FIN = 21;

    private static final String[] COLORES = {
            "#5082ef", "#a855f7", "#22c55e", "#f59e0b", "#ef4444", "#06b6d4", "#ec4899"
    };

    @Autowired
    private BloqueRecurrenteRepository bloqueRecurrenteRepository;

    public List<BloqueRecurrente> listarPorUsuario(Usuario usuario) {
        return bloqueRecurrenteRepository.findByUsuarioOrderByDiaSemanaAscHoraInicioAsc(usuario);
    }

    public Optional<BloqueRecurrente> buscarPorId(Long id) {
        return bloqueRecurrenteRepository.findById(id);
    }

    @Transactional
    public BloqueRecurrente guardar(Usuario usuario, BloqueRecurrente datos) {
        validar(datos);
        if (hayChoque(usuario, datos, null)) {
            throw new IllegalArgumentException("Ya tienes otro bloque en ese horario");
        }
        datos.setUsuario(usuario);
        if (datos.getColor() == null || datos.getColor().isBlank()) {
            int idx = listarPorUsuario(usuario).size() % COLORES.length;
            datos.setColor(COLORES[idx]);
        }
        return bloqueRecurrenteRepository.save(datos);
    }

    @Transactional
    public BloqueRecurrente actualizar(Usuario usuario, Long id, BloqueRecurrente datos) {
        BloqueRecurrente existente = bloqueRecurrenteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bloque no encontrado"));
        if (!existente.getUsuario().getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("No tienes permiso para editar este bloque");
        }
        validar(datos);
        if (hayChoque(usuario, datos, id)) {
            throw new IllegalArgumentException("Ya tienes otro bloque en ese horario");
        }
        existente.setMateria(datos.getMateria());
        existente.setDiaSemana(datos.getDiaSemana());
        existente.setHoraInicio(datos.getHoraInicio());
        existente.setHoraFin(datos.getHoraFin());
        existente.setAula(datos.getAula());
        existente.setProfesor(datos.getProfesor());
        if (datos.getColor() != null && !datos.getColor().isBlank()) {
            existente.setColor(datos.getColor());
        }
        return bloqueRecurrenteRepository.save(existente);
    }

    @Transactional
    public void eliminar(Usuario usuario, Long id) {
        BloqueRecurrente bloque = bloqueRecurrenteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bloque no encontrado"));
        if (!bloque.getUsuario().getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("No tienes permiso para eliminar este bloque");
        }
        bloqueRecurrenteRepository.delete(bloque);
    }

    public Optional<Map<String, Object>> obtenerAlertaBloque(Usuario usuario, int minutosAntes) {
        int diaHoy = LocalDate.now().getDayOfWeek().getValue();
        LocalTime ahora = LocalTime.now();
        LocalTime limite = ahora.plusMinutes(minutosAntes);

        return bloqueRecurrenteRepository.findByUsuarioAndDiaSemana(usuario, diaHoy).stream()
                .filter(c -> {
                    if (c.getHoraInicio() == null || c.getHoraFin() == null) return false;
                    boolean yaEmpezo = !ahora.isBefore(c.getHoraInicio()) && ahora.isBefore(c.getHoraFin());
                    boolean empiezaPronto = !c.getHoraInicio().isBefore(ahora) && !c.getHoraInicio().isAfter(limite);
                    return yaEmpezo || empiezaPronto;
                })
                .min(Comparator.comparing(BloqueRecurrente::getHoraInicio))
                .map(this::toAlertaMap);
    }

    public List<BloqueRecurrente> bloquesQueEmpiezanAhora(Usuario usuario, LocalTime ventanaInicio, LocalTime ventanaFin) {
        int dia = LocalDate.now().getDayOfWeek().getValue();
        return bloqueRecurrenteRepository.findByUsuarioAndDiaSemana(usuario, dia).stream()
                .filter(c -> c.getHoraInicio() != null
                        && !c.getHoraInicio().isBefore(ventanaInicio)
                        && !c.getHoraInicio().isAfter(ventanaFin))
                .toList();
    }

    public Map<String, Object> toMap(BloqueRecurrente c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("materia", c.getMateria());
        m.put("diaSemana", c.getDiaSemana());
        m.put("diaNombre", nombreDia(c.getDiaSemana()));
        m.put("horaInicio", c.getHoraInicio() != null ? c.getHoraInicio().toString().substring(0, 5) : null);
        m.put("horaFin", c.getHoraFin() != null ? c.getHoraFin().toString().substring(0, 5) : null);
        m.put("aula", c.getAula());
        m.put("profesor", c.getProfesor());
        m.put("color", c.getColor());
        return m;
    }

    public static String nombreDia(int diaSemana) {
        return DayOfWeek.of(diaSemana).getDisplayName(java.time.format.TextStyle.FULL, new Locale("es"));
    }

    public List<String> listarMateriasDistintas(Usuario usuario) {
        return listarPorUsuario(usuario).stream()
                .map(BloqueRecurrente::getMateria)
                .filter(m -> m != null && !m.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private Map<String, Object> toAlertaMap(BloqueRecurrente c) {
        Map<String, Object> m = toMap(c);
        LocalTime ahora = LocalTime.now();
        boolean enCurso = c.getHoraInicio() != null && c.getHoraFin() != null
                && !ahora.isBefore(c.getHoraInicio()) && ahora.isBefore(c.getHoraFin());
        m.put("enCurso", enCurso);
        if (enCurso) {
            m.put("mensaje", "Estás en horario de " + c.getMateria());
        } else {
            m.put("mensaje", "Te toca " + c.getMateria() + " a las " + c.getHoraInicio().toString().substring(0, 5));
        }
        return m;
    }

    private void validar(BloqueRecurrente datos) {
        if (datos.getMateria() == null || datos.getMateria().isBlank()) {
            throw new IllegalArgumentException("El título del bloque es obligatorio");
        }
        if (datos.getDiaSemana() == null || datos.getDiaSemana() < 1 || datos.getDiaSemana() > 7) {
            throw new IllegalArgumentException("Día de la semana inválido");
        }
        if (datos.getHoraInicio() == null || datos.getHoraFin() == null) {
            throw new IllegalArgumentException("Hora de inicio y fin son obligatorias");
        }
        if (!datos.getHoraFin().isAfter(datos.getHoraInicio())) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior al inicio");
        }
    }

    private boolean hayChoque(Usuario usuario, BloqueRecurrente datos, Long excluirId) {
        List<BloqueRecurrente> delDia = bloqueRecurrenteRepository.findByUsuarioAndDiaSemana(usuario, datos.getDiaSemana());
        for (BloqueRecurrente otra : delDia) {
            if (excluirId != null && excluirId.equals(otra.getId())) continue;
            if (seSolapan(datos.getHoraInicio(), datos.getHoraFin(), otra.getHoraInicio(), otra.getHoraFin())) {
                return true;
            }
        }
        return false;
    }

    private boolean seSolapan(LocalTime aInicio, LocalTime aFin, LocalTime bInicio, LocalTime bFin) {
        return aInicio.isBefore(bFin) && bInicio.isBefore(aFin);
    }
}
