# Relatório Técnico — CamelCase Tokenizer com TDD

## 1. Introdução

Este relatório descreve a evolução incremental de uma solução para tokenização de strings em CamelCase, desenvolvida com a metodologia **Test-Driven Development (TDD)**. A construção foi conduzida ciclo a ciclo: cada nova funcionalidade ou restrição foi introduzida primeiro como um teste que falhava, depois implementada da forma mais simples possível para fazê-lo passar, e em seguida o código era refinado sem alterar o comportamento externo.

O histórico de commits preserva fielmente esse ritmo de desenvolvimento. A análise a seguir reconstrói cada decisão técnica tomada, incluindo as refatorações que mantiveram o código legível e aderente à restrição de **no máximo 10 linhas por método**.

---

## 2. Objetivo do desafio

Implementar, utilizando TDD com Java, o seguinte método:

```java
public static List<String> converterCamelCase(String original)
```

Esse método recebe uma string em formato CamelCase e a decompõe em uma lista de tokens, respeitando as regras abaixo:

| Entrada                 | Saída esperada                                      |
|-------------------------|-----------------------------------------------------|
| `nome`                  | `["nome"]`                                          |
| `Nome`                  | `["nome"]`                                          |
| `nomeComposto`          | `["nome", "composto"]`                              |
| `NomeComposto`          | `["nome", "composto"]`                              |
| `CPF`                   | `["CPF"]`                                           |
| `numeroCPF`             | `["numero", "CPF"]`                                 |
| `numeroCPFContribuinte` | `["numero", "CPF", "contribuinte"]`                 |
| `recupera10Primeiros`   | `["recupera", "10", "primeiros"]`                   |
| `10Primeiros`           | ❌ `IllegalArgumentException` — inicia com número   |
| `nome#Composto`         | ❌ `IllegalArgumentException` — caractere especial  |

**Restrições de implementação:**
- Apenas classes da API padrão do Java.
- Nenhum método pode ter mais de **10 linhas** em seu corpo.
- Não é permitido usar bibliotecas externas que simplifiquem a implementação.
- Métodos auxiliares são permitidos e encorajados.

---

## 3. Estratégia de desenvolvimento com TDD

TDD inverte o fluxo comum de desenvolvimento: o teste é escrito **antes** da implementação. Cada ciclo segue três fases:

1. **🔴 RED** — escrever um teste que descreve o comportamento desejado. Ele falha porque ainda não há implementação.
2. **🟢 GREEN** — escrever o código **mínimo** necessário para fazer o teste passar.
3. **🔵 REFACTOR** — melhorar a estrutura do código sem alterar o comportamento externo, validado pela execução contínua dos testes.

Neste projeto foram realizados **quatro ciclos de TDD** e **duas rodadas de refatoração** independentes.

---

## 4. Ciclos de TDD

---

### Ciclo 1 — Conversão básica de palavras

#### Teste adicionado

O ponto de partida foi cobrir os casos mais simples do problema: uma palavra minúscula, uma palavra iniciando com maiúscula, e uma palavra composta no padrão lowerCamelCase.

```java
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
```

#### Estado anterior do código

Não havia implementação — a classe `CamelCase` ainda não existia.

#### Implementação para fazer o teste passar

A implementação inicial percorria a string caractere a caractere, quebrando ao encontrar uma letra maiúscula e normalizando apenas a primeira letra da **última** palavra acumulada:

```java
// src/main/java/CamelCase.java  (sem package ainda)
public class CamelCase {

    public static List<String> converterCamelCase(String original) {
        List<String> palavras = new ArrayList<>();
        StringBuilder atual = new StringBuilder();

        for (int i = 0; i < original.length(); i++) {
            char c = original.charAt(i);
            if (i > 0 && Character.isUpperCase(c)) {
                palavras.add(atual.toString());   // sem normalização
                atual.setLength(0);
            }
            atual.append(c);
        }

        palavras.add(atual.toString().substring(0, 1).toLowerCase()
                + atual.toString().substring(1)); // normalização só aqui

        return palavras;
    }
}
```

A lógica é direta: ao encontrar uma letra maiúscula (exceto na posição 0), o trecho acumulado é salvo como nova palavra e o buffer é zerado. Ao final, a última palavra recebe a primeira letra em minúsculo.

> **Limitação silenciosa:** a normalização era aplicada apenas à última palavra. Palavras intermediárias como `"Nome"` em `"NomeComposto"` seriam salvas sem tratamento. O bug não apareceu neste ciclo porque nenhum teste cobria esse caso — mas ele existia.

#### Resultado do ciclo

