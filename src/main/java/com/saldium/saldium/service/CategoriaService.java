package com.saldium.saldium.service;

import com.saldium.saldium.dto.categoria.CategoriaRequestDTO;
import com.saldium.saldium.dto.categoria.CategoriaResponseDTO;
import com.saldium.saldium.entidades.Categoria;
import com.saldium.saldium.exceptions.categoria.CategoriaJaExisteException;
import com.saldium.saldium.exceptions.categoria.CategoriaNaoEncontradaException;
import com.saldium.saldium.mapper.CategoriaMapper;
import com.saldium.saldium.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {
    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    public CategoriaResponseDTO save(CategoriaRequestDTO request) {
        String requestName = request.nome().toUpperCase();
        if(categoriaRepository.existsByNome(requestName)) {
            throw new CategoriaJaExisteException("A categoria "+ requestName +" já existe");
        }

        Categoria categoriaUpperCase = new Categoria();
        categoriaUpperCase.setNome(requestName);
        categoriaUpperCase.setTipo(request.tipo());

        Categoria categoria = categoriaRepository.save(categoriaUpperCase);

        return categoriaMapper.toDTO(categoria);
    }

    public List<CategoriaResponseDTO> findAll() {
        List<Categoria> categorias = categoriaRepository.findAll();

        return categorias.stream().map(categoriaMapper::toDTO).toList();
    }

    public CategoriaResponseDTO findById(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(
                        () -> new CategoriaNaoEncontradaException("Categoria não encontrada com ID: " + id));

        return categoriaMapper.toDTO(categoria);
    }

    public CategoriaResponseDTO update(Long id, CategoriaRequestDTO request) {
        Categoria categoria = categoriaRepository.findById(id).orElseThrow(
                () -> new CategoriaNaoEncontradaException("Você não pode atualizar uma categoria que não existe"));

        categoria.setNome(request.nome().toUpperCase());
        categoria.setTipo(request.tipo());

        Categoria categoriaAtualizada = categoriaRepository.save(categoria);

        return categoriaMapper.toDTO(categoriaAtualizada);
    }

    public void delete(Long id) {
        categoriaRepository.findById(id).orElseThrow(
                () -> new CategoriaNaoEncontradaException("Você não pode deletar uma categoria que não existe"));
        categoriaRepository.deleteById(id);
    }
}
