package com.decli.codehelper.ui

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Telephony
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Rule
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.decli.codehelper.model.CodeFilterWindow
import com.decli.codehelper.model.PickupCodeItem
import com.decli.codehelper.ui.home.HomeUiState
import com.decli.codehelper.ui.home.HomeViewModel
import com.decli.codehelper.ui.theme.AccentBlue
import com.decli.codehelper.ui.theme.AccentGold
import com.decli.codehelper.ui.theme.AccentGreen
import com.decli.codehelper.ui.theme.AccentTerracotta
import com.decli.codehelper.ui.theme.AppBackground
import com.decli.codehelper.ui.theme.CardSurface
import com.decli.codehelper.ui.theme.DividerTint
import com.decli.codehelper.ui.theme.HeroCool
import com.decli.codehelper.ui.theme.HeroSurface
import com.decli.codehelper.ui.theme.HeroWarm
import com.decli.codehelper.ui.theme.Ink
import com.decli.codehelper.ui.theme.InkMuted
import com.decli.codehelper.ui.theme.PendingCardBorder
import com.decli.codehelper.ui.theme.PendingCardSurface
import com.decli.codehelper.ui.theme.PickedCardBorder
import com.decli.codehelper.ui.theme.PickedCardSurface
import com.decli.codehelper.ui.theme.SwipeGreen
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CodeHelperApp(
    viewModel: HomeViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var showRuleEditor by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.updatePermissionStatus(granted)
    }

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissionStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    if (showRuleEditor) {
        RulesEditorSheet(
            initialRules = uiState.activeRules,
            onDismissRequest = { showRuleEditor = false },
            onSave = { rules ->
                if (viewModel.saveRules(rules)) {
                    showRuleEditor = false
                }
            },
        )
    }

    Scaffold(
        containerColor = AppBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 32.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header section
            item(key = "__header__") {
                HeaderSection(
                    uiState = uiState,
                    onFilterSelected = viewModel::selectFilter,
                    onForceRefreshAll = viewModel::forceRefreshAll,
                    onEditRules = { showRuleEditor = true },
                )
            }

            // Content section
            when {
                !uiState.hasSmsPermission -> {
                    item(key = "__permission__") {
                        PermissionCard(
                            onRequestPermission = {
                                permissionLauncher.launch(Manifest.permission.READ_SMS)
                            },
                        )
                    }
                }

                uiState.isLoading && uiState.items.isEmpty() -> {
                    item(key = "__loading__") {
                        LoadingCard()
                    }
                }

                uiState.items.isEmpty() -> {
                    item(key = "__empty__") {
                        EmptyStateCard(
                            title = if (uiState.showAllItems) {
                                "当前时间范围没有取件码"
                            } else {
                                "暂无未取件码"
                            },
                            subtitle = if (uiState.showAllItems) {
                                "可以切换时间筛选，或者修改正则规则后重新读取短信。"
                            } else {
                                "如果想回看已取件记录，请点击「强制刷新所有」。"
                            },
                        )
                    }
                }

                else -> {
                    items(
                        items = uiState.items,
                        key = { it.uniqueKey },
                    ) { item ->
                        PickupCodeCard(
                            item = item,
                            showRestoreAction = uiState.showAllItems && item.isPickedUp,
                            onMarkPickedUp = { viewModel.markPickedUp(item) },
                            onRestorePending = { viewModel.restorePending(item) },
                            onOpenSms = {
                                openSmsOrConversation(
                                    context = context,
                                    item = item,
                                    onMessage = { snackbarHostState.showSnackbar(it) },
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

// ── Header ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeaderSection(
    uiState: HomeUiState,
    onFilterSelected: (CodeFilterWindow) -> Unit,
    onForceRefreshAll: () -> Unit,
    onEditRules: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = HeroSurface),
        border = BorderStroke(1.dp, DividerTint),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(HeroSurface, HeroCool.copy(alpha = 0.3f)),
                    ),
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = HeroWarm.copy(alpha = 0.85f),
                    ) {
                        Icon(
                            modifier = Modifier
                                .padding(10.dp)
                                .size(28.dp),
                            imageVector = Icons.Rounded.Inventory2,
                            contentDescription = null,
                            tint = Ink,
                        )
                    }
                    Column {
                        Text(
                            text = "取件码助手",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Ink,
                        )
                        Text(
                            text = "自动读取短信，提取取件码",
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkMuted,
                        )
                    }
                }
            }

            // Pending count highlight
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (uiState.pendingCount > 0) {
                    AccentTerracotta.copy(alpha = 0.12f)
                } else {
                    AccentGreen.copy(alpha = 0.12f)
                },
                border = BorderStroke(
                    1.dp,
                    if (uiState.pendingCount > 0) {
                        AccentTerracotta.copy(alpha = 0.25f)
                    } else {
                        AccentGreen.copy(alpha = 0.25f)
                    },
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = if (uiState.pendingCount > 0) {
                                Icons.Rounded.LocalShipping
                            } else {
                                Icons.Rounded.CheckCircle
                            },
                            contentDescription = null,
                            tint = if (uiState.pendingCount > 0) AccentTerracotta else AccentGreen,
                            modifier = Modifier.size(26.dp),
                        )
                        Text(
                            text = "未取件码",
                            style = MaterialTheme.typography.titleMedium,
                            color = Ink,
                        )
                    }
                    Text(
                        text = "${uiState.pendingCount} 个",
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (uiState.pendingCount > 0) AccentTerracotta else AccentGreen,
                    )
                }
            }

            // Time filter
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "时间筛选",
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CodeFilterWindow.entries.forEach { filter ->
                        val isSelected = filter == uiState.selectedFilter
                        FilterChip(
                            modifier = Modifier.weight(1f),
                            selected = isSelected,
                            onClick = { onFilterSelected(filter) },
                            label = {
                                Text(
                                    text = filter.label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 17.sp,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentBlue,
                                selectedLabelColor = Color.White,
                            ),
                        )
                    }
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onForceRefreshAll,
                    enabled = uiState.hasSmsPermission,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue,
                        contentColor = Color.White,
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "强制刷新所有",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                OutlinedButton(
                    onClick = onEditRules,
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "设置",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            // Status hint
            Text(
                text = if (uiState.showAllItems) {
                    "当前展示全部取件码（含已取件）。已取件排在未取件后面。"
                } else {
                    "默认只展示未取件码。左滑卡片可标记「已取件」。"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = InkMuted,
            )
        }
    }
}

