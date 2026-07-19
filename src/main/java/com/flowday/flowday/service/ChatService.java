package com.flowday.flowday.service;

import com.flowday.flowday.api.dto.ChatMessageDto;
import com.flowday.flowday.api.dto.ConversationDto;
import com.flowday.flowday.api.dto.UsuarioDto;
import com.flowday.flowday.model.MensajePrivado;
import com.flowday.flowday.model.Usuario;
import com.flowday.flowday.repository.ConexionRepository;
import com.flowday.flowday.repository.MensajePrivadoRepository;
import com.flowday.flowday.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    @Autowired
    private MensajePrivadoRepository mensajePrivadoRepository;

    @Autowired
    private ConexionRepository conexionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ChatPushService chatPushService;

    @Autowired
    private NotificacionService notificacionService;

    @Transactional
    public ChatMessageDto enviar(Usuario remitente, Long destinatarioId, String contenido) {
        if (contenido == null || contenido.isBlank()) {
            throw new IllegalArgumentException("El mensaje no puede estar vacío");
        }
        String texto = contenido.trim();
        if (texto.length() > 2000) {
            texto = texto.substring(0, 2000);
        }

        Usuario destinatario = usuarioRepository.findById(destinatarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!conexionRepository.existenConectados(remitente, destinatario)) {
            throw new IllegalArgumentException("Solo puedes chatear con usuarios conectados");
        }

        MensajePrivado msg = new MensajePrivado();
        msg.setRemitente(remitente);
        msg.setDestinatario(destinatario);
        msg.setContenido(texto);
        MensajePrivado saved = mensajePrivadoRepository.save(msg);

        chatPushService.pushToUser(destinatario.getId(), toDto(saved, destinatario.getId()));
        chatPushService.pushToUser(remitente.getId(), toDto(saved, remitente.getId()));
        notificacionService.crear(destinatario, "MENSAJE",
                "Nuevo mensaje de " + remitente.getNombre(),
                texto.length() > 120 ? texto.substring(0, 117) + "..." : texto,
                "/app/chat?user=" + remitente.getId());
        return toDto(saved, remitente.getId());
    }

    public List<ChatMessageDto> listarConversacion(Usuario usuario, Long otroUserId) {
        Usuario otro = usuarioRepository.findById(otroUserId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        if (!conexionRepository.existenConectados(usuario, otro)) {
            throw new IllegalArgumentException("No estás conectado con este usuario");
        }
        return mensajePrivadoRepository.findConversacion(usuario, otro).stream()
                .map(m -> toDto(m, usuario.getId()))
                .toList();
    }

    @Transactional
    public int marcarLeidos(Usuario usuario, Long otroUserId) {
        Usuario otro = usuarioRepository.findById(otroUserId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return mensajePrivadoRepository.marcarLeidosDe(usuario, otro);
    }

    public List<ConversationDto> listarConversaciones(Usuario usuario, List<Usuario> conectados) {
        List<MensajePrivado> recientes = mensajePrivadoRepository.findRecientesInvolucrando(usuario);
        Map<Long, MensajePrivado> ultimoPorUsuario = new LinkedHashMap<>();

        for (MensajePrivado m : recientes) {
            Long otroId = m.getRemitente().getId().equals(usuario.getId())
                    ? m.getDestinatario().getId()
                    : m.getRemitente().getId();
            ultimoPorUsuario.putIfAbsent(otroId, m);
        }

        List<ConversationDto> result = new ArrayList<>();
        for (Usuario u : conectados) {
            MensajePrivado ultimo = ultimoPorUsuario.get(u.getId());
            long noLeidos = mensajePrivadoRepository.countNoLeidosDe(usuario, u);
            result.add(new ConversationDto(
                    UsuarioDto.from(u),
                    ultimo != null ? ultimo.getContenido() : null,
                    ultimo != null && ultimo.getFecha() != null ? ultimo.getFecha().toString() : null,
                    noLeidos
            ));
        }

        result.sort(Comparator
                .comparing((ConversationDto c) -> c.ultimaFecha() == null)
                .thenComparing(ConversationDto::ultimaFecha, Comparator.nullsLast(Comparator.reverseOrder())));
        return result;
    }

    public long contarNoLeidos(Usuario usuario) {
        return mensajePrivadoRepository.countNoLeidos(usuario);
    }

    @Transactional
    public int eliminarConversacion(Usuario usuario, Long otroUserId) {
        Usuario otro = usuarioRepository.findById(otroUserId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        if (!conexionRepository.existenConectados(usuario, otro)) {
            throw new IllegalArgumentException("No estás conectado con este usuario");
        }
        return mensajePrivadoRepository.deleteConversacion(usuario, otro);
    }

    private ChatMessageDto toDto(MensajePrivado m, Long viewerId) {
        return new ChatMessageDto(
                m.getId(),
                m.getRemitente().getId(),
                m.getDestinatario().getId(),
                m.getContenido(),
                m.getFecha() != null ? m.getFecha().toString() : null,
                m.isLeida(),
                m.getRemitente().getId().equals(viewerId)
        );
    }
}
