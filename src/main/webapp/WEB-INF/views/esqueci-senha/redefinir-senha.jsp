<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" href="${pageContext.request.contextPath}/assets/img/content/logo.png" type="image/x-icon">
    <title>Volta - redefinir senha</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login-cadastrar.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Belanosima:wght@400;600;700&display=swap" rel="stylesheet">
</head>
<body>
    <div id="container">
        <div class="lado-imagem" aria-hidden="true"></div>
        <div class="content">
            <a href="${pageContext.request.contextPath}/login" aria-label="Voltar ao login">
                <img id="Logo-VOLTA" src="${pageContext.request.contextPath}/assets/img/content/Logo_VOLTA.png" alt="Logo do Volta">
            </a>
            <h1>Redefinir senha</h1>

            <c:if test="${error != null}">
                <div class="mensagem erro"><c:out value="${error}"/></div>
            </c:if>
            <c:if test="${flashError != null}">
                <div class="mensagem erro"><c:out value="${flashError}"/></div>
            </c:if>
            <c:if test="${flashWarning != null}">
                <div class="mensagem erro"><c:out value="${flashWarning}"/></div>
            </c:if>

            <c:choose>
                <c:when test="${token != null}">
                    <form method="post" action="${pageContext.request.contextPath}/redefinir-senha">
                        <input type="hidden" name="token" value="<c:out value='${token}'/>">

                        <label for="senha">Nova senha:</label>
                        <div class="campo-senha">
                            <input type="password" id="senha" class="input-senha" name="password" placeholder="Nova senha" autocomplete="new-password" required>
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

                        <label for="confirmar-senha">Confirmar nova senha:</label>
                        <div class="campo-senha">
                            <input type="password" id="confirmar-senha" class="input-senha" name="confirmPassword" placeholder="Confirmar nova senha" autocomplete="new-password" required>
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

                        <button type="submit">Alterar senha</button>
                    </form>
                </c:when>
                <c:otherwise>
                    <p class="subtitulo">O link de recuperação é inválido ou expirou. Volte ao login e solicite um novo link.</p>
                </c:otherwise>
            </c:choose>

            <p><a href="${pageContext.request.contextPath}/login">Voltar ao login</a></p>
        </div>
    </div>
    <script src="${pageContext.request.contextPath}/js/password-toggle.js" defer></script>
</body>
</html>
