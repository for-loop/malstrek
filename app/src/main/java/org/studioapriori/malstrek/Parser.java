package org.studioapriori.malstrek;

public interface Parser<T, R> {
    R parse(T input);
}
