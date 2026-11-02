(function () {
  const group = document.getElementById('grupo-celulares');
  const detail = document.getElementById('tela-detalhe');
  const title = document.getElementById('detalhe-titulo');
  const text = document.getElementById('detalhe-texto');
  const images = document.getElementById('detalhe-imagens');
  const back = document.getElementById('btn-voltar');
  if (!group || !detail || !title || !text || !images || !back) return;

  const content = {
    'chave-mapa': {
      title: 'Mapa de cooperativas próximas',
      text: 'Encontre cooperativas próximas e consulte as opções disponíveis para a destinação dos resíduos.',
      image: 'assets/img/content/celular_sobre_nos.png',
      alt: 'Tela de mapa do app VOLTA'
    },
    'chave-chat': {
      title: 'Chat integrado',
      text: 'Converse com cooperativas e combine os detalhes da coleta diretamente pela plataforma.',
      image: 'assets/img/content/celular_mensagens.png',
      alt: 'Tela de conversa do app VOLTA'
    },
    'chave-foto': {
      title: 'Registro por foto',
      text: 'Registre os resíduos com fotos, descrição e localização para organizar as coletas.',
      image: 'assets/img/content/celular_inicio.png',
      alt: 'Registro de resíduos no app VOLTA'
    }
  };

  Object.keys(content).forEach(function (id) {
    const button = document.getElementById(id);
    if (!button) return;
    button.addEventListener('click', function () {
      const item = content[id];
      title.textContent = item.title;
      text.textContent = item.text;
      images.replaceChildren();
      const image = document.createElement('img');
      image.src = item.image;
      image.alt = item.alt;
      images.appendChild(image);
      group.hidden = true;
      detail.hidden = false;
    });
  });

  back.addEventListener('click', function () {
    detail.hidden = true;
    group.hidden = false;
  });
}());
