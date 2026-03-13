package com.mad.softwares.chatApplication.data.preferenceRepository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class AiUrlPreferenceRepository(
    private val dataStore: DataStore<Preferences>
) {
    private companion object{
        val API_URL_ENDPOINT = stringPreferencesKey("api_url_endpoint")
        const val tag = "AiUrlPrefre"
    }

    suspend fun saveUrlEndpoint(url:String){
        dataStore.edit { preferences ->
            preferences.set(API_URL_ENDPOINT,url)
        }
    }

    val aiUrlEndpoint: Flow<String> = dataStore.data
        .catch {
            if(it is IOException){
                Log.e(tag,"Error accessing the files",it)
            }
            else{
                throw it
            }
        }
        .map {
            preferences -> preferences[API_URL_ENDPOINT]?:""
        }
}