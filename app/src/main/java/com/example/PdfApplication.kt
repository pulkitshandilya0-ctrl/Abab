package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.PdfDatabase
import com.example.data.AiRepository
import com.example.repository.PdfRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PdfApplication : Application() {

    lateinit var database: PdfDatabase
        private set

    lateinit var pdfRepository: PdfRepository
        private set

    lateinit var aiRepository: AiRepository
        private set

    override fun onCreate() {
        super.onCreate()

        // Initialize Room Database
        database = Room.databaseBuilder(
            applicationContext,
            PdfDatabase::class.java,
            "pdf_tech_masters_db"
        ).fallbackToDestructiveMigration().build()

        // Initialize Repositories
        pdfRepository = PdfRepository(database.pdfDao())
        aiRepository = AiRepository()

        // Populate sample data in the background if the DB is empty
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pdfList = pdfRepository.allPdfs.first()
                pdfRepository.populateSampleDataIfEmpty(pdfList.size)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
