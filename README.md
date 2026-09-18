# FitManager — Sistema Integrado de Gestão para Academias

Monorepo para desenvolvimento full-stack com React (Vite + TypeScript), Node.js (Express + TypeScript) e PostgreSQL, estruturado para desenvolvimento colaborativo e escalável.

## Stack tecnológica

| Camada        | Tecnologia                     | Versão |
| ------------- | ------------------------------ | ------ |
| Frontend      | React + TypeScript (Vite)      | Atual  |
| Backend       | Node.js + Express + TypeScript | LTS    |
| Banco         | PostgreSQL                     | 16+    |
| ORM           | Prisma                         | Atual  |
| Auth          | JWT + Bcrypt                   | Atual  |
| Versionamento | Git + GitHub                   | Atual  |

---

## Pré-requisitos

Você precisa ter instalado:

* Git
* Node.js LTS
* npm
* PostgreSQL
* VS Code (recomendado)

---

# Instalação por sistema operacional

## Windows

### Instalar Node.js:

* Acesse: [https://nodejs.org/](https://nodejs.org/)
* Baixe a versão LTS
* Instale normalmente

### Instalar Git:

* Acesse: [https://git-scm.com/download/win](https://git-scm.com/download/win)
* Instale com configurações padrão

### Instalar PostgreSQL:

* Acesse: [https://www.postgresql.org/download/windows/](https://www.postgresql.org/download/windows/)
* Instale e configure senha para o usuário postgres

### Verificar instalação:

```bash
node -v
npm -v
git --version
psql --version
```

---

## Linux (Ubuntu/Debian)

```bash
sudo apt update
sudo apt install -y nodejs npm git postgresql postgresql-contrib
```

### Verificar:

```bash
node -v
npm -v
git --version
psql --version
```

---

## macOS

### Via Homebrew:

```bash
brew install node git postgresql
```

---

# Configuração inicial do projeto

## 1. Clonar repositório

```bash
git clone <URL_DO_REPOSITORIO>
cd fitmanager
```

---

## 2. Criar Frontend

```bash
npm create vite@latest frontend -- --template react-ts
cd frontend
npm install
npm install react-router-dom axios
cd ..
```

---

## 3. Criar Backend

```bash
mkdir backend
cd backend
npm init -y
npm install express cors dotenv bcrypt jsonwebtoken
npm install -D typescript ts-node-dev @types/node @types/express @types/cors
npx tsc --init
```

---

## 4. Configurar Prisma + PostgreSQL

```bash
npm install prisma --save-dev
npm install @prisma/client
npx prisma init
```

---

# Variáveis de ambiente

## Backend (`backend/.env`)

```env
DATABASE_URL="postgresql://postgres:SUA_SENHA@localhost:5432/fitmanager"
JWT_SECRET="sua_chave_secreta"
PORT=3000
```

---

# Rodando o projeto

## Frontend:

```bash
cd frontend
npm run dev
```

### URL padrão:

```txt
http://localhost:5173
```

---

## Backend:

```bash
cd backend
npm run dev
```

### URL padrão:

```txt
http://localhost:3000
```

---

## Banco:

### Rodar migrations:

```bash
npx prisma migrate dev
```

### Visualizar banco:

```bash
npx prisma studio
```

---

# Estrutura do projeto

```txt
fitmanager/
├── frontend/
│   ├── public/
│   ├── src/
│   │   ├── assets/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── routes/
│   │   ├── services/
│   │   ├── context/
│   │   ├── hooks/
│   │   ├── types/
│   │   ├── utils/
│   │   └── styles/
│   └── package.json
│
├── backend/
│   ├── prisma/
│   ├── src/
│   │   ├── config/
│   │   ├── modules/
│   │   ├── middlewares/
│   │   ├── utils/
│   │   └── routes/
│   └── package.json
│
├── docs/
├── README.md
└── .gitignore
```

---

# Perfis de usuário

## Gestor

* Cadastro de alunos
* Controle de instrutores
* Dashboard administrativo

## Instrutor

* Gerenciar treinos
* Associar alunos
* Acompanhar evolução

## Aluno

* Visualizar treinos

---

# Comandos do dia a dia

## Git

```bash
git pull origin main
git checkout -b feature/nome-da-feature
git add .
git commit -m "feat: descrição"
git push origin feature/nome-da-feature
```

---

## Frontend

```bash
npm install nome-do-pacote
npm run dev
npm run build
```

---

## Backend

```bash
npm install nome-do-pacote
npm run dev
npm run build
```

---

## Prisma

```bash
npx prisma migrate dev
npx prisma studio
npx prisma generate
```

---

# Convenções do projeto

## Git

* Branch principal: `main`
* Branch de desenvolvimento: `develop`
* Features: `feature/nome`
* Correções: `fix/nome`

---

## Commits

```txt
feat: adiciona login
fix: corrige autenticação
refactor: reorganiza rotas
style: ajusta layout
```

---

## Frontend

* Componentes em PascalCase
* Páginas separadas por perfil
* Serviços centralizados
* TypeScript obrigatório

---

## Backend

* Arquitetura modular
* Controllers separados
* Services para regra de negócio
* Middlewares para autenticação
* Rotas organizadas

---

# Roadmap inicial

## Etapa 1

* Estrutura base
* Configuração ambiente
* Banco de dados

## Etapa 2

* Login e autenticação
* Controle de perfis

## Etapa 3

* CRUD alunos
* Planos
* Pagamentos

## Etapa 4

* Treinos
* Dashboard
* Responsividade

---

# Troubleshooting

## Erro: package.json não encontrado

```bash
npm create vite@latest frontend -- --template react-ts
```

---

## Erro de conexão PostgreSQL

* Verificar se serviço está ativo
* Confirmar DATABASE_URL
* Verificar porta 5432

---

## Prisma não conecta

```bash
npx prisma generate
npx prisma migrate dev
```

---

## Porta ocupada

### Frontend:

Alterar Vite config

### Backend:

Alterar PORT no `.env`

---

# Boas práticas para equipe

* Nunca subir `.env`
* Sempre atualizar README quando necessário
* Revisar Pull Requests
* Manter padrão de código
* Priorizar clareza e organização
* Testar antes de subir alterações

---

# Equipe

* Erick Araújo Macedo  
* Gabriel Cajado Cavalcante  
* Henrique Varela Barbosa Mouta  
* Nicolas Marcelino da Mota 
* Pedro Henrique Pinheiro de Oliveira  

---

# Licença

Projeto acadêmico para fins educacionais.

---

# Resumo

O FitManager busca fornecer uma solução digital acessível para academias de pequeno e médio porte, promovendo:

* Organização administrativa
* Melhor experiência para alunos
* Gestão centralizada
* Controle financeiro
* Digitalização operacional
