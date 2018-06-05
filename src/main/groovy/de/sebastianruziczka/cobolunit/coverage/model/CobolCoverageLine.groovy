package de.sebastianruziczka.cobolunit.coverage.model

class CobolCoverageLine {
	private int lineNumber
	private CoverageStatus status

	public CobolCoverageLine(int lineNumber, CoverageStatus status){
		this.lineNumber = lineNumber
		this.status = status
	}

	public int lineNumber() {
		return this.lineNumber
	}

	public CoverageStatus status() {
		return this.status
	}

	@Override
	public String toString() {
		return 'CobolCoverageLine[' + this.lineNumber + ', ' + this.status + ']'
	}
}