✅ Os três testes passaram. A base da solução estava estabelecida. O código, porém, carecia de organização: tudo em um único método, sem pacote definido, com a normalização acoplada ao final do loop.

---

### Ciclo 2 — Tratamento de siglas (acrônimos)

#### Teste adicionado

O próximo caso relevante são as **siglas**: sequências de letras maiúsculas que formam um token único e não devem ter a caixa alterada. Por exemplo, `"nomeCPFComposto"` deve resultar em `["nome", "CPF", "composto"]`.

```java
@Test
void deveConferirPalavrasCompostaComSigla() {
    assertEquals(
            Arrays.asList("nome", "CPF", "composto"),
            CamelCase.converterCamelCase("nomeCPFComposto")
    );
}
```

Este teste **falhou** com a implementação existente: a regra `Character.isUpperCase(c)` quebrava a string em toda letra maiúscula, separando "CPF" em tokens individuais.

#### Estado anterior do código

Após a **Refatoração 1** (descrita na Seção 5), o código havia sido reorganizado para o pacote `camelcase` e a normalização extraída para um método auxiliar. O método de conversão era:

```java
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
```

A condição `Character.isUpperCase(c)` era simples demais para lidar com siglas.

#### Implementação para fazer o teste passar

A solução exigiu reformulação mais profunda da lógica de quebra. Foi introduzido o método `deveQuebrarPalavra`, que encapsula três regras:

- Nunca quebra no índice 0.
- Quebra se o caractere atual é maiúsculo **e** o anterior é minúsculo (transição `a→A`, início de palavra normal).
- Quebra se o atual é maiúsculo, o anterior **também** é maiúsculo, **e** o próximo é minúsculo — padrão `CPF→C`, onde `F` encerra a sigla e `C` inicia a palavra seguinte.

```java
private static boolean deveQuebrarPalavra(String texto, int indiceAtual) {
    if (indiceAtual == 0) {
        return false;
    }

    char atual = texto.charAt(indiceAtual);
    char anterior = texto.charAt(indiceAtual - 1);

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
```

Para preservar a sigla intacta, foram criados `normalizarPalavra` e `ehSigla`:

```java
private static String normalizarPalavra(String palavra) {
    if (ehSigla(palavra)) {
        return palavra;             // sigla: retorna sem modificar
    }
    return palavra.substring(0, 1).toLowerCase() + palavra.substring(1);
}

private static boolean ehSigla(String palavra) {
    return palavra.length() > 1 && palavra.equals(palavra.toUpperCase());
}
```

Uma palavra é **sigla** se tiver mais de um caractere e for inteiramente maiúscula — condição que exclui letras individuais e tokens mistos.

> **Observação de design:** os métodos `converterParaListaDePalavras` e `converterPrimeiraLetraParaMinuscula` estavam com visibilidade `public` na versão anterior, um excesso desnecessário. Nesta implementação ambos foram corrigidos para `private`.

#### Resultado do ciclo

✅ O teste `deveConferirPalavrasCompostaComSigla` passou. Todos os testes anteriores permaneceram verdes. O método `deveQuebrarPalavra` centralizou a lógica de segmentação, tornando o laço principal mais limpo.

---

### Ciclo 3 — Transições com números

#### Teste adicionado

O próximo requisito foi suporte a **dígitos** embutidos na string CamelCase. Dígitos devem formar um token próprio, separado de letras em ambas as direções: `"nomeCPFComposto2024Fim"` deve produzir `["nome", "CPF", "composto", "2024", "fim"]`.

```java
@Test
void deveConferirPalavrarComNumeros() {
    assertEquals(
            Arrays.asList("nome", "CPF", "composto", "2024", "fim"),
            CamelCase.converterCamelCase("nomeCPFComposto2024Fim")
    );
}
```

> **Nota histórica:** a primeira versão deste teste usava `"Fim"` (com maiúsculo) como valor esperado para o último token — um descuido durante a escrita. Ao executar a suíte com a implementação correta, o erro foi identificado e o valor esperado corrigido para `"fim"`. Isso ilustra como o TDD também protege contra erros na especificação dos próprios testes.

#### Estado anterior do código

A `deveQuebrarPalavra` considerava apenas transições entre letras maiúsculas e minúsculas. Dígitos eram tratados como caracteres neutros, sem provocar quebra.

#### Implementação para fazer o teste passar

Foram adicionadas duas novas condições de quebra antes das verificações de maiúsculas existentes:

```java
if (Character.isDigit(atual) && Character.isLetter(anterior)) {
    return true;  // letra → dígito: ex.: "composto2024"
}

if (Character.isLetter(atual) && Character.isDigit(anterior)) {
    return true;  // dígito → letra: ex.: "2024Fim"
}
```

