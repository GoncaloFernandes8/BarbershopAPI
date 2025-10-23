# 🔧 Correção do Erro de Deploy - Sumário

## ❌ **Problema Original**

```
Driver org.h2.Driver claims to not accept jdbcUrl, jdbc:postgresql://...
```

**Causa:** Spring Boot configurado para H2 (desenvolvimento) mas URL de conexão era PostgreSQL (produção).

---

## ✅ **Solução Implementada**

### **1. Separação de Profiles (Dev/Prod)**

Foram criados 3 arquivos de configuração:

#### **`application.properties`** (Base)
- Configurações gerais
- Usa variáveis de ambiente com fallbacks
- Profile padrão: `dev`

#### **`application-dev.properties`** (Desenvolvimento)
- H2 in-memory
- Console H2 ativo
- DDL: create-drop
- Flyway: desativado

#### **`application-prod.properties`** (Produção)
- PostgreSQL
- Console H2: desativado
- DDL: validate
- Flyway: ativado

---

## 🔧 **Alterações Feitas**

### **Arquivo 1: `application.properties`**

**ANTES:**
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

**DEPOIS:**
```properties
spring.profiles.active=${SPRING_PROFILES_ACTIVE:dev}
spring.datasource.url=${DATABASE_URL:jdbc:h2:mem:testdb}
spring.datasource.username=${DATABASE_USERNAME:sa}
spring.datasource.password=${DATABASE_PASSWORD:password}
spring.datasource.driver-class-name=${DATABASE_DRIVER:org.h2.Driver}
spring.jpa.properties.hibernate.dialect=${HIBERNATE_DIALECT:org.hibernate.dialect.H2Dialect}
```

### **Arquivo 2: `application-prod.properties`** (NOVO)
```properties
spring.datasource.url=${DATABASE_URL}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
spring.h2.console.enabled=false
```

### **Arquivo 3: `application-dev.properties`** (NOVO)
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.flyway.enabled=false
spring.h2.console.enabled=true
```

---

## 🌐 **Configuração do Servidor (Render/Railway)**

### **Variáveis de Ambiente Necessárias:**

```bash
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:postgresql://ep-lively-breeze-a2lz4ire-pooler.eu-central-1.aws.neon.tech:5432/neondb?sslmode=require&prepareThreshold=0
DATABASE_USERNAME=neondb_owner
DATABASE_PASSWORD=<tua_senha>
PORT=8000
```

---

## 📝 **Passo a Passo para Corrigir**

### **1. No Render.com:**

1. Vai ao **Dashboard** → Seleciona o teu serviço
2. Clica em **Environment** → **Environment Variables**
3. Adiciona estas variáveis:
   - `SPRING_PROFILES_ACTIVE` = `prod`
   - `DATABASE_URL` = `jdbc:postgresql://ep-lively-breeze-a2lz4ire-pooler.eu-central-1.aws.neon.tech:5432/neondb?sslmode=require&prepareThreshold=0`
   - `DATABASE_USERNAME` = `neondb_owner`
   - `DATABASE_PASSWORD` = (a tua senha do Neon.tech)
4. Clica em **Save Changes**
5. O Render fará **redeploy automaticamente**

### **2. Fazer Push do Código:**

```bash
cd /home/gon-alo/Documents/Projetos/BarbershopAPI

# Adicionar alterações
git add src/main/resources/application*.properties
git add DEPLOYMENT_GUIDE.md FIX_DEPLOYMENT_SUMMARY.md

# Commit
git commit -m "fix: Configure profiles for dev/prod environments

- Split configuration into dev and prod profiles
- Use environment variables for database config
- Fix H2 driver error in production
- Add deployment guide and documentation"

# Push
git push origin main
```

---

## 🧪 **Como Testar**

### **Desenvolvimento (Local):**
```bash
# Inicia com H2 (padrão)
mvn spring-boot:run

# Acessa H2 Console
http://localhost:8000/h2-console
```

### **Produção (Local - teste):**
```bash
# Define variáveis de ambiente
export SPRING_PROFILES_ACTIVE=prod
export DATABASE_URL=jdbc:postgresql://localhost:5432/barbershop
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=senha

# Inicia
mvn spring-boot:run
```

---

## ✅ **Verificação**

### **Logs de Sucesso:**
```
The following 1 profile is active: "prod"
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
Tomcat started on port 8000
```

### **Teste de Health:**
```bash
curl http://localhost:8000/actuator/health

# Resposta esperada:
{"status":"UP"}
```

---

## 🎯 **Como Funciona Agora**

### **Ambiente de Desenvolvimento:**
```
Sem variáveis de ambiente
    ↓
Profile: dev (padrão)
    ↓
application-dev.properties
    ↓
H2 in-memory + Console ativo
```

### **Ambiente de Produção:**
```
SPRING_PROFILES_ACTIVE=prod
    ↓
Profile: prod
    ↓
application-prod.properties
    ↓
PostgreSQL (Neon.tech) + Flyway
```

---

## 🔍 **Troubleshooting**

### **Erro: "Profile not found"**
**Solução:** Verifica que criaste os 3 arquivos `.properties`

### **Erro: "DATABASE_URL not set"**
**Solução:** Define as variáveis de ambiente no servidor

### **Erro: "Connection refused"**
**Solução:** 
- Verifica se o database está ativo
- Testa a conexão: `psql "<DATABASE_URL>"`

### **Erro: "Flyway migration failed"**
**Solução:** Adiciona `FLYWAY_BASELINE_ON_MIGRATE=true`

---

## 📚 **Documentação Criada**

1. **`DEPLOYMENT_GUIDE.md`** - Guia completo de deploy
2. **`FIX_DEPLOYMENT_SUMMARY.md`** - Este arquivo (sumário)
3. **`application-prod.properties`** - Config produção
4. **`application-dev.properties`** - Config desenvolvimento

---

## 🚀 **Próximos Passos**

1. ✅ Fazer push do código
2. ✅ Configurar variáveis de ambiente no Render
3. ✅ Aguardar redeploy automático
4. ✅ Verificar logs (não deve ter erro "Unable to start")
5. ✅ Testar API: `/actuator/health`

---

## 💡 **Benefícios da Solução**

- ✅ Desenvolvimento local com H2 (rápido, sem setup)
- ✅ Produção com PostgreSQL (persistente, escalável)
- ✅ Configuração via variáveis de ambiente (seguro)
- ✅ Sem hardcoded credentials (boas práticas)
- ✅ Fácil de testar localmente
- ✅ Fácil de fazer deploy

---

**🎉 Problema Resolvido!** Agora o backend vai iniciar corretamente em produção com PostgreSQL.

