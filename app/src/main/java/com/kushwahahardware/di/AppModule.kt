package com.kushwahahardware.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kushwahahardware.data.dao.*
import com.kushwahahardware.data.database.AppDatabase
import com.kushwahahardware.data.repository.RBACRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        var dbInstance: AppDatabase? = null
        val callback = AppDatabase.getCallback(kotlinx.coroutines.GlobalScope) {
            dbInstance!!.roleDao()
        }

        dbInstance = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "kushwaha_hardware_db"
        ).addCallback(callback)
            .fallbackToDestructiveMigration()
            .build()
        return dbInstance
    }


    @Provides
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideProductDao(db: AppDatabase): ProductDao = db.productDao()

    @Provides
    fun provideCustomerDao(db: AppDatabase): CustomerDao = db.customerDao()

    @Provides
    fun provideSupplierDao(db: AppDatabase): SupplierDao = db.supplierDao()

    @Provides
    fun providePurchaseDao(db: AppDatabase): PurchaseDao = db.purchaseDao()

    @Provides
    fun providePurchaseItemDao(db: AppDatabase): PurchaseItemDao = db.purchaseItemDao()

    @Provides
    fun provideSaleDao(db: AppDatabase): SaleDao = db.saleDao()

    @Provides
    fun provideSaleItemDao(db: AppDatabase): SaleItemDao = db.saleItemDao()

    @Provides
    fun provideStockHistoryDao(db: AppDatabase): StockHistoryDao = db.stockHistoryDao()

    @Provides
    fun provideShopInfoDao(db: AppDatabase): ShopInfoDao = db.shopInfoDao()

    @Provides
    fun providePaymentTransactionDao(db: AppDatabase): PaymentTransactionDao = db.paymentTransactionDao()

    @Provides
    fun provideBarcodeDao(db: AppDatabase): BarcodeDao = db.barcodeDao()

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideRoleDao(db: AppDatabase): RoleDao = db.roleDao()

    @Provides
    fun provideActivityLogDao(db: AppDatabase): ActivityLogDao = db.activityLogDao()
    @Provides
    @Singleton
    fun provideRBACRepository(
        userDao: UserDao,
        roleDao: RoleDao,
        activityLogDao: ActivityLogDao
    ): RBACRepository = RBACRepository(userDao, roleDao, activityLogDao)

    @Provides
    @Singleton
    fun provideAuthService(): com.kushwahahardware.data.service.AuthService = 
        com.kushwahahardware.data.service.FirebaseAuthService()
}
