package com.frame.zero.routes

import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.domain.production.ProductionSort
import com.frame.zero.dto.common.PagedResponse
import com.frame.zero.dto.production.AddMemberRequest
import com.frame.zero.dto.production.CreateProductionRequest
import com.frame.zero.dto.production.PhaseTransitionRequest
import com.frame.zero.dto.production.UpdateMemberRequest
import com.frame.zero.dto.production.UpdateProductionRequest
import com.frame.zero.services.ProductionService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.productionRoutes() {
  val service by inject<ProductionService>()

  authenticate("auth-jwt") {
    route("/api/v1/productions") {
      get {
        val userId = call.userId()
        val phases =
          call.request.queryParameters.getAll("phase")?.mapNotNull {
            runCatching { ProductionPhase.valueOf(it) }.getOrNull()
          } ?: emptyList()
        val query = call.request.queryParameters["q"]
        val sort =
          call.request.queryParameters["sort"]?.let {
            runCatching { ProductionSort.valueOf(it) }.getOrNull()
          } ?: ProductionSort.DEFAULT
        val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceIn(1, 100) ?: 20
        val cursor = call.request.queryParameters["cursor"]
        val (items, nextCursor) = service.list(userId, phases, query, sort, limit, cursor)
        call.respond(PagedResponse(items = items, nextCursor = nextCursor))
      }

      post {
        val userId = call.userId()
        val request = call.receive<CreateProductionRequest>()
        val production = service.create(userId, request)
        call.response.header("Location", "/api/v1/productions/${production.id}")
        call.respond(HttpStatusCode.Created, production)
      }

      route("/{id}") {
        get {
          val userId = call.userId()
          val productionId = call.pathUuid("id")
          call.respond(service.get(userId, productionId))
        }

        patch {
          val userId = call.userId()
          val productionId = call.pathUuid("id")
          val request = call.receive<UpdateProductionRequest>()
          call.respond(service.update(userId, productionId, request))
        }

        post("/phase") {
          val userId = call.userId()
          val productionId = call.pathUuid("id")
          val request = call.receive<PhaseTransitionRequest>()
          call.respond(service.transitionPhase(userId, productionId, request))
        }

        delete {
          val userId = call.userId()
          val productionId = call.pathUuid("id")
          service.delete(userId, productionId)
          call.respond(HttpStatusCode.NoContent)
        }

        route("/members") {
          get {
            val userId = call.userId()
            val productionId = call.pathUuid("id")
            call.respond(service.listMembers(userId, productionId))
          }

          post {
            val userId = call.userId()
            val productionId = call.pathUuid("id")
            val request = call.receive<AddMemberRequest>()
            val member = service.addMember(userId, productionId, request)
            call.respond(HttpStatusCode.Created, member)
          }

          patch("/{memberId}") {
            val userId = call.userId()
            val productionId = call.pathUuid("id")
            val memberId = call.pathUuid("memberId")
            val request = call.receive<UpdateMemberRequest>()
            call.respond(service.updateMemberRole(userId, productionId, memberId, request))
          }

          delete("/{memberId}") {
            val userId = call.userId()
            val productionId = call.pathUuid("id")
            val memberId = call.pathUuid("memberId")
            service.removeMember(userId, productionId, memberId)
            call.respond(HttpStatusCode.NoContent)
          }
        }
      }
    }
  }
}
