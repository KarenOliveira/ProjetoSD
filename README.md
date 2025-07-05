# 🚀 Sistema P2P - Compartilhamento de Arquivos

Um sistema peer-to-peer (P2P) para compartilhamento de arquivos desenvolvido em Java.

## 📦 Estrutura do Projeto

```
ProjetoSD/
├── src/com/oliveira/projetosd/    # Código fonte Java
│   ├── Servidor.java              # Servidor central P2P
│   ├── Peer.java                  # Cliente P2P
│   └── Mensagem.java              # Classe de mensagens
├── com/oliveira/projetosd/        # Classes compiladas
├── gson-2.10.1.jar               # Biblioteca JSON
├── peer1/, peer2/, peer3/         # Pastas de exemplo
├── teste_sistema.sh              # Script de teste bash
└── notas.txt                     # Exemplos de comandos
```

## 🎯 Execução Rápida

### **Teste Automatizado:**
```bash
bash ./teste_sistema.sh
```

### **Execução Manual:**

**Terminal 1 - Servidor:**
```bash
java -cp gson-2.10.1.jar;. com.oliveira.projetosd.Servidor
# Digite: localhost
```

**Terminal 2 - Peer 1:**
```bash
java -cp gson-2.10.1.jar;. com.oliveira.projetosd.Peer
# Digite: JOIN peer1 localhost 9001 9091 9094
```

**Terminal 3 - Peer 2:**
```bash
java -cp gson-2.10.1.jar;. com.oliveira.projetosd.Peer
# Digite: JOIN peer2 localhost 9002 9092
# Digite: SEARCH teste.txt
# Digite: DOWNLOAD localhost 9091 teste.txt
```

## 📋 Pré-requisitos

- **Java JDK 8+** instalado
- **Git Bash** (para Windows) ou terminal bash
- **Biblioteca Gson** (incluída no projeto)

## 🔧 Compilação

```bash
# Compilar o projeto
javac -cp gson-2.10.1.jar -d . src/com/oliveira/projetosd/*.java

# Verificar classes compiladas
ls com/oliveira/projetosd/*.class
```

## 🎮 Comandos Disponíveis

### **JOIN** - Conectar ao servidor
```bash
JOIN [pasta_arquivos] [ip_servidor] [porta_udp] [porta_tcp1] [porta_tcp2]
```
**Exemplos:**
```bash
JOIN peer1 localhost 9001 9091 9094
JOIN peer2 localhost 9002 9092
JOIN peer3 localhost 9003 9093
```

### **SEARCH** - Buscar arquivo
```bash
SEARCH [nome_arquivo]
```
**Exemplos:**
```bash
SEARCH teste.txt
SEARCH documento.txt
SEARCH video-aula.mp4
```

### **DOWNLOAD** - Baixar arquivo
```bash
DOWNLOAD [ip_peer] [porta_tcp] [nome_arquivo]
```
**Exemplos:**
```bash
DOWNLOAD localhost 9091 teste.txt
DOWNLOAD localhost 9092 dados.txt
```

### **LEAVE** - Sair do sistema
```bash
LEAVE
```

## 📁 Arquivos de Exemplo

O sistema cria automaticamente:
- **peer1/**: teste.txt, documento.txt, video-aula.mp4
- **peer2/**: dados.txt, relatorio.pdf, musica.mp3
- **peer3/**: config.txt, imagem.jpg, backup.zip

## 🔍 Exemplo Prático Completo

### **Passo 1: Compilar (apenas uma vez)**
```bash
javac -cp gson-2.10.1.jar -d . src/com/oliveira/projetosd/*.java
```

### **Passo 2: Executar Sistema**

**Terminal 1 - Servidor:**
```bash
java -cp gson-2.10.1.jar;. com.oliveira.projetosd.Servidor
# Digite: localhost
```

**Terminal 2 - Peer 1:**
```bash
java -cp gson-2.10.1.jar;. com.oliveira.projetosd.Peer
# Digite: JOIN peer1 localhost 9001 9091 9094
```

**Terminal 3 - Peer 2:**
```bash
java -cp gson-2.10.1.jar;. com.oliveira.projetosd.Peer
# Digite: JOIN peer2 localhost 9002 9092
# Digite: SEARCH teste.txt
# Digite: DOWNLOAD localhost 9091 teste.txt
```

## ⚠️ Observações

- **Windows**: Use Git Bash para executar scripts bash
- **Package**: `com.oliveira.projetosd`
- **Servidor**: Deve estar rodando antes dos peers
- **Portas**: Cada peer precisa de portas únicas
- **Falhas**: Sistema simula falhas aleatórias (50% chance)

## 🚀 Início Rápido

```bash
# Testar tudo automaticamente
bash ./teste_sistema.sh
```

## 📊 Funcionalidades

- ✅ **Servidor central** para gerenciar peers
- ✅ **Compartilhamento P2P** de arquivos
- ✅ **Busca distribuída** de arquivos
- ✅ **Download direto** entre peers
- ✅ **Detecção de peers ativos** (ALIVE)
- ✅ **Validação de integridade** (MD5)
- ✅ **Simulação de falhas** realística

**Sistema P2P profissional com package estruturado! 🎉** 