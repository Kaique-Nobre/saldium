package com.saldium.saldium.mapper;

import com.saldium.saldium.dto.transacao.TransacaoRequestDTO;
import com.saldium.saldium.dto.transacao.TransacaoResponseDTO;
import com.saldium.saldium.entidades.Categoria;
import com.saldium.saldium.entidades.Transacao;
import com.saldium.saldium.security.user.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransacaoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    Transacao toEntity(TransacaoRequestDTO request);

    @Mapping(target = "usuario", source = "usuario")
    @Mapping(target = "categoria", source = "categoria")
    TransacaoResponseDTO toResponseDTO(Transacao transacao);

    default String map(Usuario usuario) {
        return usuario.getUsername();
    }

    default String map(Categoria categoria) {
        return categoria.getNome();
    }
}
