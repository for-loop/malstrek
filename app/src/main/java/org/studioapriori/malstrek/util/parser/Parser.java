package org.studioapriori.malstrek.util.parser;

/**
 * Generic parser interface for converting input of type T to output of type R.
 */
public interface Parser<T, R> {
    R parse(T input);
}
