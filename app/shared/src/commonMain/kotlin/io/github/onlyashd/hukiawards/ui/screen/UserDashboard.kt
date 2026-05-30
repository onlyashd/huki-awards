package io.github.onlyashd.hukiawards.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.onlyashd.hukiawards.client.ApiClient
import io.github.onlyashd.hukiawards.model.Category
import io.github.onlyashd.hukiawards.model.IgdbGameMetadata
import io.github.onlyashd.hukiawards.model.Routes
import io.github.onlyashd.hukiawards.model.UserProfile
import io.github.onlyashd.hukiawards.model.VoteRequest
import io.github.onlyashd.hukiawards.model.Settings
import io.github.onlyashd.hukiawards.ui.components.SmallTopAppBar
import io.github.onlyashd.hukiawards.util.AppLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    val coroutineScope = rememberCoroutineScope()

    // Fetch initial category options and user progress on structural mount
    LaunchedEffect(Unit) {
        try {
            categories = api.get(Routes.Categories)
            val votesPath = if (targetUserId != null) {
                "${Routes.Votes.path}/my?targetUserId=$targetUserId"
            } else {
                "${Routes.Votes.path}/my"
            }
            settings = api.get(Routes.Settings)
            userVotes = api.get(votesPath)

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
        topBar = {
            SmallTopAppBar(
                title = {
                    Column {
                        Text("Huki Awards - 2026")
                        if (settings?.showDatesToUsers == true && settings?.votingEnd != null) {
                            Text(
                                text = "Encerra em: ${settings?.votingEnd}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                profile = profile,
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
                            style = MaterialTheme.typography.labelLarge
                        )

                        val allVoted = userVotes.size == categories.size
                        Button(
                            onClick = {
                                if (currentCategoryIndex < categories.size - 1) {
                                    currentCategoryIndex++
                                } else if (allVoted) {
                                    showOverview = true
                                }
                            },
                            enabled = currentCategoryIndex < categories.size - 1 || allVoted
                        ) {
                            Text(if (currentCategoryIndex == categories.size - 1) "Finalizar" else "Próxima")
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
                    onEditRequested = {
                        showOverview = false
                        currentCategoryIndex = 0
                    },
                    onDownloadRequested = {
                        coroutineScope.launch {
                            try {
                                val bytes = api.download(Routes.Share, id = profile?.id)
                                io.github.onlyashd.hukiawards.util.downloadImage(
                                    bytes,
                                    "${profile?.username ?: "huki-awards"}-summary.png"
                                )
                            } catch (e: Exception) {
                                AppLogger.e("Falha ao baixar imagem: ${e.message}")
                            }
                        }
                    },
                    onShareRequested = {
                        coroutineScope.launch {
                            try {
                                val shareUrl =
                                    "${Routes.Server.path}${Routes.Share.path}/${profile?.id}"
                                io.github.onlyashd.hukiawards.util.copyToClipboard(shareUrl)
                            } catch (e: Exception) {
                                AppLogger.e("Falha ao copiar link: ${e.message}")
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
                        style = MaterialTheme.typography.headlineMedium
                    )
                    if (settings?.showDatesToUsers == true && settings?.votingEnd != null) {
                        Text(
                            "Encerrou em: ${settings?.votingEnd}",
                            style = MaterialTheme.typography.bodyLarge
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

                    CategoryVotingRow(
                        category = currentCategory,
                        initialVote = existingVote,
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

                                    // Auto advance if it was a new vote or just stay? 
                                    // Let's stay so user sees the "Voted" state
                                } catch (e: Exception) {
                                    // Handle database constraint failures
                                }
                            }
                        },
                        searchGamesAction = { query ->
                            try {
                                api.get(Routes.Search, query)
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

@Composable
fun CategoryVotingRow(
    category: Category,
    initialVote: VoteRequest? = null,
    onVoteSubmitted: (gameId: Long, gameName: String, gameCoverUrl: String) -> Unit,
    searchGamesAction: suspend (query: String) -> List<IgdbGameMetadata>
) {
    var searchQuery by remember(category.id) { mutableStateOf(initialVote?.gameName ?: "") }
    var searchResults by remember { mutableStateOf(emptyList<IgdbGameMetadata>()) }
    var isSearching by remember { mutableStateOf(false) }
    var showDropdown by remember { mutableStateOf(false) }

    val hasVoted = initialVote != null

    // Every single row completely monitors its own independent search lifecycle context
    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank() || searchQuery == initialVote?.gameName) {
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

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = category.name,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        if (!category.description.isNullOrBlank()) {
            Text(
                text = category.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Box {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    if (it.isBlank()) showDropdown = false
                },
                label = { Text(if (hasVoted) "Sua indicação" else "Pesquisar jogo para indicar...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = if (hasVoted) {
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                } else {
                    OutlinedTextFieldDefaults.colors()
                },
                trailingIcon = {
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                showDropdown = false
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        } else {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
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
                    searchResults.take(10).forEach { game ->
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
                                                .size(48.dp, 64.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                    }
                                    Column {
                                        Text(
                                            text = game.name,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        if (game.genres.isNotEmpty()) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier.padding(top = 4.dp)
                                            ) {
                                                game.genres.take(3).forEach { genre ->
                                                    SuggestionChip(
                                                        onClick = {},
                                                        label = {
                                                            Text(
                                                                genre,
                                                                style = MaterialTheme.typography.labelSmall
                                                            )
                                                        },
                                                        modifier = Modifier.height(24.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            onClick = {
                                searchQuery = game.name
                                showDropdown = false
                                onVoteSubmitted(game.id, game.name, game.coverUrl)
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
