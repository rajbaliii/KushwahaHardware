package com.kushwahahardware.utils

import com.kushwahahardware.data.entity.Product

object BarcodeParser {
    /**
     * Parses a barcode to extract weight/quantity.
     * Scale barcodes often start with a prefix (e.g., 21) followed by product ID and weight.
     * Example: 21 01234 00750 -> Product 01234, weight 0.750 kg/units
     */
    fun extractQuantity(barcode: String, product: Product?): Double? {
        if (barcode.length != 12 && barcode.length != 13) return null
        
        // Check for weight-based prefix (e.g., 21 as requested)
        if (barcode.startsWith("21")) {
            // Typical format: 21 (prefix) + 5 digits (product code) + 5 digits (weight/price) + 1 (checksum)
            // Extract the weight portion (digits 8 to 12)
            val weightPart = barcode.substring(7, 12)
            val weightValue = weightPart.toDoubleOrNull() ?: return null
            
            // Convert to decimal (assuming weight is in thousands, e.g., 00750 = 0.750)
            return weightValue / 1000.0
        }
        
        return null
    }

    /**
     * Returns true if the barcode is a weight-based barcode.
     */
    fun isWeightedBarcode(barcode: String): Boolean {
        return barcode.startsWith("21") && (barcode.length == 12 || barcode.length == 13)
    }
}
