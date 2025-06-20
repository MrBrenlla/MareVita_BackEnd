
# Guía de Execución do BackEnd de MareVita

Este documento proporciona as instrucións necesarias para compilar e executar os servizos que compoñen o BackEnd do proxecto **MareVita**. O sistema está baseado en microservizos desenvolvidos en Java con Spring Boot e require ferramentas específicas para a súa construción e execución.

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
   cd ./commons
   mvn clean package -DskipTests
   ```

>*O resto de microservizos pode ser compilados en calquera orde, pero `commons` debe ser antes que `cana`, `anzol` e `minhoca`.*

3. Compilar o microservizo `barca`:

   ```bash
   cd ../barca
   mvn clean package -DskipTests
   ```

4. Compilar o microservizo `cana`:

   ```bash
   cd ../cana
   mvn clean package -DskipTests
   ```


5. Compilar o microservizo `anzol`:

   ```bash
   cd ../anzol
   mvn clean package -DskipTests
   ```

6. Compilar o microservizo `minhoca`:

   ```bash
   cd ../minhoca
   mvn clean package -DskipTests
   ```

---

## 4. Construción das Imaxes Docker

Unha vez compilados os servizos, cómpre construír as imaxes de contedor correspondentes a cada un deles empregando `docker build`:

```bash
docker build -t barca ../barca
docker build -t cana ../cana
docker build -t anzol ../anzol
docker build -t minhoca ../minhoca
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
