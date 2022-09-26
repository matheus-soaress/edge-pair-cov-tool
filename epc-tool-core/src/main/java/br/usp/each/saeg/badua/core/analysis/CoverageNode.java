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
package br.usp.each.saeg.badua.core.analysis;

import static org.jacoco.core.internal.analysis.CounterImpl.COUNTER_0_0;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.internal.analysis.CounterImpl;

public class CoverageNode {

    private final String name;

    protected CounterImpl counter;

    protected CounterImpl methodCounter;

    protected CounterImpl classCounter;

    protected CounterImpl lineCounter;

    public CoverageNode(final String name) {
        this.name = name;
        this.counter = COUNTER_0_0;
        this.methodCounter = COUNTER_0_0;
        this.classCounter = COUNTER_0_0;
        this.lineCounter = COUNTER_0_0;
    }

    public String getName() {
        return name;
    }

    public ICounter getCounter() {
        return counter;
    }

    public ICounter getMethodCounter() {
        return methodCounter;
    }

    public ICounter getClassCounter() {
        return classCounter;
    }

    public CounterImpl getLineCounter() {
        return lineCounter;
    }

    public void increment(final CoverageNode child) {
        counter = counter.increment(child.getCounter());
        methodCounter = methodCounter.increment(child.getMethodCounter());
        classCounter = classCounter.increment(child.getClassCounter());
        lineCounter = lineCounter.increment(child.getLineCounter());
    }

}
