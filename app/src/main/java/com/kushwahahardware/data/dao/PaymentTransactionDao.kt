package com.kushwahahardware.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.kushwahahardware.data.entity.PaymentTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentTransactionDao {
    @Insert
    suspend fun insertTransaction(transaction: PaymentTransaction): Long

    @Query("SELECT * FROM payment_transactions WHERE partyId = :partyId AND partyType = :partyType ORDER BY transactionDate DESC")
    fun getTransactionsByParty(partyId: Long, partyType: String): Flow<List<PaymentTransaction>>

    @Query("SELECT * FROM payment_transactions ORDER BY transactionDate DESC")
    fun getAllTransactions(): Flow<List<PaymentTransaction>>

    @Query("DELETE FROM payment_transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)
}
