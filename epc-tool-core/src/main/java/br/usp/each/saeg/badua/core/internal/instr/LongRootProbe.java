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

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

public final class LongRootProbe extends Probe {

    public LongRootProbe(final MethodNode methodNode, final int window, final boolean edgeCoverage) {
        super(methodNode, window, edgeCoverage);
    }

    @Override
    public int getType() {
        return BA_LONG_ROOT_PROBE;
    }

    @Override
    public void accept(final MethodVisitor mv) {
        if(edgeCoverage) {
            mv.visitLdcInsn(currentCoveredElem);
            mv.visitVarInsn(Opcodes.LLOAD, vPotCoveredElement);
            mv.visitInsn(Opcodes.LAND);
            mv.visitVarInsn(Opcodes.LLOAD, vCoveredElement);
            mv.visitInsn(Opcodes.LOR);
            mv.visitVarInsn(Opcodes.LSTORE, vCoveredElement);


            mv.visitLdcInsn(currentCoveredElem);
            mv.visitVarInsn(Opcodes.LSTORE, vPotCoveredElement);
        } else {
            // atualiza nos cobertos
            mv.visitLdcInsn(currentCoveredElem);
            mv.visitVarInsn(Opcodes.LLOAD, vCoveredElement);
            mv.visitInsn(Opcodes.LOR);
            mv.visitVarInsn(Opcodes.LSTORE, vCoveredElement);
        }
    }

}
