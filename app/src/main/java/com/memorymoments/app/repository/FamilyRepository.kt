package com.memorymoments.app.repository

import android.content.Context
import com.memorymoments.app.model.FamilyMember
import com.memorymoments.app.model.Person
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Compatibility wrapper bridging FamilyMember operations directly to PersonRepository.
 */
class FamilyRepository(context: Context) {
    private val personRepo = PersonRepository(context)

    val family: Flow<List<FamilyMember>> = personRepo.people.map { list ->
        list.map { it.toFamilyMember() }
    }

    suspend fun add(member: FamilyMember): Result<Unit> {
        return personRepo.add(Person.fromFamilyMember(member))
    }

    suspend fun update(member: FamilyMember): Result<Unit> {
        return personRepo.update(Person.fromFamilyMember(member))
    }

    suspend fun delete(id: String): Result<Unit> {
        return personRepo.delete(id)
    }
}
