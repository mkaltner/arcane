package app.arcane.android.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.arcane.android.R
import app.arcane.android.domain.model.ArcaneEnvironment
import app.arcane.android.domain.model.ArcaneStatus
import app.arcane.android.ui.theme.ArcaneColors
import app.arcane.android.ui.theme.ArcaneTheme
import kotlinx.coroutines.launch

@Composable
fun HomeRoute(
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        onEnvironmentSelected = viewModel::selectEnvironment,
        onDestinationSelected = viewModel::selectDestination,
        onRetryEnvironments = viewModel::refreshEnvironments,
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onEnvironmentSelected: (String) -> Unit = {},
    onDestinationSelected: (HomeDestination) -> Unit = {},
    onRetryEnvironments: () -> Unit = {},
) {
    when (uiState) {
        HomeUiState.Loading -> Scaffold(containerColor = ArcaneColors.Background) { innerPadding ->
            LoadingContent(modifier = Modifier.padding(innerPadding))
        }
        is HomeUiState.Ready -> ReadyScaffold(
            uiState = uiState,
            onEnvironmentSelected = onEnvironmentSelected,
            onDestinationSelected = onDestinationSelected,
            onRetryEnvironments = onRetryEnvironments,
        )
    }
}

@Composable
private fun ReadyScaffold(
    uiState: HomeUiState.Ready,
    onEnvironmentSelected: (String) -> Unit,
    onDestinationSelected: (HomeDestination) -> Unit,
    onRetryEnvironments: () -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = ArcaneColors.Surface) {
                NavigationDrawerContent(
                    drawer = uiState.navigationDrawer,
                    onEnvironmentSelected = { environmentId ->
                        onEnvironmentSelected(environmentId)
                        scope.launch { drawerState.close() }
                    },
                    onDestinationSelected = { destination ->
                        onDestinationSelected(destination)
                        scope.launch { drawerState.close() }
                    },
                )
            }
        },
    ) {
        Scaffold(containerColor = ArcaneColors.Background) { innerPadding ->
            ReadyContent(
                operationalDashboard = uiState.operationalDashboard,
                navigationDrawer = uiState.navigationDrawer,
                selectedDestination = uiState.selectedDestination,
                containers = uiState.containers,
                onOpenDrawer = { scope.launch { drawerState.open() } },
                onDestinationSelected = onDestinationSelected,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ArcaneColors.Background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(color = ArcaneColors.PrimaryPurple)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Preparing Arcane…", color = ArcaneColors.TextSecondary)
    }
}

@Composable
private fun ReadyContent(
    operationalDashboard: OperationalDashboardState,
    navigationDrawer: HomeNavigationDrawerState,
    selectedDestination: HomeDestination,
    containers: ContainersScreenState,
    onOpenDrawer: () -> Unit,
    onDestinationSelected: (HomeDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ArcaneColors.Background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            MenuHandleCard(
                drawer = navigationDrawer,
                onClick = onOpenDrawer,
            )
        }
        when (selectedDestination) {
            HomeDestination.Dashboard -> {
                item { OperationalDashboardCard(operationalDashboard) }
                if (operationalDashboard.resourceEntries.isNotEmpty()) {
                    item {
                        Text(
                            text = "RESOURCE VIEWS",
                            style = MaterialTheme.typography.labelMedium,
                            color = ArcaneColors.TextSecondary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    items(operationalDashboard.resourceEntries) { resource ->
                        ResourceEntryCard(
                            resource = resource,
                            onClick = {
                                if (resource.destination != null) onDestinationSelected(resource.destination)
                            },
                        )
                    }
                }
            }
            HomeDestination.Containers -> {
                item { ContainersScreenHeader(containers) }
                item { ResourceStatsRow(containers.stats) }
                when (val containersState = containers.state) {
                    ContainerListUiState.Loading -> item { EnvironmentLoadingRow() }
                    is ContainerListUiState.Error -> item {
                        Text(
                            text = containersState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ArcaneColors.ErrorRed,
                        )
                    }
                    is ContainerListUiState.Content -> {
                        if (containers.rows.isEmpty()) {
                            item {
                                Text(
                                    text = "No containers found in this environment.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ArcaneColors.TextSecondary,
                                )
                            }
                        } else {
                            items(containers.rows) { row -> ContainerRowCard(row) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuHandleCard(drawer: HomeNavigationDrawerState, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ArcaneColors.Surface),
        border = BorderStroke(1.dp, ArcaneColors.Border),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "☰",
                    style = MaterialTheme.typography.titleLarge,
                    color = ArcaneColors.PrimaryPurple,
                    fontWeight = FontWeight.Black,
                )
                Column {
                    Text(
                        text = drawer.environmentName,
                        style = MaterialTheme.typography.titleSmall,
                        color = ArcaneColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Tap to open menu and switch environment",
                        style = MaterialTheme.typography.bodySmall,
                        color = ArcaneColors.TextSecondary,
                    )
                }
            }
            Text(text = "⌄", color = ArcaneColors.TextSecondary, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun NavigationDrawerContent(
    drawer: HomeNavigationDrawerState,
    onEnvironmentSelected: (String) -> Unit,
    onDestinationSelected: (HomeDestination) -> Unit,
) {
    var environmentsExpanded by remember { mutableStateOf(false) }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ArcaneColors.Surface),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Arcane logo",
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    text = "ARCANE",
                    style = MaterialTheme.typography.titleLarge,
                    color = ArcaneColors.PrimaryPurple,
                    fontWeight = FontWeight.Black,
                )
            }
        }
        item {
            EnvironmentSelectorHeader(
                drawer = drawer,
                expanded = environmentsExpanded,
                onToggle = { environmentsExpanded = !environmentsExpanded },
            )
        }
        if (environmentsExpanded) {
            items(drawer.environmentOptions) { option ->
                EnvironmentSelectorOptionRow(
                    option = option,
                    onClick = {
                        environmentsExpanded = false
                        onEnvironmentSelected(option.id)
                    },
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Activity Center", color = ArcaneColors.TextPrimary, style = MaterialTheme.typography.bodyMedium)
                StatusPill(text = drawer.activityCount.toString())
            }
        }
        items(drawer.groups) { group ->
            NavigationGroupSection(group, onDestinationSelected)
        }
    }
}

@Composable
private fun EnvironmentSelectorHeader(
    drawer: HomeNavigationDrawerState,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ArcaneColors.SurfaceElevated),
        border = BorderStroke(1.dp, ArcaneColors.Border),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = drawer.environmentName,
                    style = MaterialTheme.typography.titleSmall,
                    color = ArcaneColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = drawer.environmentSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = ArcaneColors.TextSecondary,
                )
            }
            Text(
                text = if (expanded) "⌃" else "⌄",
                color = ArcaneColors.TextSecondary,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun EnvironmentSelectorOptionRow(
    option: EnvironmentSelectorOption,
    onClick: () -> Unit,
) {
    val borderColor = if (option.selected) ArcaneColors.PrimaryPurple else ArcaneColors.Border
    val containerColor = if (option.selected) ArcaneColors.PrimaryPurpleContainer else ArcaneColors.Surface
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ArcaneColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(text = option.subtitle, style = MaterialTheme.typography.bodySmall, color = ArcaneColors.TextSecondary)
            }
            if (option.selected) {
                StatusPill(text = "Selected")
            }
        }
    }
}

@Composable
private fun NavigationGroupSection(group: NavigationGroup, onDestinationSelected: (HomeDestination) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = group.title,
            style = MaterialTheme.typography.labelMedium,
            color = ArcaneColors.TextSecondary,
            fontWeight = FontWeight.SemiBold,
        )
        group.items.forEach { item -> NavigationItemRow(item, onDestinationSelected) }
    }
}

