# 🚀 Guia de Deploy - BarbershopAPI

## 📋 Problema Resolvido

**Erro anterior:**
```
Driver org.h2.Driver claims to not accept jdbcUrl, jdbc:postgresql://...
```

**Causa:** O Spring Boot estava configurado para H2 (desenvolvimento) mas tentava conectar ao PostgreSQL (produção).

**Solução:** Configuração com profiles separados (dev/prod) usando variáveis de ambiente.

---

## 🔧 Configuração de Variáveis de Ambiente

### **Obrigatórias no Servidor de Produção:**

#### **1. Profile do Spring**
```bash
SPRING_PROFILES_ACTIVE=prod
```

#### **2. Database URL**
```bash
DATABASE_URL=jdbc:postgresql://<host>:<porta>/<database>?sslmode=require
```

**Exemplo Neon.tech:**
```bash
DATABASE_URL=jdbc:postgresql://ep-lively-breeze-a2lz4ire-pooler.eu-central-1.aws.neon.tech:5432/neondb?sslmode=require&prepareThreshold=0
```

#### **3. Database Username**
```bash
DATABASE_USERNAME=seu_usuario
```

#### **4. Database Password**
```bash
DATABASE_PASSWORD=sua_senha
```

---

## 🌐 Configuração por Plataforma

### **Render.com**

1. Vai ao Dashboard do Render
2. Seleciona o teu serviço
3. Vai a **Environment** → **Environment Variables**
4. Adiciona as seguintes variáveis:

```bash
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:postgresql://ep-lively-breeze-a2lz4ire-pooler.eu-central-1.aws.neon.tech:5432/neondb?sslmode=require&prepareThreshold=0
DATABASE_USERNAME=neondb_owner
DATABASE_PASSWORD=<tua_senha>
PORT=8000
```

5. Clica em **Save Changes**
6. O Render fará redeploy automaticamente

---

### **Railway.app**

1. Vai ao Dashboard do Railway
2. Seleciona o teu projeto
3. Vai a **Variables**
4. Adiciona:

```bash
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:postgresql://...
DATABASE_USERNAME=...
DATABASE_PASSWORD=...
PORT=8000
```

5. O Railway fará redeploy automaticamente

---

### **Heroku**

```bash
heroku config:set SPRING_PROFILES_ACTIVE=prod
heroku config:set DATABASE_URL=jdbc:postgresql://...
heroku config:set DATABASE_USERNAME=...
heroku config:set DATABASE_PASSWORD=...
```

---

## 📁 Estrutura de Configuração

### **application.properties** (Base)
- Usa variáveis de ambiente com valores padrão
- Profile ativo: `${SPRING_PROFILES_ACTIVE:dev}`

### **application-dev.properties** (Desenvolvimento)
- H2 in-memory
- DDL auto: `create-drop`
- Flyway: desativado
- Console H2: ativado

### **application-prod.properties** (Produção)
- PostgreSQL
- DDL auto: `validate`
- Flyway: ativado
- Console H2: desativado

---

## 🧪 Como Testar Localmente

### **1. Modo Desenvolvimento (padrão)**
```bash
mvn spring-boot:run
```
- Usa H2 in-memory
- Console em: http://localhost:8000/h2-console

### **2. Modo Produção (local)**
```bash
# Windows
set SPRING_PROFILES_ACTIVE=prod
set DATABASE_URL=jdbc:postgresql://localhost:5432/barbershop
set DATABASE_USERNAME=postgres
set DATABASE_PASSWORD=senha
mvn spring-boot:run

# Linux/Mac
export SPRING_PROFILES_ACTIVE=prod
export DATABASE_URL=jdbc:postgresql://localhost:5432/barbershop
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=senha
mvn spring-boot:run
```

---

## 🔍 Verificar Configuração Atual

### **Ver qual profile está ativo:**
```bash
# Ao iniciar, verifica os logs:
The following 1 profile is active: "prod"
```

### **Testar endpoint:**
```bash
curl http://localhost:8000/actuator/health
```

---

## 📊 Variáveis Opcionais (Tuning)

### **Connection Pool (HikariCP)**
```bash
HIKARI_MAX_POOL_SIZE=10
HIKARI_MIN_IDLE=5
```

