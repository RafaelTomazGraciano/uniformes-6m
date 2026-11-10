package com.six_m.uniform.domain.aluno;

import com.six_m.uniform.domain.turma.Turma;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;

/** Filtros opcionais da listagem de alunos; cada um é ignorado quando vem nulo. */
public final class AlunoSpecifications {

    private static final String COM_ACENTO = "áàâãäéèêëíìîïóòôõöúùûüçñ";
    private static final String SEM_ACENTO = "aaaaaeeeeiiiiooooouuuucn";

    private AlunoSpecifications() {
    }

    /* A busca casa o nome do aluno ou o nome da turma ("Joana", "6B"), sem
       diferenciar maiúsculas nem acentos — é assim que a secretaria procura. */
    public static Specification<Aluno> buscaPorNomeOuTurma(String termo) {
        String normalizado = normalizar(termo);
        if (normalizado.isEmpty()) {
            return (root, query, cb) -> null;
        }
        String padrao = "%" + normalizado.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";

        return (root, query, cb) -> {
            Join<Aluno, Turma> turma = root.join("turma", JoinType.INNER);
            return cb.or(
                    cb.like(semAcento(cb, root.get("nome")), padrao, '\\'),
                    cb.like(semAcento(cb, turma.get("nome")), padrao, '\\')
            );
        };
    }

    public static Specification<Aluno> daTurma(UUID turmaId) {
        return (root, query, cb) -> turmaId == null ? null : cb.equal(root.get("turma").get("id"), turmaId);
    }

    public static Specification<Aluno> comSituacao(Boolean ativo) {
        return (root, query, cb) -> ativo == null ? null : cb.equal(root.get("ativo"), ativo);
    }

    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    private static Expression<String> semAcento(CriteriaBuilder cb, Expression<String> campo) {
        return cb.function("translate", String.class, cb.lower(campo), cb.literal(COM_ACENTO), cb.literal(SEM_ACENTO));
    }
}
