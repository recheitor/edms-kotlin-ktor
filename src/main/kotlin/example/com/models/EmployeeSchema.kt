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
data class Employee(
    val _id: String? = null,
    val name: String,
    val position: String,
    val email: String,
    val salary: Int,
    val phone: String,
    val status: Boolean? = false
) {
    init {
        require(email.isNotBlank()) { "Email is required." }
        require(Regex("\\S+@\\S+\\.\\S+").matches(email)) { "Email is invalid." }
        require(phone.isNotBlank()) { "Phone is required." }
    }

    fun toDocument(): Document = Document.parse(Json.encodeToString(this))

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromDocument(document: Document): Employee {
            return Employee(
                _id = document.getObjectId("_id").toString(),
                name = document.getString("name"),
                position = document.getString("position"),
                email = document.getString("email"),
                salary = document.getInteger("salary"),
                phone = document.getString("phone"),
                status = document.getBoolean("status") ?: false
            )
        }
    }
}


class EmployeeService(private val database: MongoDatabase) {
    var collection: MongoCollection<Document>

    init {
        database.createCollection("employees")
        collection = database.getCollection("employees")
    }

    // Get all employees
    suspend fun getAll(): List<Employee> =
            withContext(Dispatchers.IO) { collection.find().map(Employee::fromDocument).toList() }

    // Get one employee by ID
    suspend fun getOne(id: String): Employee? =
            withContext(Dispatchers.IO) {
                collection
                        .find(Filters.eq("_id", ObjectId(id)))
                        .first()
                        ?.let(Employee::fromDocument)
            }

    // Create new employee
    suspend fun create(employee: Employee): String =
            withContext(Dispatchers.IO) {
                val doc = employee.toDocument()
                collection.insertOne(doc)
                doc["_id"].toString()
            }

    // Update employee by ID
    suspend fun update(id: String, employee: Employee): Employee? =
            withContext(Dispatchers.IO) {
                collection
                        .findOneAndReplace(Filters.eq("_id", ObjectId(id)), employee.toDocument())
                        ?.let(Employee::fromDocument)
            }

    // Delete employee by ID
    suspend fun delete(id: String): Boolean =
            withContext(Dispatchers.IO) {
                val result = collection.deleteOne(Filters.eq("_id", ObjectId(id)))
                result.deletedCount > 0
            }
}