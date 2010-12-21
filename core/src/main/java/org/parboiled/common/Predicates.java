/*
 * Copyright (C) 2007 Google Inc., adapted in 2010 by Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.parboiled.common;

import static org.parboiled.common.Preconditions.*;

import java.util.Collection;

public final class Predicates {

    private Predicates() {}

    /**
     * Returns a predicate that always evaluates to {@code true}.
     *
     * @return a predicate
     */
    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> alwaysTrue() {
        return (Predicate<T>) AlwaysTruePredicate.INSTANCE;
    }

    /**
     * Returns a predicate that always evaluates to {@code false}.
     *
     * @return a predicate
     */
    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> alwaysFalse() {
        return (Predicate<T>) AlwaysFalsePredicate.INSTANCE;
    }

    /**
     * Returns a predicate that evaluates to {@code true} if the object reference
     * being tested is null.
     *
     * @return a predicate
     */
    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> isNull() {
        return (Predicate<T>) IsNullPredicate.INSTANCE;
    }

    /**
     * Returns a predicate that evaluates to {@code true} if the object reference
     * being tested is not null.
     *
     * @return a predicate
     */
    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> notNull() {
        return (Predicate<T>) NotNullPredicate.INSTANCE;
    }

    /**
     * Returns a predicate that evaluates to {@code true} if the given predicate
     * evaluates to {@code false}.
     *
     * @param predicate the inner predicate
     * @return a predicate
     */
    public static <T> Predicate<T> not(Predicate<T> predicate) {
        return new NotPredicate<T>(predicate);
    }

    /**
     * Returns a predicate that evaluates to {@code true} if each of its
     * components evaluates to {@code true}. The components are evaluated in
     * order, and evaluation will be "short-circuited" as soon as a false
     * predicate is found. It defensively copies the iterable passed in, so future
     * changes to it won't alter the behavior of this predicate. If {@code
     * components} is empty, the returned predicate will always evaluate to {@code
     * true}.
     *
     * @param components the components
     * @return a predicate
     */
    public static <T> Predicate<T> and(Collection<? extends Predicate<? super T>> components) {
        return new AndPredicate<T>(components);
    }

    /**
     * Returns a predicate that evaluates to {@code true} if each of its
     * components evaluates to {@code true}. The components are evaluated in
     * order, and evaluation will be "short-circuited" as soon as a false
     * predicate is found. It defensively copies the array passed in, so future
     * changes to it won't alter the behavior of this predicate. If {@code
     * components} is empty, the returned predicate will always evaluate to {@code
     * true}.
     *
     * @param components the components
     * @return a predicate
     */
    public static <T> Predicate<T> and(Predicate<? super T>... components) {
        return new AndPredicate<T>(ImmutableList.of(components));
    }

    /**
     * Returns a predicate that evaluates to {@code true} if both of its
     * components evaluate to {@code true}. The components are evaluated in
     * order, and evaluation will be "short-circuited" as soon as a false
     * predicate is found.
     *
     * @param first the first
     * @param second the second
     * @return a predicate
     */
    public static <T> Predicate<T> and(Predicate<? super T> first, Predicate<? super T> second) {
        checkArgNotNull(first, "first");
        checkArgNotNull(second, "second");
        return new AndPredicate<T>(ImmutableList.<Predicate<? super T>>of(first, second));
    }

    /**
     * Returns a predicate that evaluates to {@code true} if any one of its
     * components evaluates to {@code true}. The components are evaluated in
     * order, and evaluation will be "short-circuited" as soon as as soon as a
     * true predicate is found. It defensively copies the iterable passed in, so
     * future changes to it won't alter the behavior of this predicate. If {@code
     * components} is empty, the returned predicate will always evaluate to {@code
     * false}.
     *
     * @param components the components
     * @return a predicate
     */
    public static <T> Predicate<T> or(Collection<? extends Predicate<? super T>> components) {
        return new OrPredicate<T>(components);
    }

    /**
     * Returns a predicate that evaluates to {@code true} if any one of its
     * components evaluates to {@code true}. The components are evaluated in
     * order, and evaluation will be "short-circuited" as soon as as soon as a
     * true predicate is found. It defensively copies the array passed in, so
     * future changes to it won't alter the behavior of this predicate. If {@code
     * components} is empty, the returned predicate will always evaluate to {@code
     * false}.
     *
     * @param components the components
     * @return a predicate
     */
    public static <T> Predicate<T> or(Predicate<? super T>... components) {
        return new OrPredicate<T>(ImmutableList.of(components));
    }

    /**
     * Returns a predicate that evaluates to {@code true} if either of its
     * components evaluates to {@code true}. The components are evaluated in
     * order, and evaluation will be "short-circuited" as soon as as soon as a
     * true predicate is found.
     *
     * @param first the first
     * @param second the second
     * @return a predicate
     */
    public static <T> Predicate<T> or(Predicate<? super T> first, Predicate<? super T> second) {
        checkArgNotNull(first, "first");
        checkArgNotNull(second, "second");
        return new OrPredicate<T>(ImmutableList.<Predicate<? super T>>of(first, second));
    }

    /**
     * Returns a predicate that evaluates to {@code true} if the object being
     * tested {@code equals()} the given target or both are null.
     *
     * @param target the target
     * @return a predicate
     */
    public static <T> Predicate<T> equalTo(T target) {
        return (target == null) ? Predicates.<T>isNull() : new IsEqualToPredicate<T>(target);
    }

    /**
     * Returns a predicate that evaluates to {@code true} if the object being
     * tested is an instance of the given class. If the object being tested
     * is {@code null} this predicate evaluates to {@code false}.
     * <p/>
     *
     * @param clazz the clazz
     * @return a predicate
     */
    public static Predicate<Object> instanceOf(Class<?> clazz) {
        return new InstanceOfPredicate(clazz);
    }

    /**
     * Returns a predicate that evaluates to {@code true} if the object reference
     * being tested is a member of the given collection. It does not defensively
     * copy the collection passed in, so future changes to it will alter the
     * behavior of the predicate.
     * <p/>
     * This method can technically accept any Collection<?>, but using a typed
     * collection helps prevent bugs. This approach doesn't block any potential
     * users since it is always possible to use {@code Predicates.<Object>in()}.
     *
     * @param target the collection that may contain the function input
     * @return a predicate
     */
    public static <T> Predicate<T> in(Collection<? extends T> target) {
        return new InPredicate<T>(target);
    }

    private static class AlwaysTruePredicate implements Predicate<Object> {
        private static final Predicate<Object> INSTANCE = new AlwaysTruePredicate();

        public boolean apply(Object o) {
            return true;
        }

        @Override
        public
        String toString() {
            return "AlwaysTrue";
        }
    }

    private static class AlwaysFalsePredicate implements Predicate<Object> {
        private static final Predicate<Object> INSTANCE = new AlwaysFalsePredicate();

        public boolean apply(Object o) {
            return false;
        }

        @Override
        public String toString() {
            return "AlwaysFalse";
        }
    }

    private static class NotPredicate<T> implements Predicate<T> {
        private final Predicate<T> predicate;

        private NotPredicate(Predicate<T> predicate) {
            checkArgNotNull(predicate, "predicate");
            this.predicate = predicate;
        }

        public boolean apply(T t) {
            return !predicate.apply(t);
        }

        public String toString() {
            return "Not(" + predicate.toString() + ")";
        }
    }

    private static class AndPredicate<T> implements Predicate<T> {
        private final Collection<? extends Predicate<? super T>> components;

        private AndPredicate(Collection<? extends Predicate<? super T>> components) {
            this.components = components;
        }

        public boolean apply(T t) {
            for (Predicate<? super T> predicate : components) {
                if (!predicate.apply(t)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String toString() {
            return "And(" + StringUtils.join(components, ", ") + ")";
        }
    }

    private static class OrPredicate<T> implements Predicate<T> {
        private final Collection<? extends Predicate<? super T>> components;

        private OrPredicate(Collection<? extends Predicate<? super T>> components) {
            this.components = components;
        }

        public boolean apply(T t) {
            for (Predicate<? super T> predicate : components) {
                if (predicate.apply(t)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return "Or(" + StringUtils.join(components, ", ") + ")";
        }
    }

    private static class IsEqualToPredicate<T> implements Predicate<T> {
        private final T target;

        private IsEqualToPredicate(T target) {
            this.target = target;
        }

        public boolean apply(T t) {
            return target.equals(t);
        }

        @Override
        public String toString() {
            return "IsEqualTo(" + target + ")";
        }
    }

    private static class InstanceOfPredicate implements Predicate<Object> {
        private final Class<?> clazz;

        private InstanceOfPredicate(Class<?> clazz) {
            checkArgNotNull(clazz, "clazz");
            this.clazz = clazz;
        }

        public boolean apply(Object o) {
            return clazz.isInstance(o);
        }

        @Override
        public String toString() {
            return "IsInstanceOf(" + clazz.getName() + ")";
        }
    }

    private static class IsNullPredicate implements Predicate<Object> {
        private static final Predicate<Object> INSTANCE = new IsNullPredicate();

        public boolean apply(Object o) {
            return o == null;
        }

        @Override
        public String toString() {
            return "IsNull";
        }
    }

    private static class NotNullPredicate implements Predicate<Object> {
        private static final Predicate<Object> INSTANCE = new NotNullPredicate();

        public boolean apply(Object o) {
            return o != null;
        }

        @Override
        public String toString() {
            return "NotNull";
        }
    }

    private static class InPredicate<T> implements Predicate<T> {
        private final Collection<?> target;

        private InPredicate(Collection<?> target) {
            checkArgNotNull(target, "target");
            this.target = target;
        }

        public boolean apply(T t) {
            try {
                return target.contains(t);
            } catch (NullPointerException e) {
                return false;
            } catch (ClassCastException e) {
                return false;
            }
        }

        @Override
        public String toString() {
            return "In(" + target + ")";
        }
    }

}
