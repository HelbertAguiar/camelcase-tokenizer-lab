import java.util.ArrayList;
import java.util.List;

public class CamelCase {

    public static List<String> converterCamelCase(String original) {
        List<String> palavras = new ArrayList<>();
        StringBuilder atual = new StringBuilder();

        for (int i = 0; i < original.length(); i++) {
            char c = original.charAt(i);

            if (i > 0 && Character.isUpperCase(c)) {
                palavras.add(atual.toString());
                atual.setLength(0);
            }

            atual.append(c);
        }

        palavras.add(atual.toString().substring(0, 1).toLowerCase()
                + atual.toString().substring(1));

        return palavras;
    }
}