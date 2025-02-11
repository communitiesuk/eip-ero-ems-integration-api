package uk.gov.dluhc.emsintegrationapi.rest

import jakarta.validation.ConstraintValidatorContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import uk.gov.dluhc.emsintegrationapi.config.ApiProperties

@ExtendWith(MockitoExtension::class)
internal class PageSizeValidatorTest {

    private val apiProperties = ApiProperties(
        requestHeaderName = "test",
        defaultPageSize = 10,
        maxPageSize = 20,
        forceMaxPageSize = 20
    )

    private val validationErrorMessage =
        "The page size must be greater than or equal to 1 and less than or equal to ${apiProperties.maxPageSize}"

    private val pageSizeValidator = PageSizeValidator(
        apiProperties = ApiProperties(
            requestHeaderName = "test",
            defaultPageSize = 10,
            maxPageSize = 20,
            forceMaxPageSize = 20
        )
    )

    @Mock
    private lateinit var constraintContext: ConstraintValidatorContext

    @Mock
    private lateinit var constraintViolationBuilder: ConstraintValidatorContext.ConstraintViolationBuilder

    @Nested
    inner class PageSizeIsValid {
        @Test
        fun `should return true if page size is null`() {
            assertThat(pageSizeValidator.isValid(pageSize = null, constraintContext)).isEqualTo(true)
            verifyNoInteractions(constraintContext)
        }

        @Test
        fun `should return true if page size is greater than 1`() {
            assertThat(pageSizeValidator.isValid(pageSize = apiProperties.maxPageSize, constraintContext)).isEqualTo(
                true
            )
            verifyNoInteractions(constraintContext)
        }
    }

    @Nested
    inner class PageSizeIsNotValid {
        @BeforeEach
        fun mockConstraintContextMethods() {
            BDDMockito.willDoNothing().given(constraintContext).disableDefaultConstraintViolation()
            given(constraintContext.buildConstraintViolationWithTemplate(BDDMockito.anyString())).willReturn(
                constraintViolationBuilder
            )
        }

        @AfterEach
        fun afterEach() {
            verify(constraintContext).disableDefaultConstraintViolation()
            verify(constraintContext).buildConstraintViolationWithTemplate(validationErrorMessage)
        }

        @Test
        fun `should return false if page size is less than 1`() {
            assertThat(pageSizeValidator.isValid(pageSize = 0, constraintContext)).isEqualTo(false)
        }

        @Test
        fun `should return false if page size is greater max page size`() {
            assertThat(
                pageSizeValidator.isValid(
                    pageSize = apiProperties.maxPageSize + 1,
                    constraintContext
                )
            ).isEqualTo(
                false
            )
        }
    }
}
