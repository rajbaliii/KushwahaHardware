package com.kushwahahardware.data.dao

import androidx.room.*
import com.kushwahahardware.data.entity.Supplier
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {
    
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<Supplier>>
    
    @Query("SELECT * FROM suppliers WHERE id = :id")
    suspend fun getSupplierById(id: Long): Supplier?
    
    @Query("SELECT * FROM suppliers WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchSuppliers(query: String): Flow<List<Supplier>>
    
    @Query("SELECT SUM(totalDue) FROM suppliers")
    fun getTotalSupplierDue(): Flow<Double?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: Supplier): Long
    
    @Update
    suspend fun updateSupplier(supplier: Supplier)
    
    @Delete
    suspend fun deleteSupplier(supplier: Supplier)
    
    @Query("UPDATE suppliers SET totalDue = totalDue + :amount WHERE id = :supplierId")
    suspend fun updateSupplierDue(supplierId: Long, amount: Double)
}