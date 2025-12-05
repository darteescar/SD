# Diretorias
SRC_DIR = src
BIN_DIR = bin
LIB_DIR = lib

# Classpath com todas as libs e recursos
CLASSPATH = $(BIN_DIR):$(LIB_DIR)/*

# Classe principal
SERVER_MAIN = main.Server
SERVER_CLIENTE = main.Cliente

# .java
SOURCES := $(shell find $(SRC_DIR) -name "*.java")

# Compilar
all: compile

compile:
	@echo ">> Compilando projeto..."
	mkdir -p $(BIN_DIR)
	javac -cp "$(LIB_DIR)/*" -d $(BIN_DIR) $(SOURCES)
	@echo ">> Compilação concluída!"

# Executar aplicação
# Lado servidor
server:
	@echo ">> Iniciando o servidor..."
	java -cp "$(CLASSPATH)" $(SERVER_MAIN)

# Lado cliente
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

.PHONY: all compile sever cliente clean rebuild