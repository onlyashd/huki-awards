package io.github.onlyashd.hukiawards.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import huki_awards.app.shared.generated.resources.Res
import io.github.onlyashd.hukiawards.client.ApiClient
import io.github.onlyashd.hukiawards.model.Category
import io.github.onlyashd.hukiawards.model.IgdbGameMetadata
import io.github.onlyashd.hukiawards.model.Routes
import io.github.onlyashd.hukiawards.model.Routes.Companion.subPath
import io.github.onlyashd.hukiawards.model.Settings
import io.github.onlyashd.hukiawards.model.UserProfile
import io.github.onlyashd.hukiawards.model.VoteRequest
import io.github.onlyashd.hukiawards.shared.AppConfig
import io.github.onlyashd.hukiawards.ui.components.SmallTopAppBar
import io.github.onlyashd.hukiawards.util.AppLogger
import io.github.onlyashd.hukiawards.util.colors
import io.github.onlyashd.hukiawards.util.formatToFriendlyDateTime
import io.github.onlyashd.hukiawards.util.typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun UserDashboard(
    api: ApiClient,
    profile: UserProfile?,
    isAdminPreview: Boolean = false,
    targetUserId: String? = null,
    onLogoutRequested: () -> Unit
) {
    var categories by remember { mutableStateOf(emptyList<Category>()) }
    var userVotes by remember { mutableStateOf(emptyList<VoteRequest>()) }
    var settings by remember { mutableStateOf<Settings?>(null) }
    var currentCategoryIndex by remember { mutableStateOf(0) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showOverview by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Fetch initial category options and user progress on structural mount
    LaunchedEffect(Unit) {
        try {
            categories = api.get(Routes.Categories)
            settings = api.get(Routes.Settings)
            userVotes = if (targetUserId != null) {
                api.get("${api.apiBase}${Routes.Votes.path}/my?targetUserId=$targetUserId")
            } else {
                api.get("${Routes.Votes.path}/my")
            }

            if (userVotes.isNotEmpty() && userVotes.size < categories.size && settings?.isVotingOpen == true) {
                showRestoreDialog = true
            } else if (userVotes.size == categories.size && userVotes.isNotEmpty()) {
                showOverview = true
            }
        } catch (e: Exception) {
            AppLogger.e(e.stackTraceToString())
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SmallTopAppBar(
                title = {
                    Column {
                        Text(settings?.eventName ?: "Huki Awards")
                        if (settings?.showDatesToUsers == true && settings?.votingEnd != null) {
                            Text(
                                text = "Encerra em: ${settings?.votingEnd?.formatToFriendlyDateTime()}",
                                style = typography().labelSmall,
                                color = colors().onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                profile = profile,
                logoUrl = settings?.logoUrl,
                actions = {
                    DropdownMenuItem(
                        text = { Text(if (isAdminPreview) "Voltar ao Admin" else "Sair da Conta") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.ExitToApp,
                                contentDescription = "Sair"
                            )
                        },
                        onClick = onLogoutRequested
                    )

                    HorizontalDivider(color = colors().outlineVariant.copy(alpha = 0.5f))

                    DropdownMenuItem(
                        enabled = false,
                        text = {
                            Text(
                                text = "v${AppConfig.VERSION}",
                                style = typography().labelSmall,
                                color = colors().outlineVariant.copy(alpha = 0.7f),
                            )
                        },
                        leadingIcon = {
                            AsyncImage(
                                model = Res.getUri("drawable/tag.png"),
                                contentDescription = "Versão",
                                modifier = Modifier.size(20.dp),
                                colorFilter = ColorFilter.tint(LocalContentColor.current)
                            )
                        },
                        onClick = {}
                    )
                }
            )
        },
        bottomBar = {
            if (categories.isNotEmpty() && !showRestoreDialog && !showOverview) {
                Surface(
                    tonalElevation = 3.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { if (currentCategoryIndex > 0) currentCategoryIndex-- },
                            enabled = currentCategoryIndex > 0
                        ) {
                            Text("Anterior")
                        }

                        Text(
                            text = "Categoria ${currentCategoryIndex + 1} de ${categories.size}",
                            style = typography().labelLarge
                        )

                        val allVoted = userVotes.size == categories.size
                        val currentCategoryVoted =
                            userVotes.any { it.categoryId == categories[currentCategoryIndex].id }
                        Button(
                            onClick = {
                                if (currentCategoryIndex < categories.size - 1) {
                                    currentCategoryIndex++
                                } else if (allVoted) {
                                    showOverview = true
                                }
                            },
                            enabled = (currentCategoryIndex < categories.size - 1 && currentCategoryVoted) || allVoted
                        ) {
                            val nextText =
                                if (currentCategoryIndex == categories.size - 1) "Finalizar" else "Próxima"
                            Text(nextText)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (showOverview) {
                VotingOverviewSubScreen(
                    profile = profile,
                    categories = categories,
                    userVotes = userVotes,
                    isFinalPhase = settings?.phase == "VOTING",
                    onEditRequested = {
                        showOverview = false
                        currentCategoryIndex = 0
                    },
                    onDownloadRequested = {
                        coroutineScope.launch {
                            try {
                                val bytes = api.download(Routes.Share, id = profile?.id)
                                io.github.onlyashd.hukiawards.util.downloadFile(
                                    bytes,
                                    "${profile?.username ?: "huki-awards"}-summary.png",
                                    "image/png"
                                )
                            } catch (e: Exception) {
                                AppLogger.e("Falha ao baixar imagem: ${e.message}")
                            }
                        }
                    },
                    onShareRequested = {
                        coroutineScope.launch {
                            try {
                                api.post<String>(Routes.ShareDiscord, "")
                                AppLogger.i("Compartilhado com sucesso no Discord!")
                                snackbarHostState.showSnackbar("Compartilhado com sucesso no Discord!")
                            } catch (e: Exception) {
                                AppLogger.e("Falha ao compartilhar no Discord: ${e.message}")
                                snackbarHostState.showSnackbar("Falha ao compartilhar: ${e.message}")
                            }
                        }
                    }
                )
            } else if (settings?.isVotingOpen == false && !isAdminPreview) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "A votação está encerrada!",
                        style = typography().headlineMedium
                    )
                    if (settings?.showDatesToUsers == true && settings?.votingEnd != null) {
                        Text(
                            "Encerrou em: ${settings?.votingEnd?.formatToFriendlyDateTime()}",
                            style = typography().bodyLarge,
                            color = colors().onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onLogoutRequested) {
                        Text("Voltar")
                    }
                }
            } else if (showRestoreDialog) {
                AlertDialog(
                    onDismissRequest = { },
                    title = { Text("Continuar Votação?") },
                    text = { Text("Você já iniciou sua votação. Deseja continuar de onde parou ou recomeçar do zero?") },
                    confirmButton = {
                        Button(onClick = {
                            // Find first unvoted category
                            val firstUnvotedIndex = categories.indexOfFirst { cat ->
                                userVotes.none { it.categoryId == cat.id }
                            }
                            if (firstUnvotedIndex != -1) {
                                currentCategoryIndex = firstUnvotedIndex
                            }
                            showRestoreDialog = false
                        }) {
                            Text("Continuar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            coroutineScope.launch {
                                try {
                                    api.delete<Unit>(Routes.Votes, id = "my")
                                    userVotes = emptyList()
                                    currentCategoryIndex = 0
                                    showRestoreDialog = false
                                } catch (e: Exception) {
                                    AppLogger.e("Failed to reset votes: ${e.message}")
                                }
                            }
                        }) {
                            Text("Recomeçar")
                        }
                    }
                )
            } else if (categories.isNotEmpty()) {
                AnimatedContent(
                    targetState = currentCategoryIndex,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                        } else {
                            slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                        }
                    }
                ) { index ->
                    val currentCategory = categories[index]
                    val existingVote = userVotes.find { it.categoryId == currentCategory.id }

                    if (settings?.phase == "VOTING") {
                        FinalVotingGrid(
                            category = currentCategory,
                            initialVote = existingVote,
                            onVoteSubmitted = { gameId: Long, gameName: String, gameCoverUrl: String ->
                                coroutineScope.launch {
                                    try {
                                        api.post<VoteRequest>(
                                            Routes.Vote, VoteRequest(
                                                categoryId = currentCategory.id!!,
                                                igdbGameId = gameId,
                                                gameName = gameName,
                                                gameCoverUrl = gameCoverUrl,
                                                targetUserId = targetUserId
                                            )
                                        )
                                        // Update local state
                                        val newVote = VoteRequest(
                                            categoryId = currentCategory.id!!,
                                            igdbGameId = gameId,
                                            gameName = gameName,
                                            gameCoverUrl = gameCoverUrl,
                                            targetUserId = targetUserId
                                        )
                                        userVotes =
                                            userVotes.filterNot { it.categoryId == currentCategory.id } + newVote
                                    } catch (e: Exception) {
                                        AppLogger.e("Falha ao enviar voto: ${e.message}")
                                    }
                                }
                            },
                            fetchNominees = {
                                try {
                                    api.get<List<IgdbGameMetadata>>(
                                        Routes.Categories.byId(
                                            currentCategory.id!!
                                        ).subPath(Routes.Top10)
                                    )
                                } catch (e: Exception) {
                                    emptyList()
                                }
                            }
                        )
                    } else {
                        CategoryVotingRow(
                            category = currentCategory,
                            initialVote = existingVote,
                            isFinalPhase = settings?.phase == "VOTING",
                            onVoteSubmitted = { gameId, gameName, gameCoverUrl ->
                                coroutineScope.launch {
                                    try {
                                        api.post<VoteRequest>(
                                            Routes.Vote, VoteRequest(
                                                categoryId = currentCategory.id!!,
                                                igdbGameId = gameId,
                                                gameName = gameName,
                                                gameCoverUrl = gameCoverUrl,
                                                targetUserId = targetUserId
                                            )
                                        )
                                        // Update local state
                                        val newVote = VoteRequest(
                                            categoryId = currentCategory.id!!,
                                            igdbGameId = gameId,
                                            gameName = gameName,
                                            gameCoverUrl = gameCoverUrl,
                                            targetUserId = targetUserId
                                        )
                                        userVotes =
                                            userVotes.filterNot { it.categoryId == currentCategory.id } + newVote
                                    } catch (e: Exception) {
                                        AppLogger.e("Falha ao enviar voto: ${e.message}")
                                    }
                                }
                            },
                            searchGamesAction = { query ->
                                try {
                                    api.getByQuery(Routes.Search, query)
                                } catch (e: Exception) {
                                    emptyList()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FinalVotingGrid(
    category: Category,
    initialVote: VoteRequest?,
    onVoteSubmitted: (gameId: Long, gameName: String, gameCoverUrl: String) -> Unit,
    fetchNominees: suspend () -> List<IgdbGameMetadata>
) {
    var nominees by remember { mutableStateOf<List<IgdbGameMetadata>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(category.id) {
        isLoading = true
        nominees = fetchNominees()
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = category.name,
            style = typography().headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = category.description,
            style = typography().bodyMedium,
            color = colors().onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(nominees) { game ->
                    val isSelected = initialVote?.igdbGameId == game.id
                    NomineeCard(
                        game = game,
                        isSelected = isSelected,
                        onClick = {
                            onVoteSubmitted(game.id, game.name, game.coverUrl)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NomineeCard(
    game: IgdbGameMetadata,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                colors().primaryContainer
            else
                colors().surfaceVariant.copy(alpha = 0.5f)
        ),
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(2.dp, colors().primary)
        else
            null
    ) {
        Column {
            Box {
                AsyncImage(
                    model = game.coverUrl,
                    contentDescription = game.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(
                                colors().primary,
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selecionado",
                            tint = colors().onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = game.name,
                    style = typography().labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = if (isSelected)
                        androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = colors().primary
                        )
                    else
                        androidx.compose.material3.ButtonDefaults.filledTonalButtonColors()
                ) {
                    Text(
                        text = if (isSelected) "Votado" else "Votar",
                        style = typography().labelMedium
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryVotingRow(
    category: Category,
    initialVote: VoteRequest? = null,
    isFinalPhase: Boolean = false,
    onVoteSubmitted: (gameId: Long, gameName: String, gameCoverUrl: String) -> Unit,
    searchGamesAction: suspend (query: String) -> List<IgdbGameMetadata>
) {
    var searchQuery by remember(category.id) { mutableStateOf("") }
    var searchResults by remember { mutableStateOf(emptyList<IgdbGameMetadata>()) }
    var isSearching by remember { mutableStateOf(false) }
    var showDropdown by remember { mutableStateOf(false) }

    val hasVoted = initialVote != null
    val phaseActionLabel = if (isFinalPhase) "VOTO" else "INDICAÇÃO"
    val phaseSearchLabel = if (isFinalPhase) "votar" else "indicar"

    // Independent search lifecycle context
    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            searchResults = emptyList()
            showDropdown = false
            return@LaunchedEffect
        }

        isSearching = true
        delay(500) // Debounce delay
        searchResults = searchGamesAction(searchQuery)
        isSearching = false
        showDropdown = searchResults.isNotEmpty()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. Category Title & Description ---
        Text(
            text = category.name.uppercase(),
            style = typography().headlineLarge,
            color = colors().primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        if (category.description.isNotBlank()) {
            Text(
                text = category.description,
                style = typography().bodyMedium,
                color = colors().onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        // --- 2. Game Display / Placeholder ---
        Box(
            modifier = Modifier
                .size(200.dp, 270.dp) // Maintain a gaming poster aspect ratio
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (hasVoted) colors().primaryContainer
                    else colors().surfaceVariant.copy(alpha = 0.5f)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (hasVoted) {
                // Active selection state with cover art
                Box(modifier = Modifier.fillMaxSize()) {
                    if (!initialVote.gameCoverUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = initialVote.gameCoverUrl,
                            contentDescription = initialVote.gameName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    // Bottom title overlay sheet (Game of the Year style)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(colors().primary.copy(alpha = 0.9f))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "$phaseActionLabel SALVO",
                            style = typography().labelMedium,
                            color = colors().onPrimary,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            } else {
                // Empty Placeholder state
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = colors().onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nenhum jogo selecionado",
                        style = typography().bodySmall,
                        color = colors().onSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        // Selected Game Label display text
        if (hasVoted) {
            Text(
                text = initialVote.gameName,
                style = typography().titleMedium,
                modifier = Modifier.padding(vertical = 12.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        } else {
            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- 3. Persistent Search Input Field ---
        Box(modifier = Modifier.fillMaxWidth(0.9f)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    if (it.isBlank()) showDropdown = false
                },
                label = { Text("Pesquisar para alterar/$phaseSearchLabel...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            showDropdown = false
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpar")
                        }
                    } else {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                }
            )

            if (showDropdown && searchResults.isNotEmpty()) {
                DropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = { showDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.9f),
                    properties = androidx.compose.ui.window.PopupProperties(focusable = false)
                ) {
                    searchResults.take(6).forEach { game ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (game.coverUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = game.coverUrl,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(40.dp, 52.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                    }
                                    Column {
                                        Text(game.name, style = typography().bodyLarge)
                                    }
                                }
                            },
                            onClick = {
                                searchQuery = "" // Clear query to reset field interface state
                                showDropdown = false
                                onVoteSubmitted(game.id, game.name, game.coverUrl)
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