// ── State Cards ─────────────────────────────────────────────────────────────

@Composable
private fun PermissionCard(onRequestPermission: () -> Unit) {
    StateCard(
        icon = Icons.Rounded.Security,
        iconTint = AccentTerracotta,
        iconBackground = AccentTerracotta.copy(alpha = 0.12f),
        title = "需要短信读取权限",
        subtitle = "应用完全离线运行，不会联网。授权后将自动提取最近 12 小时内的取件码。",
        action = {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onRequestPermission,
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
            ) {
                Icon(Icons.Rounded.Sms, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(text = "授权读取短信", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
    )
}

@Composable
private fun LoadingCard() {
    StateCard(
        icon = Icons.Rounded.Sync,
        iconTint = AccentBlue,
        iconBackground = AccentBlue.copy(alpha = 0.12f),
        title = "正在读取短信",
        subtitle = "请稍等，取件码列表将自动刷新...",
        action = {},
    )
}

@Composable
private fun EmptyStateCard(title: String, subtitle: String) {
    StateCard(
        icon = Icons.Rounded.AutoAwesome,
        iconTint = AccentGold,
        iconBackground = AccentGold.copy(alpha = 0.12f),
        title = title,
        subtitle = subtitle,
        action = {},
    )
}

@Composable
private fun StateCard(
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    title: String,
    subtitle: String,
    action: @Composable () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, DividerTint),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(shape = CircleShape, color = iconBackground) {
                Icon(
                    modifier = Modifier
                        .padding(16.dp)
                        .size(36.dp),
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = Ink,
                textAlign = TextAlign.Center,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = InkMuted,
                textAlign = TextAlign.Center,
            )
            action()
        }
    }
}

// ── Pickup Code Card ────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PickupCodeCard(
    item: PickupCodeItem,
    showRestoreAction: Boolean,
    onMarkPickedUp: () -> Unit,
    onRestorePending: () -> Unit,
    onOpenSms: suspend () -> Unit,
) {
    if (item.isPickedUp) {
        PickupCodeCardBody(
            item = item,
            showRestoreAction = showRestoreAction,
            onRestorePending = onRestorePending,
            onOpenSms = onOpenSms,
        )
    } else {
        SwipeActionContainer(
            onActionClick = onMarkPickedUp,
        ) {
            PickupCodeCardBody(
                item = item,
                showRestoreAction = false,
                onRestorePending = onRestorePending,
                onOpenSms = onOpenSms,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun PickupCodeCardBody(
    item: PickupCodeItem,
    showRestoreAction: Boolean,
    onRestorePending: () -> Unit,
    onOpenSms: suspend () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val isPicked = item.isPickedUp
    val containerColor by animateColorAsState(
        targetValue = if (isPicked) PickedCardSurface else PendingCardSurface,
        label = "cardColor",
    )
    val borderColor = if (isPicked) PickedCardBorder else PendingCardBorder

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onDoubleClick = {
                    scope.launch { onOpenSms() }
                },
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.5.dp, borderColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Row 1: Status + Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusChip(
                    text = if (isPicked) "已取件" else "未取件",
                    accent = if (isPicked) AccentGreen else AccentTerracotta,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccessTime,
                        contentDescription = null,
                        tint = InkMuted,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = formatTime(item.receivedAtMillis),
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkMuted,
                    )
                }
            }

            // Row 2: Large code display
            Text(
                text = item.code,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 38.sp,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Monospace,
                ),
                fontWeight = FontWeight.Black,
                color = Ink,
            )

            // Row 3: SMS content
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isPicked) {
                    AccentGreen.copy(alpha = 0.06f)
                } else {
                    AccentTerracotta.copy(alpha = 0.06f)
                },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "短信内容",
                        style = MaterialTheme.typography.labelLarge,
                        color = InkMuted,
                    )
                    Text(
                        text = item.body,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Ink,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(DividerTint),
            )

            // Row 4: Matched rule + hints
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "命中规则：${item.matchedRule}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HintChip(
                        icon = Icons.Rounded.TouchApp,
                        text = "双击打开短信",
                        tint = AccentBlue,
                    )

                    if (showRestoreAction) {
                        TextButton(onClick = onRestorePending) {
                            Icon(
                                imageVector = Icons.Rounded.RestartAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "恢复未取件")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(text: String, accent: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = accent.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.3f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(accent),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = accent,
            )
        }
    }
}

