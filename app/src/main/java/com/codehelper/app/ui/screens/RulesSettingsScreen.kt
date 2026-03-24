package com.codehelper.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.codehelper.app.data.model.ExtractionRule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesSettingsScreen(
    rules: List<ExtractionRule>,
    onSave: (List<ExtractionRule>) -> Unit,
    onBack: () -> Unit,
) {
    var editableRules by remember { mutableStateOf(rules.toMutableList()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "提取规则设置",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            modifier = Modifier.size(28.dp),
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { onSave(editableRules) }) {
                        Text(
                            "保存",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Instructions
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = "规则说明：每条规则由「前缀」和「正则表达式」组成。\n" +
                            "例如：前缀填「取件码」，正则填「[a-zA-Z0-9\\-]+」\n" +
                            "则会匹配短信中「取件码」后面的字母数字组合。",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add button
            TextButton(
                onClick = {
                    editableRules = (editableRules + ExtractionRule(
                        prefix = "",
                        pattern = "[a-zA-Z0-9\\-]+"
                    )).toMutableList()
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("添加规则", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(editableRules) { index, rule ->
                    RuleCard(
                        rule = rule,
                        onUpdate = { updated ->
                            editableRules = editableRules.toMutableList().also {
                                it[index] = updated
                            }
                        },
                        onDelete = {
                            editableRules = editableRules.toMutableList().also {
                                it.removeAt(index)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RuleCard(
    rule: ExtractionRule,
    onUpdate: (ExtractionRule) -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "启用",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = rule.isEnabled,
                        onCheckedChange = { onUpdate(rule.copy(isEnabled = it)) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "删除规则",
                            tint = Color(0xFFD32F2F),
                        )
                    }
                }
            }

            OutlinedTextField(
                value = rule.prefix,
                onValueChange = { onUpdate(rule.copy(prefix = it)) },
                label = { Text("前缀关键词") },
                placeholder = { Text("如：取件码、凭") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true,
            )

            OutlinedTextField(
                value = rule.pattern,
                onValueChange = { onUpdate(rule.copy(pattern = it)) },
                label = { Text("正则表达式") },
                placeholder = { Text("如：[a-zA-Z0-9\\-]+") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true,
            )

            // Preview
            Text(
                text = "预览: ${rule.toDisplayString()}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}
