package com.jiri.perspective.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jiri.perspective.R
import com.jiri.perspective.data.repository.SubscriptionWithUsageCount
import com.jiri.perspective.ui.viewmodel.SubscriptionViewModel
import kotlinx.coroutines.launch



// Fixní seznam kategorií pro formulář.
private val categoryOptions = listOf(
    "Streaming",
    "Music",
    "Productivity",
    "Gaming",
    "Education",
    "Fitness",
    "News",
    "Other"
)

@Composable
fun SubscriptionListScreen(
    // Screen dostane ViewModel, přes který komunikuje s logikou a daty.
    viewModel: SubscriptionViewModel,
    modifier: Modifier = Modifier
) {
    // Odebíráme seznam subscriptions z ViewModelu jako Compose state.
    // Když se data změní, screen se automaticky překreslí.
    val subscriptions by viewModel.subscriptions.collectAsState()

    // Odvozené hodnoty pro horní overview kartu.
    val activeSubscriptions = subscriptions.count { it.isActive }
    val monthlyTotal = subscriptions
        .filter { it.isActive && it.billingPeriod.equals("Monthly", ignoreCase = true) }
        .sumOf { it.price }
    val totalUsageCount = subscriptions.sumOf { it.usageCount }

    // Snackbar state a coroutine scope pro zobrazování zpráv.
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Lokální UI stav pro dialogy a formulář.
    var showFormDialog by remember { mutableStateOf(false) }
    var editingSubscription by remember { mutableStateOf<SubscriptionWithUsageCount?>(null) }
    var subscriptionPendingDelete by remember { mutableStateOf<SubscriptionWithUsageCount?>(null) }

    // Lokální stav polí ve formuláři.
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(categoryOptions.first()) }

    // Stringy si načteme v composable scope, aby se daly bezpečně použít níž.
    val addSubscriptionSuccessText = stringResource(R.string.add_subscription_success)
    val editSubscriptionSuccessText = stringResource(R.string.edit_subscription_success)
    val deleteSubscriptionSuccessText = stringResource(R.string.delete_subscription_success)
    val deleteSubscriptionTitleText = stringResource(R.string.delete_subscription_title)
    val cancelText = stringResource(R.string.cancel)
    val deleteText = stringResource(R.string.delete)
    val backupToCloudText = stringResource(R.string.backup_to_cloud)
    val backupSuccessfulText = stringResource(R.string.backup_successful)
    val backupFailedText = stringResource(R.string.backup_failed)

    // Vyčistí formulář a vrátí ho do výchozího stavu pro přidání nového záznamu.
    fun resetForm() {
        editingSubscription = null
        name = ""
        description = ""
        price = ""
        category = categoryOptions.first()
    }

    // Otevře dialog pro přidání nové subscription.
    fun openAddDialog() {
        resetForm()
        showFormDialog = true
    }

    // Otevře dialog pro editaci a předvyplní formulář existujícími daty.
    fun openEditDialog(subscription: SubscriptionWithUsageCount) {
        editingSubscription = subscription
        name = subscription.name
        description = subscription.description ?: ""
        price = subscription.price.toString()
        category = subscription.category.ifBlank { categoryOptions.first() }
        showFormDialog = true
    }

    // Zobrazí snackbar zprávu.
    // showSnackbar je suspend funkce, proto ji voláme v coroutine.
    fun showMessage(message: String) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    // Zpracuje potvrzení formuláře.
    // Pokud editingSubscription == null, přidáváme nový záznam.
    // Jinak upravujeme existující.
    fun submitForm() {
        val currentEditing = editingSubscription

        if (currentEditing == null) {
            viewModel.addSubscription(
                name = name,
                description = description,
                price = price,
                category = category
            )
            showMessage(addSubscriptionSuccessText)
        } else {
            viewModel.updateSubscription(
                subscriptionId = currentEditing.id,
                name = name,
                description = description,
                price = price,
                category = category,
                startDate = currentEditing.startDate,
                nextPaymentDate = currentEditing.nextPaymentDate,
                isActive = currentEditing.isActive,
                createdAt = currentEditing.createdAt
            )
            showMessage(editSubscriptionSuccessText)
        }

        // Po potvrzení formulář zavřeme a resetneme lokální stav.
        showFormDialog = false
        resetForm()
    }

    // Scaffold je základní layout screenu.
    // Tady používáme hlavně snackbar host a floating action button.
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { openAddDialog() }) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.add_subscription_cd)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                // innerPadding přidává Scaffold, aby obsah neležel pod FAB / systémovými částmi layoutu.
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            viewModel.backupSubscriptionsToCloud()
                            showMessage(backupSuccessfulText)
                        } catch (e: Exception) {
                            showMessage(backupFailedText)
                        }
                    }
                },
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text(backupToCloudText)
            }


            // Horní přehledová karta s rychlými statistikami.
            OverviewCard(
                activeSubscriptions = activeSubscriptions,
                monthlyTotal = monthlyTotal,
                totalUsageCount = totalUsageCount
            )

            // Když nejsou žádné subscriptions, ukážeme empty state.
            // Jinak zobrazíme seznam položek.
            if (subscriptions.isEmpty()) {
                EmptySubscriptionsState(
                    onAddClick = { openAddDialog() }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(subscriptions) { subscription ->
                        SubscriptionItem(
                            subscription = subscription,
                            onEditClick = { openEditDialog(it) },
                            onDeleteClick = { subscriptionPendingDelete = it },
                            onResetUsageClick = { viewModel.resetUsageEntries(it.id) },
                            onAddUsageClick = { viewModel.addUsageEntry(it) },
                            onRemoveUsageClick = { viewModel.removeUsageEntry(it) }
                        )
                    }
                }
            }
        }
    }

    // Dialog pro add / edit formulář.
    if (showFormDialog) {
        SubscriptionFormDialog(
            isEditing = editingSubscription != null,
            name = name,
            onNameChange = { name = it },
            description = description,
            onDescriptionChange = { description = it },
            price = price,
            onPriceChange = { price = it },
            category = category,
            onCategoryChange = { category = it },
            onDismiss = {
                showFormDialog = false
                resetForm()
            },
            onConfirm = { submitForm() }
        )
    }

    // Potvrzovací dialog před smazáním subscription.
    subscriptionPendingDelete?.let { subscription ->
        AlertDialog(
            onDismissRequest = { subscriptionPendingDelete = null },
            title = { Text(deleteSubscriptionTitleText) },
            text = {
                Text(
                    stringResource(
                        R.string.delete_subscription_message,
                        subscription.name
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSubscriptionById(subscription.id)
                        subscriptionPendingDelete = null
                        showMessage(deleteSubscriptionSuccessText)
                    }
                ) {
                    Text(deleteText)
                }
            },
            dismissButton = {
                TextButton(onClick = { subscriptionPendingDelete = null }) {
                    Text(cancelText)
                }
            }
        )
    }
}