@Composable
private fun NavigationItemRow(item: NavigationItem, onDestinationSelected: (HomeDestination) -> Unit) {
    val rowColor = if (item.selected) ArcaneColors.SurfaceElevated else ArcaneColors.Surface
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = item.destination != null) {
                item.destination?.let(onDestinationSelected)
            },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = rowColor),
        border = if (item.selected) BorderStroke(1.dp, ArcaneColors.Border) else null,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = item.label,
                color = ArcaneColors.TextPrimary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (item.selected) FontWeight.SemiBold else FontWeight.Normal,
            )
            if (item.expandable) {
                Text(text = "›", color = ArcaneColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun OperationalDashboardCard(dashboard: OperationalDashboardState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ArcaneColors.SurfaceElevated),
        border = BorderStroke(1.dp, ArcaneColors.Border),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Operational dashboard",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ArcaneColors.TextPrimary,
                    )
                    Text(
                        text = "Environment: ${dashboard.selectedEnvironmentName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ArcaneColors.TextSecondary,
                    )
                }
                StatusPill(text = dashboard.snapshotState.statusLabel())
            }
            when (dashboard.snapshotState) {
                DashboardSnapshotUiState.Loading -> EnvironmentLoadingRow()
                is DashboardSnapshotUiState.Error -> Text(
                    text = dashboard.snapshotState.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ArcaneColors.ErrorRed,
                )
                is DashboardSnapshotUiState.Content -> dashboard.metrics.forEach { metric ->
                    DashboardMetricRow(metric)
                }
            }
        }
    }
}