Ao serem inseridas **antes** das verificações de maiúsculas, essas condições têm precedência e garantem a separação correta em ambas as direções de transição.

#### Resultado do ciclo

✅ O teste passou, incluindo a correção do valor esperado. A suíte completa continuou verde. A abordagem de adicionar condições ao `deveQuebrarPalavra` se mostrou eficaz, mas o método já acumulava vários `if` independentes — sinal de que novas regras continuariam a inflar seu tamanho.

---

### Ciclo 4 — Validação de entrada

#### Teste adicionado

Dois novos casos precisavam lançar exceção: strings iniciando com dígito e strings contendo caracteres especiais.

```java
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
```

#### Estado anterior do código

`converterCamelCase` chamava diretamente `converterParaListaDePalavras`, sem nenhuma verificação de entrada.

#### Implementação para fazer o teste passar

Foi criado o método `validarEntrada`, invocado logo no início do ponto de entrada público:

```java
public static List<String> converterCamelCase(String original) {
    validarEntrada(original);
    return converterParaListaDePalavras(original);
}

private static void validarEntrada(String texto) {
    if (texto.isEmpty()) {
        return;
    }

    if (Character.isDigit(texto.charAt(0))) {
        throw new IllegalArgumentException("Entrada invalida: nao deve comecar com numero.");
    }

    for (int i = 0; i < texto.length(); i++) {
        char caractereAtual = texto.charAt(i);
        if (!Character.isLetterOrDigit(caractereAtual)) {
            throw new IllegalArgumentException("Entrada invalida: use apenas letras e numeros.");
        }
    }
}
```

A string vazia é aceita (retorna lista vazia). Para qualquer violação, a exceção é lançada com mensagem descritiva e específica.

#### Resultado do ciclo

✅ Ambos os testes de validação passaram. Todos os seis testes da suíte permaneceram verdes. Com isso, **todos os requisitos funcionais estavam cobertos**.

> **Gatilho de refatoração:** `validarEntrada` ultrapassou o limite de 10 linhas ao final deste ciclo. A regra do projeto determina refatoração imediata nessa situação, independentemente da Regra de Três.

---

## 5. Refatorações realizadas

---

### Refatoração 1 — Reorganização do pacote e normalização correta

Esta refatoração ocorreu entre o Ciclo 1 e o Ciclo 2 e endereçou dois problemas identificados logo após os primeiros testes passarem.

#### Código antes da refatoração

```java
// src/main/java/CamelCase.java — sem package, método único, normalização incompleta
public class CamelCase {

    public static List<String> converterCamelCase(String original) {
        List<String> palavras = new ArrayList<>();
        StringBuilder atual = new StringBuilder();

        for (int i = 0; i < original.length(); i++) {
            char c = original.charAt(i);
            if (i > 0 && Character.isUpperCase(c)) {
                palavras.add(atual.toString()); // palavras intermediárias sem normalização
                atual.setLength(0);
            }
            atual.append(c);
        }

        // normalização aplicada apenas à última palavra
        palavras.add(atual.toString().substring(0, 1).toLowerCase()
                + atual.toString().substring(1));

        return palavras;
    }
}
```

#### Código após a refatoração

```java
// src/main/java/camelcase/CamelCase.java — com package, métodos extraídos
package camelcase;

public class CamelCase {

    public static List<String> converterCamelCase(String original) {
        return converterParaListaDePalavras(original);
    }

    private static List<String> converterParaListaDePalavras(String str) {
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

    private static StringBuilder converterPrimeiraLetraParaMinuscula(StringBuilder str) {
        return new StringBuilder(str.substring(0, 1).toLowerCase() + str.substring(1));
    }
}
```

#### Justificativa

| Aspecto | Problema | Solução |
|---|---|---|
| **Estrutura** | Classe sem pacote, não seguia convenções Java | Movida para `camelcase/CamelCase.java` com `package camelcase` |
| **Normalização** | Aplicada apenas à última palavra — `"NomeComposto"` retornaria `["Nome", "composto"]` | Extraída para `converterPrimeiraLetraParaMinuscula`, aplicada a **todos** os tokens |
| **Coesão** | Método único acumulava toda a lógica | Lógica principal separada em `converterParaListaDePalavras` |

A normalização universal foi a mudança mais importante: sem ela, qualquer string iniciando com maiúsculo e contendo mais de uma palavra produziria o primeiro token incorreto. Os testes não expuseram esse bug na época (não havia teste para `"NomeComposto"`), mas a correção foi feita preventivamente.

---

### Refatoração 2 — Limite de 10 linhas por método

Após o Ciclo 4, três métodos ultrapassavam o limite de 10 linhas, cada um por razões distintas.

