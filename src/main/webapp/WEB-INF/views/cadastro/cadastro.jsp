<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" href="${pageContext.request.contextPath}/assets/img/content/logo.png" type="image/x-icon">
    <title>Volta - cadastro de empresa</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login-cadastrar.css">
    <!-- Fonte Belanosima -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Alata&family=Belanosima:wght@400;600;700&display=swap" rel="stylesheet">
</head>
<body data-context-path="${pageContext.request.contextPath}">
    <div id="container">
        <div class="lado-imagem" aria-hidden="true"></div>
        <div class="content">
            <a href="${pageContext.request.contextPath}/" aria-label="Ir para a página inicial">
                <img id="Logo-VOLTA" src="${pageContext.request.contextPath}/assets/img/content/Logo_VOLTA.png" alt="Logo do Volta">
            </a>
            <h1>Bem-vindo(a)!</h1>
            <h2>Cadastro</h2>

            <c:if test="${error != null}">
                <div class="mensagem erro"><c:out value="${error}"/></div>
            </c:if>
            <c:if test="${flashError != null}">
                <div class="mensagem erro"><c:out value="${flashError}"/></div>
            </c:if>

            <form id="form-cadastro" method="post" action="${pageContext.request.contextPath}/cadastro">
                <label for="name">Nome da empresa:</label>
                <input type="text" id="name" name="name" placeholder="Nome da Empresa"
                       maxlength="150" autocomplete="organization"
                       value="<c:out value='${company.name}'/>" required>

                <label for="email">E-mail:</label>
                <input type="email" id="email" name="email" placeholder="E-mail"
                       maxlength="150" autocomplete="email"
                       value="<c:out value='${email}'/>" required>

                <label for="cnpj">CNPJ:</label>
                <input type="text" id="cnpj" name="cnpj" placeholder="CNPJ"
                       inputmode="numeric" maxlength="18" autocomplete="off"
                       value="<c:out value='${company.cnpj}'/>" required>

                <label for="cep">CEP:</label>
                <input type="text" id="cep" name="cep" placeholder="CEP"
                       inputmode="numeric" maxlength="9" autocomplete="postal-code"
                       value="<c:out value='${company.cep}'/>" required>

                <label for="senha">Senha:</label>
                <div class="campo-senha">
                    <input type="password" id="senha" class="input-senha" name="password"
                           placeholder="Senha" minlength="8" maxlength="72"
                           autocomplete="new-password" required>
                    <button type="button" class="btn-olho" data-toggle-password="senha"
                            aria-label="Mostrar senha" aria-pressed="false" title="Mostrar senha" hidden>
                        <svg class="icone-olho" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"
                             fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                             stroke-linejoin="round" aria-hidden="true">
                            <path d="M2.062 12.348a1 1 0 0 1 0-.696 10.75 10.75 0 0 1 19.876 0 1 1 0 0 1 0 .696 10.75 10.75 0 0 1-19.876 0"/>
                            <circle cx="12" cy="12" r="3"/>
                        </svg>
                        <svg class="icone-olho-fechado" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"
                             fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                             stroke-linejoin="round" aria-hidden="true">
                            <path d="m2 2 20 20"/>
                            <path d="M6.71 6.71A10.75 10.75 0 0 0 2.062 11.652a1 1 0 0 0 0 .696 10.75 10.75 0 0 0 15.228 5.992"/>
                            <path d="M10.73 10.73a3 3 0 0 0 4.24 4.24"/>
                            <path d="M17.29 17.29A10.75 10.75 0 0 0 21.938 12.348a1 1 0 0 0 0-.696 10.75 10.75 0 0 0-5.99-6.25"/>
                        </svg>
                    </button>
                </div>

                <label for="confirmar-senha">Confirmar Senha:</label>
                <div class="campo-senha">
                    <input type="password" id="confirmar-senha" class="input-senha" name="confirmPassword"
                           placeholder="Confirmar Senha" minlength="8" maxlength="72"
                           autocomplete="new-password" required>
                    <button type="button" class="btn-olho" data-toggle-password="confirmar-senha"
                            aria-label="Mostrar senha" aria-pressed="false" title="Mostrar senha" hidden>
                        <svg class="icone-olho" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"
                             fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                             stroke-linejoin="round" aria-hidden="true">
                            <path d="M2.062 12.348a1 1 0 0 1 0-.696 10.75 10.75 0 0 1 19.876 0 1 1 0 0 1 0 .696 10.75 10.75 0 0 1-19.876 0"/>
                            <circle cx="12" cy="12" r="3"/>
                        </svg>
                        <svg class="icone-olho-fechado" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"
                             fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                             stroke-linejoin="round" aria-hidden="true">
                            <path d="m2 2 20 20"/>
                            <path d="M6.71 6.71A10.75 10.75 0 0 0 2.062 11.652a1 1 0 0 0 0 .696 10.75 10.75 0 0 0 15.228 5.992"/>
                            <path d="M10.73 10.73a3 3 0 0 0 4.24 4.24"/>
                            <path d="M17.29 17.29A10.75 10.75 0 0 0 21.938 12.348a1 1 0 0 0 0-.696 10.75 10.75 0 0 0-5.99-6.25"/>
                        </svg>
                    </button>
                </div>

                <button type="submit">Cadastre-se</button>

                <p>Já tem uma conta? <a href="${pageContext.request.contextPath}/login">Faça login</a></p>
            </form>
        </div>
    </div>
    <script src="${pageContext.request.contextPath}/js/cadastro.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/password-toggle.js" defer></script>
</body>
</html>
