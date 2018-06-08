package de.sebastianruziczka.cobolunit.coverage;

import java.util.List;

import de.sebastianruziczka.cobolunit.coverage.model.CobolCoverageFile;
import de.sebastianruziczka.cobolunit.coverage.model.CobolCoverageMethod;

class TestCoverageMerger {
	private CobolTraceMode traceMode = null;

	public CobolCoverageFile merge(CobolCoverageFile file, List<String> coverageOuptut) {

		CobolCoverageMethod actualMethod = null;
		int offsetDifference = -1;
		for (int i = 0; i < coverageOuptut.size() - 1; i++) {
			String line = coverageOuptut.get(i);

			int tracedLineNumber = getTraceNumberFrom(line, coverageOuptut.get(i + 1));

			if (offsetDifference == -1 && this.traceMode.isParagraph(line)) {
				actualMethod = getMethodForName(this.traceMode.parseParagraphName(line), file);
				if (actualMethod != null) {
					offsetDifference = tracedLineNumber - actualMethod.startLine() + 1;
				}
				continue;
			}

			if (offsetDifference == -1 || this.traceMode.isParagraph(line)) {
				continue;
			}
			actualMethod = getMethodForLineNumber(tracedLineNumber - offsetDifference, file);

			if (actualMethod == null) {
				continue;
			}
			actualMethod
					.setLineCoveredWithRelativeIndex(tracedLineNumber - offsetDifference - actualMethod.startLine());
		}

		return file;

	}

	private CobolCoverageMethod getMethodForLineNumber(int lineNumber, CobolCoverageFile file) {
		for (CobolCoverageMethod method : file.methods()) {
			if (lineNumber >= method.startLine() && lineNumber <= method.endLine()) {
				return method;
			}
		}
		return null;
	}

	private int getTraceNumberFrom(String line, String followingLine) {
		if (this.traceMode == null) {
			this.traceMode = CobolTraceMode.getTraceModeFor(line);
		}
		return this.traceMode.getLineNumberFor(line, followingLine);
	}

	private CobolCoverageMethod getMethodForName(String methodName, CobolCoverageFile file) {
		for (CobolCoverageMethod method : file.methods()) {
			if (method.name().equals(methodName)) {
				return method;
			}
		}
		return null;
	}
}
