package com.kushwahahardware.data.dao

import androidx.room.*
import com.kushwahahardware.data.entity.Supplier
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveSuppliers(): Flow<List<Supplier>>

    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<Supplier>>

    @Query("SELECT * FROM suppliers WHERE id = :id")
    suspend fun getSupplierById(id: Long): Supplier?

    @Query("SELECT * FROM suppliers WHERE name LIKE :search ORDER BY name ASC")
    fun searchSuppliers(search: String): Flow<List<Supplier>>

    @Query("SELECT SUM(pendingAmount) FROM purchases WHERE supplierId = :supplierId AND pendingAmount > 0")
    suspend fun getSupplierPendingPayments(supplierId: Long): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: Supplier): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuppliers(suppliers: List<Supplier>): List<Long>

    @Update
    suspend fun updateSupplier(supplier: Supplier)

    @Delete
    suspend fun deleteSupplier(supplier: Supplier)

    @Query("UPDATE suppliers SET isActive = 0 WHERE id = :id")
    suspend fun deactivateSupplier(id: Long)

    @Query("SELECT COUNT(*) FROM suppliers WHERE isActive = 1")
    suspend fun getActiveSuppliersCount(): Int
    @Upsert
    suspend fun insertOrUpdate(supplier: Supplier): Long
}
