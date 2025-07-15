package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType

@ControllerAdvice
class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse> {
    val fieldErrors = ex.bindingResult.fieldErrors

    val (missingInputs, incorrectRanges) = fieldErrors.partition {
      it.code in listOf("NotBlank", "NotEmpty", "NotNull")
    }

    val type = when {
      missingInputs.isNotEmpty() -> ValidationErrorType.MISSING_INPUTS
      incorrectRanges.isNotEmpty() -> ValidationErrorType.INCORRECT_RANGE
      else -> ValidationErrorType.MISSING_INPUTS // fallback default
    }

    val fields = fieldErrors.map { "${it.field}: ${it.defaultMessage}" }

    val response = ValidationErrorResponse(
      type = type,
      message = "Validation failed for request",
      fields = fields
    )

    return ResponseEntity(response, HttpStatus.BAD_REQUEST)
  }
}