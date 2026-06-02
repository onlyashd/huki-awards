package io.github.onlyashd.hukiawards.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import huki_awards.app.shared.generated.resources.Res
import io.github.onlyashd.hukiawards.client.ApiClient
import io.github.onlyashd.hukiawards.model.Category
import io.github.onlyashd.hukiawards.model.GlobalStats
import io.github.onlyashd.hukiawards.model.Routes
import io.github.onlyashd.hukiawards.model.Routes.Categories
import io.github.onlyashd.hukiawards.model.Strings
import io.github.onlyashd.hukiawards.model.UserProfile
import io.github.onlyashd.hukiawards.shared.AppConfig
import io.github.onlyashd.hukiawards.ui.components.AdminsManagementSubScreen
import io.github.onlyashd.hukiawards.ui.components.CategoriesManagementSubScreen
import io.github.onlyashd.hukiawards.ui.components.GlobalStatsCard
import io.github.onlyashd.hukiawards.ui.components.SettingsManagementSubScreen
import io.github.onlyashd.hukiawards.ui.components.SmallTopAppBar
import io.github.onlyashd.hukiawards.ui.components.VotesManagementSubScreen
import io.github.onlyashd.hukiawards.util.AppLogger
import io.github.onlyashd.hukiawards.util.colors
import io.github.onlyashd.hukiawards.util.typography
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi

enum class AdminScreen { OVERVIEW, CATEGORIES, MANAGE_VOTES, VOTE_AS_USER, SETTINGS, ADMINS }

@OptIn(ExperimentalResourceApi::class)
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
    var settings by remember { mutableStateOf<io.github.onlyashd.hukiawards.model.Settings?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }
    var showDeleteVotesConfirmation by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

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

    val refreshSettings = {
        coroutineScope.launch {
            try {
                settings = api.get(Routes.Settings)
            } catch (e: Exception) {
                AppLogger.e("Error fetching settings: ${e.message}")
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshCategories()
        refreshStats()
        refreshSettings()
    }

    Scaffold(
        modifier = Modifier.background(colors().background),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SmallTopAppBar(
                title = { Text(Strings.ADMIN_CONSOLE) },
                profile = profile,
                logoUrl = settings?.logoUrl,
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
                    HorizontalDivider(color = colors().outlineVariant.copy(alpha = 0.5f))
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
                    icon = {
                        AsyncImage(
                            model = Res.getUri("drawable/dashboard.png"),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            colorFilter = ColorFilter.tint(LocalContentColor.current)
                        )
                    },
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
                    icon = {
                        AsyncImage(
                            model = Res.getUri("drawable/admin.png"),
                            contentDescription = "Admins",
                            modifier = Modifier.size(20.dp),
                            colorFilter = ColorFilter.tint(LocalContentColor.current)
                        )
                    },
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
                                        snackbarHostState.showSnackbar(
                                            message = Strings.ERROR_REORDER_CATEGORIES,
                                            duration = SnackbarDuration.Short
                                        )
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
                                        val bytes = api.download(Routes.ExportVotes, isAdmin = true)
                                        io.github.onlyashd.hukiawards.util.downloadFile(
                                            bytes,
                                            "votos_huki_awards.csv",
                                            "text/csv"
                                        )
                                    } catch (e: Exception) {
                                        AppLogger.e("Failed to export votes: ${e.message}")
                                        snackbarHostState.showSnackbar(
                                            message = Strings.ERROR_EXPORT_VOTES,
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            },
                            onDownloadShareImage = { category ->
                                coroutineScope.launch {
                                    try {
                                        val url =
                                            "${api.apiBase}${Routes.Categories.path}/${category.id}/share"
                                        val response = api.client.get(url) {
                                            api.token?.let { bearerAuth(it) }
                                        }

                                        if (response.status == io.ktor.http.HttpStatusCode.NoContent) {
                                            snackbarHostState.showSnackbar(
                                                message = Strings.ERROR_NO_VOTES_CATEGORY,
                                                duration = SnackbarDuration.Short
                                            )
                                            return@launch
                                        }

                                        val customBytes = response.body<ByteArray>()

                                        io.github.onlyashd.hukiawards.util.downloadFile(
                                            customBytes,
                                            "top10-${category.name}.png",
                                            "image/png"
                                        )
                                    } catch (e: Exception) {
                                        AppLogger.e("Failed to download leaderboard: ${e.message}")
                                        snackbarHostState.showSnackbar(
                                            message = "Erro ao baixar imagem: ${e.message}",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            },
                            onDownloadWinnerImage = { category ->
                                coroutineScope.launch {
                                    try {
                                        val url =
                                            "${api.apiBase}${Routes.Categories.path}/${category.id}/winner"
                                        val response = api.client.get(url) {
                                            api.token?.let { bearerAuth(it) }
                                        }

                                        if (response.status == io.ktor.http.HttpStatusCode.NoContent) {
                                            snackbarHostState.showSnackbar(
                                                message = Strings.ERROR_NO_VOTES_CATEGORY,
                                                duration = SnackbarDuration.Short
                                            )
                                            return@launch
                                        }

                                        val customBytes = response.body<ByteArray>()

                                        io.github.onlyashd.hukiawards.util.downloadFile(
                                            customBytes,
                                            "vencedor-${category.name}.png",
                                            "image/png"
                                        )
                                    } catch (e: Exception) {
                                        AppLogger.e("Failed to download winner card: ${e.message}")
                                        snackbarHostState.showSnackbar(
                                            message = "Erro ao baixar imagem: ${e.message}",
                                            duration = SnackbarDuration.Short
                                        )
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
                                users = api.get(Routes.Users, isAdmin = true)
                            } catch (e: Exception) {
                                AppLogger.e("Failed to fetch users: ${e.message}")
                                snackbarHostState.showSnackbar(
                                    message = Strings.ERROR_FETCH_USERS,
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }

                        if (selectedUser == null) {
                            Column {
                                Text(
                                    Strings.VOTE_AS_USER_FOR,
                                    style = typography().titleMedium
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(users) { user: UserProfile ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth()
                                                .clickable { selectedUser = user },
                                            colors = CardDefaults.cardColors(
                                                containerColor = colors().surfaceVariant.copy(
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
                                                        style = typography().bodyLarge
                                                    )
                                                    Text(
                                                        "@${user.username}",
                                                        style = typography().labelMedium
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
                                        style = typography().titleMedium
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
                        SettingsManagementSubScreen(
                            api = api,
                            onShowSnackbar = { message ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(message)
                                }
                            }
                        )
                    }

                    AdminScreen.ADMINS -> {
                        AdminsManagementSubScreen(
                            api = api,
                            onShowSnackbar = { message ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(message)
                                }
                            }
                        )
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
                                    snackbarHostState.showSnackbar(
                                        message = Strings.ERROR_DELETE_CATEGORY,
                                        duration = SnackbarDuration.Short
                                    )
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
                                snackbarHostState.showSnackbar(
                                    message = Strings.ERROR_CLEAR_VOTES,
                                    duration = SnackbarDuration.Short
                                )
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
                                        weight = editingCategory?.weight ?: (categories.size + 1),
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
                                    snackbarHostState.showSnackbar(
                                        message = Strings.ERROR_SAVE_CATEGORY,
                                        duration = SnackbarDuration.Short
                                    )
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
