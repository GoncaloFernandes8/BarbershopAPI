# 🔧 Correção: Emails de Lembrete Não Enviados

## ✅ **Problema Identificado e Corrigido**

### **Problema Principal:**
O `EmailService.java` **não estava a definir o endereço "FROM"** (remetente) no email, o que impedia o envio dos emails de lembrete.

### **Correção Aplicada:**
```java
// ✅ ANTES (ERRO):
helper.setTo(appointment.getClient().getEmail());
helper.setSubject("✂️ Lembrete...");
helper.setText(buildReminderEmailHtml(appointment), true);
// ❌ Faltava: helper.setFrom(...)

// ✅ DEPOIS (CORRIGIDO):
helper.setFrom(FROM_EMAIL, FROM_NAME);
helper.setTo(appointment.getClient().getEmail());
helper.setSubject("✂️ Lembrete...");
helper.setText(buildReminderEmailHtml(appointment), true);
```

### **Alterações Feitas:**

1. **Adicionado endereço "FROM"** no `EmailService.java`
2. **Melhorada validação de status** no `AppointmentReminderScheduler.java`
3. **Melhorados logs** para facilitar debug

---

## 🚀 **Passos para Ativar os Emails**

### **1. Configurar Variáveis de Ambiente**

**No Render/Koyeb/Vercel, adiciona estas variáveis:**

#### **Opção A: Usando Gmail (Recomendado)**

