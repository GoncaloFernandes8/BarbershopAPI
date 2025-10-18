# 🪒 Barbershop API

API REST para sistema de agendamento de barbearia desenvolvida em Spring Boot.

## 📋 Funcionalidades

- **Gestão de Usuários**: Registro, login e autenticação JWT
- **Gestão de Serviços**: CRUD de serviços (corte, barba, etc.)
- **Gestão de Barbeiros**: CRUD de barbeiros disponíveis
- **Sistema de Agendamentos**: 
  - Criação de marcações
  - Consulta de disponibilidade
  - Gestão de horários
- **Histórico**: Consulta de agendamentos por usuário
- **Autenticação JWT**: Sistema seguro de autenticação

## 🛠️ Tecnologias

- **Spring Boot 3.x** - Framework principal
- **Java 17+** - Linguagem de programação
- **Spring Security** - Autenticação e autorização
- **JWT** - Tokens de autenticação
- **Spring Data JPA** - Persistência de dados
- **H2 Database** - Banco de dados em memória (desenvolvimento)
- **Maven** - Gerenciamento de dependências

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
- `POST /api/auth/register` - Registro de usuário
- `POST /api/auth/login` - Login
- `POST /api/auth/logout` - Logout

### Serviços
- `GET /api/services` - Listar serviços
- `POST /api/services` - Criar serviço (admin)

### Barbeiros
- `GET /api/barbers` - Listar barbeiros
- `POST /api/barbers` - Criar barbeiro (admin)

### Agendamentos
- `GET /api/appointments` - Listar agendamentos do usuário
- `POST /api/appointments` - Criar agendamento
- `PUT /api/appointments/{id}` - Atualizar agendamento
- `DELETE /api/appointments/{id}` - Cancelar agendamento

## 🔐 Autenticação

A API utiliza JWT (JSON Web Tokens) para autenticação. Inclua o token no header:
```
Authorization: Bearer <seu-token>
```

## 🗄️ Banco de Dados

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

# Banco de dados H2
spring.datasource.url=jdbc:h2:mem:barbershop
spring.h2.console.enabled=true

# JWT
jwt.secret=sua-chave-secreta
jwt.expiration=86400000
```

## 📄 Licença

Projeto desenvolvido para fins educacionais e comerciais.