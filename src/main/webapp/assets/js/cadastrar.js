const senha = document.getElementById("senha");
const confirmarSenha = document.getElementById("confirmar-senha");

function validarConfirmacao() {
  if (confirmarSenha.value !== senha.value) {
    confirmarSenha.setCustomValidity("As senhas não coincidem.");
  } else {
    confirmarSenha.setCustomValidity("");
  }
}

senha.addEventListener("input", validarConfirmacao);
confirmarSenha.addEventListener("input", validarConfirmacao);
