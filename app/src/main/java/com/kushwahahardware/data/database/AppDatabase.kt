package com.kushwahahardware.data.database

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.kushwahahardware.data.dao.*
import com.kushwahahardware.data.entity.*

@Database(
    entities = [
        Category::class,
        Product::class,
        Customer::class,
        Supplier::class,
        Purchase::class,
        PurchaseItem::class,
        Sale::class,
        SaleItem::class,
        StockHistory::class,
        ShopInfo::class,
        PaymentTransaction::class,
        Barcode::class,
        User::class,
        Role::class,
        Permission::class,
        RolePermission::class,
        ActivityLog::class
    ],
    version = 5,

    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun supplierDao(): SupplierDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun purchaseItemDao(): PurchaseItemDao
    abstract fun saleDao(): SaleDao
    abstract fun saleItemDao(): SaleItemDao
    abstract fun stockHistoryDao(): StockHistoryDao
    abstract fun shopInfoDao(): ShopInfoDao
    abstract fun paymentTransactionDao(): PaymentTransactionDao
    abstract fun barcodeDao(): BarcodeDao
    abstract fun userDao(): UserDao
    abstract fun roleDao(): RoleDao
    abstract fun activityLogDao(): ActivityLogDao



    companion object {
        fun getCallback(scope: CoroutineScope, roleDaoProvider: () -> RoleDao): Callback {
            return object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    scope.launch(Dispatchers.IO) {
                        val roleDao = roleDaoProvider()
                        
                        // 1. Seed Permissions
                        val modules = listOf("dashboard", "inventory", "sales", "purchase", "reports", "settings", "users_roles", "activity_logs")
                        val actions = listOf("view", "add", "edit", "delete")
                        
                        val permissionIds = mutableMapOf<String, Long>()
                        modules.forEach { module ->
                            actions.forEach { action ->
                                val id = roleDao.insertPermission(Permission(moduleName = module, action = action))
                                permissionIds["$module:$action"] = id
                            }
                        }
                        
                        // 2. Seed Roles
                        val superAdminId = roleDao.insertRole(Role(name = "Super Admin", description = "Full system access", isSystemRole = true))
                        val adminId = roleDao.insertRole(Role(name = "Admin", description = "Manage products, sales & reports", isSystemRole = true))
                        val employeeId = roleDao.insertRole(Role(name = "Employee", description = "Limited sales access only", isSystemRole = true))
                        
                        // 3. Assign default permissions to Admin (everything except RBAC management)
                        modules.filter { it != "users_roles" && it != "activity_logs" }.forEach { module ->
                            actions.forEach { action ->
                                permissionIds["$module:$action"]?.let { pId ->
                                    roleDao.insertRolePermission(RolePermission(adminId, pId))
                                }
                            }
                        }
                        
                        // 4. Assign default permissions to Employee (view only, add sales/inventory)
                        listOf("dashboard", "inventory", "sales", "purchase").forEach { module ->
                            permissionIds["$module:view"]?.let { pId -> roleDao.insertRolePermission(RolePermission(employeeId, pId)) }
                        }
                        permissionIds["sales:add"]?.let { pId -> roleDao.insertRolePermission(RolePermission(employeeId, pId)) }
                        permissionIds["inventory:add"]?.let { pId -> roleDao.insertRolePermission(RolePermission(employeeId, pId)) }

                        // Note: Super Admin logic is handled in PermissionManager (canAccess returns true)
                    }
                }
            }
        }
    }
}
