package edu.unialfa.alberguepro.repository;

import edu.unialfa.alberguepro.model.MovimentacaoEstoque;
import org.springframework.data.jpa.domain.Specification;

public class MovimentacaoEstoqueSpecification {

    public static Specification<MovimentacaoEstoque> comTipo(MovimentacaoEstoque.TipoMovimentacao tipo) {
        return (root, query, criteriaBuilder) -> {
            if (tipo == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("tipo"), tipo);
        };
    }

    public static Specification<MovimentacaoEstoque> comProdutoNome(String nomeProduto) {
        return (root, query, criteriaBuilder) -> {
            if (nomeProduto == null || nomeProduto.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("produto").get("nome")), 
                "%" + nomeProduto.toLowerCase() + "%"
            );
        };
    }
}
