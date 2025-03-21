package uk.gov.dluhc.emsintegrationapi.database.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import org.hibernate.annotations.NotFound
import org.hibernate.annotations.NotFoundAction
import java.time.LocalDate

@Embeddable
class ApplicantDetails(
    @field:Size(max = 35)
    val firstName: String,

    @field:Size(max = 100)
    var middleNames: String? = null,

    @field:Size(max = 35)
    val surname: String,

    @field:Size(max = 255)
    var email: String? = null,

    var dob: LocalDate? = null,

    @field:Size(max = 50)
    var phone: String? = null,

    @OneToOne(
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "registered_address_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    @field:Valid
    val registeredAddress: Address,

    @field:Size(max = 10)
    val referenceNumber: String,

    @field:Size(max = 45)
    val ipAddress: String,

    @Enumerated(EnumType.STRING)
    val language: Language,

    @field:Size(max = 255)
    val emsElectorId: String?
) {
    override fun hashCode() = javaClass.hashCode()

    enum class Language {
        EN,
        CY
    }
}
