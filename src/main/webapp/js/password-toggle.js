(function () {
    'use strict';

    var buttons = document.querySelectorAll('[data-toggle-password]');

    Array.prototype.forEach.call(buttons, function (button) {
        var input = document.getElementById(button.getAttribute('data-toggle-password'));
        if (!input) {
            return;
        }

        button.hidden = false;

        button.addEventListener('click', function () {
            var mostrar = input.type === 'password';

            input.type = mostrar ? 'text' : 'password';
            button.classList.toggle('is-visible', mostrar);
            button.setAttribute('aria-pressed', mostrar ? 'true' : 'false');
            button.setAttribute('aria-label', mostrar ? 'Ocultar senha' : 'Mostrar senha');
            button.title = mostrar ? 'Ocultar senha' : 'Mostrar senha';

            input.focus({ preventScroll: true });

            if (input.setSelectionRange) {
                try {
                    input.setSelectionRange(input.value.length, input.value.length);
                } catch (e) {
                    // Alguns navegadores não permitem selecionar o texto; o foco já basta.
                }
            }
        });
    });
})();
