package com.kushwahahardware.data.dao

import androidx.room.*
import com.kushwahahardware.data.entity.Customer
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): Customer?

    @Query("SELECT * FROM customers WHERE phone = :phone")
    suspend fun getCustomerByPhone(phone: String): Customer?

    @Query("SELECT * FROM customers WHERE name LIKE :search OR phone LIKE :search ORDER BY name ASC")
    fun searchCustomers(search: String): Flow<List<Customer>>

    @Query("SELECT SUM(pendingAmount) FROM sales WHERE customerId = :customerId AND pendingAmount > 0")
    suspend fun getCustomerPendingPayments(customerId: Long): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<Customer>): List<Long>

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("UPDATE customers SET isActive = 0 WHERE id = :id")
    suspend fun deactivateCustomer(id: Long)

    @Query("SELECT COUNT(*) FROM customers WHERE isActive = 1")
    suspend fun getActiveCustomersCount(): Int
    @Upsert
    suspend fun insertOrUpdate(customer: Customer): Long
}
