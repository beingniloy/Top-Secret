package com.example.data.repository

import com.example.data.db.VaultDao
import com.example.data.model.VaultItem
import kotlinx.coroutines.flow.Flow

class VaultRepository(private val vaultDao: VaultDao) {

    fun getAllItems(isFake: Boolean): Flow<List<VaultItem>> {
        return vaultDao.getAllItems(isFake)
    }

    fun getItemsByType(isFake: Boolean, type: String): Flow<List<VaultItem>> {
        return vaultDao.getItemsByType(isFake, type)
    }

    fun getTrashItems(isFake: Boolean): Flow<List<VaultItem>> {
        return vaultDao.getTrashItems(isFake)
    }

    fun getItemById(id: Int): Flow<VaultItem?> {
        return vaultDao.getItemById(id)
    }

    suspend fun insertItem(item: VaultItem): Long {
        return vaultDao.insertItem(item)
    }

    suspend fun updateItem(item: VaultItem) {
        vaultDao.updateItem(item)
    }

    suspend fun deleteItemById(id: Int) {
        vaultDao.deleteItemById(id)
    }

    suspend fun emptyTrash(isFake: Boolean) {
        vaultDao.emptyTrash(isFake)
    }
}
