package camelcase;

import java.util.ArrayList;
import java.util.List;

public class CamelCase {

    public static List<String> converterCamelCase(String original) {
        validarEntrada(original);
        return converterParaListaDePalavras(original);
    }

    private static void validarEntrada(String texto) {
        if (texto.isEmpty()) return;
        if (Character.isDigit(texto.charAt(0)))
            throw new IllegalArgumentException("Entrada invalida: nao deve comecar com numero.");
        validarCaracteres(texto);
    }

    private static void validarCaracteres(String texto) {
        for (int i = 0; i < texto.length(); i++) {
            if (!Character.isLetterOrDigit(texto.charAt(i)))
                throw new IllegalArgumentException("Entrada invalida: use apenas letras e numeros.");
        }
    }

    private static List<String> converterParaListaDePalavras(String texto) {
        List<String> palavras = new ArrayList<>();
        StringBuilder palavraAtual = new StringBuilder();
        for (int i = 0; i < texto.length(); i++) {
            processarCaractere(texto, i, palavras, palavraAtual);
        }
        adicionarPalavra(palavras, palavraAtual);
        return palavras;
    }

    private static void processarCaractere(String texto, int i, List<String> palavras, StringBuilder palavraAtual) {
        if (deveQuebrarPalavra(texto, i)) {
            adicionarPalavra(palavras, palavraAtual);
            palavraAtual.setLength(0);
        }
        palavraAtual.append(texto.charAt(i));
    }

    private static boolean deveQuebrarPalavra(String texto, int indiceAtual) {
        if (indiceAtual == 0) return false;
        char atual = texto.charAt(indiceAtual);
        char anterior = texto.charAt(indiceAtual - 1);
        if (ehFronteiraNumerica(atual, anterior)) return true;
        if (!Character.isUpperCase(atual)) return false;
        return ehFronteiraUpperCase(texto, indiceAtual, anterior);
    }

    private static boolean ehFronteiraNumerica(char atual, char anterior) {
        return (Character.isDigit(atual) && Character.isLetter(anterior))
                || (Character.isLetter(atual) && Character.isDigit(anterior));
    }

    private static boolean ehFronteiraUpperCase(String texto, int indice, char anterior) {
        if (Character.isLowerCase(anterior)) return true;
        return existeProximo(texto, indice)
                && Character.isUpperCase(anterior)
                && Character.isLowerCase(texto.charAt(indice + 1));
    }

    private static boolean existeProximo(String texto, int indiceAtual) {
        return indiceAtual + 1 < texto.length();
    }

    private static void adicionarPalavra(List<String> palavras, StringBuilder palavraAtual) {
        if (!palavraAtual.isEmpty())
            palavras.add(normalizarPalavra(palavraAtual.toString()));
    }

    private static String normalizarPalavra(String palavra) {
        if (ehSigla(palavra)) return palavra;
        return palavra.substring(0, 1).toLowerCase() + palavra.substring(1);
    }

    private static boolean ehSigla(String palavra) {
        return palavra.length() > 1 && palavra.equals(palavra.toUpperCase());
    }
}