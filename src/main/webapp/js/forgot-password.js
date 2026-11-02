(function () {
    'use strict';

    var triggers = document.querySelectorAll('[data-forgot-password]');
    if (!triggers.length) {
        return;
    }

    var contextPath = (document.body.dataset.contextPath || '').replace(/\/$/, '');
    var resetPage = contextPath ? contextPath + '/esqueci-senha' : 'esqueci-senha';

    var modal = document.createElement('div');
    modal.className = 'modal-recuperacao';
    modal.hidden = true;
    modal.setAttribute('aria-hidden', 'true');
    modal.innerHTML = ''
        + '<div class="modal-recuperacao__overlay" data-close-forgot></div>'
        + '<div class="modal-recuperacao__caixa" role="dialog" aria-modal="true"'
        + ' aria-labelledby="titulo-recuperacao">'
        + '  <button type="button" class="modal-recuperacao__fechar" data-close-forgot'
        + '          aria-label="Fechar">&times;</button>'
        + '  <h2 id="titulo-recuperacao">Recuperar senha</h2>'
        + '  <p class="subtitulo">Digite o e-mail da conta Volta. Enviaremos um link'
        + '     para redefinir a senha.</p>'
        + '  <form id="form-recuperacao" novalidate>'
        + '    <label for="email-recuperacao">E-mail:</label>'
        + '    <input type="email" id="email-recuperacao" name="email"'
        + '           placeholder="Digite seu e-mail" autocomplete="email" required>'
        + '    <button type="submit" id="btn-recuperacao">Enviar link</button>'
        + '  </form>'
        + '  <div id="feedback-recuperacao" class="mensagem" role="status" hidden></div>'
        + '</div>';

    document.body.appendChild(modal);

    var form = modal.querySelector('#form-recuperacao');
    var emailInput = modal.querySelector('#email-recuperacao');
    var submitButton = modal.querySelector('#btn-recuperacao');
    var feedback = modal.querySelector('#feedback-recuperacao');
    var closeButton = modal.querySelector('.modal-recuperacao__fechar');

    function openModal() {
        modal.hidden = false;
        modal.setAttribute('aria-hidden', 'false');
        document.body.classList.add('modal-aberto');
        feedback.hidden = true;
        feedback.className = 'mensagem';
        feedback.textContent = '';
        window.setTimeout(function () {
            emailInput.focus();
        }, 50);
    }

    function closeModal() {
        modal.hidden = true;
        modal.setAttribute('aria-hidden', 'true');
        document.body.classList.remove('modal-aberto');
        document.activeElement && document.activeElement.blur();
    }

    function showFeedback(message, success, devLink) {
        feedback.hidden = false;
        feedback.className = 'mensagem ' + (success ? 'sucesso' : 'erro');
        feedback.textContent = message;

        if (devLink) {
            var link = document.createElement('a');
            link.href = devLink;
            link.className = 'modal-recuperacao__link-dev';
            link.textContent = 'Abrir link de recuperação (modo local)';
            feedback.appendChild(document.createElement('br'));
            feedback.appendChild(link);
        }
    }

    for (var i = 0; i < triggers.length; i++) {
        triggers[i].addEventListener('click', openModal);
    }

    modal.querySelectorAll('[data-close-forgot]').forEach(function (element) {
        element.addEventListener('click', closeModal);
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && !modal.hidden) {
            closeModal();
        }
    });

    form.addEventListener('submit', function (event) {
        event.preventDefault();

        var email = emailInput.value.trim();
        if (!email) {
            showFeedback('Informe o e-mail da conta Volta.', false);
            emailInput.focus();
            return;
        }

        submitButton.disabled = true;
        submitButton.textContent = 'Enviando...';
        feedback.hidden = true;

        var body = new URLSearchParams();
        body.set('email', email);

        fetch(resetPage, {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
            },
            body: body.toString()
        }).then(function (response) {
            return response.json();
        }).then(function (data) {
            showFeedback(data.message || 'Resposta inesperada do servidor.', !!data.ok, data.devLink);
            if (data.ok) {
                emailInput.value = '';
            }
        }).catch(function () {
            showFeedback('Não foi possível falar com o servidor. Tente novamente.', false);
        }).finally(function () {
            submitButton.disabled = false;
            submitButton.textContent = 'Enviar link';
        });
    });
})();
