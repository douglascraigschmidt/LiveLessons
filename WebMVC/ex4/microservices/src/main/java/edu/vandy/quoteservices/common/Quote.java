package edu.vandy.quoteservices.common;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * This class stores quotes (either persistently or non-persistently) and
 * can also be passed between clients and the microservices.
 *
 * The {@code @Value} annotation indicates that all fields of this
 * class are treated as immutable, i.e., they should be declared as
 * final and only initialized through the class constructor. This
 * annotation is typically used with Lombok to generate boilerplate
 * code like getters, a constructor, {@code toString()}, {@code
 * equals()}, and {@code hashCode()} methods automatically.
 *
 * The {@code @RequiredArgsConstructor} annotation generates a
 * constructor with required arguments, which are final fields and
 * fields with constraints such as {@code @NonNull}. This annotation
 * simplifies the instantiation of the class by ensuring that all
 * necessary fields are initialized upon construction, facilitating
 * the creation of immutable objects.
 *
 * The {@code @NoArgsConstructor(force = true)} annotation generates a
 * no-argument constructor with default values for all fields. The
 * {@code force = true} parameter sets all final fields to their
 * default values (0/false/null), which is useful for frameworks
 * and libraries that require a no-argument constructor, such as JPA.
 *
 * The {@code @Entity} annotation marks this class as a JPA entity,
 * which indicates that the class is mapped to a table in an SQL
 * database and its fields are mapped to the columns of the
 * table. This annotation is essential for integrating the class with
 * JPA for object-relational mapping, enabling the class to
 * participate in data persistence operations.
 *
 * The {@code @Table(name = "QUOTE")} annotation specifies the SQL
 * table in the database with which this entity is associated. The
 * {@code name} attribute explicitly sets the table name. If not
 * specified, the table name would default to the class name. This
 * annotation provides control over the database schema mapping,
 * allowing for the customization of table names.
 */
@SuppressWarnings("ALL")
@Value
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
@Entity 
@Table(name = "QUOTE")
public class Quote {
    /**
     * The ID # of the quote. {@code @Id} marks this field as the
     * primary key of the entity's corresponding table.  {@code
     * GeneratedValue} indicates that the database will generate the
     * primary key ({@code id}) automatically upon insertion. The
     * {@code IDENTITY} strategy indicates that the database's
     * identity column should be used.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    /**
     * The value of the quote. {@code @Column} maps the quote field to
     * the column named {@code quote} in the {@code QUOTE} table. The
     * {@code nullable = false} part specifies that the column cannot
     * contain null values, making it a required field in the
     * database.
     */
    @Column(name = "quote", nullable = false)
    public String quote;
}
