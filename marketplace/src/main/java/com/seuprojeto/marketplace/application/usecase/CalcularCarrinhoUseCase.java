package com.seuprojeto.marketplace.application.usecase;

import com.seuprojeto.marketplace.application.dto.SelecaoCarrinho;
import com.seuprojeto.marketplace.domain.model.CategoriaProduto;
import com.seuprojeto.marketplace.domain.model.Produto;
import com.seuprojeto.marketplace.domain.model.ResumoCarrinho;
import com.seuprojeto.marketplace.domain.repository.ProdutoRepositorio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class CalcularCarrinhoUseCase {

    private final ProdutoRepositorio produtoRepositorio;

    public CalcularCarrinhoUseCase(ProdutoRepositorio produtoRepositorio) {
        this.produtoRepositorio = produtoRepositorio;
    }

    public ResumoCarrinho executar(List<SelecaoCarrinho> selecaoCarrinhos) {
        BigDecimal subtotal = BigDecimal.ZERO;
        int totalItens = 0;
        int descontoCategoriaPercent = 0;

        for (SelecaoCarrinho selecao : selecaoCarrinhos) {
            Produto produto = produtoRepositorio.findById(selecao.getIdProduto())
                    .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + selecao.getIdProduto()));

            int quantidade = selecao.getQuantidade();
            subtotal = subtotal.add(produto.getPreco().multiply(new BigDecimal(quantidade)));
            totalItens += quantidade;
            descontoCategoriaPercent += getDescontoCategoria(produto.getCategoriaProduto()) * quantidade;
        }

        int descontoQuantidadePercent = getDescontoQuantidade(totalItens);
        int percentualTotal = Math.min(descontoQuantidadePercent + descontoCategoriaPercent, 25);

        BigDecimal valorDesconto = subtotal.multiply(new BigDecimal(percentualTotal))
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        return new ResumoCarrinho(subtotal, valorDesconto);
    }

    private int getDescontoQuantidade(int totalItens) {
        if (totalItens >= 4) return 10;
        if (totalItens == 3) return 7;
        if (totalItens == 2) return 5;
        return 0;
    }

    private int getDescontoCategoria(CategoriaProduto categoria) {
        switch (categoria) {
            case CAPINHA: return 3;
            case CARREGADOR: return 5;
            case FONE: return 3;
            case PELICULA: return 2;
            case SUPORTE: return 2;
            default: return 0;
        }
    }
}