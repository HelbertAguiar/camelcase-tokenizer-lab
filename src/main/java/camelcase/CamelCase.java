package camelcase;

import java.util.ArrayList;
import java.util.List;

public class CamelCase {

    public static List<String> converterCamelCase(String original) {
        return converterParaListaDePalavras(original);
    }

    private static List<String> converterParaListaDePalavras(String texto) {
        List<String> palavras = new ArrayList<>();
        StringBuilder palavraAtual = new StringBuilder();

        for (int i = 0; i < texto.length(); i++) {
            char caractereAtual = texto.charAt(i);
            if (deveQuebrarPalavra(texto, i)) {
                adicionarPalavra(palavras, palavraAtual);
                palavraAtual.setLength(0);
            }
            palavraAtual.append(caractereAtual);
        }

        adicionarPalavra(palavras, palavraAtual);

        return palavras;
    }

    private static boolean deveQuebrarPalavra(String texto, int indiceAtual) {
        if (indiceAtual == 0) {
            return false;
        }

        char atual = texto.charAt(indiceAtual);
        char anterior = texto.charAt(indiceAtual - 1);

        if (Character.isDigit(atual) && Character.isLetter(anterior)) {
            return true;
        }

        if (Character.isLetter(atual) && Character.isDigit(anterior)) {
            return true;
        }

        if (!Character.isUpperCase(atual)) {
            return false;
        }

        if (Character.isLowerCase(anterior)) {
            return true;
        }

        return existeProximo(texto, indiceAtual)
                && Character.isUpperCase(anterior)
                && Character.isLowerCase(texto.charAt(indiceAtual + 1));
    }

    private static boolean existeProximo(String texto, int indiceAtual) {
        return indiceAtual + 1 < texto.length();
    }

    private static void adicionarPalavra(List<String> palavras, StringBuilder palavraAtual) {
        if (!palavraAtual.isEmpty()) {
            palavras.add(normalizarPalavra(palavraAtual.toString()));
        }
    }

    private static String normalizarPalavra(String palavra) {
        if (ehSigla(palavra)) {
            return palavra;
        }

        return palavra.substring(0, 1).toLowerCase() + palavra.substring(1);
    }

    private static boolean ehSigla(String palavra) {
        return palavra.length() > 1 && palavra.equals(palavra.toUpperCase());
    }
}