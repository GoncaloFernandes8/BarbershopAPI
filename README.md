# 🪒 Barbershop API

API REST para sistema de marcação de barbearia desenvolvida em Spring Boot.

**Versão:** 1.1.0 | **Status:** ✅ Produção

## 📋 Funcionalidades

### **Gestão de Utilizadores** 👥
- ✅ Registo com verificação de email
- ✅ Login com JWT
- ✅ Refresh token automático
- ✅ Sistema de autenticação seguro

### **Gestão de Serviços** ✂️
- ✅ CRUD completo (criar, listar, editar, desativar)
- ✅ Duração e buffer configuráveis
- ✅ Preços em cêntimos
- ✅ Status ativo/inativo

### **Gestão de Barbeiros** 💈
- ✅ CRUD completo
- ✅ Status ativo/inativo
- ✅ Horários de trabalho configuráveis
- ✅ Gestão de folgas/férias

### **Sistema de Marcações** 📅
- ✅ Criação de marcações
- ✅ **[NOVO]** Edição completa de marcações
- ✅ **[NOVO]** Atualização de status (PENDING, CONFIRMED, COMPLETED, etc)
- ✅ Cancelamento
- ✅ Consulta de disponibilidade em tempo real
- ✅ Verificação automática de conflitos
- ✅ Histórico por utilizador

### **Gestão de Horários** ⏰
- ✅ Working Hours (horários de trabalho por dia da semana)
- ✅ Time-off (folgas e férias com período e motivo)
- ✅ Cálculo de disponibilidade considerando horários e folgas

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

## 📡 Endpoints da API

### **Autenticação** 🔐
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/auth/register` | Registo de utilizador |
| POST | `/auth/login` | Login (retorna JWT) |
| POST | `/auth/refresh` | Refresh do token |
| POST | `/auth/verify` | Verificar email |
| POST | `/auth/verify/resend` | Reenviar email de verificação |

### **Serviços** ✂️
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/services` | Listar todos os serviços |
| GET | `/services/{id}` | Buscar serviço por ID |
| POST | `/services` | Criar serviço |
| PUT | `/services/{id}` | Editar serviço |
| DELETE | `/services/{id}` | Desativar serviço |

### **Barbeiros** 💈
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/barbers` | Listar todos os barbeiros |
| GET | `/barbers/{id}` | Buscar barbeiro por ID |
| POST | `/barbers` | Criar barbeiro |
| PUT | `/barbers/{id}` | Editar barbeiro |
| DELETE | `/barbers/{id}` | Desativar barbeiro |

### **Marcações** 📅
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/appointments` | Listar marcações (com filtros) |
| GET | `/appointments/{id}` | Buscar marcação por ID |
| GET | `/appointments/my` | Minhas marcações (autenticado) |
| POST | `/appointments` | Criar marcação |
| **PUT** | `/appointments/{id}` | **[NOVO] Editar marcação completa** |
| **PATCH** | `/appointments/{id}/status` | **[NOVO] Atualizar status** |
| PATCH | `/appointments/{id}/cancel` | Cancelar marcação |

### **Clientes** 👤
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/clients` | Listar todos os clientes |
| GET | `/clients/{id}` | Buscar cliente por ID |
| POST | `/clients` | Criar cliente |
| PUT | `/clients/{id}` | Editar cliente |
| DELETE | `/clients/{id}` | Deletar cliente |

### **Horários de Trabalho** ⏰
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/working-hours?barberId={id}` | Listar horários de um barbeiro |
| POST | `/working-hours` | Criar horário de trabalho |
| DELETE | `/working-hours/{id}` | Deletar horário |

### **Folgas/Férias** 🏖️
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/time-off?barberId={id}&from={date}&to={date}` | Listar folgas |
| POST | `/time-off` | Criar folga/férias |
| DELETE | `/time-off/{id}` | Deletar folga |

### **Disponibilidade** 📊
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/availability?barberId={id}&serviceId={id}&date={yyyy-MM-dd}` | Horários disponíveis |

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

## 🆕 Novidades na v1.1.0

### **Edição de Appointments**
Agora é possível editar marcações existentes sem precisar cancelar e criar uma nova:

```bash
PUT /appointments/{id}
{
  "barberId": 2,              # Trocar barbeiro
  "startsAt": "2025-11-01T15:00:00Z",  # Reagendar
  "notes": "Nova observação"   # Atualizar notas
}
```

### **Atualização de Status**
Gerir o ciclo de vida completo de uma marcação:

```bash
PATCH /appointments/{id}/status
{
  "status": "COMPLETED"  # PENDING | CONFIRMED | COMPLETED | CANCELLED | NO_SHOW
}
```

### **Validações Melhoradas**
- ✅ Validação de email nos clientes
- ✅ Validação de telefone com regex
- ✅ Validação de senha (mínimo 6 caracteres)
- ✅ Mensagens de erro personalizadas

**Veja mais:** [BACKEND_IMPROVEMENTS.md](./BACKEND_IMPROVEMENTS.md) | [CHANGELOG.md](./CHANGELOG.md)

---

## 📚 Documentação

- **[BACKEND_IMPROVEMENTS.md](./BACKEND_IMPROVEMENTS.md)** - Documentação completa das melhorias
- **[CHANGELOG.md](./CHANGELOG.md)** - Histórico de versões
- **Swagger/OpenAPI** - Em desenvolvimento

---

## 🧪 Testes

### Executar Testes
```bash
mvn test
```

### Exemplos de Uso

**Criar Marcação:**
```bash
POST /appointments
{
  "barberId": 1,
  "serviceId": 1,
  "clientId": 1,
  "startsAt": "2025-11-01T14:00:00Z",
  "notes": "Cliente prefere degradé"
}
```

**Editar Marcação:**
```bash
PUT /appointments/550e8400-e29b-41d4-a716-446655440000
{
  "startsAt": "2025-11-01T15:00:00Z"
}
```

**Atualizar Status:**
```bash
PATCH /appointments/550e8400-e29b-41d4-a716-446655440000/status
{
  "status": "CONFIRMED"
}
```

---

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Adiciona MinhaFeature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

---

## 📄 Licença

Projeto desenvolvido para fins educacionais e comerciais.

---

**Desenvolvido com ☕ e 💚**  
**Versão:** 1.1.0 | **Última Atualização:** Outubro 2025