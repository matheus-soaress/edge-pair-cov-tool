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

    // -- final fields

    /*
    public final int vCovered;
    public final int vAlive;
    public final int vSleepy;
     */

    // begin matheus
    public final int vCoveredNos;
    public final int vCoveredEdges;
    // end matheus

    // -- fields

    //protected boolean singlePredecessor;
    /*
    protected long potcov;
    protected long potcovpuse;
    protected long born;
    protected long disabled;
    protected long sleepy;
    */

    // begin matheus
    protected long noAtualCoberto;
    protected long arestaAtualCoberta;
    // end matheus

    // Used by integer probes
    protected Probe(final MethodNode methodNode) {
        super(-1);
        /*
        vCovered = methodNode.maxLocals;
        vAlive = methodNode.maxLocals + 1;
        vSleepy = methodNode.maxLocals + 2;
         */
        /* begin matheus */
        vCoveredNos = methodNode.maxLocals;
        vCoveredEdges = methodNode.maxLocals + 1;
        /* end matheus */
    }

    // used by long probes
    protected Probe(final MethodNode methodNode, final int window) {
        super(-1);
        /*
        vCovered = methodNode.maxLocals + 6 * window;
        vAlive = methodNode.maxLocals + 6 * window + 2;
        vSleepy = methodNode.maxLocals + 6 * window + 4;
         */

        /* begin matheus */
        vCoveredNos = methodNode.maxLocals + 4 * window;
        vCoveredEdges = methodNode.maxLocals + 4 * window + 2;
        /* end matheus */
    }

    @Override
    public final AbstractInsnNode clone(final Map<LabelNode, LabelNode> labels) {
        throw new UnsupportedOperationException();
    }

}
