# Diretorias
SRC_DIR = src
BIN_DIR = bin
LIB_DIR = lib

# Classpath com todas as libs e recursos
CLASSPATH = $(BIN_DIR):$(LIB_DIR)/*

# Classe principal
SERVER_MAIN = main.Server
SERVER_CLIENTE = main.Cliente

# Argumentos para o servidor (passados com make server D=.. S=.. RESET=..)
D ?= 0
S ?= 0
RESET ?= 0

# .java
SOURCES := $(shell find $(SRC_DIR) -name "*.java")

# Compilar
all: compile

compile:
	@echo ">> Compilando projeto..."
	mkdir -p $(BIN_DIR)
	javac -cp "$(LIB_DIR)/*" -d $(BIN_DIR) $(SOURCES)
	@echo ">> Compilação concluída!"

# Executar servidor
server:
	@if [ "$(D)" = "0" ] || [ "$(S)" = "0" ]; then \
		echo "Uso: make server D=<valor> S=<valor> [RESET=1]"; \
		exit 1; \
	fi
	@echo ">> Iniciando o servidor com D=$(D) S=$(S) RESET=$(RESET)..."
	@if [ "$(RESET)" = "1" ]; then \
		java -cp "$(CLASSPATH)" $(SERVER_MAIN) $(D) $(S) $(RESET) < $(shell tty); \
	else \
		java -cp "$(CLASSPATH)" $(SERVER_MAIN) $(D) $(S); \
	fi

# Executar cliente
cliente:
	@echo ">> Iniciando o cliente..."
	java -cp "$(CLASSPATH)" $(SERVER_CLIENTE)

# Clean
clean:
	@echo ">> A limpar ficheiros compilados..."
	rm -rf $(BIN_DIR)/*

# Rebuild
rebuild: clean compile
	@echo ">> Rebuild concluído!"

.PHONY: all compile server cliente clean rebuild
