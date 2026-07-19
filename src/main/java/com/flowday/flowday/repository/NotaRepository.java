package com.flowday.flowday.repository;

import com.flowday.flowday.model.Nota;
import com.flowday.flowday.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotaRepository extends JpaRepository<Nota, String> {

    List<Nota> findByUsuarioOrderByPinnedDescUpdatedAtDesc(Usuario usuario);
}
