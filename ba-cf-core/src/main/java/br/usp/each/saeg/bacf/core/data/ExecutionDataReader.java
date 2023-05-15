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
package br.usp.each.saeg.bacf.core.data;

import br.usp.each.saeg.bacf.core.internal.data.CompactDataInput;

import java.io.IOException;
import java.io.InputStream;

public class ExecutionDataReader {

    private static final int EOF = -1;

    private final CompactDataInput in;

    private IExecutionDataVisitor executionDataVisitor;

    private boolean firstBlock = true;

    public ExecutionDataReader(final InputStream input) {
        in = new CompactDataInput(input);
    }

    public void setExecutionDataVisitor(final IExecutionDataVisitor visitor) {
        executionDataVisitor = visitor;
    }

    public void read() throws IOException {
        int b = in.read();
        while (EOF != b) {
            final byte type = (byte) b;
            if (firstBlock) {
                assertValue(ExecutionDataWriter.BLOCK_HEADER, type);
            }
            readBlock(type);
            b = in.read();
        }
    }

    private void readBlock(final byte type) throws IOException {
        switch (type) {
        case ExecutionDataWriter.BLOCK_HEADER:
            readHeader();
            break;
        case ExecutionDataWriter.BLOCK_EXECUTIONDATA:
            readExecutionData();
            break;
        default:
            throw new IOException(String.format("Unknown block type 0x%x.", type));
        }
    }

    private void readHeader() throws IOException {
        assertValue(ExecutionDataWriter.MAGIC_NUMBER, in.readChar());
        assertValue(ExecutionDataWriter.FORMAT_VERSION, in.readChar());
        firstBlock = false;
    }

    private void readExecutionData() throws IOException {
        if (executionDataVisitor == null) {
            throw new IOException("No execution data visitor.");
        }
        final long id = in.readLong();
        final String name = in.readUTF();
        final long[] data = in.readLongArray();
        executionDataVisitor.visitClassExecution(new ExecutionData(id, name, data));
    }

    private void assertValue(final byte expected, final byte actual) throws IOException {
        if (expected != actual) {
            throw new IOException("Invalid execution data file.");
        }
    }

    private void assertValue(final char expected, final char actual) throws IOException {
        if (expected != actual) {
            throw new IOException("Invalid execution data file.");
        }
    }

}
