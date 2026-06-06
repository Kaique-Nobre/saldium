package com.saldium.saldium.service;

import com.saldium.saldium.dto.transacao.TransacaoRequestDTO;
import com.saldium.saldium.dto.transacao.TransacaoResponseDTO;
import com.saldium.saldium.entidades.Categoria;
import com.saldium.saldium.entidades.Transacao;
import com.saldium.saldium.exceptions.categoria.CategoriaIncompativelException;
import com.saldium.saldium.exceptions.categoria.CategoriaNaoEncontradaException;
import com.saldium.saldium.exceptions.transacao.TransacaoNaoEncontradaException;
import com.saldium.saldium.mapper.TransacaoMapper;
import com.saldium.saldium.repository.CategoriaRepository;
import com.saldium.saldium.repository.TransacaoRepository;
import com.saldium.saldium.security.user.Role;
import com.saldium.saldium.security.user.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransacaoService {
    private final TransacaoRepository transacaoRepository;
    private final CategoriaRepository categoriaRepository;
    private final TransacaoMapper transacaoMapper;

    public TransacaoResponseDTO save(TransacaoRequestDTO request) {
        Usuario usuario = getUsuarioAutenticado();

        Categoria categoria = getCategoriaUsuarioESistema(request, usuario);

        validarCategoria(request, categoria);

        Transacao transacao = new Transacao();
        transacao.setDescricao(request.descricao());
        transacao.setValor(request.valor());
        transacao.setTipoTransacao(request.tipoTransacao());
        transacao.setCategoria(categoria);
        transacao.setUsuario(usuario);
        transacao.setDataCriacao(OffsetDateTime.now());

        Transacao transacaoSalva = transacaoRepository.save(transacao);

        return  transacaoMapper.toResponseDTO(transacaoSalva);
    }

    public List<TransacaoResponseDTO> findAll() {
        Usuario usuario = getUsuarioAutenticado();

        List<Transacao> transacoes;

        if(usuario.getRole() ==  Role.ROLE_ADMIN) {
            transacoes = transacaoRepository.findAll();
        }else {
            transacoes = transacaoRepository.findAllByUsuario(usuario);
        }
        return transacaoMapper.toResponseList(transacoes);
    }

    public TransacaoResponseDTO findById(Long id) {
        Usuario usuario = getUsuarioAutenticado();

        Transacao transacao = PegarTransacaoPorRole(id, usuario);

        return transacaoMapper.toResponseDTO(transacao);
    }

    public TransacaoResponseDTO update(Long id, TransacaoRequestDTO request) {
        Usuario usuario = getUsuarioAutenticado();

        Transacao transacao = PegarTransacaoPorRole(id, usuario);

        Categoria categoria = getCategoriaUsuario(request, usuario);

        validarCategoria(request, categoria);

        transacao.setDescricao(request.descricao());
        transacao.setValor(request.valor());
        transacao.setTipoTransacao(request.tipoTransacao());
        transacao.setCategoria(categoria);

        Transacao transacaoSalva = transacaoRepository.save(transacao);

        return transacaoMapper.toResponseDTO(transacaoSalva);
    }

    public void delete(Long id) {
        Usuario usuario = getUsuarioAutenticado();
        Transacao transacao = PegarTransacaoPorRole(id, usuario);
        transacaoRepository.delete(transacao);
    }

    private Transacao PegarTransacaoPorRole(Long id, Usuario usuario) {
        Transacao transacao;

        if(usuario.getRole() ==  Role.ROLE_ADMIN) {
            transacao = transacaoRepository
                    .findById(id).orElseThrow(
                            () -> new TransacaoNaoEncontradaException("Transação não encontrada com ID: " + id));
        }else {
            transacao = transacaoRepository
                    .findByIdAndUsuario(id, usuario).orElseThrow(
                            () -> new TransacaoNaoEncontradaException("Transação não encontrada com ID: " + id));
        }
        return transacao;
    }

    private Categoria getCategoriaUsuarioESistema(TransacaoRequestDTO request, Usuario usuario) {
        Categoria categoria;
        if (usuario.getRole() ==  Role.ROLE_ADMIN) {
            categoria = categoriaRepository
                    .findById(request.categoria_id()).orElseThrow(
                            () -> new CategoriaNaoEncontradaException("Categoria não encontrada com ID: " + request.categoria_id()));
        }else {
            categoria = categoriaRepository
                    .findAccessibleById(request.categoria_id(), usuario).orElseThrow(
                            () -> new CategoriaNaoEncontradaException("Categoria não encontrada com ID: " + request.categoria_id()));
        }
        return categoria;
    }

    private Categoria getCategoriaUsuario(TransacaoRequestDTO request, Usuario usuario) {
        Categoria categoria;
        if (usuario.getRole() ==  Role.ROLE_ADMIN) {
            categoria = categoriaRepository
                    .findById(request.categoria_id()).orElseThrow(
                            () -> new CategoriaNaoEncontradaException("Categoria não encontrada com ID: " + request.categoria_id()));
        }else {
            categoria = categoriaRepository
                    .findByIdAndUsuario(request.categoria_id(), usuario).orElseThrow(
                            () -> new CategoriaNaoEncontradaException("Categoria não encontrada com ID: " + request.categoria_id()));
        }
        return categoria;
    }

    private static void validarCategoria(TransacaoRequestDTO request, Categoria categoria) {
        if (!categoria.getTipo().equals(request.tipoTransacao())) {
            throw new CategoriaIncompativelException("O tipo da categoria selecionada não é compatível com essa transação");
        }
    }

    private Usuario getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return (Usuario) authentication.getPrincipal();
    }
}