@Composable
private fun DashboardMetricRow(metric: DashboardMetric) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = metric.label,
                style = MaterialTheme.typography.bodyMedium,
                color = ArcaneColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(text = metric.detail, style = MaterialTheme.typography.bodySmall, color = ArcaneColors.TextSecondary)
        }
        Text(
            text = metric.value,
            style = MaterialTheme.typography.titleLarge,
            color = ArcaneColors.PrimaryPurple,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun DashboardSnapshotUiState.statusLabel(): String = when (this) {
    DashboardSnapshotUiState.Loading -> "Loading"
    is DashboardSnapshotUiState.Content -> "Ready"
    is DashboardSnapshotUiState.Error -> "Error"
}

@Composable
private fun NextStepCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ArcaneColors.PrimaryPurpleContainer),
        border = BorderStroke(1.dp, ArcaneColors.PrimaryPurple.copy(alpha = 0.45f)),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
            Text(
                text = "NEXT STEP",
                style = MaterialTheme.typography.labelMedium,
                color = ArcaneColors.PrimaryPurple,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pick an environment, then we’ll open the resource dashboard.",
                style = MaterialTheme.typography.titleMedium,
                color = ArcaneColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun EnvironmentListCard(
    state: EnvironmentListUiState,
    onEnvironmentSelected: (String) -> Unit,
    onRetry: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ArcaneColors.SurfaceElevated),
        border = BorderStroke(1.dp, ArcaneColors.Border),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Environments",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ArcaneColors.TextPrimary,
                    )
                    Text(
                        text = "Loaded from /api/environments",
                        style = MaterialTheme.typography.bodySmall,
                        color = ArcaneColors.TextSecondary,
                    )
                }
                StatusPill(text = environmentListStatus(state))
            }
            when (state) {
                EnvironmentListUiState.Loading -> EnvironmentLoadingRow()
                EnvironmentListUiState.Empty -> Text(
                    text = "No environments are available for this account yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ArcaneColors.TextSecondary,
                )
                is EnvironmentListUiState.Error -> ErrorRow(message = state.message, onRetry = onRetry)
                is EnvironmentListUiState.Content -> state.environments.forEach { environment ->
                    EnvironmentRow(
                        environment = environment,
                        selected = environment.id == state.selectedEnvironmentId,
                        onClick = { onEnvironmentSelected(environment.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EnvironmentLoadingRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator(color = ArcaneColors.PrimaryPurple)
        Text(text = "Loading environments…", color = ArcaneColors.TextSecondary)
    }
}

@Composable
private fun ErrorRow(message: String, onRetry: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = ArcaneColors.ErrorRed,
        )
        Card(
            modifier = Modifier.clickable(onClick = onRetry),
            shape = RoundedCornerShape(999.dp),
            colors = CardDefaults.cardColors(containerColor = ArcaneColors.PrimaryPurpleContainer),
            border = BorderStroke(1.dp, ArcaneColors.PrimaryPurple.copy(alpha = 0.35f)),
        ) {
            Text(
                text = "Retry",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                color = ArcaneColors.PrimaryPurple,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun EnvironmentRow(
    environment: ArcaneEnvironment,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) ArcaneColors.PrimaryPurple else ArcaneColors.Border
    val containerColor = if (selected) ArcaneColors.PrimaryPurpleContainer else ArcaneColors.Surface
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = environment.name.ifBlank { environment.id },
                    style = MaterialTheme.typography.titleSmall,
                    color = ArcaneColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                StatusPill(text = environmentBadge(environment, selected))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = environment.apiUrl,
                style = MaterialTheme.typography.bodySmall,
                color = ArcaneColors.TextSecondary,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = environment.status.replaceFirstChar { char -> char.uppercase() },
                style = MaterialTheme.typography.bodySmall,
                color = if (environment.enabled) ArcaneColors.SuccessGreen else ArcaneColors.TextSecondary,
            )
        }
    }
}

private fun environmentListStatus(state: EnvironmentListUiState): String = when (state) {
    EnvironmentListUiState.Loading -> "Loading"
    EnvironmentListUiState.Empty -> "Empty"
    is EnvironmentListUiState.Error -> "Error"
    is EnvironmentListUiState.Content -> "Ready"
}

private fun environmentBadge(environment: ArcaneEnvironment, selected: Boolean): String = when {
    selected -> "Selected"
    environment.isEdge -> "Edge"
    environment.enabled -> "Ready"
    else -> "Disabled"
}

@Composable
private fun ResourceEntryCard(resource: DashboardResourceEntry, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ArcaneColors.SurfaceElevated),
        border = BorderStroke(1.dp, ArcaneColors.Border),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = resource.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ArcaneColors.TextPrimary,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = resource.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ArcaneColors.TextSecondary,
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = resource.value,
                    style = MaterialTheme.typography.headlineSmall,
                    color = ArcaneColors.PrimaryPurple,
                    fontWeight = FontWeight.Bold,
                )
                StatusPill(text = resource.badge)
            }
        }
    }
}

