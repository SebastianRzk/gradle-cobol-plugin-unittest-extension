package de.sebastianruziczka.cobolunit.coverage.report

import de.sebastianruziczka.cobolunit.coverage.model.CobolCoverageFile
import de.sebastianruziczka.cobolunit.coverage.model.CobolCoverageLine
import de.sebastianruziczka.cobolunit.coverage.model.CobolCoverageMethod
import de.sebastianruziczka.cobolunit.coverage.model.CoverageStatus

class LineRateComputer {


	public double compute(List<CobolCoverageFile> classPackage) {
		HitsMisses result = count(classPackage)
		return computeResult(result)
	}

	public double compute(CobolCoverageFile file) {
		return computeResult(count(file))
	}

	private double computeResult(HitsMisses result) {
		println result.hits + '////' + result.misses
		if (result.hits == 0) {
			return 0.0
		}
		return (result.hits * 1.0)/ (result.hits + result.misses)
	}

	public HitsMisses count (List<CobolCoverageFile> files) {
		HitsMisses result = new HitsMisses()
		for ( CobolCoverageFile file : files) {
			result = result.plus(count(file))
		}
		return result
	}


	public HitsMisses count (CobolCoverageFile file) {
		HitsMisses result = new HitsMisses()
		for (CobolCoverageMethod method : file.methods()) {
			for (CobolCoverageLine line : method.methodStatus()) {
				if (line.status() == CoverageStatus.passed) {
					result.hits ++
				}else {
					result.misses ++
				}
			}
		}
		return result
	}
}
class HitsMisses{
	int hits = 0
	int misses = 0

	public HitsMisses plus( HitsMisses y ) {
		HitsMisses result = new HitsMisses()
		result.hits = this.hits + y.hits
		result.misses = this.misses + y.misses
		return result
	}
}

