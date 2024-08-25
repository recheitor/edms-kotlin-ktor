package example.com.plugins

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bson.Document
import org.bson.types.ObjectId


@Serializable
data class RequestCategory(
    val id: String? = null,
    val name: String
) {
    fun toDocument(): Document = Document.parse(Json.encodeToString(this))

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromDocument(document: Document): RequestCategory {
            return RequestCategory(
                id = document.getObjectId("_id").toString(),
                name = document.getString("name")
            )
        }
    }
}

class RequestCategoryService(private val database: MongoDatabase) {
    private val collection: MongoCollection<Document> = database.getCollection("requestcategories")

    suspend fun getAll(): List<RequestCategory> =
        withContext(Dispatchers.IO) { collection.find().map(RequestCategory::fromDocument).toList() }

    suspend fun create(category: RequestCategory): String =
        withContext(Dispatchers.IO) {
            val doc = category.toDocument()
            collection.insertOne(doc)
            doc["_id"].toString()
        }

    suspend fun delete(id: String): Boolean =
        withContext(Dispatchers.IO) {
            val result = collection.deleteOne(Filters.eq("_id", ObjectId(id)))
            result.deletedCount > 0
        }
}