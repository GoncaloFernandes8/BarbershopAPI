# 📝 Changelog - BarbershopAPI

## [1.1.0] - Outubro 2025

### 🆕 Adicionado

#### **Novos Endpoints:**

1. **PUT /appointments/{id}** - Editar appointment completo
   - Permite atualizar barbeiro, serviço, cliente, data/hora e notas
   - Validação de conflitos de horário
   - Impede edição de appointments cancelados
   - Request parcial (apenas campos fornecidos são atualizados)

2. **PATCH /appointments/{id}/status** - Atualizar status do appointment
   - Status válidos: PENDING, CONFIRMED, CANCELLED, COMPLETED, NO_SHOW
   - Auto-desativa appointment quando status = CANCELLED
   - Validação de status com mensagem de erro clara

#### **Novos DTOs:**

1. **UpdateAppointmentRequest.java**
   ```java
   - barberId: Long (opcional)
   - serviceId: Long (opcional)
   - clientId: Long (opcional)
   - startsAt: OffsetDateTime (opcional)
   - notes: String (opcional)
   ```

2. **UpdateStatusRequest.java**
   ```java
   - status: String (obrigatório)
   ```

#### **Novos Métodos no Repository:**

1. **AppointmentRepository**
   - `findAllByBarberIdAndIsActiveTrueAndStartsAtLessThanAndEndsAtGreaterThan()`
   - Usado para verificação de conflitos ao editar appointments

#### **Novos Métodos no Service:**

1. **AppointmentService.update(UUID id, UpdateAppointmentRequest req)**
   - Lógica completa de atualização de appointments
   - Verificação de conflitos excluindo o próprio appointment
   - Recalculo automático de `endsAt`
   - Transação atômica

---

### 🔧 Melhorado

#### **Validações:**

1. **ClientCreateRequest.java**
   - ✅ Adicionada validação de email com `@Email`
   - ✅ Adicionada validação de telefone com regex
   - ✅ Adicionada validação de senha (mínimo 6 caracteres)
   - ✅ Mensagens de erro personalizadas

2. **ClientUpdateRequest.java**
   - ✅ Adicionada validação de telefone com regex
   - ✅ Mensagem de erro personalizada

#### **Tratamento de Erros:**

O `RestExceptionHandler` já possui tratamento completo para:
- ✅ IllegalStateException → 400 (usado quando tenta editar appointment cancelado)
- ✅ IllegalArgumentException → 400 (usado para status inválido)
- ✅ Mensagens de erro claras e consistentes

---

### 🐛 Corrigido

- Nenhum bug crítico identificado
- Sistema estava funcional, apenas faltava funcionalidade de edição

---

### 🔒 Segurança

- ✅ Validação robusta de inputs
- ✅ Verificação de existência de recursos antes de operações
- ✅ Prevenção de conflitos de horário
- ✅ Validação de tipos de dados (UUID, status enum, etc)

---

### 📊 Estatísticas

**Arquivos Criados:** 3
- UpdateAppointmentRequest.java
- UpdateStatusRequest.java
- BACKEND_IMPROVEMENTS.md

**Arquivos Modificados:** 5
- AppointmentController.java (+2 endpoints)
- AppointmentService.java (+1 método complexo)
- AppointmentRepository.java (+1 query method)
- ClientCreateRequest.java (validações)
- ClientUpdateRequest.java (validações)

**Linhas de Código Adicionadas:** ~150 linhas

**Endpoints Totais na API:** 30+

---

### 🚀 Performance

- ✅ Queries otimizadas com JPA method names
- ✅ Transações atômicas
- ✅ Verificação de conflitos eficiente
- ✅ Update parcial (apenas campos modificados são atualizados)

---

### 📖 Documentação

- ✅ BACKEND_IMPROVEMENTS.md criado com documentação completa
- ✅ Exemplos de uso de todos os endpoints
- ✅ Casos de uso detalhados
- ✅ Guia de testes

---

### 🔜 Próximas Versões (Roadmap)

#### **v1.2.0 - Planejado**
- [ ] Histórico de alterações em appointments
- [ ] Endpoint de estatísticas
- [ ] Validação de horário comercial
- [ ] Notificações de alterações

#### **v2.0.0 - Futuro**
- [ ] Sistema de permissões (RBAC)
- [ ] Relatórios de faturamento
- [ ] Sistema de recorrência
- [ ] Integração com calendários externos

---

### ⚙️ Compatibilidade

**Backend:**
- Java: 17+
- Spring Boot: 3.x
- Spring Data JPA
- PostgreSQL / H2

**Frontend:**
- Compatível com versões anteriores
- Novos endpoints são opcionais
- Nenhuma breaking change

---

### 🎯 Impacto

**Para Desenvolvedores:**
- API RESTful completa
- CRUD completo de appointments
- Documentação detalhada

**Para Usuários:**
- Edição de marcações sem cancelar
- Melhor gestão de status
- Menos retrabalho

**Para Sistema:**
- Mais flexibilidade
- Melhor rastreamento
- Histórico preservado

---

## [1.0.0] - Versão Inicial

### Funcionalidades Base
- CRUD Appointments (criar, listar, buscar, cancelar)
- CRUD Barbers (completo)
- CRUD Services (completo)
- CRUD Clients (completo)
- CRUD Working Hours
- CRUD Time-off
- Sistema de Autenticação (JWT)
- Verificação de email
- Cálculo de disponibilidade
- Prevenção de conflitos

---

**Mantido por:** Equipe de Desenvolvimento Barbershop  
**Versão Atual:** 1.1.0  
**Data de Lançamento:** Outubro 2025

