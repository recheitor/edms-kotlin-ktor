package example.com.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.timeOffRequestsRoutes(timeOffService: TimeOffRequestService) {
    route("/timeOffRequests") {

        // Get all time off requests
        get("/") {
            try {
                val requests = timeOffService.getAll()
                call.respond(requests)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "An error occurred")))
            }
        }

        // Get time off requests by employee ID
        get("/{employeeId}") {
            val employeeId = call.parameters["employeeId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Wrong employeeId"))
            try {
                val requests = timeOffService.getByEmployee(employeeId)
                call.respond(requests)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "An error occurred")))
            }
        }

        // Create a new time off request
        post("/new") {
            try {
                val parameters = call.receiveParameters()

                val request = TimeOffRequest(
                    request_category_id = parameters["request_category_id"] ?: throw IllegalArgumentException("Missing request_category_id"),
                    employee_id = parameters["employee_id"] ?: throw IllegalArgumentException("Missing employee_id"),
                    start_date = parameters["start_date"] ?: throw IllegalArgumentException("Missing start_date"),
                    end_date = parameters["end_date"] ?: throw IllegalArgumentException("Missing end_date")
                )

                val id = timeOffService.create(request)
                call.respond(HttpStatusCode.Created, mapOf("id" to id))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Invalid input")))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "An unexpected error occurred")))
            }
        }

        // Delete a time off request
        delete("/{requestId}") {
            val requestId = call.parameters["requestId"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Wrong requestId"))
            try {
                val success = timeOffService.delete(requestId)
                if (success) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Request deleted successfully"))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Request not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "An unexpected error occurred")))
            }
        }
    }
}