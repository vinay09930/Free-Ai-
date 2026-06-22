package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "chat_entities")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "message_entities")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatId: Long,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "provider_entities")
data class ProviderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String, // OFFICIAL, BROWSER, LOCAL
    val status: String,
    val apiKey: String? = null,
    val isConnected: Boolean = false
)

@Entity(tableName = "model_entities")
data class ModelEntity(
    @PrimaryKey val id: String,
    val name: String,
    val providerId: String,
    val parameters: String? = null,
    val size: String? = null,
    val isInstalled: Boolean = false
)

@Entity(tableName = "document_entities")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_entities ORDER BY createdAt DESC")
    fun getAllChats(): Flow<List<ChatEntity>>

    @Insert
    suspend fun insertChat(chat: ChatEntity): Long

    @Query("SELECT * FROM message_entities WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: Long): Flow<List<MessageEntity>>

    @Insert
    suspend fun insertMessage(message: MessageEntity)
}

@Dao
interface ProviderDao {
    @Query("SELECT * FROM provider_entities")
    fun getAllProviders(): Flow<List<ProviderEntity>>

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertProvider(provider: ProviderEntity)
}

@Dao
interface ModelDao {
    @Query("SELECT * FROM model_entities")
    fun getAllModels(): Flow<List<ModelEntity>>

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertModel(model: ModelEntity)
}

@Dao
interface DocumentDao {
    @Query("SELECT * FROM document_entities")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity)
}

@Entity(tableName = "user_entities")
data class UserEntity(
    @PrimaryKey val email: String,
    val name: String,
    val profilePhotoUrl: String? = null,
    val lastLoginToken: String? = null
)

@Database(entities = [
    ChatEntity::class, 
    MessageEntity::class, 
    UserEntity::class, 
    ProviderEntity::class, 
    ModelEntity::class, 
    DocumentEntity::class
], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun providerDao(): ProviderDao
    abstract fun modelDao(): ModelDao
    abstract fun documentDao(): DocumentDao
}
