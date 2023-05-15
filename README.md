# BA-CF

BA-CF (Bitwise Algorithm supporting Control Flow coverage ou Algoritmo Bit-a-Bit de suporte a cobertura de fluxo de controle) é uma ferramenta de suporte ao teste estrutural baseado em critérios de fluxo de controle para programas compilados em Bytecode, em especial programas Java.

BA-CF é uma ferramenta experimental e ainda está em construção.

## Exemplos

### Instrumentação

Para instrumentar as classes use o comando **instrument**. A opção `-edges` instrumenta as classes para rastrear arestas e a opção `-edge-pairs` instrumenta para rastreamento de pares de arestas. 

```
java -jar ba-cf-cli-{VERSION}-all.jar instrument
```

O .jar da  ba-cf deve estar no *classpath* ao executar as classes instrumentadas.

### Relatório

Após a execução, o arquivo coverage.ser será criado no seu diretório atual. Use o programa **report** para obter a cobertura. A opção `-edge-s` deve ser usada para obter a cobertura de programas instrumentados para rastreamento de arestas e `-edge-pairs` para pares de arestas, pois por padrão a ferramenta obtém a cobertura de classes instrumentadas para rastrear nós. Por padrão, o programa exibe o cobertura de cada classe `(# de requisitos cobertos na classe/# de requisitos na classe)`. A opção `-show-methods` exibe a cobertura por método `(# de requisitos cobertos no método/# de requisitos no método)`. Também é possível exportar o relatório no formato XML usando a opção `-xml` e especificando o local e nome do arquivo.

```
java -jar ba-cf-cli-{VERSION}-all.jar report
```

## Licença

BA-CF é licenciada sob a Eclipse Public License - v 1.0 (http://www.eclipse.org/legal/epl-v10.html)

## Nota

A BA-CF é construída com algumas classes da ASM (http://asm.ow2.org), args4j (http://args4j.kohsuke.org) e JaCoCo (http://www.eclemma.org/jacoco/) incorporadas. As ferramentas de interface de linha de comando foram inspiradas no pull request #86 da JaCoCo.

- ASM é distribuída sob a BSD License.
- args4j é distribuída sob a MIT License.
- JaCoCo é distribuída sob a Eclipse Public License - v 1.0.

Durante nossa implementação, nos baseamos em parte no código da BADUA e JaCoCo. Qualquer semelhança não é mera coincidência.
