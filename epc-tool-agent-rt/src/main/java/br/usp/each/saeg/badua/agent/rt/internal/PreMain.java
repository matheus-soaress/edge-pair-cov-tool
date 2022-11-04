/**
 * Copyright (c) 2022, 2022 University of Sao Paulo and Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matheus Soares - initial API and implementation and/or initial documentation
 */
package br.usp.each.saeg.badua.agent.rt.internal;

import br.usp.each.saeg.badua.core.runtime.IRuntime;
import br.usp.each.saeg.badua.core.runtime.ModifiedSystemClassRuntime;

import java.lang.instrument.Instrumentation;
import java.security.CodeSource;
import java.util.jar.JarFile;

public final class PreMain {

    private PreMain() {
        // No instances
    }

    public static void premain(final String opts, final Instrumentation inst) throws Exception {
        final CodeSource codeSource = PreMain.class.getProtectionDomain().getCodeSource();
        inst.appendToBootstrapClassLoaderSearch(new JarFile(codeSource.getLocation().getPath()));

        Init.init(inst);
    }

    public static class Init {
        public static void init(final Instrumentation inst) throws Exception {
            final IRuntime runtime = ModifiedSystemClassRuntime.createFor(inst, "java/lang/UnknownError");
            runtime.startup(Agent.getInstance().getData());
            inst.addTransformer(new CoverageTransformer(runtime, PreMain.class.getPackage().getName(), false, false));
        }
    }

}