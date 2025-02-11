package uk.gov.dluhc.emsintegrationapi.database.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.hibernate.Hibernate
import org.hibernate.annotations.NotFound
import org.hibernate.annotations.NotFoundAction

@Entity
class PostalVoteApplicationPrimaryElectorDetails(
    @Id
    var applicationId: String,

    @field:Size(max = 35)
    @NotNull
    var firstName: String,

    @field:Size(max = 100)
    var middleNames: String? = null,

    @field:Size(max = 35)
    @NotNull
    var surname: String,

    @OneToOne(
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "address_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    var address: Address,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as PostalVoteApplicationPrimaryElectorDetails

        return applicationId == other.applicationId
    }

    override fun hashCode(): Int = javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(" +
            "applicationId = $applicationId , " +
            "firstName = $firstName , " +
            "middleNames = $middleNames , " +
            "surname = $surname , " +
            "address = $address )"
    }
}
