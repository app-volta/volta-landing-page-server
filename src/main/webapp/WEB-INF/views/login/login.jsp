<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR" id="html-login">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" href="${pageContext.request.contextPath}/assets/img/content/logo.png" type="image/x-icon">
    <title>Volta - login</title>
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
            <h1>Bem-vindo(a) de volta!</h1>
            <h2>Login</h2>

            <c:if test="${param.reset == '1'}">
                <div class="mensagem sucesso">Senha redefinida com sucesso. Entre com a nova senha.</div>
            </c:if>
            <c:if test="${error != null}">
                <div class="mensagem erro"><c:out value="${error}"/></div>
            </c:if>
            <c:if test="${flashError != null}">
                <div class="mensagem erro"><c:out value="${flashError}"/></div>
            </c:if>
            <c:if test="${flashSuccess != null}">
                <div class="mensagem sucesso"><c:out value="${flashSuccess}"/></div>
            </c:if>

            <form method="post" action="${pageContext.request.contextPath}/login">
                <label for="email">E-mail:</label>
                <input type="email" id="email" name="email" placeholder="Digite seu e-mail"
                       autocomplete="username" required>

                <label for="senha">Senha:</label>
                <div class="campo-senha">
                    <input type="password" id="senha" class="input-senha" name="password" placeholder="Senha"
                           autocomplete="current-password" required>
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

                <button type="submit">Login</button>

                <p class="link-senha">
                    <button type="button" class="btn-link-senha" data-forgot-password>Esqueci minha senha</button>
                </p>

                <p class="link-cadastro">
                    Ainda não tem conta?
                    <a href="${pageContext.request.contextPath}/cadastro">Cadastre sua empresa</a>
                </p>
            </form>
        </div>
    </div>
    <script src="${pageContext.request.contextPath}/js/forgot-password.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/password-toggle.js" defer></script>
</body>
</html>
