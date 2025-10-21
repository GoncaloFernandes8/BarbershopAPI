# 🪒 Barbershop API

API REST para sistema de marcação de barbearia desenvolvida em Spring Boot.

## 📋 Funcionalidades

- **Gestão de Utilizadores**: Registo, login e autenticação JWT
- **Gestão de Serviços**: CRUD de serviços (corte, barba, etc.)
- **Gestão de Barbeiros**: CRUD de barbeiros disponíveis
- **Sistema de Marcações**: 
  - Criação de marcações
  - Consulta de disponibilidade
  - Gestão de horários
- **Histórico**: Consulta de marcações por utilizador
- **Autenticação JWT**: Sistema seguro de autenticação

## 🛠️ Tecnologias

- **Spring Boot 3.x** - Framework principal
- **Java 17+** - Linguagem de programação
- **Spring Security** - Autenticação e autorização
- **JWT** - Tokens de autenticação
- **Spring Data JPA** - Persistência de dados
- **H2 Database** - Base de dados em memória (desenvolvimento)
- **Maven** - Gestão de dependências

## 🚀 Como Executar

### Pré-requisitos
- Java 17+
- Maven 3.6+

### Instalação
```bash
# Instalar dependências
mvn clean install

# Executar aplicação
mvn spring-boot:run

# API disponível em http://localhost:8080
```

### Build
```bash
# Gerar JAR
mvn clean package

# Executar JAR
java -jar target/barbershop-api.jar
```

## 📡 Endpoints Principais

### Autenticação
- `POST /api/auth/register` - Registo de utilizador
- `POST /api/auth/login` - Login
- `POST /api/auth/logout` - Logout

### Serviços
- `GET /api/services` - Listar serviços
- `POST /api/services` - Criar serviço (admin)

### Barbeiros
- `GET /api/barbers` - Listar barbeiros
- `POST /api/barbers` - Criar barbeiro (admin)

### Marcações
- `GET /api/appointments` - Listar marcações do utilizador
- `POST /api/appointments` - Criar marcação
- `PUT /api/appointments/{id}` - Atualizar marcação
- `DELETE /api/appointments/{id}` - Cancelar marcação

## 🔐 Autenticação

A API utiliza JWT (JSON Web Tokens) para autenticação. Inclua o token no header:
```
Authorization: Bearer <seu-token>
```

## 🗄️ Base de Dados

- **Desenvolvimento**: H2 Database (memória)
- **Produção**: Configurável (MySQL, PostgreSQL, etc.)
- **Acesso H2 Console**: http://localhost:8080/h2-console

## 📦 Estrutura do Projeto

```
src/main/java/
├── controllers/       # Controladores REST
├── services/         # Lógica de negócio
├── repositories/     # Acesso a dados
├── models/           # Entidades JPA
├── dto/              # Data Transfer Objects
├── config/           # Configurações
└── security/         # Configurações de segurança
```

## 🔧 Configuração

### application.properties
```properties
# Porta da aplicação
server.port=8080

# Base de dados H2
spring.datasource.url=jdbc:h2:mem:barbershop
spring.h2.console.enabled=true

# JWT
jwt.secret=sua-chave-secreta
jwt.expiration=86400000
```

## 📄 Licença

Projeto desenvolvido para fins educacionais e comerciais.