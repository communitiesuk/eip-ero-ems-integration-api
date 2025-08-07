package uk.gov.dluhc.emsintegrationapi.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.Hibernate

@Table(name = "ero_absent_vote_hold")
@Entity
class EroAbsentVoteHold(
    @Id
    @Column(name = "ero_id", nullable = false, length = 100)
    var eroId: String,

    @NotNull
    @Column(name = "hold_enabled", nullable = false)
    var holdEnabled: Boolean
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as EroAbsentVoteHold

        return eroId == other.eroId
    }

    override fun hashCode(): Int = eroId.hashCode()

    override fun toString(): String =
        "${this::class.simpleName}(eroId=$eroId, holdEnabled=$holdEnabled)"
}
