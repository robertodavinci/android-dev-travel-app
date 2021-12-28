package com.apps.travel_app.data.room

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import java.util.prefs.Preferences

private var USER_PREFERENCES_NAME = "user_preferences"

class DataStoreManager(val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = USER_DATASTORE)

    companion object {

        val NAME = stringPreferencesKey("NAME")
        val PHONE_NUMBER = stringPreferencesKey("PHONE_NUMBER")
        val ADDRESS = stringPreferencesKey("ADDRESS")

    }


    suspend fun savetoDataStore(phonebook: PhoneBook) {
        context.dataStore.edit {

            it[NAME] = phonebook.name
            it[PHONE_NUMBER] = phonebook.phoneNumber
            it[ADDRESS] = phonebook.address

        }
    }

    suspend fun getFromDataStore() = context.dataStore.data.map {
        Phonebook(
            name = it[NAME] ?: "",
            phoneNumber = it[PHONE_NUMBER] ?: "",
            address = it[ADDRESS] ?: ""
        )
    }
}