# 📧 Guia de Configuração de Email - Sistema de Lembretes

## 🎯 Funcionalidade

O sistema envia automaticamente emails de lembrete para os clientes **1 hora antes** de cada marcação.

### **Email Inclui:**
- ✂️ Nome do barbeiro
- 🕐 Data e horário da marcação
- 💰 Serviço e preço
- 📝 Observações (se houver)
- 💡 Dicas úteis

---

## 🚀 Como Funciona

### **Scheduler Automático**
- Executa **a cada 5 minutos**
- Verifica marcações nas próximas 55-65 minutos
- Envia email apenas 1 vez por marcação
- Limpa cache automaticamente

### **Condições para Envio**
✅ Marcação ativa  
✅ Status: `SCHEDULED`, `PENDING` ou `CONFIRMED`  
✅ Cliente tem email válido  
✅ Marcação inicia entre 55-65 minutos

---

## 📝 Configuração do Email

### **Opção 1: Gmail (Recomendado)**

#### **1. Criar Senha de App do Gmail**

1. Vai a [Google Account](https://myaccount.google.com/)
2. **Segurança** → **Verificação em 2 passos** (ativa se ainda não estiver)
3. **Segurança** → **Senhas de apps**
4. Seleciona:
   - **App:** Mail
   - **Dispositivo:** Outro (escreve "Barbershop API")
5. **Gerar** → Copia a senha de 16 dígitos

#### **2. Configurar Variáveis de Ambiente**

**Para desenvolvimento local** (`application-dev.properties`):
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=teu-email@gmail.com
spring.mail.password=xxxx xxxx xxxx xxxx
```

**Para produção (Render/Koyeb)**, adiciona estas variáveis:
```
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=teu-email@gmail.com
MAIL_PASSWORD=xxxx xxxx xxxx xxxx
```

---

### **Opção 2: Outlook/Hotmail**

```properties
spring.mail.host=smtp-mail.outlook.com
spring.mail.port=587
spring.mail.username=teu-email@outlook.com
spring.mail.password=tua-senha
```

---

### **Opção 3: Serviços Profissionais**

#### **SendGrid** (Grátis até 100 emails/dia)
```properties
spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=SG.xxxxxxxxxxxxx
```

#### **Mailgun** (Grátis até 1000 emails/mês)
```properties
spring.mail.host=smtp.mailgun.org
spring.mail.port=587
spring.mail.username=postmaster@teu-dominio.mailgun.org
spring.mail.password=tua-senha-mailgun
```

---

## 🔧 Configuração no Render/Koyeb

### **1. No Dashboard da Plataforma:**

**Environment Variables:**
```
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=teu-email@gmail.com
MAIL_PASSWORD=xxxx xxxx xxxx xxxx
```

### **2. Verificar Logs:**

Após deployment, verifica os logs para:
```
✅ Lembrete enviado com sucesso para cliente@email.com
```

Ou erros:
```
❌ Erro ao enviar lembrete: Authentication failed
```

---

## 🧪 Testar Localmente

### **1. Criar marcação de teste:**

Cria uma marcação para **daqui a 1 hora**:

```bash
POST http://localhost:8000/appointments
{
  "barberId": 1,
  "serviceId": 1,
  "clientId": 1,
  "startsAt": "2025-10-23T16:30:00+01:00",  # 1 hora à frente
  "notes": "Teste de lembrete"
}
```

### **2. Aguardar 5-10 minutos**

O scheduler vai detectar e enviar o email.

### **3. Verificar Logs:**

```
INFO - Encontradas 1 marcação(ões) para lembrete
INFO - ✅ Lembrete enviado com sucesso para cliente@email.com
```

---

## 🎨 Personalizar Template de Email

### **Editar:** `src/main/java/barbershopAPI/barbershopAPI/services/EmailService.java`

**Método:** `buildReminderEmailHtml()`

Podes alterar:
- Cores (gradient: `#667eea`, `#764ba2`)
- Textos e emojis
- Estrutura HTML
- Adicionar logo da barbearia
- Adicionar botão de confirmação

---

## 📊 Monitorização

### **Logs Úteis:**

```
# Ver lembretes enviados
INFO - ✅ Lembrete enviado com sucesso

# Ver marcações encontradas
INFO - Encontradas X marcação(ões) para lembrete

# Erros
ERROR - ❌ Erro ao enviar lembrete
```

### **Verificar em Produção:**

```bash
# Ver logs do Render/Koyeb
render logs --tail

# Ou
koyeb logs --tail
```

---

## ⚠️ Troubleshooting

### **Problema: "Authentication failed"**

**Solução:**
1. Verifica se a senha de app está correta
2. Verifica se a verificação em 2 passos está ativa
3. Tenta gerar nova senha de app

### **Problema: "Connection timeout"**

**Solução:**
1. Verifica firewall/porta 587
2. Tenta porta 465 (SSL):
   ```properties
   spring.mail.port=465
   spring.mail.properties.mail.smtp.ssl.enable=true
   ```

### **Problema: "Email não chega"**

**Solução:**
1. Verifica spam/lixo
2. Verifica logs do servidor
3. Verifica se o cliente tem email válido
4. Testa com outro email

### **Problema: "Marcação não dispara lembrete"**

**Solução:**
1. Verifica se a marcação está ativa
2. Verifica status (deve ser SCHEDULED/PENDING/CONFIRMED)
3. Verifica se o cliente tem email
4. Verifica logs do scheduler

---

## 🔐 Segurança

### **NUNCA faças:**
❌ Commit de senhas no Git  
❌ Hardcode de credentials  
❌ Partilha de senhas de app

### **SEMPRE faz:**
✅ Usa variáveis de ambiente  
✅ Usa `.gitignore` para configs locais  
✅ Gera senhas de app separadas por ambiente  
✅ Roda logs em produção (não verbose)

---

## 📈 Melhorias Futuras

Sugestões de evolução:

1. **Email de Confirmação** quando cliente cria marcação
2. **Email de Cancelamento** quando marcação é cancelada
3. **Email de Feedback** após marcação completa
4. **Email de Agradecimento** para clientes recorrentes
5. **Dashboard de Emails** com estatísticas
6. **Templates customizáveis** por barbearia
7. **Preferências de notificação** por cliente
8. **SMS** como alternativa ao email

---

## 📞 Suporte

Problemas? Verifica:
- [Gmail App Passwords](https://support.google.com/accounts/answer/185833)
- [Spring Mail Docs](https://docs.spring.io/spring-boot/docs/current/reference/html/io.html#io.email)
- Logs da aplicação

---

**🎉 Configuração Completa!**

Agora os teus clientes recebem lembretes automáticos 1 hora antes de cada marcação! ✂️

