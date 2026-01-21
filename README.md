# Barbearia - Sistema de Agendamento Online

Sistema completo de agendamento para barbearia com backend em Java (Spring Boot) e frontend em Next.js.

## Funcionalidades

### Para Clientes (Público)
- Agendamento online em 5 passos simples
- Escolha de serviço, profissional, data e horário
- Confirmação imediata por email
- Cancelamento e reagendamento (até 4h antes)
- Página de confirmação com detalhes

### Para Administração
- Dashboard com estatísticas em tempo real
- Agenda visual (calendário semana/dia)
- Gestão de serviços (criar, editar, preços)
- Gestão de disponibilidade (expediente + bloqueios)
- Relatórios de faturamento e no-shows
- Ações rápidas: concluir, cancelar, marcar no-show

## Tecnologias

### Backend
- Java 21
- Spring Boot 3.2
- Spring Security + JWT
- PostgreSQL + Flyway
- Testcontainers
- SpringDoc OpenAPI (Swagger)

### Frontend
- Next.js 14 (App Router)
- TypeScript
- Tailwind CSS
- shadcn/ui
- React Query (TanStack)
- react-big-calendar

## Pré-requisitos

- Docker e Docker Compose
- (Para desenvolvimento) Java 21, Maven, Node.js 20+

## Início Rápido

### 1. Clone e configure

```bash
cd barbearia
cp .env.example .env
# Edite .env se necessário
```

### 2. Inicie com Docker

```bash
docker-compose up -d
```

### 3. Acesse

- **Frontend (Cliente)**: http://localhost:3000
- **Frontend (Admin)**: http://localhost:3000/login
- **API/Swagger**: http://localhost:8080/swagger-ui.html

### Credenciais padrão

- **Email**: admin@barbearia.com
- **Senha**: admin123

## Desenvolvimento Local

### Backend

```bash
cd backend

# Com Maven
./mvnw spring-boot:run

# Ou compile e execute
./mvnw clean package
java -jar target/*.jar
```

**Requisitos**: PostgreSQL rodando (ou use docker-compose up postgres)

### Frontend

```bash
cd frontend
npm install
npm run dev
```

## Variáveis de Ambiente

| Variável | Descrição | Padrão |
|----------|-----------|--------|
| `DB_HOST` | Host do PostgreSQL | postgres |
| `DB_PASSWORD` | Senha do banco | barbearia123 |
| `JWT_SECRET` | Chave secreta JWT (256+ bits) | (gerada) |
| `ADMIN_EMAIL` | Email do admin inicial | admin@barbearia.com |
| `ADMIN_PASSWORD` | Senha do admin inicial | admin123 |
| `SMTP_HOST` | Host SMTP para emails | (vazio=desabilitado) |
| `NOTIFICATIONS_ENABLED` | Habilitar notificações | false |

Veja `.env.example` para lista completa.

## Estrutura do Projeto

```
barbearia/
├── backend/
│   ├── src/main/java/com/barbearia/
│   │   ├── domain/          # Entidades e repositórios
│   │   ├── application/     # Serviços, DTOs, mappers
│   │   ├── infrastructure/  # Config, security, notifications
│   │   └── api/            # Controllers
│   ├── src/main/resources/
│   │   ├── db/migration/   # Flyway migrations
│   │   └── application.yml
│   ├── src/test/           # Testes
│   ├── Dockerfile
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── app/            # Rotas Next.js
│   │   ├── components/     # Componentes React
│   │   ├── lib/            # Utilitários
│   │   ├── store/          # Estado (Zustand)
│   │   ├── hooks/          # React hooks
│   │   └── types/          # TypeScript types
│   ├── Dockerfile
│   └── package.json
├── docker-compose.yml
└── .env.example
```

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

## Deploy em Produção

### Opção 1: Docker Compose

```bash
# Configure variáveis de produção
cp .env.example .env
# Edite .env com valores de produção

# Build e start
docker-compose -f docker-compose.yml up -d --build
```

### Opção 2: Cloud

**Railway/Render/Fly.io**:
1. Conecte repositório Git
2. Configure variáveis de ambiente
3. Deploy automático

**Kubernetes/AWS ECS**:
- Use as imagens Docker geradas
- Configure secrets para variáveis sensíveis
- Use RDS/Cloud SQL para PostgreSQL

### Checklist de Produção

- [ ] Alterar `JWT_SECRET` para valor seguro (256+ bits)
- [ ] Alterar senha do admin
- [ ] Configurar SMTP para emails
- [ ] Habilitar `NOTIFICATIONS_ENABLED=true`
- [ ] Configurar backup do PostgreSQL
- [ ] Usar HTTPS (nginx/traefik na frente)
- [ ] Configurar CORS para domínio de produção

## Testes

### Backend

```bash
cd backend

# Testes unitários
./mvnw test

# Testes de integração (requer Docker)
./mvnw verify
```

### Frontend

```bash
cd frontend
npm run lint
```