1. **Criar Senha de App do Gmail:**
   - Vai a [Google Account Security](https://myaccount.google.com/security)
   - Ativa **Verificação em 2 passos**
   - Vai a **Senhas de apps**
   - Cria nova senha para "Barbershop API"
   - Copia a senha de 16 dígitos

2. **Adicionar no Render/Koyeb:**
   ```
   MAIL_HOST=smtp.gmail.com
   MAIL_PORT=587
   MAIL_USERNAME=teu-email@gmail.com
   MAIL_PASSWORD=xxxx xxxx xxxx xxxx  (senha de app gerada)
   MAIL_FROM=teu-email@gmail.com
   MAIL_FROM_NAME=Barbershop
   ```

#### **Opção B: Usando SendGrid (Grátis até 100 emails/dia)**

1. Regista-te em [SendGrid](https://sendgrid.com/)
2. Cria uma API Key
3. Adiciona as variáveis:
   ```
   MAIL_HOST=smtp.sendgrid.net
   MAIL_PORT=587
   MAIL_USERNAME=apikey
   MAIL_PASSWORD=SG.xxxxxxxxxxxxxxxxxxxxxx  (API Key)
   MAIL_FROM=no-reply@barbershop.pt
   MAIL_FROM_NAME=Barbershop
   ```

---

### **2. Fazer Deploy das Alterações**

```bash
cd /home/gon-alo/Documents/Projetos/BarbershopAPI

# Commit das alterações
git add .
git commit -m "fix(email): adicionar endereço FROM no EmailService e melhorar validações"

# Push para GitHub
git push origin main
```

O Render/Koyeb vai fazer deploy automaticamente.

---

### **3. Verificar Logs**

**No Render:**
```
Dashboard → Logs → Ver logs em tempo real
```

**No Koyeb:**
```
Dashboard → Service → Logs
```

**Procura por:**
```
✅ Encontradas X marcação(ões) para lembrete
✅ Email de lembrete enviado para cliente@email.com
```

**Ou erros:**
```
❌ Erro ao enviar email: Authentication failed
❌ Erro ao enviar email: Invalid Addresses
```

---

## 🧪 **Testar Localmente**

### **1. Configurar variáveis de ambiente locais**

Cria ou edita: `src/main/resources/application-dev.properties`

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=teu-email@gmail.com
spring.mail.password=xxxx xxxx xxxx xxxx
```

### **2. Executar a API**

```bash
cd /home/gon-alo/Documents/Projetos/BarbershopAPI
mvn spring-boot:run
```

### **3. Criar marcação de teste**

Cria uma marcação para **daqui a 1 hora**:

```bash
POST http://localhost:8000/appointments
{
  "barberId": 1,
  "serviceId": 1,
  "clientId": 1,
  "startsAt": "2025-10-29T17:30:00+00:00",  # Ajusta para 1 hora à frente
  "notes": "Teste de lembrete"
}
```

### **4. Aguardar 5-10 minutos**

O scheduler executa a cada 5 minutos. Verifica os logs:

```
INFO - Verificando marcações entre 2025-10-29T17:25:00 e 2025-10-29T17:35:00
INFO - Encontradas 1 marcação(ões) para lembrete
INFO - ✅ Email de lembrete enviado para cliente@email.com (Marcação: xxx-xxx-xxx)
```

---

## 📊 **Como Funciona o Sistema de Lembretes**

### **Scheduler Automático:**
- ✅ Executa **a cada 5 minutos** (cron: `0 */5 * * * *`)
- ✅ Verifica marcações nas próximas **55-65 minutos**
- ✅ Envia email **apenas 1 vez** por marcação
- ✅ Limpa cache automaticamente após 2 horas

### **Condições para Envio:**
- ✅ Marcação está ativa (`isActive = true`)
- ✅ Status não é `CANCELLED`, `COMPLETED` ou `NO_SHOW`
- ✅ Cliente tem email válido
- ✅ Marcação inicia entre 55-65 minutos
- ✅ Email ainda não foi enviado (cache)

### **Validações:**
```java
// 1. Marcação ativa
appointment.getIsActive() == true

// 2. Status válido
appointment.getStatus() != CANCELLED
appointment.getStatus() != COMPLETED
appointment.getStatus() != NO_SHOW

// 3. Cliente tem email
appointment.getClient() != null
appointment.getClient().getEmail() != null
!appointment.getClient().getEmail().isBlank()

// 4. Janela de tempo
now + 55min ≤ startsAt ≤ now + 65min

// 5. Não foi enviado antes
!remindersSent.contains(appointment.getId())
```

---

## ⚠️ **Troubleshooting**

### **Problema: "Invalid Addresses" ou "setFrom" error**
**Causa:** Email FROM não configurado ou inválido  
**Solução:** ✅ Já corrigido neste commit

### **Problema: "Authentication failed"**
**Causa:** Credenciais de email incorretas  
**Solução:**
1. Verifica se a senha de app do Gmail está correta
2. Verifica se a verificação em 2 passos está ativa
3. Tenta gerar nova senha de app
4. Verifica se `MAIL_USERNAME` e `MAIL_PASSWORD` estão corretos

### **Problema: "Connection timeout"**
**Causa:** Porta bloqueada ou firewall  
**Solução:**
1. Verifica se a porta 587 está aberta
2. Tenta porta 465 (SSL):
   ```
   MAIL_PORT=465
   spring.mail.properties.mail.smtp.ssl.enable=true
   ```

### **Problema: "Email não chega"**
**Causa:** Email pode estar no spam ou logs mostram erro  
**Solução:**
1. Verifica pasta spam/lixo
2. Verifica logs do servidor para erros
3. Verifica se o cliente tem email válido na BD
4. Testa com outro endereço de email
5. Verifica se as variáveis de ambiente estão configuradas

### **Problema: "Scheduler não executa"**
**Causa:** `@EnableScheduling` pode estar desativado  
**Solução:**
1. Verifica se `@EnableScheduling` está em `BarbershopApiApplication.java` ✅
2. Verifica logs: `INFO - Encontradas X marcação(ões) para lembrete`
3. Verifica se a aplicação está em modo produção

### **Problema: "Marcação criada mas não dispara lembrete"**
**Causa:** Status ou timing incorreto  
**Solução:**
1. Verifica se a marcação está ativa: `isActive = true`
2. Verifica status: deve ser `PENDING`, `CONFIRMED` ou `SCHEDULED`
3. Verifica se a marcação é para **daqui a 1 hora** (55-65 minutos)
4. Verifica se o cliente tem email válido
5. Aguarda 5 minutos (scheduler executa a cada 5 min)

---

## 📈 **Monitorização em Produção**

### **Logs Importantes:**

```bash
# Scheduler está a funcionar
INFO - Verificando marcações entre [data_inicio] e [data_fim]

# Marcações encontradas
INFO - Encontradas 3 marcação(ões) para lembrete

# Email enviado com sucesso
INFO - ✅ Email de lembrete enviado para cliente@email.com (Marcação: xxx)

# Email já foi enviado antes
DEBUG - Lembrete já enviado para marcação xxx

# Marcação em estado inválido
DEBUG - Marcação xxx em estado CANCELLED - não enviando lembrete

# Cliente sem email
WARN - Marcação xxx não tem email válido do cliente

# Erro de autenticação
ERROR - ❌ Erro ao enviar email: Authentication failed

# Cache limpo
DEBUG - Removidas 5 marcações antigas do cache de lembretes
```

### **Comandos Úteis:**

**Ver logs em tempo real (Render):**
```bash
render logs --tail
```

**Ver logs em tempo real (Koyeb):**
```bash
koyeb logs --tail
```

**Filtrar logs de email:**
```bash
render logs | grep "lembrete"
koyeb logs | grep "Email"
```

---

## 🎯 **Verificação Final**

### **Checklist de Configuração:**

- [x] `EmailService.java` atualizado com `setFrom()`
- [x] `AppointmentReminderScheduler.java` com validações melhoradas
- [ ] Variáveis de ambiente configuradas no Render/Koyeb
  - [ ] `MAIL_HOST`
  - [ ] `MAIL_PORT`
  - [ ] `MAIL_USERNAME`
  - [ ] `MAIL_PASSWORD`
  - [ ] `MAIL_FROM` (opcional, fallback: no-reply@barbershop.pt)
  - [ ] `MAIL_FROM_NAME` (opcional, fallback: Barbershop)
- [ ] Deploy feito com sucesso
- [ ] Logs verificados sem erros de autenticação
- [ ] Teste realizado com marcação real

---

## 📚 **Documentação Adicional**

- **Setup Completo:** [EMAIL_SETUP_GUIDE.md](./EMAIL_SETUP_GUIDE.md)
- **Gmail App Passwords:** [Google Support](https://support.google.com/accounts/answer/185833)
- **SendGrid Docs:** [SendGrid Getting Started](https://docs.sendgrid.com/)
- **Spring Mail Docs:** [Spring Boot Mail](https://docs.spring.io/spring-boot/docs/current/reference/html/io.html#io.email)

---

## 🎉 **Próximos Passos**

1. ✅ Fazer commit e push das alterações
2. ⏳ Configurar variáveis de ambiente no Render/Koyeb
3. ⏳ Aguardar deploy automático
4. ⏳ Verificar logs para confirmar que está a funcionar
5. ⏳ Criar marcação de teste para validar

---

**🔧 Correção Completa!**

Agora o sistema está pronto para enviar emails de lembrete automaticamente! ✂️📧

Se continuares com problemas, verifica os logs e consulta a secção de troubleshooting acima.

---

**Autor:** Gonçalo Fernandes  
**Data:** 29 Outubro 2025  
**Versão:** 1.2.0