@Composable
private fun HintChip(icon: ImageVector, text: String, tint: Color) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = tint.copy(alpha = 0.08f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = icon,
                contentDescription = null,
                tint = tint,
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = InkMuted,
            )
        }
    }
}

// ── Swipe Action Container (Fixed) ──────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SwipeActionContainer(
    onActionClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val revealWidth = 120.dp
    val revealWidthPx = with(density) { revealWidth.toPx() }
    val offsetX = remember { Animatable(0f) }
    var actionTriggered by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
    ) {
        // Green action background
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(SwipeGreen),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Box(
                modifier = Modifier
                    .width(revealWidth)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .combinedClickable(
                            onClick = {
                                if (!actionTriggered) {
                                    actionTriggered = true
                                    onActionClick()
                                }
                            },
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DoneAll,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                    Text(
                        text = "已取件",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                    )
                }
            }
        }

        // Foreground content with drag
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .draggable(
                    state = rememberDraggableState { delta ->
                        if (!actionTriggered) {
                            scope.launch {
                                offsetX.snapTo(
                                    (offsetX.value + delta).coerceIn(-revealWidthPx, 0f),
                                )
                            }
                        }
                    },
                    orientation = Orientation.Horizontal,
                    onDragStopped = { velocity ->
                        if (!actionTriggered) {
                            scope.launch {
                                val shouldReveal =
                                    offsetX.value <= (-revealWidthPx * 0.35f) || velocity < -800f
                                offsetX.animateTo(
                                    targetValue = if (shouldReveal) -revealWidthPx else 0f,
                                    animationSpec = tween(durationMillis = 200),
                                )
                            }
                        }
                    },
                ),
        ) {
            content()
        }
    }
}

