import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import camelcase.CamelCase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    void deveConferirPalavrasCompostaComSigla(){
        assertEquals(
                Arrays.asList("nome", "CPF", "composto"),
                CamelCase.converterCamelCase("nomeCPFComposto")
        );
    }

    @Test
    void deveConferirPalavrarComNumeros() {
        assertEquals(
                Arrays.asList("nome", "CPF", "composto", "2024", "fim"),
                CamelCase.converterCamelCase("nomeCPFComposto2024Fim")
        );
    }

    @Test
    void deveRejeitarTextoQueComecaComNumero() {
        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> CamelCase.converterCamelCase("10Primeiros")
        );

        assertEquals("Entrada invalida: nao deve comecar com numero.", excecao.getMessage());
    }

    @Test
    void deveRejeitarTextoComCaracterEspecial() {
        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> CamelCase.converterCamelCase("nome#Composto")
        );

        assertEquals("Entrada invalida: use apenas letras e numeros.", excecao.getMessage());
    }

//    @Test
//    void mainTeste() {
//        List<String> lista = CamelCase.converterCamelCase("nome");
//        System.out.println(lista);
//    }

}
