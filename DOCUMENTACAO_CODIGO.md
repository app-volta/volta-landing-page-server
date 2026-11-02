# Documentação do código — VOLTA Back-end

Este documento explica, de forma resumida, a responsabilidade dos principais
arquivos do projeto. Ele complementa o [`README.md`](README.md), que concentra
instalação, configuração e regras de uso.

## Sumário

1. [Visão geral](#1-visão-geral)
2. [Fluxo de uma requisição](#2-fluxo-de-uma-requisição)
3. [Estrutura de pastas](#3-estrutura-de-pastas)
4. [Configuração e build](#4-configuração-e-build)
5. [Controllers](#5-controllers)
6. [DAOs](#6-daos)
7. [Models e enums](#7-models-e-enums)
8. [Services](#8-services)
9. [Utilitários e infraestrutura](#9-utilitários-e-infraestrutura)
10. [Views JSP e JSPF](#10-views-jsp-e-jspf)
11. [JavaScript](#11-javascript)
12. [CSS](#12-css)
13. [HTMLs estáticos](#13-htmls-estáticos)
14. [Testes](#14-testes)
15. [Banco de dados](#15-banco-de-dados)
16. [Como adicionar uma nova entidade](#16-como-adicionar-uma-nova-entidade)
17. [Convenções e observações](#17-convenções-e-observações)

## 1. Visão geral

A VOLTA é uma aplicação Java 17 baseada em:

- Jakarta Servlet 6 e JSP/JSTL;
- PostgreSQL com JDBC;
- HikariCP para pool de conexões;
- jBCrypt para hash de senha;
- Jakarta Mail/Angus Mail para envio de e-mails;
- dotenv-java para leitura do `.env`;
- org.json; BrasilAPI para consultar CNPJ e obter coordenadas por CEP; AwesomeAPI
  para localizar endereço por CEP;
- JUnit 5 para testes.

A aplicação segue uma organização próxima de MVC:

- `controller`: recebe a requisição, valida permissões e dados e decide o fluxo;
- `dao`: executa SQL e converte linhas do banco em objetos;
- `models`: entidades e enums do domínio;
- `service`: operações de negócio que precisam de transação;
- `util`: sessão, validações, senha, e-mail, CEP, CNPJ, mensagens e bootstrap;
- `src/main/webapp/WEB-INF/views`: JSPs internos;
- `src/main/webapp/WEB-INF/jspf`: fragmentos compartilhados.

## 2. Fluxo de uma requisição

1. O navegador chama uma rota como `/empresa?action=list`.
2. O filtro `NoCacheFilter` desativa o cache das respostas e define UTF-8 para
   conteúdo textual.
3. O controller correspondente recebe a requisição.
4. `BaseController` verifica a sessão e monta atributos comuns:
   `systemAdmin`, `companyUser`, `cooperativeUser`, `homePath`, `canManage` e
   `adminSections`.
5. O controller valida permissões, parâmetros e regras de negócio.
6. O DAO executa a consulta ou gravação.
7. Em caso de sucesso, o controller faz `forward` para uma JSP ou `redirect`
   para outra rota.
8. Mensagens de sucesso/erro são transportadas pelo `Flash` e exibidas pelo
   fragmento `flash.jspf`.

Rotas públicas: `/login`, `/acesso-restrito`, `/cadastro`, `/esqueci-senha` e
`/redefinir-senha`. As demais exigem sessão; os CRUDs também verificam o perfil
e o vínculo com empresa/cooperativa.

## 3. Estrutura de pastas

```text
src/main/java/br/com/volta/
  controller/   controllers das rotas e regras de autorização
  dao/          acesso ao PostgreSQL e exclusão em cascata
  models/       entidades, enums e seções do painel
  service/      transações de negócio
  util/         bootstrap, sessão, validação, senha, e-mail, CEP e CNPJ

src/main/webapp/
  index.html                    landing page pública
  area/ coleta/ ...             atalhos HTML para os CRUDs reais
  css/                          CSS das telas JSP
  js/                           JavaScript das telas JSP
  assets/css/ assets/js/ ...    cópia estática usada pela landing page
  WEB-INF/views/                JSPs internos
  WEB-INF/jspf/                 menus, flash e modal compartilhados
  WEB-INF/web.xml               filtro, sessão e páginas de erro

src/test/java/br/com/volta/util/
  testes de validação, senha e recuperação de senha
```

## 4. Configuração e build

### `pom.xml`

Define o projeto Maven com empacotamento `war`, Java 17 e as dependências de
Servlet, PostgreSQL, HikariCP, JSTL, jBCrypt, e-mail, dotenv, JSON, JUnit e
SLF4J. O nome final do artefato é `volta.war`.

### `src/main/webapp/WEB-INF/web.xml`

Configura:

- o filtro `NoCacheFilter` em `/*`;
- `index.html` como welcome file;
- sessão de 30 minutos;
- páginas de erro 401, 403, 404 e 500.

### `.env.example`

Modelo das variáveis locais. As principais são:

- banco: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`;
- admin inicial: `ADMIN_NAME`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`,
  `ADMIN_COMPANY_ID`;
- e-mail: `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`, `SMTP_PASSWORD`,
  `SMTP_FROM`, `SMTP_STARTTLS`;
- aplicação: `APP_BASE_URL`, `APP_ENV`.

O `.env` real não deve ser enviado ao GitHub.

### `.gitignore`

Ignora `target/`, arquivos do IntelliJ, configuração local do Smart Tomcat,
`db.properties`, `.env` e o marcador local
`.volta-admin-password-reset`.

## 5. Controllers

Todos os controllers ficam em `src/main/java/br/com/volta/controller` e usam
`@WebServlet`. A maioria dos CRUDs aceita `action=list`, `new`, `edit`,
`insert`, `update` e `delete`.

| Arquivo | Rota | Responsabilidade |
|---|---|---|
| `BaseController.java` | — | Classe base dos controllers. Verifica sessão, define atributos das JSPs, centraliza redirect, forward e tratamento de erro. |
| `AdminController.java` | `/admin` | Painel exclusivo do `SYSTEM_ADMIN`. Conta registros e monta os grupos `operationSections` e `accessSections`. |
| `AreaController.java` | `/area` | CRUD de unidades/setores. Empresa vê apenas as próprias áreas; admin global vê todas. |
| `CadastroController.java` | `/cadastro` | Cadastro público de empresa. Valida o CNPJ pela BrasilAPI, consulta o CEP e exige endereço em São Paulo, além de validar e-mail e senha; usa `RegistrationService` e redireciona ao login. |
| `CollectionController.java` | `/coleta` | CRUD de coletas, alteração de status e data agendada. Cooperativa vê as próprias coletas; empresa vê as coletas das suas ocorrências. |
| `CompanyController.java` | `/empresa` | CRUD de empresas. Empresa gerencia apenas o próprio cadastro; admin global gerencia todas. Valida CNPJ e CEP. |
| `CooperativeController.java` | `/cooperativa` | CRUD de cooperativas, tipos de resíduo aceitos e coordenadas obtidas pelo CEP. Valida CNPJ e CEP de São Paulo. |
| `ForgotPasswordController.java` | `/esqueci-senha` | Recebe o e-mail, cria token, monta o link e tenta enviar por SMTP. Aceita resposta JSON para o modal. |
| `IncidentController.java` | `/ocorrencia` | CRUD de ocorrências, filtros, status, prioridade, resíduo, área, foto, descrição e peso. Admin escolhe a empresa; empresa fica restrita à própria. |
| `LoginController.java` | `/login`, `/acesso-restrito` | Autentica e redireciona por perfil. `/acesso-restrito` aceita somente `SYSTEM_ADMIN`. |
| `LogoutController.java` | `/logout` | Invalida a sessão e redireciona para a landing page. |
| `ResetPasswordController.java` | `/redefinir-senha` | Valida o token, aplica a política de senha e grava o novo hash BCrypt. |
| `RoleController.java` | `/perfil` | CRUD de perfis. Exclusivo do `SYSTEM_ADMIN`; protege os perfis fixos do sistema. |
| `UserController.java` | `/usuario` | CRUD de usuários: o admin global gerencia usuários de empresas e cooperativas; o administrador da empresa gerencia somente usuários da própria empresa. Protege contra remover o último administrador. |
| `WasteTypeController.java` | `/tipo-residuo` | CRUD de tipos de resíduo, com categoria, descrição e risco padrão. |

### Detalhes importantes

- `BaseController` só deixa passar sem sessão nas rotas públicas. Sem permissão,
  a resposta é `403`.
- `CadastroController` usa `CnpjService` para consultar pela BrasilAPI a existência
  do CNPJ e `ZipCodeService` para localizar o endereço e confirmar que o CEP é de
  São Paulo. Se a consulta de CNPJ estiver indisponível, o cadastro falha de forma
  segura.
- `RegistrationService` grava empresa e primeiro usuário `ADMIN` na mesma
  transação.
- `CollectionController` usa `CollectionStatus.canTransitionTo` para impedir
  voltar etapas ou sair de estados finais.
- `IncidentController` usa `IncidentStatus.canTransitionTo` para controlar o
  fluxo da ocorrência.
- `UserController` impede que o último `SYSTEM_ADMIN` ou o último administrador
  de uma empresa seja removido/rebaixado.

## 6. DAOs

Os DAOs usam `PreparedStatement`, `try-with-resources` e devolvem os objetos do
modelo. Erros de SQL são convertidos em `DaoException`.

| Arquivo | Responsabilidade |
|---|---|
| `DatabaseConnection.java` | Cria e fecha o pool HikariCP; lê `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`. |
| `DaoException.java` | Exceção de persistência; identifica violação de chave estrangeira e de unicidade pelo SQLState. |
| `AreaDAO.java` | Insere, busca, lista, filtra, atualiza, conta e exclui áreas. |
| `CascadeDeleteDAO.java` | Exclui um registro e seus dependentes percorrendo as chaves estrangeiras do banco. |
| `CollectionDAO.java` | CRUD de coletas, filtros por empresa/cooperativa/ocorrência, atualização de status e data. |
| `CompanyDAO.java` | CRUD de empresas, busca por CNPJ, filtros, contagem e inserção dentro de transação. |
| `CooperativeDAO.java` | CRUD de cooperativas, busca por CNPJ, filtros, contagem e vínculo com tipos de resíduo. |
| `IncidentDAO.java` | CRUD de ocorrências, filtros por empresa, área, usuário e status, contagem e atualização de status. |
| `RoleDAO.java` | CRUD de perfis, busca por tipo, filtros e contagem de usuários. |
| `UserDAO.java` | CRUD de usuários, busca por e-mail, filtros, vínculo com empresa/cooperativa, troca de senha e inserção transacional. |
| `WasteTypeDAO.java` | CRUD de tipos de resíduo, filtros, vínculo com cooperativas e contagem. |

Além das tabelas principais, `CollectionDAO` grava o histórico em
`collection_status`, e `CooperativeDAO` consulta `review` para calcular a média
de avaliações. Essas tabelas são usadas diretamente mesmo sem terem um DAO
próprio.

## 7. Models e enums

Os models representam as tabelas e os relacionamentos do domínio.

| Arquivo | O que representa |
|---|---|
| `AdminSection.java` | Enum com as seções administrativas: chave, label e rota. |
| `Area.java` | Unidade/setor de uma empresa (`company`, `sectorName`, `locationDescription`). |
| `Collection.java` | Coleta vinculada a uma ocorrência e a uma cooperativa, com data, status, tipo, urgência e tempo estimado. |
| `CollectionStatus.java` | Status `SOLICITADA`, `AGENDADA`, `ACEITA`, `EM_ANDAMENTO`, `CONCLUIDA`, `CANCELADA` e regras de transição. |
| `Company.java` | Empresa com nome, CNPJ, CEP e endereço. |
| `ContaminationLevel.java` | Níveis `BAIXO`, `MEDIO`, `ALTO` para contaminação. |
| `Cooperative.java` | Cooperativa com CNPJ, CEP, coordenadas, horários, capacidade e tipos de resíduo. |
| `Incident.java` | Ocorrência com empresa, usuário, área, resíduo, prioridade, status, contaminação, foto, descrição, data e peso. |
| `IncidentStatus.java` | Status `PENDENTE`, `EM_ANALISE`, `RESOLVIDA`, `CANCELADA` e regras de transição. |
| `Priority.java` | Prioridade `BAIXA`, `MEDIA`, `ALTA`, `URGENTE`. |
| `RiskLevel.java` | Risco `BAIXO`, `MEDIO`, `ALTO` usado no tipo de resíduo. |
| `Role.java` | Perfil de acesso (`ADMIN`, `COMPANY_USER`, `COOPERATIVE`, `SYSTEM_ADMIN`) e quantidade de usuários. |
| `User.java` | Usuário com empresa ou cooperativa, perfil, nome, e-mail, senha hash e cargo. |
| `WasteType.java` | Tipo de resíduo com categoria, descrição e risco padrão. |

### Transições de status

- **Ocorrência:** `PENDENTE → EM_ANALISE/CANCELADA`; `EM_ANALISE →
  RESOLVIDA/CANCELADA`; `RESOLVIDA` e `CANCELADA` são finais.
- **Coleta:** `SOLICITADA → AGENDADA/ACEITA/CANCELADA`; `AGENDADA →
  ACEITA/EM_ANDAMENTO/CANCELADA`; `ACEITA → EM_ANDAMENTO/CANCELADA`;
  `EM_ANDAMENTO → CONCLUIDA/CANCELADA`; `CONCLUIDA` e `CANCELADA` são finais.

## 8. Services

### `RegistrationService.java`

Cria a empresa e o primeiro usuário administrador dela na mesma transação. Se a
empresa for gravada, mas o usuário falhar, o `rollback` desfaz os dois inserts.
A senha é convertida em hash BCrypt antes de ser gravada.

## 9. Utilitários e infraestrutura

| Arquivo | Responsabilidade |
|---|---|
| `AdminAccount.java` | Centraliza o e-mail do administrador configurado no ambiente. |
| `AppBootstrap.java` | Listener executado no start do Tomcat: garante os perfis do sistema, cria o admin global e fecha o pool no shutdown. |
| `AuthSession.java` | Inicia a sessão, recarrega os relacionamentos do usuário e remove a senha da sessão. |
| `CnpjService.java` | Consulta CNPJ na BrasilAPI, com cache de 10 minutos e falha fechada quando o serviço está indisponível. |
| `EmailService.java` | Valida a configuração SMTP e envia o link de recuperação de senha. |
| `Env.java` | Lê o `.env` e as variáveis de ambiente, procurando o arquivo também no diretório do Tomcat. |
| `Flash.java` | Guarda mensagens de sucesso/erro na sessão e as expõe na próxima requisição. |
| `NoCacheFilter.java` | Desativa cache das respostas e define UTF-8 para recursos textuais. |
| `PasswordResetService.java` | Mantém tokens de recuperação em memória, com validade de 30 minutos, uso único e cooldown de 60 segundos. |
| `PasswordUtil.java` | Gera e verifica hashes BCrypt; rejeita hashes legados ou malformados. |
| `SessionUtil.java` | Centraliza o usuário logado e as verificações de perfil, empresa e permissão. |
| `Validator.java` | Valida CNPJ, CPF, CEP, e-mail, URL, celular, senha, São Paulo e converte números/enums. |
| `ZipCodeService.java` | Consulta endereço/coordenadas por CEP usando BrasilAPI e AwesomeAPI. |

## 10. Views JSP e JSPF

Todas as JSPs ficam em `WEB-INF/views` e usam `pageEncoding="UTF-8"`. A saída de
dados dinâmicos usa `<c:out>` para escapar HTML.

### Fragmentos compartilhados

| Arquivo | Função |
|---|---|
| `jspf/nav.jspf` | Menu lateral, logo, links por perfil, nome do usuário e botão Sair. |
| `jspf/flash.jspf` | Exibe mensagens de sucesso, erro e aviso. |
| `jspf/confirm-dialog.jspf` | Modal `<dialog>` reutilizável para confirmar exclusões. |

### Login e painel

| Arquivo | Função |
|---|---|
| `views/login/login.jsp` | Login público de empresas, cooperativas e administradores. |
| `views/login/acesso-restrito.jsp` | Login exclusivo do administrador do sistema. |
| `views/login/painel.jsp` | Painel `/admin` com os grupos Operação e Acesso. |

### Cadastro e recuperação de senha

| Arquivo | Função |
|---|---|
| `views/cadastro/cadastro.jsp` | Cadastro público de empresa e primeiro administrador. |
| `views/esqueci-senha/redefinir-senha.jsp` | Formulário de nova senha a partir de um token. |

### CRUDs por entidade

Cada entidade tem uma página de listagem e uma de formulário:

| Entidade | Lista | Formulário |
|---|---|---|
| Áreas | `views/area/lista.jsp` | `views/area/formulario.jsp` |
| Coletas | `views/coleta/lista.jsp` | `views/coleta/formulario.jsp` |
| Cooperativas | `views/cooperativa/lista.jsp` | `views/cooperativa/formulario.jsp` |
| Empresas | `views/empresa/lista.jsp` | `views/empresa/formulario.jsp` |
| Ocorrências | `views/ocorrencia/lista.jsp` | `views/ocorrencia/formulario.jsp` |
| Perfis | `views/perfil/lista.jsp` | `views/perfil/formulario.jsp` |
| Tipos de resíduo | `views/tipo-residuo/lista.jsp` | `views/tipo-residuo/formulario.jsp` |
| Usuários | `views/usuario/lista.jsp` | `views/usuario/formulario.jsp` |

As listas mostram filtros, tabela, ações de editar/excluir e botão de criação. Os
formulários servem tanto para criação quanto para edição e carregam os selects
necessários no controller.

### Erros

`views/error/401.jsp`, `403.jsp`, `404.jsp` e `500.jsp` são mapeados no
`web.xml` e mostram uma mensagem amigável para cada código HTTP.

## 11. JavaScript

| Arquivo | Função |
|---|---|
| `js/cadastro.js` | Confere se senha e confirmação são iguais antes de enviar o cadastro. |
| `js/forgot-password.js` | Monta o modal de recuperação de senha, envia a requisição e mostra o link em ambiente de desenvolvimento. |
| `js/landing.js` | Controla a interação dos celulares e dos detalhes na landing page. |
| `js/password-toggle.js` | Mostra/oculta a senha nos campos com `data-toggle-password`, trocando ícone e `aria-label`. |
| `assets/js/cadastrar.js` | Versão estática antiga da validação de confirmação de senha. |
| `assets/js/landing.js` | Versão da interação da landing page usada pelo `index.html`. |
| `assets/js/main.js` | Script auxiliar antigo da landing page; não é carregado pelas JSPs internas. |

## 12. CSS

| Arquivo | Função |
|---|---|
| `css/base.css` | Reset, tipografia e variáveis de cor. |
| `css/landing.css` | Landing page pública. |
| `css/app.css` | Layout interno: menu lateral, CRUDs, tabelas, formulários, painel administrativo e responsividade. |
| `css/login-cadastrar.css` | Login, cadastro, redefinição de senha e botão de mostrar/ocultar senha. |
| `assets/css/base.css`, `assets/css/landing.css` | Estilos carregados pela landing page (`index.html`). |
| `assets/css/login-cadastrar.css`, `assets/css/app.css` | Cópias estáticas mantidas em `assets`; as telas JSP usam os arquivos correspondentes de `css/`. |

O painel administrativo usa cards compactos em `app.css`, com os grupos
**Operação** e **Acesso**, números em verde e legenda de cadastros.

## 13. HTMLs estáticos

| Arquivo | Função |
|---|---|
| `index.html` | Landing page pública com apresentação da VOLTA. |
| `login.html`, `login-crud.html` | Redirecionam para `/login`. |
| `area/formulario.html`, `area/lista.html` | Redirecionam para `/area?action=...`. |
| `coleta/*.html` | Redirecionam para `/coleta?action=...`. |
| `cooperativa/*.html` | Redirecionam para `/cooperativa?action=...`. |
| `empresa/*.html` | Redirecionam para `/empresa?action=...`. |
| `ocorrencia/*.html` | Redirecionam para `/ocorrencia?action=...`. |
| `usuario/*.html` | Redirecionam para `/usuario?action=...`. |

Esses HTMLs existem para manter compatibilidade com os links da landing page e
não contêm regra de negócio; o CRUD real é sempre a rota Java.

## 14. Testes

Os testes ficam em `src/test/java/br/com/volta/util`:

| Arquivo | Cobertura |
|---|---|
| `ValidatorTest.java` | CNPJ, CPF, celular, CEP, e-mail, URL, São Paulo, senha, enums e números. |
| `PasswordUtilTest.java` | Hash BCrypt, verificação e rejeição de hash legado/malformado. |
| `PasswordResetServiceTest.java` | Criação, consumo, expiração, cooldown, descarte e tokens por usuário. |

Comandos:

```bash
mvn test
mvn clean package
```

Os testes atuais não acessam banco nem APIs externas.

## 15. Banco de dados

As tabelas consultadas diretamente pelos DAOs são:

- `company`
- `cooperative`
- `cooperative_waste_type`
- `users`
- `role`
- `area`
- `incident`
- `collection`
- `collection_status` (histórico de mudanças de status)
- `review` (avaliações usadas no cálculo da média das cooperativas)
- `waste_type`

Observações:

- O script de criação do banco é mantido externamente pelo grupo.
- A tabela `users` possui a constraint `chk_users_owner`, que exige vínculo com
  empresa ou cooperativa. Por isso o admin global usa uma empresa de referência
  configurada em `ADMIN_COMPANY_ID` ou a primeira empresa cadastrada.
- A exclusão em cascata é feita pelo `CascadeDeleteDAO`, que percorre as chaves
  estrangeiras antes de apagar o registro principal.
- O pool é criado sob demanda e fechado no shutdown do Tomcat.

## 16. Como adicionar uma nova entidade

1. Crie o model em `models/`.
2. Crie o DAO em `dao/` com os métodos de CRUD.
3. Crie o controller em `controller/` estendendo `BaseController` e anote com
   `@WebServlet("/nova-rota")`.
4. Crie `lista.jsp` e `formulario.jsp` em `WEB-INF/views/nova-entidade/`.
5. Adicione a seção em `AdminSection` para ela aparecer no menu lateral.
6. Adicione o valor em `operationSections` ou `accessSections` no
   `AdminController` para aparecer no painel.
7. Inclua a entidade na exclusão em cascata, se ela tiver dependentes.
8. Crie testes para as validações e regras novas.

## 17. Convenções e observações

- Código e comentários em português, com pacotes em minúsculo.
- Todos os arquivos textuais devem permanecer em UTF-8.
- SQL sempre com `PreparedStatement`.
- Conexões, statements e result sets com `try-with-resources`.
- Escritas sempre por POST; listagens por GET.
- Depois de um POST, o padrão é redirecionar e usar `Flash`.
- JSPs usam `<c:out>` para dados dinâmicos.
- A senha nunca é guardada em texto puro: apenas o hash BCrypt.
- O `.env` e o marcador local de senha do admin não vão para o repositório.
- A sessão expira em 30 minutos.
- Uma requisição protegida sem sessão é redirecionada para `/login`; quando há
  sessão, mas falta permissão para a operação, o sistema responde com 403.
- O painel `/admin` é exclusivo do `SYSTEM_ADMIN`.
