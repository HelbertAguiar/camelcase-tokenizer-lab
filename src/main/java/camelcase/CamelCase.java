package camelcase;

import java.util.ArrayList;
import java.util.List;

public class CamelCase {

    public static List<String> converterCamelCase(String original) {
        return converterParaListaDePalavras(original);
    }

    public static StringBuilder converterPrimeiraLetraParaMinuscula(StringBuilder str) {
        return new StringBuilder(
            (str.substring(0,1).toLowerCase() + str.substring(1))
        );
    }

    public static List<String> converterParaListaDePalavras(String str) {
        List<String> listaStrings = new ArrayList<>();
        StringBuilder palavra = new StringBuilder();

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (i > 0 && Character.isUpperCase(c)) {
                listaStrings.add(converterPrimeiraLetraParaMinuscula(palavra).toString());
                palavra.setLength(0);
            }
            palavra.append(c);
        }

        if (!palavra.isEmpty()) {
            listaStrings.add(converterPrimeiraLetraParaMinuscula(palavra).toString());
        }

        return listaStrings;
    }

}

