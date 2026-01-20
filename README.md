# SD (Sistemas Distribuídos) (Português)
Implementação de um serviço de registo de eventos em séries temporais e de agregação de informação, acessível remotamente através de um servidor. É possível consultar o respetivo [enunciado](registoDeEventos/enunciado.pdf) e [relatório](registoDeEventos/relatorio.pdf).

### Nota Final: 13.9 / 20  😐

## Membros do grupo:

* [darteescar](https://github.com/darteescar)
* [luis7788](https://github.com/luis7788)
* [tiagofigueiredo7](https://github.com/tiagofigueiredo7)
* [inesferribeiro](https://github.com/inesferribeiro)

## Dependências

Para executar este projeto é necessário ter instalado:

- **Java JDK 17** (ou superior)
- **GNU Make**
- **MariaDB Server**

> ℹ️ O driver JDBC da MariaDB (`mariadb-java-client-2.7.1.jar`) já está incluído na pasta `lib/`, e a `Makefile` adiciona automaticamente todos os `.jar` desta pasta ao classpath.

### Base de Dados

O servidor utiliza a base de dados MariaDB para persistência de dados.

Após instalar o MariaDB, é necessário executar os seguintes comandos:

```sql
CREATE DATABASE base_dados_sd;

CREATE USER IF NOT EXISTS 'me'@'localhost' IDENTIFIED BY 'mypass';

GRANT ALL PRIVILEGES ON base_dados_sd.* TO 'me'@'localhost';

FLUSH PRIVILEGES;
```

## Executável
Para compilar os ficheiros executáveis do servidor, do cliente e de testes, basta fazer:

```bash
$ cd registoDeEventos
$ make
```

### Programa principal

Para executar o servidor, basta fazer:

```console
$ make server <D> <S> <W> <I> [RESET]
```   

- `<D>` – número de séries que o servidor deve contabilizar para as suas operações

- `<S>` - número de séries que o servidor deve manter em memória

- `<W>` - número de _threads_ responsáveis pela execução de tarefas (tamanho da ThreadPool)

- `<I>` - tempo de intervalo entre a passagem de dias

- `[RESET]` - flag opcional que apaga todas as entradas das bases de dados (com exceção da dos utilizadores).

Para executar o cliente, basta:

```console
$ make cliente
```

E a partir daqui poderá interagir com o servidor através do menu apresentado. Para mais informações, pode consultar o [relatório](registoDeEventos/relatorio.pdf).

### Scripts de teste

Foram desenvolvidos alguns scripts para testar o funcionamento do servidor e do cliente. Para os executar, basta ter o servidor a correr e escolher um dos seguintes comandos:

```bash
$ make insert-test
$ make insert-test-invalid
$ make insert-test-final
```

O primeiro comando executa um teste de inserções de eventos, o segundo um teste de inserções de eventos inválidos e o terceiro, um teste que envolve várias operações. No fim de cada teste, são apresentados alguns resultados estatísticos na pasta `scripts/results`.

## Documentação

Para gerar a documentação do projeto, basta fazer:

```bash
$ cd registoDeEventos
$ make doc
```

Irá ser criada uma pasta `docs` com a documentação gerada. Para aceder à documentação, basta abrir o ficheiro `index.html` que se encontra dentro da pasta `docs`. Por exemplo:

```bash
$ cd docs
$ xdg-open index.html
```

## Limpeza

Para apagar os ficheiros compilados, basta fazer:

```bash
$ cd registoDeEventos
$ make clean
```

É de notar que os ficheiros de resultado da execução de scripts não são apagados com este comando.

# SD (Distributed Systems) (English)
Implementation of a time series event logging and information aggregation service, remotely accessible through a server. You can check the respective [statement](registoDeEventos/enunciado.pdf) and [report](registoDeEventos/relatorio.pdf).

### Final Grade: 13.9 / 20 😐

## Group Members:

* [darteescar](https://github.com/darteescar)
* [luis7788](https://github.com/luis7788)
* [tiagofigueiredo7](https://github.com/tiagofigueiredo7)
* [inesferribeiro](https://github.com/inesferribeiro)

## Dependencies

To run this project, you need to have installed:

- **Java JDK 17** (or higher)
- **GNU Make**
- **MariaDB Server**

> ℹ️ The MariaDB JDBC driver (`mariadb-java-client-2.7.1.jar`) is already included in the `lib/` folder, and the `Makefile` automatically adds all `.jar` files from this folder to the classpath.

### Database

The server uses the MariaDB database for data persistence.

After installing MariaDB, you need to run the following commands:

```sql
CREATE DATABASE base_dados_sd;
CREATE USER IF NOT EXISTS 'me'@'localhost' IDENTIFIED BY 'mypass';
GRANT ALL PRIVILEGES ON base_dados_sd.* TO 'me'@'localhost;
FLUSH PRIVILEGES;
```

## Executable
To compile the executable files for the server, client, and tests, just run:

```bash
$ cd registoDeEventos
$ make
```

### Main Program

To run the server, just do:

```console
$ make server <D> <S> <W> <I> [RESET]
```

- `<D>` – number of series the server should account for in its operations
- `<S>` - number of series the server should keep in memory
- `<W>` - number of threads responsible for executing tasks (ThreadPool size)
- `<I>` - time interval between day transitions
- `[RESET]` - optional flag that deletes all entries from the databases (except for users).

To run the client, just do:
```console
$ make cliente
```

From here, you can interact with the server through the presented menu. For more information, you can check the [report](registoDeEventos/relatorio.pdf).

### Test Scripts

Some scripts were developed to test the functionality of the server and client. To run them, just have the server running and choose one of the following commands:

```bash
$ make insert-test
$ make insert-test-invalid
$ make insert-test-final
```

The first command runs an event insertion test, the second runs an invalid event insertion test, and the third runs a test involving multiple operations. At the end of each test, some statistical results are presented in the `scripts/results` folder.

## Documentation

To generate the project documentation, just run:

```bash
$ cd registoDeEventos
$ make doc
```

A `docs` folder will be created with the generated documentation. To access the documentation, just open the `index.html` file located inside the `docs` folder. For example:

```bash
$ cd docs
$ xdg-open index.html
```

## Cleanup

To delete the compiled files, just run:

```bash
$ cd registoDeEventos
$ make clean
```