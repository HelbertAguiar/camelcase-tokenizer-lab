import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import camelcase.CamelCase;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CamelCaseTest {

    @Test
    void deveConverterNomeMinusculo() {
        assertEquals(Arrays.asList("nome"), CamelCase.converterCamelCase("nome"));
    }

    @Test
    void deveConverterPalavraMaiscula() {
        assertEquals(Arrays.asList("nome"), CamelCase.converterCamelCase("Nome"));
    }

    @Test
    void deveConverterNomeCompostoComecandoComMinuscula() {
        assertEquals(
            Arrays.asList("nome", "composto"),
            CamelCase.converterCamelCase("nomeComposto")
        );
    }

//    @Test
//    void mainTeste() {
//        List<String> lista = CamelCase.converterCamelCase("nome");
//        System.out.println(lista);
//    }

}
