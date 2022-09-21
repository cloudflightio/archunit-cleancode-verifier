package io.cloudflight.cleancode.archunit.rules.jpa

import com.tngtech.archunit.core.importer.ClassFileImporter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.OffsetDateTime
import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

class JpaRuleSetTest {

    private val rules = JpaRuleSet()

    @Test
    fun `do not use column nullable`() {
        val importer = ClassFileImporter().importClasses(EntityWithColumnAnnotations::class.java)
        rules.do_not_use_column_names.check(importer)
        rules.do_not_use_column_length.check(importer)
        assertThrows<AssertionError> {
            rules.do_not_use_column_nullable.check(importer)
        }
    }

    @Test
    fun `do not use column name`() {
        val importer = ClassFileImporter().importClasses(EntityWithColumnNames::class.java)
        rules.do_not_use_column_length.check(importer)
        rules.do_not_use_column_nullable.check(importer)
        assertThrows<AssertionError> {
            rules.do_not_use_column_names.check(importer)
        }
    }

    @Test
    fun `do not use column length`() {
        val importer = ClassFileImporter().importClasses(EntityWithColumnLength::class.java)
        rules.do_not_use_column_names.check(importer)
        rules.do_not_use_column_nullable.check(importer)
        assertThrows<AssertionError> {
            rules.do_not_use_column_length.check(importer)
        }
    }

    @Test
    fun `no violations on well-written entity`() {
        val importer = ClassFileImporter().importClasses(Project::class.java)
        rules.do_not_use_column_nullable.check(importer)
        rules.do_not_use_column_names.check(importer)
        rules.do_not_use_column_length.check(importer)
    }

    @Test
    fun `non-optional relationship annotations`() {
        val importer = ClassFileImporter().importClasses(EntityWithJoinToOtherEntity::class.java)
        rules.nullable_flag_of_kotlin_needs_to_match_jpa_specification.check(importer)
    }

    @Test
    fun `optional relationship annotations`() {
        val importer = ClassFileImporter().importClasses(EntityWithJoinToOtherOptionalEntity::class.java)
        rules.nullable_flag_of_kotlin_needs_to_match_jpa_specification.check(importer)
    }

    @Test
    fun `optional errnonous relationship annotations`() {
        val importer = ClassFileImporter().importClasses(EntityWithJoinToOtherOptionalErronousEntity::class.java)
        assertThrows<AssertionError> {
            rules.nullable_flag_of_kotlin_needs_to_match_jpa_specification.check(importer)
        }
    }

    @Test
    fun `nullable column with element collection`() {
        val importer = ClassFileImporter().importClasses(EntityWithFiles::class.java)
        rules.do_not_use_column_nullable.check(importer)
    }
}

@Entity
class Project(
    @Id
    val projectId: String,

    @field:NotNull
    @field:Size(max = 30)
    val userId: String,

    @field:NotNull
    var lastModifiedDate: OffsetDateTime = OffsetDateTime.now(),

    @field:NotNull
    @Column
    var expirationDate: OffsetDateTime = OffsetDateTime.now().plusHours(1),
    var orderedDate: OffsetDateTime? = null,

    @field:NotNull
    val isShoppingCartProject: Boolean = false
) {

    @NotNull
    val createdDate: OffsetDateTime = OffsetDateTime.now()

    var addedToCartDate: OffsetDateTime? = null
}


@Entity
class EntityWithColumnAnnotations(
    @Id
    val id: Long = 0,
    @Column(nullable = false)
    val x: Double
)

@Entity
class EntityWithColumnNames(
    @Id
    val id: Long = 0,
    @Column(name = "x")
    val x: Double
)

@Entity
class EntityWithColumnLength(
    @Id
    val id: Long = 0,
    @Column(length = 10)
    val x: String
)

@Entity
class EntityWithJoinToOtherEntity(
    @Id
    val id: Long = 0,
    @ManyToOne(optional = false)
    var someEntity: EntityWithColumnLength
)

@Entity
class EntityWithJoinToOtherOptionalEntity(
    @Id
    val id: Long = 0,
    @ManyToOne(optional = true)
    var someEntity: EntityWithColumnLength?
)

@Entity
class EntityWithJoinToOtherOptionalErronousEntity(
    @Id
    val id: Long = 0,
    @ManyToOne(optional = false)
    var someEntity: EntityWithColumnLength?
)


class EntityWithFiles {
    @ElementCollection
    @CollectionTable(
        name = "some_entity_file",
        joinColumns = [JoinColumn(name = "some_entity_id")],
        uniqueConstraints = [UniqueConstraint(name = "unique_some_entity_file_id", columnNames = ["file_id"])]
    )
    @Column(name = "file_id", nullable = false)
    var someFiles = mutableSetOf<FileReference>()
}

@Entity
class FileReference