@Composable
private fun ContainersScreenHeader(containers: ContainersScreenState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ArcaneColors.SurfaceElevated),
        border = BorderStroke(1.dp, ArcaneColors.Border),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = containers.title,
                style = MaterialTheme.typography.titleLarge,
                color = ArcaneColors.TextPrimary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Environment: ${containers.selectedEnvironmentName}",
                style = MaterialTheme.typography.bodyMedium,
                color = ArcaneColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun ResourceStatsRow(stats: List<ResourceStatCard>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        stats.forEach { stat ->
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ArcaneColors.SurfaceElevated),
                border = BorderStroke(1.dp, ArcaneColors.Border),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = stat.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = ArcaneColors.TextSecondary,
                    )
                    Text(
                        text = stat.value,
                        style = MaterialTheme.typography.titleLarge,
                        color = ArcaneColors.PrimaryPurple,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun ContainerRowCard(row: ContainerRowState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ArcaneColors.SurfaceElevated),
        border = BorderStroke(1.dp, ArcaneColors.Border),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = row.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = ArcaneColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = row.image.ifBlank { "No image" },
                    style = MaterialTheme.typography.bodySmall,
                    color = ArcaneColors.TextSecondary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = row.status,
                    style = MaterialTheme.typography.bodySmall,
                    color = ArcaneColors.TextSecondary,
                )
            }
            StatusPill(text = row.badge)
        }
    }
}

@Composable
private fun StatusPill(text: String) {
    val (container, content) = when (text) {
        "Selected", "Ready", "Next", "Healthy", "Clear", "Running" -> ArcaneColors.SuccessGreenContainer to ArcaneColors.SuccessGreen
        "Queued", "Edge", "Loading", "Cleanup", "Review", "Stopped" -> ArcaneColors.SurfaceMuted to ArcaneColors.PortBlue
        "Error", "Attention" -> ArcaneColors.SurfaceMuted to ArcaneColors.ErrorRed
        else -> ArcaneColors.PrimaryPurpleContainer to ArcaneColors.PrimaryPurple
    }
    Card(
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        border = BorderStroke(1.dp, content.copy(alpha = 0.35f)),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = content,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    ArcaneTheme(darkTheme = true) {
        HomeScreen(
            uiState = HomeUiState.Ready(
                status = ArcaneStatus(
                    title = "Arcane Manager ready",
                    message = "Server: https://arcane.example.com. Signed in as demo. Choose an environment to continue.",
                    selectedEnvironmentId = "local",
                ),
                environments = EnvironmentListUiState.Content(
                    environments = listOf(
                        ArcaneEnvironment(
                            id = "local",
                            name = "Local Docker",
                            apiUrl = "unix:///var/run/docker.sock",
                            status = "online",
                            enabled = true,
                            isEdge = false,
                        ),
                    ),
                    selectedEnvironmentId = "local",
                ),
            ),
        )
    }
}
