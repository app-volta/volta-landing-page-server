//DECLARAÇÃO DE VARIAVEIS
const titulo_empresa = document.getElementById("slider-titulo-empresa")
const div_um = document.getElementById('bloco-um');
const div_dois = document.getElementById('bloco-dois');
const div_tres = document.getElementById('bloco-tres');
const div_um_coop = document.getElementById('bloco-um-coop');
const div_dois_coop = document.getElementById('bloco-dois-coop');
const div_tres_coop = document.getElementById('bloco-tres-coop');

const titulo_coop = document.getElementById("slider-titulo-coop")
const btn_avancar = document.getElementById("btn-avancar");
const btn_voltar = document.getElementById("btn-voltar");
const btn_avancar_coop = document.getElementById("btn-avancar-coop");
const btn_voltar_coop = document.getElementById("btn-voltar-coop");

let index = 0;
let index_coop = 0;

const lista_titulo_empresa = [
    "Cadastre-se",
    "Procure cooperativas",
    "Analise",
    "Contate",
    "Registre",
    "Agende",
    "Aguarde"
]

const lista_bloco_um = [
    "Comece a criar sua conta.",

    "Acesse o mapa de cooperativas.",

    "Acesse o menu de cooperativas salvas.",

    "Acesse o chat privado da cooperativa.",

    "Clique na opção “Criar nova coleta”, na página inicial.",

    "Clique em “Agendar”.",

    "Agora é só esperar a data da coleta."

]

const lista_bloco_dois = [

    "Preencha os campos com as informações pedidas.",

    "Escolha alguma das cooperativas mais próximas, ou pesquise.",

    "Leia as informações da cooperativa que te interessa, prestando atenção no rating dela.",

    "Converse e discuta com a cooperativa sobre como será a coleta.",

    "Registre as informações necessárias. Caso tenha dificuldades em registrar informações sobre os resíduos, use a opção de fotografia.",

    "A coleta agora está marcada no calendário tanto para a cooperativa quanto para a empresa.",

    "Quando o dia da coleta chegar, o processo está finalizado."

]

const lista_bloco_tres = [

    "Aceite os Termos e Condições e conclua o cadastro.",

    "Clique em salvar, deixando a cooperativa escolhida dentro de um menu.",

    "Aprove a cooperativa para que possam começar a conversar.",

    "Ao final da conversa, prossiga para a parte do registro.",

    "Finalize o registro.",

    "Tudo pronto.",

    "Você também pode adicionar uma cooperativa aos “favoritos”, agendando mais coletas com essa cooperativa regularmente."

]

const lista_titulo_coop = [
    "Cadastre-se",
    "Se identifique",
    "Aguarde e converse",
    "Confirme",
]

const lista_bloco_um_coop = [

    "Comece a criar sua conta de cooperativa.",

    "Informe qual tipo de resíduo sua cooperativa é especializada.",

    "Espere que uma empresa escolha e contate sua cooperativa.",

    "Aguarde a empresa agendar a coleta."

]

const lista_bloco_dois_coop = [

    "Preencha os campos com as informações pedidas.",

    "Essa informação fica visível para empresas, para que elas possam fazer as melhores escolhas para elas.",

    "Converse com a empresa sobre o agendamento da coleta.",

    "Confirme o agendamento."

]

const lista_bloco_tres_coop = [

    "Aceite os Termos e Condições e conclua o cadastro.",

    "Adicione também fotos e a localização da sua cooperativa.",

    "Agende a coleta quando estiver tudo certo.",

    "Tudo pronto."

]

//FUNÇÕES

function ajustar_slider() {

    titulo_empresa.innerText = lista_titulo_empresa[index];

    div_um.innerText = lista_bloco_um[index];

    div_dois.innerText = lista_bloco_dois[index];

    div_tres.innerText = lista_bloco_tres[index];

    titulo_coop.innerText = lista_titulo_coop[index_coop];

    div_um_coop.innerText = lista_bloco_um_coop[index_coop];

    div_dois_coop.innerText = lista_bloco_dois_coop[index_coop];

    div_tres_coop.innerText = lista_bloco_tres_coop[index_coop];

}

function mudar_slider(btn) {

    if (btn == "avancar") {

        if (index == lista_bloco_um.length - 1) {

            index = 0;

        } else {

            index++;

        }

    } else if (btn == "voltar") {

        if (index == 0) {

            index = lista_bloco_um.length - 1;

        } else {

            index--;

        }

    } else if (btn == "avancar_coop") {

        if (index_coop == lista_bloco_um_coop.length - 1) {

            index_coop = 0;

        } else {

            index_coop++;

        }

    } else if (btn == "voltar_coop") {

        if (index_coop == 0) {

            index_coop = lista_bloco_um_coop.length - 1;

        } else {

            index_coop--;

        }

    }

    ajustar_slider();

}

//EVENTOS

ajustar_slider();

btn_avancar.addEventListener("click", () => { mudar_slider("avancar") });

btn_voltar.addEventListener("click", () => { mudar_slider("voltar") });

btn_avancar_coop.addEventListener("click", () => { mudar_slider("avancar_coop") });

btn_voltar_coop.addEventListener("click", () => { mudar_slider("voltar_coop") });
