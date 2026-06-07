package com.saldium.saldium.repository;

import com.saldium.saldium.entidades.Categoria;
import com.saldium.saldium.entidades.Transacao;
import com.saldium.saldium.security.user.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
    List<Transacao> findAllByUsuario(Usuario usuario);
    Optional<Transacao> findByIdAndUsuario(Long id, Usuario usuario);
    void deleteByIdAndUsuario(Long id, Usuario usuario);
    boolean existsByCategoriaId(Long id);
}
