package example.com.plugins

import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.application.*

fun Route.requestCategoriesRoutes(categoryService: RequestCategoryService) {
    route("/requestCategories") {

        // Get all categories
        get("/") {
            try {
                val categories = categoryService.getAll()
                call.respond(categories)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "An error occurred")))
            }
        }

        // Create a new category
        post("/create") {
            val parameters = call.receiveParameters()
            val category = RequestCategory(
                name = parameters["name"] ?: throw IllegalArgumentException("Missing name")
            )
            try {
                val id = categoryService.create(category)
                call.respond(HttpStatusCode.Created, mapOf("id" to id))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Invalid input")))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "An unexpected error occurred")))
            }
        }

        // Delete a category by ID
        delete("/{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Wrong id")
            try {
                val success = categoryService.delete(id)
                if (success) {
                    call.respond(HttpStatusCode.OK, "Request category deleted successfully")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Request category not found")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "An unexpected error occurred")))
            }
        }
    }
}