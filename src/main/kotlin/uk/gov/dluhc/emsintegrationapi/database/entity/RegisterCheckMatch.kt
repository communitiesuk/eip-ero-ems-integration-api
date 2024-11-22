package uk.gov.dluhc.emsintegrationapi.database.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.Hibernate
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.NotFound
import org.hibernate.annotations.NotFoundAction
import java.sql.Types
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Table
@Entity
class RegisterCheckMatch(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(Types.CHAR)
    var id: UUID? = null,

    @NotNull
    var emsElectorId: String,

    @NotNull
    var attestationCount: Int,

    @OneToOne(
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.EAGER
    )
    @JoinColumn(name = "personal_detail_id")
    @NotFound(action = NotFoundAction.EXCEPTION)
    var personalDetail: PersonalDetail,

    var registeredStartDate: LocalDate?,

    var registeredEndDate: LocalDate?,

    var applicationCreatedAt: Instant?,

    var franchiseCode: String?,

    @OneToOne(
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.EAGER
    )
    @JoinColumn(name = "postal_voting_arrangement", referencedColumnName = "id")
    @NotFound(action = NotFoundAction.IGNORE)
    var postalVotingArrangement: VotingArrangement?,

    @OneToOne(
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.EAGER
    )
    @JoinColumn(name = "proxy_voting_arrangement", referencedColumnName = "id")
    @NotFound(action = NotFoundAction.IGNORE)
    var proxyVotingArrangement: VotingArrangement?,

    @Column(updatable = false)
    @CreationTimestamp
    var dateCreated: Instant? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as RegisterCheck

        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(id = $id, dateCreated = $dateCreated)"
    }
}
