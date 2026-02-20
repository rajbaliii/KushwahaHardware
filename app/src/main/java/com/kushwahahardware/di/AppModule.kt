package com.kushwahahardware.di

import android.content.Context
import com.kushwahahardware.data.database.AppDatabase
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }
    
    @Provides
    fun provideProductDao(database: AppDatabase) = database.productDao()
    
    @Provides
    fun provideCategoryDao(database: AppDatabase) = database.categoryDao()
    
    @Provides
    fun provideSupplierDao(database: AppDatabase) = database.supplierDao()
    
    @Provides
    fun providePurchaseDao(database: AppDatabase) = database.purchaseDao()
    
    @Provides
    fun provideSaleDao(database: AppDatabase) = database.saleDao()
    
    @Provides
    fun provideCustomerDao(database: AppDatabase) = database.customerDao()
    
    @Provides
    fun provideStockHistoryDao(database: AppDatabase) = database.stockHistoryDao()
    
    @Provides
    fun provideShopInfoDao(database: AppDatabase) = database.shopInfoDao()
}