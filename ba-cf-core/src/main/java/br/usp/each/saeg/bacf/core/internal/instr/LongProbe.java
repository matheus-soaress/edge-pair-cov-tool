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

public final class LongProbe extends Probe {

    public LongProbe(final MethodNode methodNode, final int window, final boolean edgeCoverage, final boolean edgePairCoverage) {
        super(methodNode, window, edgeCoverage, edgePairCoverage);
    }

    @Override
    public int getType() {
        return BA_LONG_PROBE;
    }

    @Override
    public void accept(final MethodVisitor mv) {

        if(edgePairCoverage) {
            mv.visitLdcInsn(currentActiveElement);
            mv.visitVarInsn(Opcodes.LLOAD, vParentActiveElement);
            mv.visitInsn(Opcodes.LAND);
            mv.visitVarInsn(Opcodes.LLOAD, vGrandparentActiveElement);
            mv.visitInsn(Opcodes.LAND);
            mv.visitVarInsn(Opcodes.LLOAD, vCoveredElement);
            mv.visitInsn(Opcodes.LOR);
            mv.visitVarInsn(Opcodes.LSTORE, vCoveredElement);

            mv.visitVarInsn(Opcodes.LLOAD, vParentActiveElement);
            mv.visitVarInsn(Opcodes.LSTORE, vGrandparentActiveElement);

            mv.visitLdcInsn(currentActiveElement);
            mv.visitVarInsn(Opcodes.LSTORE, vParentActiveElement);
        } else if(edgeCoverage) {
            mv.visitLdcInsn(currentActiveElement);
            mv.visitVarInsn(Opcodes.LLOAD, vParentActiveElement);
            mv.visitInsn(Opcodes.LAND);
            mv.visitVarInsn(Opcodes.LLOAD, vCoveredElement);
            mv.visitInsn(Opcodes.LOR);
            mv.visitVarInsn(Opcodes.LSTORE, vCoveredElement);


            mv.visitLdcInsn(currentActiveElement);
            mv.visitVarInsn(Opcodes.LSTORE, vParentActiveElement);
        } else {
            // atualiza nos cobertos
            mv.visitLdcInsn(currentActiveElement);
            mv.visitVarInsn(Opcodes.LLOAD, vCoveredElement);
            mv.visitInsn(Opcodes.LOR);
            mv.visitVarInsn(Opcodes.LSTORE, vCoveredElement);
        }
    }

}
