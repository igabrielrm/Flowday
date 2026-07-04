package com.uce.servidorproyecto.repository;

import com.uce.servidorproyecto.model.HorarioClase;
import com.uce.servidorproyecto.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HorarioClaseRepository extends JpaRepository<HorarioClase, Long> {

    List<HorarioClase> findByUsuarioOrderByDiaSemanaAscHoraInicioAsc(Usuario usuario);

    List<HorarioClase> findByUsuarioAndDiaSemana(Usuario usuario, Integer diaSemana);
}
