package de.sebastianruziczka.cobolunit.coverage.model

class CobolCoverageMethod {

	private String name
	private int startLine
	private int endLine
	private List<CoverageStatus> lines = new LinkedList<>()

	public CobolCoverageMethod(String name, int startLine) {
		println 'init new method '+name
		this.name = name
		this.startLine = startLine
	}

	public void setEnd(int endLine) {
		this.endLine = endLine
	}

	public void addComment(int line) {
		this.addLineStatus(line, CoverageStatus.comment)
	}

	public void addEmptyLine(int line) {
		this.addLineStatus(line, CoverageStatus.empty)
	}

	public void addFollowLine(int line) {
		this.addLineStatus(line, CoverageStatus.follow_line)
	}

	private void addLineStatus(int line, CoverageStatus status) {
		int relativeLine = line - this.startLine
		while(this.lines.size()< relativeLine) {
			this.lines.add(CoverageStatus.not_passed)
		}
		this.lines.add(status)
	}

	@Override
	public String toString() {
		return 'CobolCoverageMethod(' + this.name + ', start: ' + this.startLine + ', end: ' + this.endLine + '){\n\t' + this.lines.join('\n\t') + '\n}'
	}
}
