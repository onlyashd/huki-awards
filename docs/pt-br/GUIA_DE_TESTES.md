# Guia de Teste Manual

Este guia fornece um passo a passo para verificar se sua instância do Huki Awards está configurada
corretamente e se todos os recursos estão funcionando conforme o esperado.

## 1. Autenticação e Perfil

- [ ] **Login do Discord**: Clique no botão "Entrar com o Discord". Você será redirecionado para o
  Discord, será solicitado a autorizar o aplicativo e, em seguida, retornará ao site do Huki Awards.
- [ ] **Persistência da Sessão**: Atualize a página após fazer login. Seu perfil (nome e avatar)
  ainda deverá estar visível na barra superior.
- [ ] **Sair**: Clique no seu perfil e selecione "Sair". Você será desconectado e redirecionado para
  a página inicial/de login.

## 2. Painel de Administração (Requer Função de Administrador)

- [ ] **Controle de Acesso**: Faça login com a conta que você adicionou à tabela `admins` em
  `initial_population.sql`. Você deverá ver a opção "Painel de Administração" no menu de perfil.
- [ ] **Gerenciamento de Fases**:
    - Altere a fase para **INDICAÇÃO** e salve. Verifique se os usuários podem pesquisar e indicar
      jogos.
    - Altere a fase para **VOTAÇÃO** e salve. Verifique se os usuários podem votar apenas nos
      indicados
      existentes.
- [ ] **Gerenciamento de Categorias**:
    - **Criar**: Adicione uma nova categoria (por exemplo, "Melhor Indie"). Verifique se ela aparece
      na
      lista principal.
    - **Editar**: Altere o nome ou a descrição de uma categoria.
    - **Excluir**: Remova uma categoria e confirme se ela desapareceu.
- [ ] **Estatísticas Globais**: Abra a aba "Estatísticas" no Painel de Administração. Verifique se o
  número total de usuários e votos está sendo atualizado corretamente.
- [ ] **Registros de Auditoria**: Execute uma ação (como alterar o nome do evento) e verifique a
  aba "Registros de Auditoria". Sua ação deve ser registrada com seu nome de usuário e data/hora.

## 3. Fase de Nomeação

- [ ] **Busca de Jogo**: Em uma categoria, digite o nome de um jogo (ex.: "Elden Ring"). Verifique
  se as sugestões do IGDB aparecem.
- [ ] **Enviar Nomeação**: Selecione um jogo e clique em "Nomear". O jogo agora deve aparecer como
  sua escolha para essa categoria.
- [ ] **Alterar Nomeação**: Selecione um jogo diferente. Verifique se sua nomeação anterior foi
  substituída.

## 4. Fase de Votação (Finais)

- [ ] **Exibição dos Nomeados**: Certifique-se de que apenas os jogos nomeados durante a fase de
  Nomeação (ou adicionados manualmente pelos administradores) estejam disponíveis para votação.
- [ ] **Votar**: Clique em "Votar" em um nomeado. Verifique se a interface reflete sua seleção.

## 5. Interface do Usuário e Localização

- [ ] **Alternância de Idioma**: Alterne entre inglês e português (se disponível). Verifique se
  todas as strings em `Strings.kt` estão atualizadas corretamente.
- [ ] **Design Responsivo**: Redimensione o seu navegador ou abra em um dispositivo móvel. O layout
  deve se adaptar (os cards devem se empilhar, os menus devem se tornar ícones de hambúrguer).

## 6. Ambiente e Segurança

- [ ] **Configurações de CORS**: Se hospedado num domínio, tente acessar a API de um domínio
  diferente não autorizado. O acesso deve ser bloqueado pelo CORS, a menos que especificado em
  `ALLOWED_HOSTS`.
- [ ] **Rastreamento de Auditoria**: Verifique se, mesmo que uma entrada do banco de dados seja
  alterada manualmente, o painel de administração registra quem alterou o quê através da interface
  do usuário.

## Solução de Problemas

Se algum desses testes falhar:

1. Verifique os **Logs do Servidor** em busca de exceções.
2. Verifique se `DATABASE_URL` e as credenciais do Discord/IGDB estão corretas nas suas variáveis
   de ambiente.
3. Certifique-se de que o script `initial_population.sql` foi executado corretamente.
4. Verifique as **Ferramentas de Desenvolvedor (F12)** do navegador em busca de erros no console ou
   solicitações de rede bloqueadas.