# Diretorias
SRC_DIR = src
BIN_DIR = bin
LIB_DIR = lib

# Classpath com todas as libs e recursos
CLASSPATH = $(BIN_DIR):$(LIB_DIR)/*

# Classe principal
SERVER_MAIN = main.Server
SERVER_CLIENTE = main.ClienteUI

# Argumentos para o servidor (passados com make server D=.. S=.. W=.. RESET=..)
D ?= 0
S ?= 0
W ?= 0
I ?= 60000   # Default 60 segundos
RESET ?= 0

# Argumento para o cliente (passado com make cliente HOST=.. PORT=..)
HOST ?= localhost
PORT ?= 12345

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
	@if [ "$(D)" = "0" ] || [ "$(S)" = "0" ] || [ "$(W)" = "0" ]; then \
		echo "Uso: make server D=<valor> S=<valor> W=<valor> I=<intervalo_ms> [RESET=1]"; \
		exit 1; \
	fi
	@echo ">> Iniciando o servidor com D=$(D) S=$(S) W=$(W) I=$(I) RESET=$(RESET)..."
	@if [ "$(RESET)" = "1" ]; then \
		java -cp "$(CLASSPATH)" $(SERVER_MAIN) $(D) $(S) $(W) $(I) $(RESET) < $(shell tty); \
	else \
		java -cp "$(CLASSPATH)" $(SERVER_MAIN) $(D) $(S) $(W) $(I); \
	fi

# Executar cliente
cliente:
	@echo ">> Iniciando o cliente em $(HOST):$(PORT)..."
	java -cp "$(CLASSPATH)" $(SERVER_CLIENTE) $(HOST) $(PORT)

# Executar Stress Test de Inserções Corretas
insert-test:
	@echo ">> A executar teste de inserções..."
	java -cp "$(CLASSPATH)" scripts.ParallelInsertTest

insert-test-invalid:
	@echo ">> A executar teste de inserções inválidas..."
	java -cp "$(CLASSPATH)" scripts.ParallelInsertCorruptedTest

insert-test-final:
	@echo ">> A executar teste de várias operações..."
	java -cp "$(CLASSPATH)" scripts.RobustScalabilityTestFinal

doc: 
	@echo ">> A gerar documentação..."
	javadoc -Xdoclint:none -d docs -sourcepath src -subpackages scripts:utils:main:databases:menu:entities:enums
	@echo ">> Documentação gerada na pasta 'docs'!"

# Clean
clean:
	@echo ">> A limpar ficheiros compilados..."
	rm -rf $(BIN_DIR)/*

# Rebuild
rebuild: clean compile
	@echo ">> Rebuild concluído!"

.PHONY: all compile server cliente clean rebuild
