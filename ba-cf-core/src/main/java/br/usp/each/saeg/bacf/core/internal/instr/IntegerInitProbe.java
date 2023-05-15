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
package br.usp.each.saeg.bacf.core.internal.instr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

public final class IntegerInitProbe extends Probe {

    public IntegerInitProbe(final MethodNode methodNode, final boolean edgeCoverage,
                            final boolean edgePairCoverage) {
        super(methodNode, edgeCoverage, edgePairCoverage);
    }

    @Override
    public int getType() {
        return BA_INT_INIT_PROBE;
    }

    @Override
    public void accept(final MethodVisitor mv) {

        if (edgePairCoverage) {
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitVarInsn(Opcodes.ISTORE, vGrandparentActiveElement);
        }
        if (edgeCoverage || edgePairCoverage) {
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitVarInsn(Opcodes.ISTORE, vParentActiveElement);
        }
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitVarInsn(Opcodes.ISTORE, vCoveredElement);
    }

}