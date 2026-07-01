package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.VaultItem
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {
    @Query("SELECT * FROM vault_items WHERE isTrash = 0 AND isFake = :isFake ORDER BY isPinned DESC, timestamp DESC")
    fun getAllItems(isFake: Boolean): Flow<List<VaultItem>>

    @Query("SELECT * FROM vault_items WHERE isTrash = 0 AND isFake = :isFake AND type = :type ORDER BY isPinned DESC, timestamp DESC")
    fun getItemsByType(isFake: Boolean, type: String): Flow<List<VaultItem>>

    @Query("SELECT * FROM vault_items WHERE isTrash = 1 AND isFake = :isFake ORDER BY timestamp DESC")
    fun getTrashItems(isFake: Boolean): Flow<List<VaultItem>>

    @Query("SELECT * FROM vault_items WHERE id = :id")
    fun getItemById(id: Int): Flow<VaultItem?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: VaultItem): Long

    @Update
    suspend fun updateItem(item: VaultItem)

    @Query("DELETE FROM vault_items WHERE id = :id")
    suspend fun deleteItemById(id: Int)

    @Query("DELETE FROM vault_items WHERE isTrash = 1 AND isFake = :isFake")
    suspend fun emptyTrash(isFake: Boolean)
}
