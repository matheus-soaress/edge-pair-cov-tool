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
package br.usp.each.saeg.badua.core.internal.instr;

import java.util.Map;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

public abstract class Probe extends AbstractInsnNode {

    // -- constants

    public static final int BA_INT_PROBE = -1;

    public static final int BA_INT_INIT_PROBE = -2;

    public static final int BA_INT_UPDATE_PROBE = -3;

    public static final int BA_INT_ROOT_PROBE = -4;

    public static final int BA_LONG_PROBE = -5;

    public static final int BA_LONG_INIT_PROBE = -6;

    public static final int BA_LONG_UPDATE_PROBE = -7;

    public static final int BA_LONG_ROOT_PROBE = -8;

    public final int vCoveredElement;

    public final int vParentActiveElement;

    public final int vGrandparentActiveElement;

    public final boolean edgeCoverage;

    public final boolean edgePairCoverage;

    protected long currentActiveElement;

    // Used by integer probes
    protected Probe(final MethodNode methodNode, final boolean edgeCoverage, final boolean edgePairCoverage) {
        super(-1);

        vCoveredElement = methodNode.maxLocals;
        if (edgePairCoverage) {
            vParentActiveElement = methodNode.maxLocals + 1;
            vGrandparentActiveElement = methodNode.maxLocals + 2;
        } else if (edgeCoverage) {
            vParentActiveElement = methodNode.maxLocals + 1;
            vGrandparentActiveElement = 0;
        } else {
            vParentActiveElement = 0;
            vGrandparentActiveElement = 0;
        }
        this.edgeCoverage = edgeCoverage;
        this.edgePairCoverage = edgePairCoverage;
    }

    // used by long probes
    protected Probe(final MethodNode methodNode, final int window, final boolean edgeCoverage,
                    final boolean edgePairCoverage) {
        super(-1);

        if (edgePairCoverage) {
            vCoveredElement = methodNode.maxLocals + 6 * window;
            vParentActiveElement = methodNode.maxLocals + 6 * window + 2;
            vGrandparentActiveElement = methodNode.maxLocals + 6 * window + 4;
        } else if (edgeCoverage) {
            vCoveredElement = methodNode.maxLocals + 4 * window;
            vParentActiveElement = methodNode.maxLocals + 4 * window + 2;
            vGrandparentActiveElement = 0;
        } else {
            vCoveredElement = methodNode.maxLocals + 2 * window;
            vParentActiveElement = 0;
            vGrandparentActiveElement = 0;
        }
        this.edgeCoverage = edgeCoverage;
        this.edgePairCoverage = edgePairCoverage;
    }

    @Override
    public final AbstractInsnNode clone(final Map<LabelNode, LabelNode> labels) {
        throw new UnsupportedOperationException();
    }

}