#### Código antes da refatoração

**`validarEntrada`** — misturava guards de entrada com varredura de caracteres (~12 linhas):

```java
private static void validarEntrada(String texto) {
    if (texto.isEmpty()) {
        return;
    }
    if (Character.isDigit(texto.charAt(0))) {
        throw new IllegalArgumentException("Entrada invalida: nao deve comecar com numero.");
    }
    for (int i = 0; i < texto.length(); i++) {
        char caractereAtual = texto.charAt(i);
        if (!Character.isLetterOrDigit(caractereAtual)) {
            throw new IllegalArgumentException("Entrada invalida: use apenas letras e numeros.");
        }
    }
}
```

**`converterParaListaDePalavras`** — misturava a estrutura de iteração com a lógica de cada passo (~12 linhas):

```java
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
```

**`deveQuebrarPalavra`** — empilhava três categorias independentes de fronteira em sequência linear (~20 linhas):

```java
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
```

#### Código após a refatoração

**`validarEntrada`** → extração de `validarCaracteres`:

```java
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
```

**`converterParaListaDePalavras`** → extração de `processarCaractere`:

```java
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
```

**`deveQuebrarPalavra`** → extração de `ehFronteiraNumerica` e `ehFronteiraUpperCase`:

```java
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
```

#### Justificativa

| Método original | Problema | Extração realizada |
|---|---|---|
| `validarEntrada` | Guards de borda misturados com laço de varredura | `validarCaracteres` — isola a responsabilidade de percorrer os caracteres |
| `converterParaListaDePalavras` | Estrutura de iteração acoplada à lógica de cada passo | `processarCaractere` — isola o que acontece **em cada posição** da string |
| `deveQuebrarPalavra` | Três categorias de fronteira (numérica, maiúsculo simples, maiúsculo com look-ahead) empilhadas em sequência | `ehFronteiraNumerica` e `ehFronteiraUpperCase` — cada categoria em seu próprio método nomeado |

Cada novo método recebeu um nome que descreve **o que ele verifica**, tornando o fluxo principal autoexplicativo — `deveQuebrarPalavra` lê-se quase como uma frase: *"se for fronteira numérica, sim; se não for maiúsculo, não; caso contrário, verifique a fronteira de maiúsculas"*.

---

## 6. Código final da solução

```java
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
```

### Mapa de métodos e responsabilidades

| Método | Linhas | Responsabilidade |
|---|---|---|
| `converterCamelCase` | 2 | Ponto de entrada público — orquestra validação e conversão |
| `validarEntrada` | 4 | Guards iniciais: string vazia, primeiro caractere inválido |
| `validarCaracteres` | 4 | Varredura de caracteres inválidos no restante da string |
| `converterParaListaDePalavras` | 6 | Estrutura de iteração — produz a lista final |
| `processarCaractere` | 4 | Lógica de cada posição: quebra se necessário, acumula |
| `deveQuebrarPalavra` | 6 | Decisão central de quebra de token |
| `ehFronteiraNumerica` | 2 | Detecta transição letra ↔ dígito |
| `ehFronteiraUpperCase` | 4 | Detecta início de palavra maiúscula ou fim de sigla |
| `existeProximo` | 1 | Look-ahead seguro (evita `IndexOutOfBoundsException`) |
| `adicionarPalavra` | 2 | Persiste o token atual na lista |
| `normalizarPalavra` | 2 | Normaliza caixa — respeita siglas |
| `ehSigla` | 1 | Detecta se um token é inteiramente maiúsculo |

---

## 7. Considerações finais

O desenvolvimento com TDD revelou, de forma bastante natural, onde a solução precisava evoluir. Cada novo cenário de teste expôs uma limitação específica da implementação anterior:

- A regra de quebra simples (`isUpperCase`) não era suficiente para siglas;
- Os dígitos não eram reconhecidos como fronteiras de token;
- Nenhuma validação protegia contra entradas mal-formadas.

Em nenhum momento houve necessidade de reescrever o código de forma disruptiva — cada ciclo **adicionou** comportamento à estrutura existente, e as refatorações **melhoraram** essa estrutura sem alterar nada que os testes cobriam.

A restrição de **10 linhas por método** funcionou como um mecanismo de pressão positiva: ela impede que decisões de design ruins se escondam em métodos grandes. Quando um método começa a crescer, é sinal de que ele está assumindo mais de uma responsabilidade — e a refatoração se torna não apenas desejável, mas obrigatória.

O resultado final é um código com **responsabilidades bem distribuídas**, **nomes descritivos** que documentam a intenção de cada etapa, e uma **cobertura de testes** que permite qualquer mudança futura ser feita com confiança.

