package br.usp.each.saeg.badua.core.analysis;

public class SourceCodeLine {

    public final int line;
    public final boolean covered;

    public SourceCodeLine(int line, boolean covered) {
        this.line = line;
        this.covered = covered;
    }
}
