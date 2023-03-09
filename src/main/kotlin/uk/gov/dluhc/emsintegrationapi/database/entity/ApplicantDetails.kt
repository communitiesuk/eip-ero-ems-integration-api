package uk.gov.dluhc.emsintegrationapi.database.entity

import org.hibernate.annotations.NotFound
import org.hibernate.annotations.NotFoundAction
import java.time.LocalDate
import javax.persistence.CascadeType
import javax.persistence.Embeddable
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.validation.constraints.NotNull

@Embeddable
class ApplicantDetails(
    @NotNull
    val firstName: String,
    var middleNames: String? = null,
    @NotNull
    val surname: String,

    var email: String? = null,

    var dob: LocalDate? = null,

    var phoneNumber: String? = null,

    @OneToOne(
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "registered_address_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    val registeredAddress: Address,

    @NotNull
    val referenceNumber: String,

    @NotNull
    val ipAddress: String,

    var language: String? = null,

    @NotNull
    val emsElectoralId: String
) {
    override fun hashCode() = javaClass.hashCode()
}
