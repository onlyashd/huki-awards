package io.github.onlyashd.hukiawards.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.onlyashd.hukiawards.client.ApiClient
import io.github.onlyashd.hukiawards.model.Category
import io.github.onlyashd.hukiawards.model.Routes
import io.github.onlyashd.hukiawards.model.Routes.Categories
import io.github.onlyashd.hukiawards.model.Strings
import io.github.onlyashd.hukiawards.model.UserProfile
import io.github.onlyashd.hukiawards.model.GlobalStats
import io.github.onlyashd.hukiawards.ui.components.*
import io.github.onlyashd.hukiawards.util.AppLogger
import io.github.onlyashd.hukiawards.util.colors
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.launch

enum class AdminScreen { OVERVIEW, CATEGORIES, MANAGE_VOTES, VOTE_AS_USER, SETTINGS, ADMINS }

@Composable
fun AdminDashboard(
    api: ApiClient,
    profile: UserProfile?,
    onLogoutRequested: () -> Unit,
    onToggleUserView: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(AdminScreen.OVERVIEW) }
    var categories by remember { mutableStateOf(emptyList<Category>()) }
    var globalStats by remember { mutableStateOf<GlobalStats?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }
    var showDeleteVotesConfirmation by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Helper to refresh categories from DB
    val refreshCategories = {
        coroutineScope.launch {
            try {
                categories = api.get(Categories)
            } catch (e: Exception) {
                println("Error fetching categories: ${e.message}")
            }
        }
    }

    val refreshStats = {
        coroutineScope.launch {
            try {
                globalStats = api.get(Routes.Stats)
            } catch (e: Exception) {
                AppLogger.e("Error fetching global stats: ${e.message}")
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshCategories()
        refreshStats()
    }

    Scaffold(
        modifier = Modifier.background(colors().background),
        topBar = {
            SmallTopAppBar(
                title = { Text(Strings.ADMIN_CONSOLE) },
                profile = profile,
                actions = {
                    DropdownMenuItem(
                        text = { Text(Strings.VIEW_AS_USER) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "User View"
                            )
                        },
                        onClick = {
                            onToggleUserView()
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    DropdownMenuItem(
                        text = { Text(Strings.LOGOUT) },
                        leadingIcon = {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Sair"
                            )
                        },
                        onClick = onLogoutRequested
                    )
                }
            )
        },
        floatingActionButton = {
            // Only show the Add FloatingActionButton if we are on the Categories panel
            if (currentScreen == AdminScreen.CATEGORIES) {
                FloatingActionButton(
                    onClick = {
                        editingCategory = null
                        showCreateDialog = true
                    },
                    containerColor = colors().primaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = Strings.CREATE_CATEGORY)
                }
            }
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // SIDE NAVIGATION MENU BAR
            NavigationRail(
                containerColor = colors().surfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxHeight()
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                NavigationRailItem(
                    selected = currentScreen == AdminScreen.OVERVIEW,
                    onClick = {
                        currentScreen = AdminScreen.OVERVIEW
                        refreshStats()
                    },
                    icon = { Icon(Icons.Default.Info, null) },
                    label = { Text(Strings.SUMMARY, color = colors().onBackground) }
                )

                NavigationRailItem(
                    selected = currentScreen == AdminScreen.CATEGORIES,
                    onClick = { currentScreen = AdminScreen.CATEGORIES },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, null) },
                    label = { Text(Strings.CATEGORIES, color = colors().onBackground) }
                )

                NavigationRailItem(
                    selected = currentScreen == AdminScreen.MANAGE_VOTES,
                    onClick = { currentScreen = AdminScreen.MANAGE_VOTES },
                    icon = { Icon(Icons.Default.ThumbUp, null) },
                    label = { Text(Strings.VOTES, color = colors().onBackground) }
                )

                NavigationRailItem(
                    selected = currentScreen == AdminScreen.VOTE_AS_USER,
                    onClick = { currentScreen = AdminScreen.VOTE_AS_USER },
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text(Strings.VOTE_FOR, color = colors().onBackground) }
                )

                NavigationRailItem(
                    selected = currentScreen == AdminScreen.SETTINGS,
                    onClick = { currentScreen = AdminScreen.SETTINGS },
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text(Strings.SETTINGS, color = colors().onBackground) }
                )

                NavigationRailItem(
                    selected = currentScreen == AdminScreen.ADMINS,
                    onClick = { currentScreen = AdminScreen.ADMINS },
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text(Strings.ADMINS, color = colors().onBackground) }
                )
            }

            // MAIN CONTENT HOLDER CONTAINER
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(24.dp)
            ) {
                when (currentScreen) {
                    AdminScreen.OVERVIEW -> {
                        globalStats?.let {
                            GlobalStatsCard(stats = it)
                        } ?: Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    AdminScreen.CATEGORIES -> {
                        CategoriesManagementSubScreen(
                            categories = categories,
                            onDelete = { id ->
                                categoryToDelete = categories.find { it.id == id }
                            },
                            onEdit = { id ->
                                editingCategory = categories.find { it.id == id }
                                showCreateDialog = true
                            },
                            onReorder = { reorderedIds ->
                                coroutineScope.launch {
                                    try {
                                        api.post<Unit>(
                                            Routes.ReorderCategories,
                                            reorderedIds,
                                            isAdmin = true
                                        )
                                        refreshCategories()
                                    } catch (e: Exception) {
                                        AppLogger.e("Failed to reorder categories: ${e.message}")
                                    }
                                }
                            }
                        )
                    }

                    AdminScreen.MANAGE_VOTES -> {
                        VotesManagementSubScreen(
                            categories = categories,
                            onDeleteAll = { showDeleteVotesConfirmation = true },
                            onExportCsv = {
                                coroutineScope.launch {
                                    try {
                                        val bytes = api.download(Routes.ExportVotes)
                                        io.github.onlyashd.hukiawards.util.downloadImage(
                                            bytes,
                                            "votos_huki_awards.csv"
                                        )
                                    } catch (e: Exception) {
                                        AppLogger.e("Failed to export votes: ${e.message}")
                                    }
                                }
                            },
                            onDownloadShareImage = { category ->
                                coroutineScope.launch {
                                    try {
                                        val url =
                                            "${api.apiBase}${Routes.Categories.path}/${category.id}/share"
                                        val customBytes = api.client.get(url) {
                                            bearerAuth(api.token)
                                        }.body<ByteArray>()

                                        io.github.onlyashd.hukiawards.util.downloadImage(
                                            customBytes,
                                            "top10-${category.name}.png"
                                        )
                                    } catch (e: Exception) {
                                        AppLogger.e("Failed to download leaderboard: ${e.message}")
                                    }
                                }
                            },
                            onDownloadWinnerImage = { category ->
                                coroutineScope.launch {
                                    try {
                                        val url =
                                            "${api.apiBase}${Routes.Categories.path}/${category.id}/winner"
                                        val customBytes = api.client.get(url) {
                                            bearerAuth(api.token)
                                        }.body<ByteArray>()

                                        io.github.onlyashd.hukiawards.util.downloadImage(
                                            customBytes,
                                            "vencedor-${category.name}.png"
                                        )
                                    } catch (e: Exception) {
                                        AppLogger.e("Failed to download winner card: ${e.message}")
                                    }
                                }
                            }
                        )
                    }

                    AdminScreen.VOTE_AS_USER -> {
                        var users by remember { mutableStateOf(emptyList<UserProfile>()) }
                        var selectedUser by remember { mutableStateOf<UserProfile?>(null) }

                        LaunchedEffect(Unit) {
                            try {
                                users = api.get(Routes.Users)
                            } catch (e: Exception) {
                                AppLogger.e("Failed to fetch users: ${e.message}")
                            }
                        }

                        if (selectedUser == null) {
                            Column {
                                Text(
                                    Strings.VOTE_AS_USER_FOR,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(users) { user: UserProfile ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth()
                                                .clickable { selectedUser = user },
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                                    alpha = 0.5f
                                                )
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                AsyncImage(
                                                    model = user.avatarUrl,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(40.dp)
                                                        .clip(RoundedCornerShape(20.dp))
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        user.name,
                                                        style = MaterialTheme.typography.bodyLarge
                                                    )
                                                    Text(
                                                        "@${user.username}",
                                                        style = MaterialTheme.typography.labelMedium
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { selectedUser = null }) {
                                        Icon(Icons.Default.Clear, contentDescription = Strings.BACK)
                                    }
                                    Text(
                                        Strings.VOTING_AS.replace("%s", selectedUser?.name ?: ""),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                Box(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
                                    UserDashboard(
                                        api = api,
                                        profile = selectedUser,
                                        isAdminPreview = true,
                                        targetUserId = selectedUser?.id,
                                        onLogoutRequested = { selectedUser = null }
                                    )
                                }
                            }
                        }
                    }

                    AdminScreen.SETTINGS -> {
                        SettingsManagementSubScreen(api = api)
                    }

                    AdminScreen.ADMINS -> {
                        AdminsManagementSubScreen(api = api)
                    }
                }
            }
        }
    }

    // CONFIRMATION DIALOG FOR DELETIONS
    if (categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text(Strings.DELETE_CATEGORY) },
            text = {
                Text(
                    Strings.DELETE_CATEGORY_CONFIRM.replace(
                        "%s",
                        categoryToDelete?.name ?: ""
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = categoryToDelete?.id
                        if (id != null) {
                            coroutineScope.launch {
                                try {
                                    api.delete<Unit>(Categories, id, isAdmin = true)
                                    refreshCategories()
                                } catch (e: Exception) {
                                    AppLogger.e("Failed to delete category: ${e.message}")
                                }
                            }
                        }
                        categoryToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = colors().error)
                ) {
                    Text(Strings.DELETE)
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text(Strings.CANCEL)
                }
            }
        )
    }

    if (showDeleteVotesConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteVotesConfirmation = false },
            title = { Text(Strings.CLEAR_VOTES) },
            text = { Text(Strings.CLEAR_VOTES_CONFIRM) },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                api.delete<Unit>(Routes.Votes, isAdmin = true)
                                showDeleteVotesConfirmation = false
                            } catch (e: Exception) {
                                AppLogger.e("Failed to clear votes: ${e.message}")
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = colors().error)
                ) {
                    Text(Strings.CLEAR_ALL)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteVotesConfirmation = false }) {
                    Text(Strings.CANCEL)
                }
            }
        )
    }

    // MODAL DIALOG CONTAINER FOR CREATING/EDITING CATEGORIES
    if (showCreateDialog) {
        var newCategoryName by remember { mutableStateOf(editingCategory?.name ?: "") }
        var newCategoryDesc by remember { mutableStateOf(editingCategory?.description ?: "") }
        var newCategoryWeight by remember {
            mutableStateOf(
                editingCategory?.weight?.toString() ?: "1"
            )
        }

        AlertDialog(
            onDismissRequest = {
                showCreateDialog = false
                editingCategory = null
            },
            title = {
                Text(
                    if (editingCategory == null) Strings.CREATE_CATEGORY else Strings.EDIT_CATEGORY,
                    color = colors().onBackground
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text(Strings.NAME, color = colors().onBackground) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCategoryDesc,
                        onValueChange = { newCategoryDesc = it },
                        label = {
                            Text(
                                Strings.DESCRIPTION_OPTIONAL,
                                color = colors().onBackground
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCategoryWeight,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        onValueChange = { newCategoryWeight = it },
                        label = { Text(Strings.WEIGHT_ORDER, color = colors().onBackground) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonColors(
                        containerColor = colors().background,
                        contentColor = colors().onBackground,
                        disabledContainerColor = colors().background,
                        disabledContentColor = colors().background
                    ),
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            coroutineScope.launch {
                                try {
                                    val categoryData = Category(
                                        id = editingCategory?.id,
                                        name = newCategoryName,
                                        description = newCategoryDesc,
                                        weight = newCategoryWeight.toIntOrNull() ?: 1,
                                    )

                                    if (editingCategory == null) {
                                        api.post<Unit>(Categories, categoryData, isAdmin = true)
                                    } else {
                                        api.put<Unit>(
                                            Categories,
                                            editingCategory!!.id!!,
                                            categoryData,
                                            isAdmin = true
                                        )
                                    }

                                    showCreateDialog = false
                                    editingCategory = null
                                    refreshCategories()
                                } catch (e: Exception) {
                                    AppLogger.e("Failed to save category: ${e.message}")
                                }
                            }
                        }
                    }
                ) {
                    Text(Strings.SAVE, color = colors().onBackground)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCreateDialog = false
                    editingCategory = null
                }) {
                    Text(Strings.CANCEL, color = colors().onSurface)
                }
            }
        )
    }
}
