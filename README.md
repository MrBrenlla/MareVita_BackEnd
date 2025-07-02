
# Guía de Execución do BackEnd de MareVita

Este repositorio forma parte do Traballo de Fin de Máster (TFM) no Máster Universitario en Enxeñaría Informática da Universidade da Coruña (UDC).

Contén o backend de MareVita, unha aplicación orientada á pesca deportiva en Galicia. A arquitectura estrutúrase en microservizos desenvolvidos en Java con Spring Boot, organizados segundo principios de modularidade e separación de responsabilidades.

Inclúe:
- Microservizos independentes para xestión de usuarios, capturas e alertas e últimas capturas e estatísticas.
- Un módulo `commons` compartido entre servizos con definicións comúns das clases gardadas en MongoDB.
- Un `gateway` baseado en Spring Cloud Gateway, encargado do enrutado e da seguridade (JWT).

O sistema está deseñado para despregue mediante Docker, facilitando escalabilidade e mantemento. O backend serve como núcleo funcional da app MareVita e está adaptado ao contexto galego da pesca deportiva.

>FrontEnd dispoñible en [MareVita_FrontEnd](https://github.com/MrBrenlla/MareVita_FrontEnd.git)

---

## 1. Requisitos Previos

Antes de comezar, asegúrese de ter instaladas e correctamente configuradas as seguintes ferramentas no seu sistema:

- **Java 17** ou superior.
- **Apache Maven** (versión 3.6 ou superior).
- Un sistema de contedores compatible con **Docker**, como:
    - [Docker Desktop](https://www.docker.com/products/docker-desktop/)
    - [Rancher Desktop](https://rancherdesktop.io/)

---

## 2. Estrutura do Proxecto

O proxecto MareVita está composto polos seguintes módulos:

- `barca`: Gateaway
- `commons`: libraría compartida entre os servizos.
- `cana`: microservizo de usuarios.
- `anzol`: microservizo de capturas e alertas.
- `minhoca`: microservizo de últimas capturas e estatísticas.

Cada un deles está localizado nun subdirectorio específico dentro do cartafol raíz do proxecto.

---

## 3. Compilación dos Servizos

Para compilar os servizos, cómpre empregar Maven. O proceso consiste en acceder a cada un dos directorios e executar os comandos de construción.

A continuación, preséntanse os pasos detallados:

1. Acceder ao directorio onde se atopan os módulos descargados:

   ```bash
   cd O/teu/directorio/
   ```

2. Compilar a libraría compartida `commons`:

   ```bash
   cd ./Commons
   mvn clean package -DskipTests
   ```

>*O resto de microservizos pode ser compilados en calquera orde, pero `commons` debe ser antes que `cana`, `anzol` e `minhoca`.*

3. Compilar o microservizo `barca`:

   ```bash
   cd ../Barca
   mvn clean package -DskipTests
   ```

4. Compilar o microservizo `cana`:

   ```bash
   cd ../Cana
   mvn clean package -DskipTests
   ```


5. Compilar o microservizo `anzol`:

   ```bash
   cd ../Anzol
   mvn clean package -DskipTests
   ```

6. Compilar o microservizo `minhoca`:

   ```bash
   cd ../Minhoca
   mvn clean package -DskipTests
   ```

---

## 4. Construción das Imaxes Docker

Unha vez compilados os servizos, cómpre construír as imaxes de contedor correspondentes a cada un deles empregando `docker build`:

```bash
docker build -t barca ../Barca
docker build -t cana ../Cana
docker build -t anzol ../Anzol
docker build -t minhoca ../Minhoca
```

Estes comandos crearán catro imaxes etiquetadas como `barca`, `cana`, `anzol` e `minhoca` respectivamente.

---

## 5. Verificación das Imaxes

Para verificar que as imaxes foron construídas correctamente, pode utilizar o seguinte comando:

```bash
docker images
```

Deberían aparecer listadas as imaxes cos nomes especificados no paso anterior.

---

## 6. Execución cos Contedores usando Docker Compose

Para facilitar a execución conxunta de todos os servizos, pode empregarse un ficheiro `docker-compose.yml` localizado no directorio raíz do proxecto.

1.  Asegúrese de que o ficheiro `docker-compose.yml` está correctamente configurado cos nomes das imaxes construídas (`barca`, `cana`, `anzol`, `minhoca`) e cos portos necesarios expostos.

2.  Inicie todos os servizos coa seguinte orde:

    ```
    docker compose up
    ```

    Ou, para correr en segundo plano:

    ```
    docker compose up -d
    ``` 

3.  Para deter os servizos:
    ```
    docker compose down
    ``` 

4.  Para ver os logs dos contedores:

    ```
    docker compose logs -f
    ``` 


----------

## 7. Consideracións Adicionais

-   Este documento abarca a compilación, creación das imaxes Docker e execución mediante `docker-compose`.

-   Asegúrese de que os portos 8080(gateaway) e 27027(mongodb) non estean en uso por outras aplicacións, e de estalo pode modificalos no `docker-compose.yml`.

-   En caso de utilizar **Rancher Desktop**, asegúrese de que a funcionalidade de Docker CLI está habilitada.


----------

## 8. Conclusión

Este proceso permite dispor de todos os compoñentes do BackEnd de MareVita en forma de contedores Docker, listos para seren executados e orquestrados de forma automatizada. A contedorización simplifica o despregamento en distintos entornos e favorece a reproducibilidade durante o desenvolvemento e as probas.

----------

**Autor:** Brais García Brenlla  
**Proxecto:** MareVita  
**Data:** Xuño de 2025
