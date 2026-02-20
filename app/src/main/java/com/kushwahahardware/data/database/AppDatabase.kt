package com.kushwahahardware.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kushwahahardware.data.dao.*
import com.kushwahahardware.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Product::class,
        Category::class,
        Supplier::class,
        Purchase::class,
        PurchaseItem::class,
        Sale::class,
        SaleItem::class,
        Customer::class,
        StockHistory::class,
        ShopInfo::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun supplierDao(): SupplierDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun saleDao(): SaleDao
    abstract fun customerDao(): CustomerDao
    abstract fun stockHistoryDao(): StockHistoryDao
    abstract fun shopInfoDao(): ShopInfoDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kushwaha_hardware_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
    
    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database)
                }
            }
        }
        
        private suspend fun populateDatabase(database: AppDatabase) {
            // Insert default categories
            val categoryDao = database.categoryDao()
            if (categoryDao.getCategoryCount() == 0) {
                categoryDao.insertAll(Category.getDefaultCategories())
            }
            
            // Insert default shop info
            val shopInfoDao = database.shopInfoDao()
            if (shopInfoDao.getShopInfoSync() == null) {
                shopInfoDao.insertShopInfo(ShopInfo())
            }
        }
    }
}