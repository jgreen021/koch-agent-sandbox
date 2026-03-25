package com.koch.anomaly;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

/**
 * Factory for creating JPA Specifications generically.
 * Supports any field that implements Comparable (e.g., Integer, Double, LocalDateTime).
 */
public final class SpecificationFactory {

    private SpecificationFactory() {
        // Private constructor to prevent instantiation
    }

    /**
     * Creates an equality specification for a given field and value.
     * 
     * @param <T> The entity type
     * @param <V> The field value type (must be Comparable)
     * @param fieldName The name of the field in the entity
     * @param value The value to filter by
     * @return A JPA Specification
     */
    public static <T, V extends Comparable<? super V>> Specification<T> equalTo(String fieldName, V value) {
        return (root, query, cb) -> {
            if (value == null) {
                return null;
            }
            return cb.equal(root.get(fieldName), value);
        };
    }

    /**
     * Creates a greater-than-or-equal-to specification for range filtering.
     */
    public static <T, V extends Comparable<? super V>> Specification<T> greaterThanOrEqualTo(String fieldName, V value) {
        return (root, query, cb) -> {
            if (value == null) {
                return null;
            }
            return cb.greaterThanOrEqualTo(root.get(fieldName), value);
        };
    }

    /**
     * Creates a less-than-or-equal-to specification for range filtering.
     */
    public static <T, V extends Comparable<? super V>> Specification<T> lessThanOrEqualTo(String fieldName, V value) {
        return (root, query, cb) -> {
            if (value == null) {
                return null;
            }
            return cb.lessThanOrEqualTo(root.get(fieldName), value);
        };
    }
}
