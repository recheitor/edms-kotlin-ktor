package example.com.plugins

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.bson.Document
import org.bson.types.ObjectId
import java.text.SimpleDateFormat
import java.util.Date
import example.com.utils.addHoursToDates

@Serializable
data class TimeOffRequest(
    val id: String? = null,
    val request_category_id: String,
    val employee_id: String,
    val start_date: String,
    val end_date: String 
) {
    fun toDocument(): Document = Document()
        .append("request_category_id", ObjectId(request_category_id))
        .append("employee_id", ObjectId(employee_id))
        .append("start_date", parseDate(start_date))
        .append("end_date", parseDate(end_date))

    companion object {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

        fun fromDocument(document: Document): TimeOffRequest = TimeOffRequest(
            id = document.getObjectId("_id").toString(),
            request_category_id = document.getObjectId("request_category_id").toString(),
            employee_id = document.getObjectId("employee_id").toString(),
            start_date = formatDate(document.getDate("start_date")),
            end_date = formatDate(document.getDate("end_date"))
        )

        private fun formatDate(date: Date): String = dateFormat.format(date)
        private fun parseDate(dateString: String): Date = dateFormat.parse(dateString)
    }
}

class TimeOffRequestService(private val database: MongoDatabase) {
    private val collection: MongoCollection<Document> = database.getCollection("timeoffrequests")

    suspend fun getAll(): List<TimeOffRequest> =
        withContext(Dispatchers.IO) { 
            collection.find()
                .map(TimeOffRequest::fromDocument)
                .map { addHoursToDates(it, 2) }
                .toList() 
        }

    suspend fun getByEmployee(employeeId: String): List<TimeOffRequest> =
        withContext(Dispatchers.IO) {
            collection.find(Filters.eq("employee_id", ObjectId(employeeId)))
                .map(TimeOffRequest::fromDocument)
                .map { addHoursToDates(it, 2) }
                .toList()
        }

    suspend fun create(request: TimeOffRequest): String =
        withContext(Dispatchers.IO) {
            val adjustedRequest = addHoursToDates(request, 2) 
            val doc = adjustedRequest.toDocument()
            collection.insertOne(doc)
            doc["_id"].toString()
        }

    suspend fun delete(requestId: String): Boolean =
        withContext(Dispatchers.IO) {
            val result = collection.deleteOne(Filters.eq("_id", ObjectId(requestId)))
            result.deletedCount > 0
        }
}