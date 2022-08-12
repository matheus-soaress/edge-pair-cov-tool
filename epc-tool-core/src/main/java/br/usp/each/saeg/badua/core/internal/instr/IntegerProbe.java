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

public final class IntegerProbe extends Probe {

    public IntegerProbe(final MethodNode methodNode, final boolean edgeCoverage) {
        super(methodNode, edgeCoverage);
    }

    @Override
    public int getType() {
        return BA_INT_PROBE;
    }

    @Override
    public void accept(final MethodVisitor mv) {

        // atualiza nos cobertos
        if(edgeCoverage) {
            InstrSupport.push(mv, (int) currentCoveredElem);
            mv.visitVarInsn(Opcodes.ILOAD, vPotCoveredElement);
            mv.visitInsn(Opcodes.IAND);
            mv.visitVarInsn(Opcodes.ILOAD, vCoveredElement);
            mv.visitInsn(Opcodes.IOR);
            mv.visitVarInsn(Opcodes.ISTORE, vCoveredElement);


            InstrSupport.push(mv, (int) currentCoveredElem);
            mv.visitVarInsn(Opcodes.ISTORE, vPotCoveredElement);
        } else {
            InstrSupport.push(mv, (int) currentCoveredElem);
            mv.visitVarInsn(Opcodes.ILOAD, vCoveredElement);
            mv.visitInsn(Opcodes.IOR);
            mv.visitVarInsn(Opcodes.ISTORE, vCoveredElement);
        }

    }

}