@Composable
fun EmptySubscriptionsState(
    onAddClick: () -> Unit
) {
    // Empty state karta, která se zobrazí když seznam subscriptions je prázdný.
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = stringResource(R.string.empty_subscriptions_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = stringResource(R.string.empty_subscriptions_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            Button(
                onClick = onAddClick,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(stringResource(R.string.add_subscription))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SubscriptionFormDialog(
    // isEditing říká, jestli dialog slouží pro add nebo edit.
    isEditing: Boolean,
    name: String,
    onNameChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    price: String,
    onPriceChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    // Jednoduchá validační logika formuláře.
    val isNameValid = name.isNotBlank()
    val parsedPrice = price.toDoubleOrNull()
    val isPriceValid = parsedPrice != null && parsedPrice > 0.0
    val isFormValid = isNameValid && isPriceValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) {
                    stringResource(R.string.edit_subscription)
                } else {
                    stringResource(R.string.add_subscription)
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.name)) },
                    singleLine = true,
                    isError = !isNameValid,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Chybová hláška pro neplatné jméno.
                if (!isNameValid) {
                    Text(
                        text = stringResource(R.string.name_required_error),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text(stringResource(R.string.description_optional)) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = onPriceChange,
                    label = { Text(stringResource(R.string.monthly_price_czk)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = price.isNotBlank() && !isPriceValid,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )

                // Chybová hláška pro neplatnou cenu.
                if (price.isNotBlank() && !isPriceValid) {
                    Text(
                        text = stringResource(R.string.price_required_error),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.category),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                // Kategorie se vybírá přes chipy z předdefinovaného seznamu.
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categoryOptions.forEach { option ->
                        val isSelected = category == option

                        AssistChip(
                            onClick = { onCategoryChange(option) },
                            label = {
                                Text(
                                    text = categoryDisplayName(option),
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                                labelColor = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                }
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = isFormValid
            ) {
                Text(
                    if (isEditing) {
                        stringResource(R.string.save)
                    } else {
                        stringResource(R.string.add_subscription)
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun OverviewCard(
    activeSubscriptions: Int,
    monthlyTotal: Double,
    totalUsageCount: Int
) {
    // Horní souhrnná karta na screenu.
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = stringResource(R.string.overview),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CompactStat(
                    label = stringResource(R.string.active),
                    value = activeSubscriptions.toString(),
                    modifier = Modifier.weight(1f)
                )
                CompactStat(
                    label = stringResource(R.string.monthly),
                    value = stringResource(R.string.value_monthly_czk, monthlyTotal),
                    modifier = Modifier.weight(1f)
                )
                CompactStat(
                    label = stringResource(R.string.uses),
                    value = totalUsageCount.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun CompactStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    // Malý blok s jednou statistikou: label + value.
    Column(
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun SubscriptionItem(
    subscription: SubscriptionWithUsageCount,
    onEditClick: (SubscriptionWithUsageCount) -> Unit,
    onDeleteClick: (SubscriptionWithUsageCount) -> Unit,
    onResetUsageClick: (SubscriptionWithUsageCount) -> Unit,
    onAddUsageClick: (Long) -> Unit,
    onRemoveUsageClick: (Long) -> Unit
) {
    // Price per use počítáme jen pokud usageCount > 0.
    val pricePerUse = if (subscription.usageCount > 0) {
        subscription.price / subscription.usageCount.toDouble()
    } else {
        null
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = subscription.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Description zobrazíme jen pokud není null ani prázdná.
                    if (!subscription.description.isNullOrBlank()) {
                        Text(
                            text = subscription.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Akce pro edit, reset usage a delete.
                Row {
                    IconButton(onClick = { onEditClick(subscription) }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.edit)
                        )
                    }

                    IconButton(onClick = { onResetUsageClick(subscription) }) {
                        Text(
                            text = "↻",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = { onDeleteClick(subscription) }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.delete_cd),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Category chip.
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = categoryDisplayName(subscription.category),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                // Zvýrazněná cena subscription.
                Text(
                    text = stringResource(
                        R.string.value_price_with_currency,
                        subscription.price,
                        subscription.currency
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MinimalInfoBox(
                    label = stringResource(R.string.price_per_use),
                    value = pricePerUse?.let {
                        stringResource(
                            R.string.value_price_per_use_with_currency,
                            it,
                            subscription.currency
                        )
                    } ?: "-",
                    modifier = Modifier.weight(1f)
                )

                UsageStepperLike(
                    usageCount = subscription.usageCount,
                    onAddClick = { onAddUsageClick(subscription.id) },
                    onRemoveClick = { onRemoveUsageClick(subscription.id) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MinimalInfoBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    // Menší box pro jednoduchou doplňkovou informaci.
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun UsageStepperLike(
    usageCount: Int,
    onAddClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Box se zobrazením usage a dvěma tlačítky minus / plus.
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.usage),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = usageCount.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepperBubbleButton(
                    text = "-",
                    enabled = usageCount > 0,
                    onClick = onRemoveClick
                )

                StepperBubbleButton(
                    text = "+",
                    enabled = true,
                    onClick = onAddClick
                )
            }
        }
    }
}

@Composable
fun StepperBubbleButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Barvy tlačítka se mění podle toho, jestli je aktivní.
    val backgroundColor = if (enabled) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
    }

    val contentColor = if (enabled) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }

    Box(
        modifier = modifier
            .size(36.dp)
            .background(
                color = backgroundColor,
                shape = CircleShape
            )
            .clickable(
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

@Composable
private fun categoryDisplayName(category: String): String {
    // Převádí interní text kategorie na lokalizovaný název ze strings.xml.
    return when (category) {
        "Streaming" -> stringResource(R.string.category_streaming)
        "Music" -> stringResource(R.string.category_music)
        "Productivity" -> stringResource(R.string.category_productivity)
        "Gaming" -> stringResource(R.string.category_gaming)
        "Education" -> stringResource(R.string.category_education)
        "Fitness" -> stringResource(R.string.category_fitness)
        "News" -> stringResource(R.string.category_news)
        else -> stringResource(R.string.category_other)
    }
}