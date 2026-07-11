package com.uce.servidorproyecto.repository;

import com.uce.servidorproyecto.model.BloqueRecurrente;
import com.uce.servidorproyecto.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloqueRecurrenteRepository extends JpaRepository<BloqueRecurrente, Long> {

    List<BloqueRecurrente> findByUsuarioOrderByDiaSemanaAscHoraInicioAsc(Usuario usuario);

    List<BloqueRecurrente> findByUsuarioAndDiaSemana(Usuario usuario, Integer diaSemana);
}
