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
package br.usp.each.saeg.bacf.cli;

import br.usp.each.saeg.bacf.agent.rt.internal.Offline;
import br.usp.each.saeg.bacf.core.instr.Instrumenter;
import br.usp.each.saeg.bacf.core.runtime.StaticAccessGenerator;
import br.usp.each.saeg.commons.io.Files;
import br.usp.each.saeg.commons.time.TimeWatch;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.*;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Instrument {

    private final File src;

    private final File dest;

    private final boolean edges;

    private final boolean edgePairs;

    private final Instrumenter instrumenter;

    public Instrument(final InstrumentOptions options) {
        this.src = options.getSource();
        this.dest = options.getDestination();
        this.edges = options.edges();
        this.edgePairs = options.edgePairs();
        instrumenter = new Instrumenter(new StaticAccessGenerator(Offline.class.getName()));
    }

    public int instrument() throws IOException {

        if (src.getAbsoluteFile().equals(dest.getAbsoluteFile())) {
            throw new IOException("'src' and 'dest' can't be the same folder");
        }

        if (src.isFile()) {
            return instrument(src, new File(dest, src.getName()));
        }

        final List<File> files = Files.listRecursive(src, new FilenameFilter() {

            @Override
            public boolean accept(final File dir, final String name) {
                return new File(dir, name).isFile();
            }

        });

        int n = 0;
        for (final File file : files) {
            n += instrument(file, new File(dest, relativize(src, file)));
        }
        return n;
    }

    private int instrument(final File src, final File dest) throws IOException {
        final File destParent = dest.getParentFile();
        if (!destParent.mkdirs() && !destParent.exists()) {
            throw new IOException("failed to create directory: " + destParent);
        }
        final InputStream input = new FileInputStream(src);
        try {
            final OutputStream output = new FileOutputStream(dest);
            try {
                return instrumenter.instrumentAll(input, output, src.getPath(), edges, edgePairs);
            } finally {
                output.close();
            }
        } catch (final IOException e) {
            dest.delete();
            throw e;
        } finally {
            input.close();
        }
    }

    private String relativize(final File a, final File b) {
        return a.toURI().relativize(b.toURI()).getPath();
    }

    public static void main(final String[] args) {
        final InstrumentOptions options = new InstrumentOptions();
        final CmdLineParser parser = new CmdLineParser(options);

        try {
            parser.parseArgument(args);
        } catch (final CmdLineException e) {
            System.err.println(e.getLocalizedMessage());
            parser.printUsage(System.err);
            System.exit(1);
        }

        try {
            final TimeWatch tw = TimeWatch.start();
            final int total = new Instrument(options).instrument();
            final long seconds = tw.time(TimeUnit.SECONDS);

            System.out.println(MessageFormat.format(
                    "{0} classes instrumented in {1} seconds", total, seconds));

        } catch (final IOException e) {
            System.err.println("Failed: " + e.getLocalizedMessage());
            System.err.println("Cause Exception: " + e.getCause());
            e.printStackTrace();
            System.exit(1);
        }
    }

}