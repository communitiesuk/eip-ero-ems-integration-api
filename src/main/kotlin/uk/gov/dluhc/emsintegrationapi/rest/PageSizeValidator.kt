package uk.gov.dluhc.emsintegrationapi.rest

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.config.ApiProperties
import kotlin.reflect.KClass

@Component
class PageSizeValidator(private val apiProperties: ApiProperties) : ConstraintValidator<ValidPageSize, Int> {
    private val validationMessage =
        "The page size must be greater than or equal to 1 and less than or equal to ${apiProperties.maxPageSize}"

    override fun isValid(pageSize: Int?, constraintContext: ConstraintValidatorContext): Boolean {
        val valid = pageSize == null || pageSize >= 1 && pageSize <= apiProperties.maxPageSize
        if (!valid) {
            constraintContext.disableDefaultConstraintViolation()
            constraintContext.buildConstraintViolationWithTemplate(validationMessage).addConstraintViolation()
        }
        return valid
    }
}

@Constraint(validatedBy = [PageSizeValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class ValidPageSize(
    val message: String = "Invalid page size",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
