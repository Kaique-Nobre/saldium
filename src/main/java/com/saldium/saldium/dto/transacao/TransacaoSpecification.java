package com.saldium.saldium.dto.transacao;

import com.saldium.saldium.entidades.Transacao;
import com.saldium.saldium.security.user.Role;
import com.saldium.saldium.security.user.Usuario;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;


public class TransacaoSpecification {

    public static Specification<Transacao> comFiltros(TransacaoFiltroDTO filtro, Usuario usuario) {

        return (root, query, cb) -> {

            List<Predicate> predicates =
                    new ArrayList<>();

            if(usuario.getRole() != Role.ROLE_ADMIN) {
                predicates.add(
                        cb.equal(
                                root.get("usuario"),
                                usuario
                        )
                );
            }

            if(filtro.tipo() != null) {
                predicates.add(
                        cb.equal(
                                root.get("tipoTransacao"),
                                filtro.tipo()
                        )
                );
            }

            if(filtro.categoriaId() != null) {
                predicates.add(
                        cb.equal(
                                root.get("categoria").get("id"),
                                filtro.categoriaId()
                        )
                );
            }

            if (filtro.dataInicial() != null && filtro.dataFinal() != null) {

                LocalDate inicio = filtro.dataInicial();
                LocalDate fim = filtro.dataFinal();

                predicates.add(
                        cb.between(
                                root.get("dataTransacao"),
                                inicio,
                                fim
                        )
                );
            }

            return cb.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}
