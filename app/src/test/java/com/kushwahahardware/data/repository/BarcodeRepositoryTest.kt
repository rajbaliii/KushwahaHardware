package com.kushwahahardware.data.repository

import com.kushwahahardware.data.dao.*
import com.kushwahahardware.data.entity.*
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

@ExperimentalCoroutinesApi
class BarcodeRepositoryTest {

    private lateinit var barcodeDao: BarcodeDao
    private lateinit var repository: BarcodeRepository

    @Before
    fun setup() {
        barcodeDao = mockk(relaxed = true)
        repository = BarcodeRepository(barcodeDao)
    }

    @Test
    fun `generateUniqueBarcodes should return requested number of barcodes`() = runTest {
        val count = 5
        coEvery { barcodeDao.getBarcodeBySerial(any()) } returns null
        coEvery { barcodeDao.insertBarcode(any()) } returns 1L

        val result = repository.generateUniqueBarcodes(count)

        assertEquals(count, result.size)
        coVerify(exactly = count) { barcodeDao.insertBarcode(any()) }
    }

    @Test
    fun `generateUniqueBarcodes should follow the correct format`() = runTest {
        val count = 1
        val datePrefix = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        coEvery { barcodeDao.getBarcodeBySerial(any()) } returns null

        val result = repository.generateUniqueBarcodes(count)

        val barcode = result[0]
        assert(barcode.startsWith("BC-$datePrefix-"))
        // BC-YYYYMMDD-XXXXXX -> "BC" + "-" + "YYYYMMDD" + "-" + "XXXXXX" = 2+1+8+1+6 = 18 characters
        assertEquals(18, barcode.length)
    }

    @Test
    fun `generateUniqueBarcodes should retry if serial already exists`() = runTest {
        val count = 1
        val existingSerial = "BC-20260303-EXIST1"
        val newSerial = "BC-20260303-NEWNEW"

        // First call returns an object (exists), second returns null (unique)
        coEvery { barcodeDao.getBarcodeBySerial(any()) } returnsMany listOf(
            Barcode(serialNumber = existingSerial),
            null
        )
        coEvery { barcodeDao.insertBarcode(any()) } returns 1L

        val result = repository.generateUniqueBarcodes(count)

        assertEquals(1, result.size)
        coVerify(exactly = 2) { barcodeDao.getBarcodeBySerial(any()) }
        coVerify(exactly = 1) { barcodeDao.insertBarcode(any()) }
    }
}
