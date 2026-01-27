# Sistema de Agendamento Online

Sistema completo de agendamento para barbearia com backend em Java (Spring Boot).

## Tecnologias

### Backend
- Java 21
- Spring Boot 3.2
- Spring Security + JWT
- PostgreSQL + Flyway
- Testcontainers
- SpringDoc OpenAPI (Swagger)

## API Endpoints

### Públicos (sem autenticação)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/public/services` | Lista serviços ativos |
| GET | `/api/public/barbers` | Lista barbeiros ativos |
| GET | `/api/public/availability/slots` | Horários disponíveis |
| GET | `/api/public/availability/month` | Dias disponíveis no mês |
| POST | `/api/booking` | Criar agendamento |
| GET | `/api/booking/{token}` | Buscar por token |
| POST | `/api/booking/{token}/cancel` | Cancelar |
| POST | `/api/booking/{token}/reschedule` | Reagendar |

### Autenticação

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/auth/login` | Login |
| POST | `/api/auth/refresh` | Renovar token |

### Admin (requer JWT)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/admin/appointments` | Buscar agendamentos |
| POST | `/api/admin/appointments` | Criar manualmente |
| POST | `/api/admin/appointments/{id}/cancel` | Cancelar |
| POST | `/api/admin/appointments/{id}/complete` | Concluir |
| POST | `/api/admin/appointments/{id}/no-show` | Marcar no-show |
| GET/POST/PUT/DELETE | `/api/admin/services/*` | CRUD serviços |
| GET/PUT | `/api/admin/me/working-hours` | Expediente |
| GET/POST/DELETE | `/api/admin/time-blocks/*` | Bloqueios |
| GET | `/api/admin/reports/dashboard` | Dashboard stats |
| GET | `/api/admin/reports/period` | Relatório período |

## Regras de Negócio

- **Slots**: 15 minutos
- **Antecedência mínima**: 1 hora
- **Janela de agendamento**: 30 dias
- **Cancelamento cliente**: até 4h antes
- **Buffer entre serviços**: configurável por serviço
- **Sem sobreposição**: agendamentos e bloqueios






