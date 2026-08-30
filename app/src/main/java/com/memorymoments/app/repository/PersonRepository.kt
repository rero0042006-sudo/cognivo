package com.memorymoments.app.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.memorymoments.app.data.local.PreferenceKeys
import com.memorymoments.app.data.local.appDataStore
import com.memorymoments.app.model.FamilyMember
import com.memorymoments.app.model.Person
import com.memorymoments.app.utils.Constants
import com.memorymoments.app.utils.ImageStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PersonRepository(context: Context) {
    private val appContext = context.applicationContext
    private val dataStore = appContext.appDataStore
    private val imageStorage = ImageStorage(appContext)
    private val gson = Gson()
    private val personListType = object : TypeToken<List<Person>>() {}.type
    private val familyListType = object : TypeToken<List<FamilyMember>>() {}.type
    private val mutex = Mutex()

    val people: Flow<List<Person>> = dataStore.data.map { prefs ->
        val peopleJson = prefs[PreferenceKeys.PEOPLE]
        if (!peopleJson.isNullOrBlank()) {
            decodePeople(peopleJson)
        } else {
            // Migrate from existing family members seamlessly
            val familyJson = prefs[PreferenceKeys.FAMILY_MEMBERS]
            if (!familyJson.isNullOrBlank()) {
                val family = decodeFamily(familyJson)
                val migrated = family.map { Person.fromFamilyMember(it) }
                // Persist migrated list asynchronously
                if (migrated.isNotEmpty()) {
                    persist(migrated)
                }
                migrated
            } else {
                emptyList()
            }
        }
    }

    suspend fun add(person: Person): Result<Unit> = mutex.withLock {
        val current = people.first()
        if (current.size >= Constants.MAX_FAMILY_MEMBERS) {
            return Result.failure(IllegalStateException("family-complete"))
        }
        if (current.any { it.id == person.id }) {
            return Result.failure(IllegalStateException("duplicate"))
        }
        persist(current + person)
        Result.success(Unit)
    }

    suspend fun update(person: Person): Result<Unit> = mutex.withLock {
        val current = people.first()
        if (current.none { it.id == person.id }) {
            return Result.failure(IllegalStateException("missing"))
        }
        persist(current.map { if (it.id == person.id) person.copy(updatedAt = System.currentTimeMillis()) else it })
        Result.success(Unit)
    }

    suspend fun delete(id: String): Result<Unit> = mutex.withLock {
        val current = people.first()
        persist(current.filterNot { it.id == id })
        imageStorage.deleteForMember(id)
        MemoryRepository(appContext).removePersonReference(id)
        LifeEventRepository(appContext).removePersonReference(id)
        Result.success(Unit)
    }

    suspend fun getById(id: String): Person? {
        return people.first().find { it.id == id }
    }

    private suspend fun persist(persons: List<Person>) {
        dataStore.edit { prefs ->
            val json = gson.toJson(persons)
            prefs[PreferenceKeys.PEOPLE] = json
            // Also maintain legacy FAMILY_MEMBERS in sync so old code/data is completely safe
            prefs[PreferenceKeys.FAMILY_MEMBERS] = gson.toJson(persons.map { it.toFamilyMember() })
        }
    }

    private fun decodePeople(json: String?): List<Person> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            gson.fromJson<List<Person>>(json, personListType).orEmpty()
        }.getOrDefault(emptyList())
    }

    private fun decodeFamily(json: String?): List<FamilyMember> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            gson.fromJson<List<FamilyMember>>(json, familyListType).orEmpty()
        }.getOrDefault(emptyList())
    }
}
