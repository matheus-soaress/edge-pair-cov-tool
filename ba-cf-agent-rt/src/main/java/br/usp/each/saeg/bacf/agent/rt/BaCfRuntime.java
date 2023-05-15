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
package br.usp.each.saeg.bacf.agent.rt;

import br.usp.each.saeg.bacf.agent.rt.internal.Agent;

/**
 * Entry point (API) to access the BA-CF agent runtime.
 */
public final class BaCfRuntime {

    private BaCfRuntime() {
        // no instances
    }

    /**
     * Returns the agent instance of the BA-CF runtime in this JVM.
     *
     * @return agent instance
     */
    public static IAgent getAgent() {
        return Agent.getInstance();
    }

}
