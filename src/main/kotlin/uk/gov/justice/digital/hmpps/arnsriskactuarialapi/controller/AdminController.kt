package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@RestController
@RequestMapping("/admin")
class AdminController {

  @PostMapping("/update-offence-mapping")
  @Operation(
    summary = "Update offence mappings",
    description = "Updates the offence mappings used for risk score calculations.",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Offence mappings successfully updated.",
        content = [Content()]
      ),
      ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
    ],
    security = [],
  )
  fun postUpdateOffenceMappings(): ResponseEntity<Unit> {
    // TODO: ACT-215 Load mappings into Redis from Manage Offences API
    return ResponseEntity.ok().build()
  }
}