### **JPA/Hibernate**
```bash
JPA_SHOW_SQL=false           # true em dev, false em prod
HIBERNATE_FORMAT_SQL=false   # true em dev, false em prod
```

### **Logging**
```bash
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_BARBERSHOP=DEBUG
```

---

## 🆘 Troubleshooting

### **Problema 1: Driver H2 não aceita PostgreSQL URL**
**Erro:**
```
Driver org.h2.Driver claims to not accept jdbcUrl, jdbc:postgresql://...
```

**Solução:**
```bash
✅ Definir: SPRING_PROFILES_ACTIVE=prod
```

---

### **Problema 2: Conexão recusada ao PostgreSQL**
**Erro:**
```
Connection refused
```

**Checklist:**
- ✅ `DATABASE_URL` está correto?
- ✅ Firewall/Security Group permite conexões?
- ✅ Database está ativo (Neon.tech pode hibernar)?
- ✅ Username e password estão corretos?

**Teste a conexão:**
```bash
psql "postgresql://ep-lively-breeze-a2lz4ire-pooler.eu-central-1.aws.neon.tech:5432/neondb?sslmode=require"
```

---

### **Problema 3: Flyway migration falha**
**Erro:**
```
FlywayException: Found non-empty schema without metadata table
```

**Solução:**
```bash
# Adicionar às variáveis de ambiente:
FLYWAY_BASELINE_ON_MIGRATE=true
```

Ou temporariamente desativar Flyway:
```bash
FLYWAY_ENABLED=false
```

---

### **Problema 4: Port já em uso**
**Erro:**
```
Port 8000 is already in use
```

**Solução:**
```bash
# Mudar a porta
PORT=8080
```

---

## 🔐 Segurança - Boas Práticas

### **❌ NUNCA faças isto:**
```java
// ❌ Hardcoded credentials
spring.datasource.username=admin
spring.datasource.password=senha123
```

### **✅ SEMPRE faz isto:**
```java
// ✅ Environment variables
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}
```

### **Secrets no Git:**
- ❌ Nunca commita `application-prod.properties` com credenciais
- ✅ Usa `.gitignore` para arquivos sensíveis
- ✅ Usa variáveis de ambiente no servidor

---

## 📝 Checklist de Deploy

Antes de fazer push:

- [ ] `SPRING_PROFILES_ACTIVE=prod` definido
- [ ] `DATABASE_URL` configurado
- [ ] `DATABASE_USERNAME` configurado
- [ ] `DATABASE_PASSWORD` configurado
- [ ] `PORT` definido (se necessário)
- [ ] Código compilado sem erros: `mvn clean package`
- [ ] Testes passam: `mvn test`
- [ ] `.gitignore` atualizado

Depois do deploy:

- [ ] Logs não mostram erros: `Unable to start web server`
- [ ] Health check responde: `/actuator/health`
- [ ] API responde: `/api/...`
- [ ] Conexão ao database funciona

---

## 🎯 Exemplo Completo - Neon.tech

### **Variáveis de Ambiente:**
```bash
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:postgresql://ep-lively-breeze-a2lz4ire-pooler.eu-central-1.aws.neon.tech:5432/neondb?sslmode=require&prepareThreshold=0
DATABASE_USERNAME=neondb_owner
DATABASE_PASSWORD=npg_xxxxxxxxxxx
PORT=8000
FLYWAY_ENABLED=true
FLYWAY_BASELINE_ON_MIGRATE=true
```

### **Build Command:**
```bash
mvn clean package -DskipTests
```

### **Start Command:**
```bash
java -jar target/barbershopAPI-0.0.1-SNAPSHOT.jar
```

---

## 📚 Referências

- [Spring Boot Profiles](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.profiles)
- [Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby)
- [Flyway Documentation](https://flywaydb.org/documentation/)

---

**💡 Dica Final:** Sempre testa localmente com `SPRING_PROFILES_ACTIVE=prod` antes de fazer deploy!

---

**✅ Problema resolvido!** Agora o backend vai funcionar corretamente em produção com PostgreSQL e em desenvolvimento com H2.

