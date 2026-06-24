package com.example.repository

import com.example.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import kotlin.random.Random

class PdfRepository(private val pdfDao: PdfDao) {

    val allPdfs: Flow<List<PdfFile>> = pdfDao.getAllPdfs()
    val favoritePdfs: Flow<List<PdfFile>> = pdfDao.getFavoritePdfs()
    val folders: Flow<List<Folder>> = pdfDao.getAllFolders()
    val chatSessions: Flow<List<ChatSession>> = pdfDao.getAllChatSessions()

    fun getPdfsInFolder(folderId: Int): Flow<List<PdfFile>> = pdfDao.getPdfsInFolder(folderId)

    suspend fun getPdfById(id: Int): PdfFile? = pdfDao.getPdfById(id)

    suspend fun createPdf(name: String, size: Long, pageCount: Int, folderId: Int? = null): PdfFile {
        val path = "/storage/emulated/0/Documents/$name"
        val pdf = PdfFile(
            name = name,
            path = path,
            size = size,
            pageCount = pageCount,
            folderId = folderId
        )
        val id = pdfDao.insertPdf(pdf)
        return pdf.copy(id = id.toInt())
    }

    suspend fun updatePdf(pdf: PdfFile) {
        pdfDao.updatePdf(pdf)
    }

    suspend fun deletePdf(pdf: PdfFile) {
        pdfDao.deletePdf(pdf)
    }

    suspend fun deletePdfById(id: Int) {
        pdfDao.deletePdfById(id)
    }

    suspend fun toggleFavorite(id: Int) {
        val pdf = pdfDao.getPdfById(id) ?: return
        pdfDao.updatePdf(pdf.copy(isFavorite = !pdf.isFavorite))
    }

    suspend fun renamePdf(id: Int, newName: String) {
        val pdf = pdfDao.getPdfById(id) ?: return
        val currentFolder = pdf.path.substringBeforeLast("/")
        val updatedName = if (newName.endsWith(".pdf", ignoreCase = true)) newName else "$newName.pdf"
        pdfDao.updatePdf(pdf.copy(
            name = updatedName,
            path = "$currentFolder/$updatedName"
        ))
    }

    suspend fun setPassword(id: Int, isLocked: Boolean) {
        val pdf = pdfDao.getPdfById(id) ?: return
        pdfDao.updatePdf(pdf.copy(isLocked = isLocked))
    }

    // --- Folder Operations ---
    suspend fun createFolder(name: String): Folder {
        val folder = Folder(name = name)
        val id = pdfDao.insertFolder(folder)
        return folder.copy(id = id.toInt())
    }

    suspend fun deleteFolder(folder: Folder) {
        pdfDao.deleteFolder(folder)
    }

    suspend fun movePdfToFolder(pdfId: Int, folderId: Int?) {
        val pdf = pdfDao.getPdfById(pdfId) ?: return
        pdfDao.updatePdf(pdf.copy(folderId = folderId))
    }

    // --- PDF Merge operation ---
    suspend fun mergePdfs(pdfFiles: List<PdfFile>, newName: String): PdfFile {
        val totalPages = pdfFiles.sumOf { it.pageCount }
        val totalSize = pdfFiles.sumOf { it.size }
        return createPdf(newName, totalSize, totalPages)
    }

    // --- PDF Split operation ---
    suspend fun splitPdf(pdfFile: PdfFile): List<PdfFile> {
        val splitList = mutableListOf<PdfFile>()
        val baseName = pdfFile.name.substringBeforeLast(".pdf")
        if (pdfFile.pageCount <= 1) {
            val part = createPdf("${baseName}_part1.pdf", pdfFile.size, 1)
            splitList.add(part)
        } else {
            val pagesPerPart = pdfFile.pageCount / 2
            val part1 = createPdf("${baseName}_part1.pdf", pdfFile.size / 2, pagesPerPart)
            val part2 = createPdf("${baseName}_part2.pdf", pdfFile.size / 2, pdfFile.pageCount - pagesPerPart)
            splitList.add(part1)
            splitList.add(part2)
        }
        return splitList
    }

    // --- AI Chat Session Operations ---
    suspend fun createChatSession(pdfId: Int?, pdfName: String, title: String): ChatSession {
        val session = ChatSession(pdfId = pdfId, pdfName = pdfName, title = title)
        val id = pdfDao.insertChatSession(session)
        return session.copy(id = id.toInt())
    }

    fun getMessagesForSession(sessionId: Int): Flow<List<ChatMessage>> = pdfDao.getMessagesForSession(sessionId)

    suspend fun insertMessage(sessionId: Int, text: String, isUser: Boolean): ChatMessage {
        val msg = ChatMessage(sessionId = sessionId, isUser = isUser, text = text)
        val id = pdfDao.insertMessage(msg)
        return msg.copy(id = id.toInt())
    }

    suspend fun deleteChatSession(id: Int) {
        pdfDao.deleteChatSession(id)
    }

    // --- AI Study Operations ---
    fun getStudyItemsForPdf(pdfId: Int): Flow<List<StudyItem>> = pdfDao.getStudyItemsForPdf(pdfId)

    suspend fun insertStudyItem(pdfId: Int?, type: String, question: String, answer: String): StudyItem {
        val item = StudyItem(pdfId = pdfId, type = type, question = question, answer = answer)
        val id = pdfDao.insertStudyItem(item)
        return item.copy(id = id.toInt())
    }

    suspend fun deleteStudyItem(id: Int) {
        pdfDao.deleteStudyItem(id)
    }

    // --- Populate Sample Data for First-Launch Experience ---
    suspend fun populateSampleDataIfEmpty(pdfsCount: Int) {
        if (pdfsCount == 0) {
            val defaultFolders = listOf("Work", "Study", "Personal", "Receipts")
            val folderIds = defaultFolders.map { createFolder(it).id }

            // Insert default PDFs
            val samplePdfs = listOf(
                Triple("Quarterly Financial Report.pdf", 4200000L, 24),
                Triple("Jetpack Compose Best Practices.pdf", 1850000L, 12),
                Triple("Gemini API Documentation.pdf", 8500000L, 56),
                Triple("Project Roadmap 2026.pdf", 3200000L, 8),
                Triple("Apartment Lease Agreement.pdf", 510000L, 5),
                Triple("OCR Scan Receipt.pdf", 1200000L, 1)
            )

            samplePdfs.forEachIndexed { index, (name, size, pages) ->
                val fId = if (index < folderIds.size) folderIds[index] else null
                val pdf = createPdf(name, size, pages, fId)
                if (index < 2) {
                    pdfDao.updatePdf(pdf.copy(isFavorite = true))
                }
            }
        }
    }
}
