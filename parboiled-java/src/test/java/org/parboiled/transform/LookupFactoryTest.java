/*
 * Copyright (C) 2026 parboiled contributors
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

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import static org.objectweb.asm.Opcodes.*;
import static org.testng.Assert.*;

public class LookupFactoryTest {

    @DataProvider
    public Object[][] lookupSources() {
        return new Object[][] {{false}, {true}};
    }

    @Test(dataProvider = "lookupSources")
    public void cachedLookupDoesNotRetainClassLoader(boolean registered) throws Exception {
        LookupFactory factory = new LookupFactory();
        ReferenceQueue<ClassLoader> collected = new ReferenceQueue<>();
        WeakReference<ClassLoader> loader = cacheDisposableLookup(factory, registered, collected);

        System.gc();
        assertSame(collected.remove(5000), loader, "The cached lookup must not retain its class loader");
        assertNull(loader.get());
        Reference.reachabilityFence(factory);
    }

    private static WeakReference<ClassLoader> cacheDisposableLookup(LookupFactory factory,
            boolean registered, ReferenceQueue<ClassLoader> collected) throws Exception {
        Class<?> host = new HostLoader().host();
        Lookup lookup = (Lookup) host.getMethod("lookup").invoke(null);
        if (registered) {
            factory.registerLookup(host, lookup);
        } else {
            lookup = factory.lookupFor(host);
        }
        assertNotNull(lookup);
        assertSame(factory.lookupFor(host), lookup);
        return new WeakReference<>(host.getClassLoader(), collected);
    }

    @Test
    public void registeredLookupRemainsAvailableWhileHostIsAlive() throws Exception {
        LookupFactory factory = new LookupFactory();
        Class<?> host = new HostLoader().host();
        WeakReference<Lookup> registered = registerLookup(factory, host);

        System.gc();
        assertNotNull(registered.get(), "A live host must retain an explicitly registered lookup");
        assertSame(factory.lookupFor(host), registered.get());
        Reference.reachabilityFence(host);
    }

    private static WeakReference<Lookup> registerLookup(LookupFactory factory, Class<?> host)
            throws Exception {
        Lookup lookup = (Lookup) host.getMethod("lookup").invoke(null);
        factory.registerLookup(host, lookup);
        return new WeakReference<>(lookup);
    }

    @Test
    public void registrationReplacesCachedLookup() throws Exception {
        LookupFactory factory = new LookupFactory();
        Class<?> host = new HostLoader().host();
        Lookup initial = factory.lookupFor(host);
        Lookup replacement = (Lookup) host.getMethod("lookup").invoke(null);
        assertNotSame(initial, replacement);

        factory.registerLookup(host, replacement);

        assertSame(factory.lookupFor(host), replacement);
    }

    private static class HostLoader extends ClassLoader {
        HostLoader() {
            super(null);
        }

        Class<?> host() {
            ClassWriter writer = new ClassWriter(0);
            writer.visit(V11, ACC_PUBLIC, "DisposableLookupHost", null, "java/lang/Object", null);
            MethodVisitor method = writer.visitMethod(ACC_PUBLIC | ACC_STATIC, "lookup",
                    "()Ljava/lang/invoke/MethodHandles$Lookup;", null, null);
            method.visitCode();
            method.visitMethodInsn(INVOKESTATIC, "java/lang/invoke/MethodHandles", "lookup",
                    "()Ljava/lang/invoke/MethodHandles$Lookup;", false);
            method.visitInsn(ARETURN);
            method.visitMaxs(1, 0);
            method.visitEnd();
            writer.visitEnd();
            byte[] bytes = writer.toByteArray();
            return defineClass(null, bytes, 0, bytes.length);
        }
    }
}
