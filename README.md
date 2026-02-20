# Kushwaha Hardware - Android App

A comprehensive inventory management and billing application for Kushwaha Hardware store located in Mahanwa, Bihar, India.

## Features

### Dashboard
- Shop branding with "Kushwaha Hardware, Mahanwa, Bihar"
- Summary cards: Total Products, Today's Sales, Total Pending Amount
- Quick action buttons: New Sale, Add Purchase, Add Product
- Low stock alerts

### Inventory Management
- Product list with search and filter by category/brand
- Add/Edit products with full details
- Stock tracking with low stock alerts
- Stock history log

### Purchase Management
- Supplier management
- Create purchase entries with multiple products
- Track pending payments to suppliers
- Purchase history

### Sales & Invoicing
- Create invoices with shop header
- Customer management
- Cash and Credit payment options
- Auto stock deduction on sale
- PDF invoice generation
- Share invoices via WhatsApp

### Reports
- Daily/Weekly/Monthly sales reports
- Profit calculation
- Stock reports (low stock alerts)
- Supplier pending payments
- Customer pending payments

### Settings
- Shop information management
- Export data to Excel (Products, Sales, Purchases, Stock)
- Cloud backup placeholder (Google Drive)
- Biometric app lock
- Category management

## Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Repository pattern
- **Database**: Room (SQLite)
- **Dependency Injection**: Hilt
- **Navigation**: Jetpack Navigation Component
- **PDF Generation**: iTextPDF
- **Excel Export**: Apache POI
- **Biometric Auth**: AndroidX Biometric

## Project Structure

```
app/src/main/java/com/kushwahahardware/
├── data/
│   ├── dao/              # Room DAO interfaces
│   ├── database/         # AppDatabase
│   ├── entity/           # Data entities
│   └── repository/       # Repository classes
├── di/
│   └── AppModule.kt      # Hilt dependency injection
├── navigation/
│   ├── BottomNavItem.kt  # Bottom navigation items
│   └── Screen.kt         # Screen routes
├── ui/
│   ├── screens/          # Compose screens
│   ├── theme/            # App theme and colors
│   └── viewmodel/        # ViewModels
├── utils/
│   ├── BiometricHelper.kt
│   ├── CurrencyUtils.kt
│   ├── DateUtils.kt
│   ├── ExcelExporter.kt
│   └── PdfGenerator.kt
├── KushwahaHardwareApp.kt
└── MainActivity.kt
```

## Database Schema

### Tables
1. **products** - Product inventory
2. **categories** - Product categories (Paint, Plumbing, Steel, Iron, Tools, Others)
3. **suppliers** - Supplier information
4. **purchases** - Purchase records
5. **purchase_items** - Purchase line items
6. **sales** - Sales records
7. **sale_items** - Sale line items
8. **customers** - Customer information
9. **stock_history** - Stock movement log
10. **shop_info** - Shop settings

## Setup Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17 or higher
- Android SDK 34
- Kotlin 1.9.0 or higher

### Installation

1. **Clone or download the project**
   ```bash
   cd KushwahaHardware
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing Android Studio project"
   - Choose the `KushwahaHardware` folder

3. **Sync Project**
   - Android Studio will automatically sync the Gradle files
   - If not, click `File > Sync Project with Gradle Files`

4. **Build the project**
   - Click `Build > Make Project` or press `Ctrl+F9`

5. **Run on device/emulator**
   - Connect an Android device or start an emulator
   - Click `Run > Run 'app'` or press `Shift+F10`

### Permissions
The app requires the following permissions:
- `INTERNET` - For cloud backup (optional)
- `WRITE_EXTERNAL_STORAGE` - For saving PDFs and Excel files
- `USE_BIOMETRIC` - For fingerprint authentication

## Usage

### First Launch
1. App opens directly to Dashboard (no login required)
2. Default categories are pre-loaded: Paint, Plumbing, Steel, Iron, Tools, Others
3. Shop info is set to "Kushwaha Hardware, Mahanwa, Bihar"

### Creating a Sale
1. Tap "New Sale" on Dashboard or go to Sales tab
2. Enter customer details (optional)
3. Select payment type (Cash/Credit)
4. Add products with quantities
5. Tap "Save & Share Invoice"
6. PDF invoice is generated and can be shared via WhatsApp

### Adding Products
1. Go to Inventory tab
2. Tap "+" FAB button
3. Fill product details
4. Save

### Managing Purchases
1. Go to Purchase tab
2. Add suppliers in the Suppliers tab
3. Create new purchase with supplier, invoice number, and items
4. Track pending payments

### Exporting Data
1. Go to Settings tab
2. Tap "Export Data" section
3. Select export type (Products, Sales, Purchases, Stock)
4. Files are saved to Downloads folder

### Enabling Biometric Lock
1. Go to Settings tab
2. Toggle "Biometric Lock" in Security section
3. App will require fingerprint on next launch

## Invoice Format

```
-------------------------------------
        KUSHWAHA HARDWARE
        Mahanwa, Bihar
-------------------------------------
Date: DD/MM/YYYY     Invoice No: XXXX
Customer: ___________  Phone: ________
-------------------------------------
Item | Qty | Rate | Amount
-------------------------------------
           Total: ₹XXXX.XX
           Paid:  ₹XXXX.XX
        Pending:  ₹XXXX.XX
-------------------------------------
     Thank you for your purchase!
-------------------------------------
```

## Configuration

### Changing Shop Details
1. Go to Settings > Shop Information
2. Edit shop name, location, phone, etc.

### Managing Categories
1. Go to Settings > Manage Categories
2. Add or remove categories as needed

## Troubleshooting

### Build Errors
- Ensure you're using JDK 17
- Clear build cache: `Build > Clean Project`
- Invalidate caches: `File > Invalidate Caches`

### PDF Not Sharing
- Ensure storage permission is granted
- Check if file provider is configured correctly

### Biometric Not Working
- Ensure device has fingerprint sensor
- Check if fingerprints are enrolled in device settings

## Future Enhancements

- [ ] Full Google Drive backup/restore
- [ ] Barcode scanning
- [ ] Multi-language support (Hindi)
- [ ] Advanced analytics with charts
- [ ] SMS notifications for pending payments
- [ ] GST calculation
- [ ] Multiple user support

## License

This project is proprietary software for Kushwaha Hardware store.

## Support

For issues or questions, contact the developer.

---

**Kushwaha Hardware**  
Mahanwa, Bihar, India  
Built with ❤️ for local businesses