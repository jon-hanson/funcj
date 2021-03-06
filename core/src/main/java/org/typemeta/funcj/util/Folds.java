package org.typemeta.funcj.util;

import org.typemeta.funcj.functions.Functions;

import java.util.*;
import java.util.stream.Stream;

/**
 * Fold operations.
 */
public abstract class Folds {
    /**
     * Left-fold a function over an {@link Iterable}.
     * @param f         the binary function to be applied for the fold
     * @param z         the starting value for the fold
     * @param itb       the iterable to be folded over
     * @param <T>       the iterable element type
     * @param <R>       the result type of fold operation
     * @return          the folded value
     */
    public static <T, R> R foldLeft(Functions.F2<R, T, R> f, R z, Iterable<T> itb) {
        R acc = z;
        for (T t : itb) {
            acc = f.apply(acc, t);
        }
        return acc;
    }

    /**
     * Left-fold a function over a {@link Stream}.
     * @param f         the binary function to be applied for the fold
     * @param z         the starting value for the fold
     * @param str       the stream to be folded over
     * @param <T>       the stream element type
     * @param <R>       the result type of fold operation
     * @return          the folded value
     */
    public static <T, R> R foldLeft(Functions.F2<R, T, R> f, R z, Stream<T> str) {
        R acc = z;
        for (Iterator<T> iter = str.iterator(); iter.hasNext();) {
            acc = f.apply(acc, iter.next());
        }
        return acc;
    }

    /**
     * Left-fold a function over a non-empty {@link Iterable}.
     * @param f         the binary operator to be applied for the fold
     * @param iter      the iterable to be folded over
     * @param <T>       the iterable element type
     * @return          the folded value
     */
    public static <T> T foldLeft1(Functions.Op2<T> f, Iterable<T> iter) {
        T acc = null;
        for (T t : iter) {
            if (acc == null) {
                acc = t;
            } else {
                acc = f.apply(acc, t);
            }
        }

        if (acc == null) {
            throw new IllegalArgumentException("Supplied Iterable argument is empty");
        } else {
            return acc;
        }
    }

    /**
     *
     * Right-fold a function over an {@link List}}
     * @param f         the binary function to be applied for the fold
     * @param z         the starting value for the fold
     * @param l         the list to fold over
     * @param <T>       the list element type
     * @param <R>       the result type of fold operation
     * @return          the folded value
     */
    public static <T, R> R foldRight(Functions.F2<T, R, R> f, R z, List<T> l) {
        R acc = z;
        for (int i = l.size() - 1; i >= 0; --i) {
            acc = f.apply(l.get(i), acc);
        }
        return acc;
    }

    /**
     * Right-fold a function over a non-empty {@link List}.
     * @param f         the binary operator to be applied for the fold
     * @param l         the {@code List} to fold over
     * @param <T>       the list element type
     * @return          the folded value
     */
    public static <T> T foldRight1(Functions.Op2<T> f, List<T> l) {
        final int i0 = l.size() - 1;
        T acc = null;
        for (int i = i0; i >= 0; --i) {
            if (i == i0) {
                acc = l.get(i);
            } else {
                acc = f.apply(l.get(i), acc);
            }
        }
        return acc;
    }

    /**
     * Right-fold a function over an {@link Set}}
     * @param f         the binary function to be applied for the fold
     * @param z         the starting value for the fold
     * @param s         the set to fold over
     * @param <T>       the set element type
     * @param <R>       the result type of fold operation
     * @return          the folded value
     */
    public static <T, R> R foldRight(Functions.F2<T, R, R> f, R z, Set<T> s) {
        return foldRight(f, z, new ArrayList<T>(s));
    }

    /**
     * Right-fold a function over a non-empty  {@link Set}}
     * @param f         the binary function to be applied for the fold
     * @param s         the set to fold over
     * @param <T>       the set element type
     * @return          the folded value
     */
    public static <T> T foldRight1(Functions.Op2<T> f, Set<T> s) {
        return foldRight1(f, new ArrayList<T>(s));
    }
}