// ── Rules Editor ────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun RulesEditorSheet(
    initialRules: List<String>,
    onDismissRequest: () -> Unit,
    onSave: (List<String>) -> Unit,
) {
    val rules = remember(initialRules) {
        mutableStateListOf<String>().apply { addAll(initialRules) }
    }
    val templates = remember {
        listOf(
            """取件码[：:\s]*([A-Za-z0-9-]+)""",
            """凭[：:\s]*([A-Za-z0-9-]+)""",
            """货码[：:\s]*([A-Za-z0-9-]+)""",
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.imePadding(),
        dragHandle = null,
        containerColor = CardSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(28.dp),
                )
                Text(
                    text = "规则设置",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Ink,
                )
            }

            Text(
                text = "每行填写一条正则表达式。保存后立即重新读取短信，提取到的取件码卡片会显示命中的规则。",
                style = MaterialTheme.typography.bodyLarge,
                color = InkMuted,
            )

            // Templates
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "常用模板（点击添加）",
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    templates.forEach { template ->
                        FilterChip(
                            selected = template in rules,
                            onClick = {
                                if (template !in rules) {
                                    rules += template
                                }
                            },
                            label = {
                                Text(
                                    text = template,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                        )
                    }
                }
            }

            // Rule inputs
            rules.indices.forEach { index ->
                OutlinedTextField(
                    value = rules[index],
                    onValueChange = { rules[index] = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    label = { Text(text = "规则 ${index + 1}") },
                    supportingText = {
                        Text(text = "示例：取件码[：:\\s]*([A-Za-z0-9-]+)")
                    },
                    trailingIcon = {
                        if (rules.size > 1) {
                            TextButton(onClick = { rules.removeAt(index) }) {
                                Text(text = "删除", color = AccentTerracotta)
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                )
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = { rules += "" },
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Rule,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "新增规则")
                }

                Button(
                    onClick = { onSave(rules.toList()) },
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(text = "保存并刷新", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

// ── Utilities ───────────────────────────────────────────────────────────────

private suspend fun openSmsOrConversation(
    context: Context,
    item: PickupCodeItem,
    onMessage: suspend (String) -> Unit,
) {
    val intents = buildList {
        add(
            Intent(
                Intent.ACTION_VIEW,
                ContentUris.withAppendedId(Telephony.Sms.Inbox.CONTENT_URI, item.smsId),
            ),
        )
        if (item.sender.isNotBlank()) {
            add(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("sms:${Uri.encode(item.sender)}"),
                ),
            )
        }
    }.map { intent ->
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    val resolvedIntent = intents.firstOrNull { intent ->
        intent.resolveActivity(context.packageManager) != null
    }

    if (resolvedIntent == null) {
        onMessage("当前系统短信应用不支持直接打开短信")
        return
    }

    runCatching {
        context.startActivity(resolvedIntent)
    }.onFailure {
        onMessage("打开短信失败，请确认系统短信应用可用")
        return
    }

    if (resolvedIntent.data?.scheme == "sms") {
        onMessage("当前系统未定位到单条短信，已打开对应短信会话")
    }
}

private fun formatTime(millis: Long): String =
    Instant
        .ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))
