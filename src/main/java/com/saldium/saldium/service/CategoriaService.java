package com.saldium.saldium.service;

import com.saldium.saldium.dto.categoria.CategoriaRequestDTO;
import com.saldium.saldium.dto.categoria.CategoriaResponseDTO;
import com.saldium.saldium.entidades.Categoria;
import com.saldium.saldium.exceptions.categoria.CategoriaEmUsoException;
import com.saldium.saldium.exceptions.categoria.CategoriaJaExisteException;
import com.saldium.saldium.exceptions.categoria.CategoriaNaoEncontradaException;
import com.saldium.saldium.mapper.CategoriaMapper;
import com.saldium.saldium.repository.CategoriaRepository;
import com.saldium.saldium.repository.TransacaoRepository;
import com.saldium.saldium.security.user.Role;
import com.saldium.saldium.security.user.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoriaService {
    private final CategoriaRepository categoriaRepository;
    private final TransacaoRepository transacaoRepository;
    private final CategoriaMapper categoriaMapper;

    @Transactional(rollbackFor = Exception.class)
    public CategoriaResponseDTO save(CategoriaRequestDTO request) {
        Usuario usuario = getUsuarioAutenticado();

        String requestName = request.nome().toUpperCase();
        boolean existByNome = categoriaRepository.existsByNome(requestName);
        Optional<Categoria> categoriaByNome = categoriaRepository.findByNome(requestName);

        if(existByNome && categoriaByNome.get().getUsuario().getId() == usuario.getId() || existByNome && categoriaByNome.get().isCategoriaDoSistema()) {
            throw new CategoriaJaExisteException("A categoria "+ requestName +" já existe");
        }
        Categoria categoriaUpperCase = new Categoria();
        categoriaUpperCase.setNome(requestName);
        categoriaUpperCase.setTipo(request.tipo());
        categoriaUpperCase.setUsuario(usuario);

        if(usuario.getRole() == Role.ROLE_ADMIN) {
            categoriaUpperCase.setCategoriaDoSistema(true);
        }else {
            categoriaUpperCase.setCategoriaDoSistema(false);
        }

        Categoria categoria = categoriaRepository.save(categoriaUpperCase);

        return categoriaMapper.toDTO(categoria);
    }

    public List<CategoriaResponseDTO> findAll() {
        Usuario usuario = getUsuarioAutenticado();
        List<Categoria> categorias;
        if(usuario.getRole() == Role.ROLE_ADMIN) {
            categorias = categoriaRepository.findAll();
        }else {
            categorias = categoriaRepository.findAllAvailableForUser(usuario);
        }

        return categorias.stream().map(categoriaMapper::toDTO).toList();
    }

    public CategoriaResponseDTO findById(Long id) {
        Usuario usuario = getUsuarioAutenticado();

        Categoria categoria;

        if(usuario.getRole() == Role.ROLE_ADMIN) {
            categoria = categoriaRepository.findById(id)
                    .orElseThrow(
                            () -> new CategoriaNaoEncontradaException("Categoria não encontrada com ID: " + id));
        }else {
            categoria = categoriaRepository.findAccessibleById(id, usuario)
                    .orElseThrow(
                            () -> new CategoriaNaoEncontradaException("Categoria não encontrada com ID: " + id));
        }

        return categoriaMapper.toDTO(categoria);
    }

    @Transactional(rollbackFor = Exception.class)
    public CategoriaResponseDTO update(Long id, CategoriaRequestDTO request) {
        Usuario usuario = getUsuarioAutenticado();

        Categoria categoria;

        if(usuario.getRole() == Role.ROLE_ADMIN) {
            categoria = categoriaRepository.findById(id)
                    .orElseThrow(
                            () -> new CategoriaNaoEncontradaException("Categoria não encontrada com ID: " + id));
        }else {
            categoria = categoriaRepository.findByIdAndUsuario(id, usuario)
                    .orElseThrow(
                            () -> new CategoriaNaoEncontradaException("Categoria não encontrada com ID: " + id));
            if (transacaoRepository.existsByCategoriaId(categoria.getId())) {
                throw new CategoriaEmUsoException(
                        "Não é possível alterar uma categoria que possui transações associadas, exclua as transações que possuem essa categoria para poder alterá-la");
            }
        }

        categoria.setNome(request.nome().toUpperCase());
        categoria.setTipo(request.tipo());

        Categoria categoriaAtualizada = categoriaRepository.save(categoria);

        return categoriaMapper.toDTO(categoriaAtualizada);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Usuario usuario = getUsuarioAutenticado();

        Categoria categoria;

        if(usuario.getRole() == Role.ROLE_ADMIN) {
            categoria = categoriaRepository.findById(id)
                    .orElseThrow(
                            () -> new CategoriaNaoEncontradaException("Categoria não encontrada com ID: " + id));
        }else {
            categoria = categoriaRepository.findByIdAndUsuario(id, usuario)
                    .orElseThrow(
                            () -> new CategoriaNaoEncontradaException("Categoria não encontrada com ID: " + id));
            if (transacaoRepository.existsByCategoriaId(categoria.getId())) {
                throw new CategoriaEmUsoException(
                        "Não é possível excluir uma categoria que possui transações associadas, exclua as transações que possuem essa categoria para poder deleta-la");
            }
        }
        categoriaRepository.delete(categoria);
    }

    private Usuario getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return (Usuario) authentication.getPrincipal();
    }
}
