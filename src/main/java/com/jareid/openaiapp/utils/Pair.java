package com.jareid.openaiapp.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a generic pair of two distinct types.
 *
 * @param <F> the type of the first element of the pair
 *
 * @author Jamie Reid
 * @version Last updated: 2023-07-24, Version 0.1.0
 * @since 2023-07-08
 */
public class Pair<F> {

    /** The first element of this pair. */
    private final F first;

    /** The second element of this pair. */
    private final F second;

    /**
     * Constructs a new pair with the given first and second elements.
     *
     * @param first  the first element
     * @param second the second element
     */
    public Pair(F first, F second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Returns the first element of this pair.
     *
     * @return the first element
     */
    public F getFirst() {
        return first;
    }

    /**
     * Returns the second element of this pair.
     *
     * @return the second element
     */
    public F getSecond() {
        return second;
    }

    @Override
    public String toString() {
        return "Pair{first=" + first + ", second=" + second + '}';
    }

    public List<F> convertToList() {
        List<F> list = new ArrayList<>();
        list.add( first );
        list.add( second );
        return list;
    }

    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        Pair< ? > pair = (Pair< ? >) o;
        return Objects.equals( first, pair.first ) &&
                Objects.equals( second, pair.second );
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}