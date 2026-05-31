# Huki Awards 2026 - Documentação de Funcionalidades (PT-BR)

## Introdução

O **Huki Awards** é uma plataforma de votação personalizada para premiações de jogos, integrando-se
com a API do IGDB para busca de metadados e utilizando o Discord para autenticação segura. O sistema
foi projetado para ser intuitivo para os usuários e robusto para os administradores.

## Funcionalidades para Usuários

### 1. Experiência de Votação

- **Fluxo Guiado:** A votação é dividida por categorias, apresentadas uma por vez para evitar
  sobrecarga de informação e manter o foco em cada escolha.
- **Navegação Flexível:** Botões de "Anterior" e "Próximo" permitem que o usuário revise suas
  escolhas e mude de ideia antes de prosseguir.
- **Busca em Tempo Real (IGDB):** Pesquisa dinâmica por jogos utilizando a base de dados oficial do
  IGDB, garantindo que títulos e capas estejam sempre corretos.
- **Progresso Persistente:** O sistema salva automaticamente o progresso da votação. Se o usuário
  fechar o navegador, ele poderá continuar exatamente de onde parou ao retornar.
- **Edição de Votos:** Enquanto a janela de votação definida pelos administradores estiver aberta, o
  usuário tem total liberdade para retornar ao dashboard e alterar seus votos.

### 2. Compartilhamento Social

- **Resumo de Escolhas:** Uma tela dedicada apresenta um resumo visual de todos os jogos indicados
  pelo usuário.
- **Gerador de Imagem (Share Card):** Funcionalidade que gera uma imagem PNG personalizada
  diretamente no servidor, incluindo:
    - Nome e avatar do usuário sincronizados do Discord.
    - Lista organizada de categorias e jogos escolhidos.
    - Identidade visual do Huki Awards 2026.
    - Carimbo de data e hora da votação.
- **Download Direto:** Integração com a API do navegador para baixar o resumo instantaneamente,
  facilitando o compartilhamento em redes sociais como Twitter, Instagram ou Discord.

## Funcionalidades para Administradores

### 1. Painel de Controle (Admin Dashboard)

- **Aba de Resumo (Overview):** Um dashboard visual que mostra o total de votos, votantes únicos e a
  porcentagem de participação por categoria.
- **Gestão de Categorias:** Interface completa para criar, editar (nome, descrição, peso/ordem) e
  remover categorias da premiação.
- **Monitoramento de Votos:** Visualização em tempo real de todos os votos registrados, permitindo
  auditar a participação.
- **Gestão de Admins:** Sistema de promoção de usuários comuns a administradores através do nome de
  usuário do Discord. Inclui proteção para "Admins de Sistema" que não podem ser removidos
  acidentalmente.
- **Logs de Auditoria:** Cada ação administrativa (alterações de categoria, atualizações de
  configurações, limpeza de votos) é registrada com carimbo de data/hora e autor para transparência.
- **Votar como Usuário:** Capacidade de administradores visualizarem o site como um usuário
  específico para ajudar em problemas ou votar em seu nome, se necessário.

### 2. Configurações do Evento

- **Gestão de Fases:** Alternância entre as fases de "NOMEAÇÃO" (usuários sugerem jogos) e "
  VOTAÇÃO" (usuários escolhem entre os nomeados).
- **Agendamento de Datas:** Definição precisa de quando a votação começa e quando termina.
- **Controle de Visibilidade:** Opção para mostrar ou ocultar o período de votação para os usuários
  na barra superior do site.
- **Abertura/Fechamento Manual:** Interruptor mestre para abrir ou fechar as votações
  instantaneamente, independente do horário programado.

### 3. Resultados e Divulgação

- **Leaderboards Dinâmicos:** Visualização do Top 10 de jogos mais votados para cada categoria com
  contagem exata de votos.
- **Exportação de Rankings:** Geração de imagens PNG profissionais com o ranking parcial (Top 10) de
  cada categoria, prontas para divulgação oficial.
- **Cartão de Vencedor:** Geração de arte especial com bordas douradas e capa em alta resolução para
  anunciar os vencedores de cada categoria de forma épica.
- **Exportação CSV:** Download de todos os dados brutos de votação em formato CSV para análise
  externa.

## Arquitetura e Tecnologia

- **Frontend:** Desenvolvido com **Compose Multiplatform** visando **WebAssembly (Wasm)** para alta
  performance e fidelidade visual no navegador.
- **Backend:** Construído em **Kotlin** com **Ktor**, utilizando **Exposed** para comunicação com o
  banco de dados PostgreSQL.
- **Segurança:** Autenticação via **OAuth2 (Discord)** com gerenciamento de sessões através de
  tokens **JWT (JSON Web Tokens)**.
- **Infraestrutura:** Banco de dados hospedado no **Supabase** e integração com a API da *
  *Twitch/IGDB** para metadados de jogos.
