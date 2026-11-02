//DECLARAÇÃO DE VARIÁVEIS

const chaveMapa = document.getElementById("chave-mapa");
const chaveChat = document.getElementById("chave-chat");
const chaveFoto = document.getElementById("chave-foto");

const grupoCelulares = document.getElementById("grupo-celulares");
const telaDetalhe = document.getElementById("tela-detalhe");
const detalheTitulo = document.getElementById("detalhe-titulo");
const detalheImagens = document.getElementById("detalhe-imagens");
const detalheTexto = document.getElementById("detalhe-texto");
const btnVoltar = document.getElementById("btn-voltar");

let chaveAtiva = null;

const slides = [
  {
    titulo: "Mapa de cooperativas próximas",
    imagens: [
      {
        src: "assets/img/content/celular_sobre_nos.png",
        alt: "Tela do mapa de cooperativas próximas do app VOLTA",
      },
    ],
    texto:
      "O mapa de cooperativas mostra aquelas perto da sua unidade. E são apresentadas com uma avaliação de estrelas.",
  },
  {
    titulo: "Chat integrado",
    imagens: [
      {
        src: "assets/img/content/celular_mensagens.png",
        alt: "Tela de chat do app VOLTA",
      },
    ],
    texto:
      "O chat no aplicativo deixa o contato entre empresa e cooperativa mais rápido, por estar de fácil acesso.",
  },
  {
    titulo: "Registro por foto",
    imagens: [
      {
        src: "assets/img/content/celular_camera.png",
        alt: "Tela da câmera do registro por foto do app VOLTA",
      },
      {
        src: "assets/img/content/celular_analise.png",
        alt: "Tela de análise de IA do registro por foto do app VOLTA",
      },
    ],
    texto:
      "O registro por foto é acompanhado por uma análise de IA, que deixa o registro dos resíduos mais fácil.",
  },
];


//FUNÇÕES

function abrirDetalhe(indice, chave) {
  const slide = slides[indice];

  detalheTitulo.textContent = slide.titulo;
  detalheTexto.textContent = slide.texto;

  detalheImagens.innerHTML = "";

  for (const imagem of slide.imagens) {
    const img = document.createElement("img");
    img.src = imagem.src;
    img.alt = imagem.alt;
    detalheImagens.appendChild(img);
  }

  chaveAtiva = chave;
  grupoCelulares.hidden = true;
  telaDetalhe.hidden = false;
  btnVoltar.focus();
}

function fecharDetalhe() {
  grupoCelulares.hidden = false;
  telaDetalhe.hidden = true;

  if (chaveAtiva) {
    chaveAtiva.focus();
  }
}

//EVENTOS
btnVoltar.addEventListener("click", () => {
  fecharDetalhe();
});

document.addEventListener("keydown", (evento) => {
  if (evento.key === "Escape" && !telaDetalhe.hidden) {
    fecharDetalhe();
  }
});

chaveMapa.addEventListener("click", () => {
  abrirDetalhe(0, chaveMapa);
});

chaveChat.addEventListener("click", () => {
  abrirDetalhe(1, chaveChat);
});

chaveFoto.addEventListener("click", () => {
  abrirDetalhe(2, chaveFoto);
});
