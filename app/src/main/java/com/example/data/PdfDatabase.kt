package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- ROOM ENTITIES ---

@Entity(tableName = "pdf_files")
data class PdfFile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val path: String,
    val size: Long,
    val pageCount: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isLocked: Boolean = false,
    val tags: String = "", // Comma-separated tags
    val folderId: Int? = null
)

@Entity(tableName = "folders")
data class Folder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_sessions")
data class ChatSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pdfId: Int?,
    val pdfName: String,
    val title: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: Int,
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_items")
data class StudyItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pdfId: Int?,
    val type: String, // "flashcard" or "quiz"
    val question: String,
    val answer: String, // Answer or options JSON (e.g. "A: Options|B: Option|C: Option|D: Option||Correct: A")
    val timestamp: Long = System.currentTimeMillis()
)

// --- DATA ACCESS OBJECTS (DAOs) ---

@Dao
interface PdfDao {
    @Query("SELECT * FROM pdf_files ORDER BY timestamp DESC")
    fun getAllPdfs(): Flow<List<PdfFile>>

    @Query("SELECT * FROM pdf_files WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoritePdfs(): Flow<List<PdfFile>>

    @Query("SELECT * FROM pdf_files WHERE folderId = :folderId ORDER BY timestamp DESC")
    fun getPdfsInFolder(folderId: Int): Flow<List<PdfFile>>

    @Query("SELECT * FROM pdf_files WHERE id = :id LIMIT 1")
    suspend fun getPdfById(id: Int): PdfFile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPdf(pdf: PdfFile): Long

    @Update
    suspend fun updatePdf(pdf: PdfFile)

    @Delete
    suspend fun deletePdf(pdf: PdfFile)

    @Query("DELETE FROM pdf_files WHERE id = :id")
    suspend fun deletePdfById(id: Int)

    // --- Folders ---
    @Query("SELECT * FROM folders ORDER BY name ASC")
    fun getAllFolders(): Flow<List<Folder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: Folder): Long

    @Delete
    suspend fun deleteFolder(folder: Folder)

    // --- AI Chat Sessions ---
    @Query("SELECT * FROM chat_sessions ORDER BY timestamp DESC")
    fun getAllChatSessions(): Flow<List<ChatSession>>

    @Query("SELECT * FROM chat_sessions WHERE pdfId = :pdfId ORDER BY timestamp DESC")
    fun getChatSessionsForPdf(pdfId: Int): Flow<List<ChatSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatSession(session: ChatSession): Long

    @Query("DELETE FROM chat_sessions WHERE id = :id")
    suspend fun deleteChatSession(id: Int)

    // --- AI Chat Messages ---
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: Int): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    // --- AI Study Items ---
    @Query("SELECT * FROM study_items WHERE pdfId = :pdfId ORDER BY timestamp DESC")
    fun getStudyItemsForPdf(pdfId: Int): Flow<List<StudyItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyItem(item: StudyItem): Long

    @Query("DELETE FROM study_items WHERE id = :id")
    suspend fun deleteStudyItem(id: Int)
}

// --- ROOM DATABASE ---

@Database(
    entities = [PdfFile::class, Folder::class, ChatSession::class, ChatMessage::class, StudyItem::class],
    version = 1,
    exportSchema = false
)
abstract class PdfDatabase : RoomDatabase() {
    abstract fun pdfDao(): PdfDao
}
