/*
 * Copyright (C) 2022 parboiled contributors
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

package org.parboiled.transform;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.WeakHashMap;

/**
 * Helper that can be used to create {@link Lookup} instances for
 * specific classes.
 */
final class LookupFactory {

	private WeakHashMap<Class<?>, Lookup> lookups = new WeakHashMap<>();
	private Lookup trustedLookup;

	LookupFactory() {
		loadTrustedLookup();
	}

	/**
	 * Tries to load a trusted {@link Lookup} instance.
	 *
	 * <p>
	 * Adapted from <a href="https://github.com/google/guice/blob/cf759d44c78e8490e3d54df6a27918e0811bbdf9/core/src/com/google/inject/internal/aop/HiddenClassDefiner.java#L40">HiddenClassDefiner</a>
	 * of Google Guice.
	 * </p>
	 */
	private void loadTrustedLookup() {
		try {
			Class<?> unsafeType = Class.forName("sun.misc.Unsafe");
			Field theUnsafeField = unsafeType.getDeclaredField("theUnsafe");
			theUnsafeField.setAccessible(true);
			Object unsafeInstance = theUnsafeField.get(null);
			Field trustedLookupField = Lookup.class.getDeclaredField("IMPL_LOOKUP");
			Method baseMethod = unsafeType.getMethod("staticFieldBase", Field.class);
			Object trustedLookupBase = baseMethod.invoke(unsafeInstance, trustedLookupField);
			Method offsetMethod = unsafeType.getMethod("staticFieldOffset", Field.class);
			Object trustedLookupOffset = offsetMethod.invoke(unsafeInstance, trustedLookupField);
			Method getObjectMethod = unsafeType.getMethod("getObject", Object.class, long.class);
			this.trustedLookup =
					(Lookup) getObjectMethod.invoke(unsafeInstance, trustedLookupBase, trustedLookupOffset);
		} catch (Exception e) {
			// Unsafe and trusted lookup is not available
		}
	}

	/**
	 * Determines a {@link Lookup} instance for the given hostClass.
	 * <p>
	 * The method first tries to use a static method of the hostClass with the
	 * following signature:
	 * </p>
	 * <p>
	 * <code>
	 *     public static {@link Lookup} lookup();
	 * </code>
	 * </p>
	 * <p>
	 * If this fails then it tries to use a trusted lookup
	 * instance created via sun.misc.Unsafe.
	 * </p>
	 *
	 * @param hostClass The target class of the lookup instance
	 * @return a lookup instance or <code>null</code> if not found
	 */
	Lookup lookupFor(Class<?> hostClass) {
		Lookup lookup = lookups.get(hostClass);
		if (lookup == null) {
			try {
				// try to find a lookup() method first
				Method lookupMethod = hostClass.getMethod("lookup");
				lookup = (Lookup) lookupMethod.invoke(null);
			} catch (Exception e) {
				// failed to use lookup() method
			}

			if (lookup == null && trustedLookup != null) {
				// use trusted lookup instance if available
				lookup = trustedLookup.in(hostClass);
			}

			if (lookup != null) {
				lookups.put(hostClass, lookup);
			}
		}
		return lookup;
	}
}