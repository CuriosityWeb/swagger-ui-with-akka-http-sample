package CuriosityWeb.service

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.{GET, Path}

@Tag(name = "HealthCheckService")
final class HealthCheckService extends Service {

  override def routes: Route = health

  @GET
  @Path("/health")
  @Operation(summary = "Simple health check")
  @ApiResponse(responseCode = "200", content = Array(new Content(mediaType = MediaType.TEXT_PLAIN)))
  def health: Route = path("health") {
    complete(StatusCodes.OK)
  }
}
