package com.saldium.saldium.repository;

import com.saldium.saldium.entidades.Categoria;
import com.saldium.saldium.entidades.TipoTransacao;
import com.saldium.saldium.security.user.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    boolean existsByNome(String nome);

    @Query("""
    SELECT c
    FROM Categoria c
    WHERE c.categoriaDoSistema = true
       OR c.usuario = :usuario
""")
    List<Categoria> findAllAvailableForUser(
            @Param("usuario") Usuario usuario
    );

    @Query("""
    SELECT c
    FROM Categoria c
    WHERE c.id = :id
    AND (
            c.categoriaDoSistema = true
            OR c.usuario = :usuario
        )
""")
    Optional<Categoria> findAccessibleById(
            @Param("id") Long id,
            @Param("usuario") Usuario usuario
    );

    Optional<Categoria> findByIdAndUsuario(Long id, Usuario usuario);

    Optional<Categoria> findByNome(String nome);

    @Query("""
    SELECT c
    FROM Categoria c
    WHERE (
            c.categoriaDoSistema = true
            OR c.usuario = :usuario
          )
      AND c.tipo = :tipo
""")
    List<Categoria> findAllAvailableForUserAndByTipo(
            @Param("usuario") Usuario usuario,
            @Param("tipo") TipoTransacao tipo
    );

    @Query("""
    SELECT c
    FROM Categoria c
    WHERE c.tipo = :tipo
""")
    List<Categoria> findAllByTipoTransacao(@Param("tipo") TipoTransacao tipo);

    @Modifying
    @Transactional
    @Query("""
    DELETE 
    FROM Categoria c
    WHERE c.usuario.id = :usuarioId
""")
    void deleteAllFromUsuario(@Param("usuarioId") Long usuarioId);
}
