package de.sebastianruziczka.cobolunit.coverage.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sebastianruziczka.cobolunit.coverage.CobolTraceMode;

public class CobolCoverageFile {

	private static final Logger LOGGER = LoggerFactory.getLogger(CobolCoverageFile.class.getName());

	private String name;
	private List<CobolCoverageMethod> methods = new ArrayList<>();
	private int offsetDifference = -1;
	private CobolTraceMode traceMode = null;

	public CobolCoverageFile(String name) {
		this.name = name;
	}

	public void addMethod(CobolCoverageMethod method) {
		this.methods.add(method);
	}

	public List<CobolCoverageMethod> methods() {
		return this.methods;
	}

	public String name() {
		return this.name;
	}

	@Override
	public String toString() {
		return "CobolCoverageFile(" + this.name + ") { \n\t"
				+ this.methods.stream().map(Object::toString).reduce((y, x) -> x + ",\n\t" + y).orElse("") + "\n}";
	}

	public void feedLine(String line, String nextLine) {

		int tracedLineNumber = getTraceNumberFrom(line, nextLine);
		Optional<CobolCoverageMethod> actualMethod = null;
		if (offsetDifference == -1 && this.traceMode.isParagraph(line)) {
			actualMethod = getMethodForName(this.traceMode.parseParagraphName(line));
			if (actualMethod.isPresent()) {
				offsetDifference = tracedLineNumber - actualMethod.get().startLine() + 1;
				if (this.traceMode == CobolTraceMode.gnucobol1) {// Fix if first statement is not in first line
					offsetDifference = offsetDifference - actualMethod.get().firstStatement();
				}
			}
			return;
		}

		LOGGER.debug(tracedLineNumber + "");
		LOGGER.debug("Offset " + offsetDifference);
		LOGGER.debug("traced: " + (tracedLineNumber - offsetDifference));

		if (offsetDifference == -1 || this.traceMode.isParagraph(line)) {
			return;
		}
		actualMethod = getMethodForLineNumber(tracedLineNumber - offsetDifference);

		if (!actualMethod.isPresent()) {
			return;
		}
		actualMethod.get()
				.setLineCoveredWithRelativeIndex(tracedLineNumber - offsetDifference - actualMethod.get().startLine());

	}

	private Optional<CobolCoverageMethod> getMethodForLineNumber(int lineNumber) {
		for (CobolCoverageMethod method : this.methods()) {
			if (lineNumber >= method.startLine() && lineNumber <= method.endLine()) {
				return Optional.of(method);
			}
		}
		return Optional.empty();
	}

	private int getTraceNumberFrom(String line, String followingLine) {
		if (this.traceMode == null) {
			this.traceMode = CobolTraceMode.getTraceModeFor(line);
			LOGGER.info("INIT Tracemode: " + this.traceMode);
		}
		return this.traceMode.getLineNumberFor(line, followingLine);
	}

	private Optional<CobolCoverageMethod> getMethodForName(String methodName) {
		for (CobolCoverageMethod method : this.methods()) {
			if (method.name().equals(methodName)) {
				return Optional.of(method);
			}
		}
		return Optional.empty();
	}

}