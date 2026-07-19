package com.flowday.flowday.repository;

import com.flowday.flowday.model.BloqueRecurrente;
import com.flowday.flowday.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloqueRecurrenteRepository extends JpaRepository<BloqueRecurrente, Long> {

    List<BloqueRecurrente> findByUsuarioOrderByDiaSemanaAscHoraInicioAsc(Usuario usuario);

    List<BloqueRecurrente> findByUsuarioAndDiaSemana(Usuario usuario, Integer diaSemana);
}
