package com.saldium.saldium.util.categorias;

import com.saldium.saldium.dto.CategoriaRequestDTO;
import com.saldium.saldium.dto.CategoriaResponseDTO;
import com.saldium.saldium.entidades.Categoria;
import com.saldium.saldium.entidades.TipoCategoria;

public class CategoriasCreator {
    public static CategoriaResponseDTO criarCategoriaResponse() {
        return new CategoriaResponseDTO(1L, "SALÁRIO", "RENDA");
    }

    public static CategoriaRequestDTO criarCategoriaRequest() {
        return new CategoriaRequestDTO("salário", TipoCategoria.RENDA);
    }

    public static Categoria criarCategoria() {
        return new Categoria(1L, "SALÁRIO", TipoCategoria.RENDA);
    }
}
