package example.com.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.employeesRoutes(employeeService: EmployeeService) {
    route("/employees") {

        // Get all employees
        get("/") {
            try {
                val employees = employeeService.getAll()
                call.respond(employees)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "An error occurred")))
            }
        }

        // Get one employee by ID
        get("/{id}") {
            val id = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Wrong id"))
            try {
                val employee = employeeService.getOne(id)
                    ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Employee not found"))
                call.respond(employee)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "An error occurred")))
            }
        }

        // Create a new employee
        post("/create") {
            val parameters = call.receiveParameters()
            val employee = Employee(
                name = parameters["name"] ?: throw IllegalArgumentException("Missing name"),
                position = parameters["position"] ?: throw IllegalArgumentException("Missing position"),
                email = parameters["email"] ?: throw IllegalArgumentException("Missing email"),
                salary = parameters["salary"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid salary"),
                phone = parameters["phone"] ?: throw IllegalArgumentException("Missing phone"),
                status = parameters["status"]?.toBoolean() ?: false
            )
            try {
                val id = employeeService.create(employee)
                call.respond(HttpStatusCode.Created, mapOf("id" to id))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Invalid input")))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "An unexpected error occurred")))
            }
        }

        // Update an employee
        put("/{id}") {
            val id = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Wrong id"))
            val parameters = call.receiveParameters()
            val employee = Employee(
                name = parameters["name"] ?: throw IllegalArgumentException("Missing name"),
                position = parameters["position"] ?: throw IllegalArgumentException("Missing position"),
                email = parameters["email"] ?: throw IllegalArgumentException("Missing email"),
                salary = parameters["salary"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid salary"),
                phone = parameters["phone"] ?: throw IllegalArgumentException("Missing phone"),
                status = parameters["status"]?.toBoolean() ?: throw IllegalArgumentException("Invalid status")
            )
            try {
                val updatedEmployee = employeeService.update(id, employee)
                    ?: return@put call.respond(HttpStatusCode.NotFound, mapOf("error" to "Employee not found"))
                call.respond(HttpStatusCode.OK, updatedEmployee)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Invalid input")))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "An unexpected error occurred")))
            }
        }

        // Delete an employee
        delete("/{id}") {
            val id = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Wrong id"))
            try {
                val success = employeeService.delete(id)
                if (success) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Employee deleted successfully"))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Employee not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "An unexpected error occurred")))
            }
        }
    }
}