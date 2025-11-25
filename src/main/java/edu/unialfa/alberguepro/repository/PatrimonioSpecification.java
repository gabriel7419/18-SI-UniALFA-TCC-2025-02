package edu.unialfa.alberguepro.repository;

import edu.unialfa.alberguepro.model.ControlePatrimonio;
import org.springframework.data.jpa.domain.Specification;

public class PatrimonioSpecification {

    public static Specification<ControlePatrimonio> comNome(String nome) {
        return (root, query, criteriaBuilder) -> {
            if (nome == null || nome.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("nome")), "%" + nome.toLowerCase() + "%");
        };
    }

    public static Specification<ControlePatrimonio> comStatus(String status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null || status.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<ControlePatrimonio> comLocalAtual(String localAtual) {
        return (root, query, criteriaBuilder) -> {
            if (localAtual == null || localAtual.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("localAtual")), "%" + localAtual.toLowerCase() + "%");
        };
    }
}
