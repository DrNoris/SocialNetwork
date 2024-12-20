package com.example.lab6.domain;

import java.util.Objects;

public class Tuple<E1, E2> {
    private E1 e1;
    private E2 e2;

    public Tuple(E1 e1, E2 e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    public E1 getLeft() {
        return e1;
    }

    public E2 getRight() {
        return e2;
    }

    public void setLeft(E1 newE1) {
        e1 = newE1;
    }

    public void setRight(E2 newE2) {
        e2 = newE2;
    }

    @Override
    public String toString() {
        return " " + e1 + "," + e2;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // Same object
        if (obj == null || getClass() != obj.getClass()) return false; // Type check

        Tuple<?, ?> other = (Tuple<?, ?>) obj;

        // Use Objects.equals to safely compare nulls
        return Objects.equals(e1, other.e1) && Objects.equals(e2, other.e2);
    }

    @Override
    public int hashCode() {
        // Use Objects.hash to handle nulls and produce a consistent hash code
        return Objects.hash(e1, e2);
    }
}
