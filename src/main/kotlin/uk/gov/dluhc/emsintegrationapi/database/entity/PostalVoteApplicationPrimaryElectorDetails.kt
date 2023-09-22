package uk.gov.dluhc.emsintegrationapi.database.entity

import org.hibernate.Hibernate
import org.hibernate.annotations.NotFound
import org.hibernate.annotations.NotFoundAction
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

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
