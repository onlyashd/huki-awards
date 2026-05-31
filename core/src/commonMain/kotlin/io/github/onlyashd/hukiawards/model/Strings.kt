package io.github.onlyashd.hukiawards.model

object Strings {
    // Admin Dashboard
    const val ADMIN_CONSOLE = "Admin Console"
    const val VIEW_AS_USER = "Ver como Usuário"
    const val LOGOUT = "Sair da Conta"
    const val CREATE_CATEGORY = "Criar categoria"
    const val EDIT_CATEGORY = "Editar categoria"
    const val DELETE_CATEGORY = "Excluir categoria"
    const val DELETE_CATEGORY_CONFIRM =
        "Tem certeza que deseja excluir a categoria \"%s\"? Esta ação não pode ser desfeita."
    const val CLEAR_VOTES = "Zerar votação?"
    const val CLEAR_VOTES_CONFIRM =
        "Esta ação irá apagar TODOS os votos registrados no banco de dados. Isso é irreversível."
    const val CLEAR_ALL = "Zerar tudo"
    const val CANCEL = "Cancelar"
    const val SAVE = "Salvar"
    const val DELETE = "Excluir"
    const val NAME = "Nome"
    const val DESCRIPTION_OPTIONAL = "Descrição (opcional)"
    const val WEIGHT_ORDER = "Peso (para ordenação)"
    const val VOTE_AS_USER_FOR = "Selecione um usuário para votar em nome dele:"
    const val VOTING_AS = "Votando como: %s"
    const val BACK = "Voltar"

    // Side Nav
    const val SUMMARY = "Resumo"
    const val CATEGORIES = "Categorias"
    const val VOTES = "Votos"
    const val VOTE_FOR = "Votar por..."
    const val SETTINGS = "Configurações"
    const val ADMINS = "Admins"

    // Stats
    const val TOTAL_VOTES = "Total de Votos"
    const val UNIQUE_VOTERS = "Votantes Únicos"
    const val CATEGORY_PARTICIPATION = "Participação por Categoria"
    const val GENERAL_STATS = "Estatísticas Gerais"
    const val UNIQUE_ELECTORS = "Eleitores Únicos"
    const val VOTE_COUNT = "%d votos"

    // Errors
    const val ERROR_EXPORT_VOTES = "Falha ao exportar votos. Verifique se há votos registrados."
    const val ERROR_FETCH_USERS = "Falha ao carregar usuários."
    const val ERROR_SAVE_SETTINGS = "Falha ao salvar configurações."
    const val ERROR_FETCH_ADMINS = "Falha ao carregar administradores."
    const val ERROR_REORDER_CATEGORIES = "Falha ao reordenar categorias."
    const val ERROR_DELETE_CATEGORY = "Falha ao excluir categoria."
    const val ERROR_SAVE_CATEGORY = "Falha ao salvar categoria."
    const val ERROR_CLEAR_VOTES = "Falha ao zerar votos."
    const val ERROR_NO_VOTES_CATEGORY = "Não há dados suficientes para gerar este arquivo."

    // Settings
    const val GLOBAL_SETTINGS = "Configurações Globais"
    const val GLOBAL_SETTINGS_HELP =
        "Configurações que afetam todo o sistema, como o nome do evento e o status da votação."
    const val EVENT_NAME = "Nome do Evento"
    const val VOTING_PERIOD_OPEN = "Período de Votação Aberto"
    const val SHOW_DATES_TO_USERS = "Mostrar datas para usuários"
    const val EVENT_PHASE = "Fase do Evento"
    const val NOMINATION_PHASE_DESC = "Fase de Indicações (Usuários sugerem jogos)"
    const val VOTING_PHASE_DESC = "Fase Final (Usuários votam em finalistas)"
    const val LOGO_URL = "URL do Logo"
    const val FAVICON_URL = "URL do Favicon"
    const val START_DATE_LABEL = "Início (dd/MM/yyyy)"
    const val END_DATE_LABEL = "Fim (dd/MM/yyyy)"
    const val SAVE_CHANGES = "Salvar Alterações"
    const val SETTINGS_SAVED_SUCCESS = "Configurações salvas com sucesso!"

    // Admins
    const val MANAGE_ADMINS = "Gerenciar Administradores"
    const val PROMOTE_USER = "Promover Usuário"
    const val DEFAULT_ADMIN = "Administrador Padrão"
    const val REMOVE_ADMIN = "Remover"
    const val PROMOTED_SUCCESS = "Usuário promovido com sucesso!"
    const val DEMOTED_SUCCESS = "Administrador removido com sucesso!"
    const val ERROR_DEMOTE_ADMIN = "Falha ao remover administrador."
    const val ERROR_PROMOTE_ADMIN =
        "Falha ao promover usuário. Verifique se o username está correto."
    const val DISCORD_USERNAME_LABEL = "Username"
    const val PROMOTE_DIALOG_TITLE = "Promover Usuário"
    const val PROMOTE_DIALOG_TEXT = "Digite o username do Discord para promover a administrador:"
}
