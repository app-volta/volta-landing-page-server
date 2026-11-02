(function () {
    'use strict';

    var form = document.getElementById('form-cadastro');
    var password = document.getElementById('senha');
    var confirmPassword = document.getElementById('confirmar-senha');

    if (!form || !password || !confirmPassword) {
        return;
    }

    function validatePasswordConfirmation() {
        if (confirmPassword.value !== password.value) {
            confirmPassword.setCustomValidity('As senhas não coincidem.');
        } else {
            confirmPassword.setCustomValidity('');
        }
    }

    password.addEventListener('input', validatePasswordConfirmation);
    confirmPassword.addEventListener('input', validatePasswordConfirmation);

    form.addEventListener('submit', function (event) {
        validatePasswordConfirmation();
        if (!form.checkValidity()) {
            event.preventDefault();
            form.reportValidity();
        }
    });
})();
