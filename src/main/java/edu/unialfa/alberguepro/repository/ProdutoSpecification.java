package edu.unialfa.alberguepro.repository;

import edu.unialfa.alberguepro.model.Produto;
import edu.unialfa.alberguepro.model.Unidade;
import org.springframework.data.jpa.domain.Specification;

public class ProdutoSpecification {

    public static Specification<Produto> comNome(String nome) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("nome")), "%" + nome.toLowerCase() + "%");
    }

    public static Specification<Produto> comTipo(String tipo) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("tipo"), tipo);
    }

    public static Specification<Produto> comUnidade(Unidade unidade) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("unidade"), unidade);
    }
}
