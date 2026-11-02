# VOLTA — Back-end

Sistema web de gestão de resíduos, ocorrências e coletas.
O sistema organiza o fluxo **EMPRESA → VOLTA → COOPERATIVA**. A empresa pode se
cadastrar pela landing page, entrar com o próprio login e gerenciar somente os
seus dados (unidades, funcionários, ocorrências e coletas). O administrador
global continua com acesso a todas as empresas e cooperativas.

Projeto do Instituto J&F (Germinare Tech).

## Repositórios

- **Back-end:** [`app-volta/volta-landing-page-server`](https://github.com/app-volta/volta-landing-page-server)
  - Branch de desenvolvimento: `back-end`
- **Landing page / front-end público:** [`app-volta/volta-landing-page-static`](https://github.com/app-volta/volta-landing-page-static)

## Tecnologias

- Java 17
- Maven
- Jakarta Servlet 6 e JSP/JSTL
- PostgreSQL com JDBC
- HikariCP (pool de conexões)
- jBCrypt (hash de senha)
- Jakarta Mail / Angus Mail (envio de e-mail)
- dotenv-java (variáveis de ambiente)
- org.json (consultas de CEP e CNPJ)
- BrasilAPI (consulta de CNPJ e CEP)
- JUnit 5 (testes automatizados)
- Tomcat 10.1 ou superior

## Estrutura

```text
src/main/java/br/com/volta/
  controller/  recebe as requisições HTTP (camada C do MVC)
  dao/         acesso ao banco com JDBC/PreparedStatement e DatabaseConnection (HikariCP)
  models/      entidades, enums e AdminSection (seções do painel)
  service/     RegistrationService (cadastro de empresa + administrador em transação)
  util/        PasswordUtil, EmailService, PasswordResetService, Env, ZipCodeService,
               CnpjService, SessionUtil, AuthSession, Flash, NoCacheFilter, Validator,
               AdminAccount e AppBootstrap

src/main/webapp/
  index.html
  area/ coleta/ cooperativa/ empresa/ ocorrencia/ usuario/   atalhos HTML que redirecionam ao CRUD real
  css/  assets/img/ js/                                      estilos, imagens e scripts do front-end
  WEB-INF/views/   páginas JSP internas por entidade (area, coleta, cooperativa, empresa,
                    ocorrencia, usuario, perfil, tipo-residuo, login, cadastro,
                    esqueci-senha, error)
  WEB-INF/jspf/    menus, mensagens e modal de confirmação compartilhados
  WEB-INF/web.xml

```

A árvore pública de `src/main/webapp` foi sincronizada a partir do repositório
[`app-volta/volta-landing-page-static`](https://github.com/app-volta/volta-landing-page-static).
O back-end fica no repositório
[`app-volta/volta-landing-page-server`](https://github.com/app-volta/volta-landing-page-server),
na branch `back-end`. O `WEB-INF` contém apenas os recursos internos do back-end
(JSPs, includes e `web.xml`) e não faz parte do front-end.

Para uma visão arquivo por arquivo, consulte
[`DOCUMENTACAO_CODIGO.md`](DOCUMENTACAO_CODIGO.md).

## Como rodar

### 1. Requisitos

- JDK 17
- Maven
- Tomcat 10.1 ou superior (no IntelliJ, plugin Smart Tomcat)
- Um banco PostgreSQL com as tabelas criadas

### 2. Banco de dados

Execute no PostgreSQL o script SQL do banco (arquivo mantido pelo grupo)
para criar as tabelas.

### 3. Variáveis de ambiente

Copie o arquivo de exemplo e preencha com os dados do seu banco:

```bash
cp .env.example .env
```

No Windows, copie o arquivo `.env.example` e renomeie a cópia para `.env`.

Preencha as variáveis do banco:

| Variável | O que é |
|---|---|
| `DB_URL` | URL JDBC do banco |
| `DB_USERNAME` | usuário do banco |
| `DB_PASSWORD` | senha do banco |

Para a conta administrativa inicial:

| Variável | O que é |
|---|---|
| `ADMIN_NAME` | nome exibido do administrador global |
| `ADMIN_EMAIL` | e-mail da conta administrativa inicial |
| `ADMIN_PASSWORD` | senha inicial da conta administrativa |
| `ADMIN_COMPANY_ID` | empresa usada como vínculo técnico (a constraint `chk_users_owner` exige empresa ou cooperativa) |

O bootstrap roda no start do Tomcat, garante os cargos `SYSTEM_ADMIN`,
`COMPANY_USER`, `ADMIN` e `COOPERATIVE` e cria a conta administrativa se ela
ainda não existir. Na primeira subida, a senha é sincronizada com `ADMIN_PASSWORD` uma
única vez; depois disso, a recuperação de senha pode alterar a senha sem ser
sobrescrita no próximo start. Para aplicar `ADMIN_PASSWORD` novamente, apague o
arquivo `.volta-admin-password-reset` da raiz do projeto e reinicie o Tomcat.

Para a recuperação de senha por e-mail:

| Variável | O que é |
|---|---|
| `SMTP_HOST` | servidor SMTP |
| `SMTP_PORT` | porta SMTP (normalmente `587`) |
| `SMTP_USER` | usuário de autenticação SMTP |
| `SMTP_PASSWORD` | senha de app do SMTP |
| `SMTP_FROM` | remetente que aparece no e-mail |
| `SMTP_STARTTLS` | `true` para usar STARTTLS |
| `APP_BASE_URL` | URL pública da aplicação, sem barra no final |
| `APP_ENV` | `dev` para liberar o link de recuperação no log |

`APP_BASE_URL` é recomendada para gerar o link de recuperação com o endereço
público correto. Se ela não estiver definida, o sistema monta o link a partir da
própria requisição (esquema, host, porta e contexto). Em produção, configure-a
explicitamente. Se o SMTP não estiver configurado, o sistema continua
funcionando; em `APP_ENV=dev` o link de recuperação é gravado no log do Tomcat
para testes.

Para o Gmail, `SMTP_PASSWORD` **não é a senha normal da conta**: é uma senha de
app de 16 letras, gerada em <https://myaccount.google.com/apppasswords> com a
verificação em duas etapas ativa. Sem ela o envio não acontece e o log registra
qual variável está faltando.

O `.env` está no `.gitignore` e **não deve ser enviado ao GitHub**.

### 4. Executar no IntelliJ

1. Abra a pasta do projeto no IntelliJ e aguarde o Maven baixar as dependências.
2. Crie uma configuração **Smart Tomcat** apontando para o seu Tomcat.
   Deployment directory: `src/main/webapp`. Context path: `/volta`.
3. Execute a configuração.
4. Acesse `http://localhost:8080/volta/`.

> **Atenção com o `.env` no Smart Tomcat:** o plugin inicia o Tomcat com o
> diretório de trabalho na pasta de configuração dele
> (`%USERPROFILE%\.SmartTomcat\<projeto>\<contexto>`). Como o `.env` é procurado
> primeiro no diretório atual, essa cópia tem prioridade sobre o `.env` do
> projeto. Mantenha as duas iguais (copie o `.env` do projeto para essa pasta)
> ou a aplicação usará credenciais antigas. O caminho do `.env` carregado
> aparece no log de inicialização como `Arquivo .env carregado de: ...`.

A página inicial é o `index.html` do front-end (`/`). Os botões **Login** e
**Cadastro** da landing page apontam para `http://localhost:8080/volta/login` e
`http://localhost:8080/volta/cadastro`. O cadastro público cria a empresa e o
primeiro usuário `ADMIN` dela; unidades, funcionários, ocorrências e coletas são
cadastrados depois, dentro da área da empresa. As páginas HTML em `area/`,
`coleta/`, `empresa/` etc. são atalhos que redirecionam para os endpoints reais
dos controllers (`/area`, `/coleta`...).

### 5. Gerar o .war (opcional)

```bash
mvn clean package
```

O arquivo `target/volta.war` pode ser colocado na pasta `webapps` do Tomcat.

## Funcionalidades

Controllers disponíveis: `/login`, `/acesso-restrito`, `/logout`, `/cadastro`, `/esqueci-senha`,
`/redefinir-senha`, `/admin`, `/empresa`, `/cooperativa`, `/usuario`, `/area`,
`/ocorrencia`, `/coleta`, `/perfil` e `/tipo-residuo`.

### Acesso e perfis

- `/` serve `index.html`, o front-end público copiado do repositório `volta-landing-page-static`.
- `/cadastro` é público: cria a empresa e o primeiro usuário `ADMIN` na mesma
  transação. Depois do cadastro, o usuário é enviado para `/login` com uma mensagem
  de sucesso.
- O CNPJ é validado pelo dígito verificador, consultado na BrasilAPI/Receita
  Federal e não pode ser repetido. Se o serviço de consulta estiver indisponível,
  o cadastro é bloqueado com uma mensagem de tentativa novamente.
- O CEP é consultado automaticamente; o endereço é preenchido pelo servidor e o
  CEP precisa pertencer à cidade de São Paulo (`SP`). O formulário não exibe mais
  o texto de localização embaixo do campo.
- `/login` autentica administradores, empresas e cooperativas e redireciona cada
  perfil para sua área inicial:
  - `SYSTEM_ADMIN` → `/admin`, com acesso a todas as empresas e cooperativas;
  - `ADMIN` de empresa → `/empresa?action=dashboard`, restrito à própria empresa;
  - `COOPERATIVE` → `/coleta?action=list`, restrito às próprias coletas.
- `/acesso-restrito` tem uma tela separada, sem cadastro de empresa, e aceita
  somente o perfil `SYSTEM_ADMIN` para abrir o painel administrativo.
- Credenciais inválidas mostram sempre a mesma mensagem genérica.
- As telas de login, acesso restrito, cadastro e redefinição de senha têm botão
  para mostrar/ocultar a senha digitada.
- Os controllers de `/admin` e dos CRUDs verificam a sessão e o perfil antes de
  executar qualquer operação; sem sessão, a resposta é **403**. Empresas só
  enxergam e alteram registros vinculados ao próprio `company_id`.
- A exclusão de registros usa um modal HTML `<dialog>` reutilizável, sempre por POST.
- Ao excluir um registro, o sistema também remove os dependentes vinculados
  (por exemplo: excluir uma cooperativa apaga as coletas, históricos, avaliações,
  conversas e usuários ligados a ela). O login da conta administrativa é protegido:
  a empresa vinculada ao administrador não pode ser excluída pelo painel.
- O logo no topo leva para a página inicial do perfil; o botão **Sair** encerra a
  sessão e retorna ao login correspondente ao perfil.

### Área da empresa

Depois do login, a empresa abre `/empresa?action=dashboard` com atalhos para os CRUDs. O menu exibe **Painel**, **Minha empresa**, **Unidades**,
**Funcionários**, **Ocorrências** e **Coletas**:

| Tela | Rota | O que a empresa gerencia |
|---|---|---|
| Minha empresa | `/empresa?action=list` | nome, CNPJ, CEP e endereço da própria empresa |
| Unidades | `/area?action=list` | setores/unidades vinculados à própria empresa |
| Funcionários | `/usuario?action=list` | usuários da própria empresa |
| Ocorrências | `/ocorrencia?action=list` | ocorrências das unidades da própria empresa |
| Coletas | `/coleta?action=list` | coletas geradas pelas ocorrências da própria empresa |

O cadastro público não cria unidade nem funcionário automaticamente: a empresa
cria a primeira unidade em **Unidades** e os demais funcionários em
**Funcionários**. O usuário criado no cadastro público é o `ADMIN` da empresa e
pode gerenciar esses registros.

### Seções do painel

As seções são definidas em `AdminSection` e usadas pelo menu lateral. O painel
`/admin` lê os contadores de `sectionCounts` e organiza os cards em dois grupos
definidos no `AdminController`:

- `operationSections`: Empresas, Cooperativas, Áreas, Ocorrências e Coletas.
- `accessSections`: Usuários, Tipos de resíduo e Perfis de acesso.

| Seção | Rota | Entidade / DAO | Grupo no painel |
|---|---|---|---|
| Empresas | `/empresa` | `CompanyDAO` | Operação |
| Cooperativas | `/cooperativa` | `CooperativeDAO` + `WasteTypeDAO` | Operação |
| Áreas | `/area` | `AreaDAO` | Operação |
| Ocorrências | `/ocorrencia` | `IncidentDAO` | Operação |
| Coletas | `/coleta` | `CollectionDAO` | Operação |
| Usuários | `/usuario` | `UserDAO`, `RoleDAO` | Acesso |
| Tipos de resíduo | `/tipo-residuo` | `WasteTypeDAO` | Acesso |
| Perfis de acesso | `/perfil` | `RoleDAO` | Acesso |

Cada seção tem listagem com filtros combináveis, criação, edição, exclusão com
confirmação e mensagens de sucesso. As listas permitem filtrar por texto,
relacionamentos, enums e períodos; a ocorrência pode ser criada pelo botão
**Nova ocorrência**, com seleção de empresa, usuário, área, resíduo, prioridade,
contaminação, URL de foto, descrição e peso estimado validados no servidor.

Para adicionar uma nova seção ao menu, inclua-a em `AdminSection`. Para ela
também aparecer no painel, adicione o valor à lista `operationSections` ou
`accessSections` no `AdminController`. O visual do painel é compacto, com cards
brancos, número em destaque e legenda **Cadastradas** ou **Cadastrados**.

### Perfis de dados

- `SYSTEM_ADMIN`: administrador global, gerencia empresas, cooperativas, usuários,
  áreas, ocorrências, coletas, perfis e tipos de resíduo.
- `ADMIN`: administrador de uma empresa, criado no cadastro público; gerencia os
  dados da própria empresa.
- `COMPANY_USER`: usuário comum de empresa; pode autenticar, mas não pode gravar
  dados (as ações de escrita exigem `ADMIN` ou `SYSTEM_ADMIN`).
- `COOPERATIVE`: usuário de cooperativa; acessa as coletas da própria cooperativa
  e pode alterar apenas status e data agendada.

## Regras gerais

- A sessão é criada por `AuthSession` e consultada pelos controllers via
  `SessionUtil`. Cada perfil é redirecionado para a sua rota inicial.
- A exclusão de qualquer registro acontece somente por POST, nunca por GET.
- O painel `/admin` é exclusivo do `SYSTEM_ADMIN`.
- O cadastro público e o cadastro de empresa + administrador usam uma única
  transação: se o usuário não puder ser criado, a empresa também não fica no banco.
- As senhas são gravadas e verificadas somente com BCrypt. Hashes legados de
  outro algoritmo não autenticam e precisam ser redefinidos.
- O pool HikariCP é criado no start e fechado no shutdown. Os DAOs usam
  `try-with-resources`, que devolve a conexão ao pool em vez de encerrá-la.

## Banco de dados

Os CRUDs usam apenas as tabelas já existentes (`company`, `cooperative`,
`cooperative_waste_type`, `users`, `role`, `area`, `incident`, `collection` e
`waste_type`). O cadastro público também usa essas mesmas tabelas.

## Recuperação de senha

1. Na tela de login, o botão **Esqueci minha senha** abre um pop-up.
2. O pop-up pede o e-mail da conta. Qualquer usuário cadastrado (`SYSTEM_ADMIN`,
   `ADMIN`, `COMPANY_USER` ou `COOPERATIVE`) pode receber o link.
3. O link é enviado para o próprio endereço informado, por SMTP.
4. O reenvio fica limitado por um intervalo de 60 segundos por usuário no servidor.
5. O token é aleatório, válido por 30 minutos e de uso único.
6. O link abre `/redefinir-senha?token=...`, onde a nova senha é informada.
7. A senha é gravada com BCrypt e o token é consumido.
8. Para e-mails não cadastrados, a resposta é genérica, sem revelar a existência
   da conta.

O link é montado a partir de `APP_BASE_URL`; se essa variável não estiver
configurada, o sistema usa o endereço da própria requisição (esquema, host,
porta e contexto).

Em `APP_ENV=dev`, se o envio falhar, o pop-up também mostra o link de
recuperação na tela para não bloquear o teste local. Em produção, use
`APP_ENV=prod`.

### Gmail

O Gmail não aceita a senha normal da conta no SMTP. É obrigatório usar uma
**senha de app** de 16 caracteres:

1. Ative a verificação em duas etapas em `https://myaccount.google.com/security`.
2. Gere a senha em `https://myaccount.google.com/apppasswords`.
3. Copie as 16 letras (sem espaços) e coloque em `SMTP_PASSWORD` no `.env`.
4. Reinicie o Tomcat.

O projeto já vem com `SMTP_HOST=smtp.gmail.com`, `SMTP_PORT=587`,
`SMTP_USER` e `SMTP_FROM` apontando para a conta VOLTA. Falta apenas preencher
`SMTP_PASSWORD` com a senha de app.

## Equipe

- Breno Gomes Dos Santos
- Carlos Eduardo Rodrigues Do Amaral
- Davi Liu Evangelista
- Enzo Herrera Ribeiro
- Gabriel Peotta
- Gustavo Azenha da Silva
- Gustavo Melo de Souza
- Lucas Fabiano Rodrigues De Oliveira
- Lucca Davi Begliomini
- Miguel Lapa Sperandio

## Testes

```bash
mvn test
```

Os testes cobrem validação de CNPJ/CPF, CEP, e-mail, URL e celular, cidade/estado
de São Paulo, força de senha, conversão de enums e números, normalização de
e-mail, BCrypt e tokens de recuperação de senha.

## Documentação do código

O arquivo [`DOCUMENTACAO_CODIGO.md`](DOCUMENTACAO_CODIGO.md) explica, de forma
resumida e com sumário, a responsabilidade de cada controller, DAO, model,
serviço, utilitário, JSP, script e arquivo de configuração do projeto.
