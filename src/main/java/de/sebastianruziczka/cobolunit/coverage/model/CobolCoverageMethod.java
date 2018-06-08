package de.sebastianruziczka.cobolunit.coverage.model;

import java.util.LinkedList;
import java.util.List;

public class CobolCoverageMethod {

	private String name;
	private int startLine;
	private int endLine;
	private List<CoverageStatus> lines = new LinkedList<>();

	public CobolCoverageMethod(String name, int startLine) {
		this.name = name;
		this.startLine = startLine;
	}

	public void setEnd(int endLine) {
		this.endLine = endLine;
	}

	public void addComment(int line) {
		this.addLineStatus(line, CoverageStatus.comment);
	}

	public void addEmptyLine(int line) {
		this.addLineStatus(line, CoverageStatus.empty);
	}

	public void addFollowLine(int line) {
		this.addLineStatus(line, CoverageStatus.follow_line);
	}

	public void setLineCoveredWithRelativeIndex(int line) {
		this.addLineStatus(line + this.startLine, CoverageStatus.passed);
	}

	private void addLineStatus(int line, CoverageStatus status) {
		int relativeLine = line - this.startLine;

		if (relativeLine < this.lines.size()) {
			this.lines.set(relativeLine, status);
			return;
		}

		while (this.lines.size() < relativeLine) {
			this.lines.add(CoverageStatus.not_passed);
		}

		this.lines.add(status);
	}

	public List<CobolCoverageLine> methodStatus() {
		List<CobolCoverageLine> result = new LinkedList<>();
		CoverageStatus lastStatus = null;
		for (int i = this.startLine; i <= this.endLine; i++) {
			int relativeIndex = i - startLine;

			if (relativeIndex >= this.lines.size()) {
				result.add(new CobolCoverageLine(i, CoverageStatus.not_passed));
				continue;
			}

			CoverageStatus actualStatus = this.lines.get(relativeIndex);
			if (actualStatus == CoverageStatus.passed) {
				lastStatus = actualStatus;
				result.add(new CobolCoverageLine(i, actualStatus));
				continue;
			} else if (actualStatus == CoverageStatus.not_passed) {
				lastStatus = actualStatus;
				result.add(new CobolCoverageLine(i, actualStatus));
				continue;
			} else if (actualStatus == CoverageStatus.follow_line) {
				result.add(new CobolCoverageLine(i, lastStatus));
				continue;
			}
		}

		return result;
	}

	@Override
	public String toString() {
		return "CobolCoverageMethod(" + this.name + ", start: " + this.startLine + ", end: " + this.endLine + "){\n\t"
				+ this.lines.stream().map(Object::toString).reduce((x, y) -> x + "\n\t" + y).orElse("") + "\n}";
	}

	public String name() {
		return this.name;
	}

	public int startLine() {
		return this.startLine;
	}

	public int endLine() {
		return this.endLine;
	}

	public int firstStatement() {
		for (int i = 0; i < this.lines.size(); i++) {
			CoverageStatus status = this.lines.get(i);
			if (status != CoverageStatus.comment && status != CoverageStatus.empty) {
				return i;
			}
		}
		return this.lines.size();
	}
}
