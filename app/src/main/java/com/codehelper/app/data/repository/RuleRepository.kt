package com.codehelper.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.codehelper.app.data.model.ExtractionRule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "rules")

class RuleRepository(private val context: Context) {

    private val gson = Gson()
    private val rulesKey = stringPreferencesKey("extraction_rules")

    companion object {
        val DEFAULT_RULES = listOf(
            ExtractionRule(id = "default_1", prefix = "取件码", pattern = "[a-zA-Z0-9\\-]+"),
            ExtractionRule(id = "default_2", prefix = "凭", pattern = "[a-zA-Z0-9\\-]+"),
            ExtractionRule(id = "default_3", prefix = "提货码", pattern = "[a-zA-Z0-9\\-]+"),
            ExtractionRule(id = "default_4", prefix = "取货码", pattern = "[a-zA-Z0-9\\-]+"),
        )
    }

    val rules: Flow<List<ExtractionRule>> = context.dataStore.data.map { prefs ->
        val json = prefs[rulesKey]
        if (json != null) {
            try {
                val type = object : TypeToken<List<ExtractionRule>>() {}.type
                gson.fromJson(json, type)
            } catch (e: Exception) {
                DEFAULT_RULES
            }
        } else {
            DEFAULT_RULES
        }
    }

    suspend fun saveRules(rules: List<ExtractionRule>) {
        context.dataStore.edit { prefs ->
            prefs[rulesKey] = gson.toJson(rules)
        }
    }
}
