package de.sebastianruziczka.cobolunit.coverage.report

import de.sebastianruziczka.CobolExtension
import de.sebastianruziczka.cobolunit.coverage.model.CobolCoverageFile
import de.sebastianruziczka.cobolunit.coverage.model.CobolCoverageLine
import de.sebastianruziczka.cobolunit.coverage.model.CobolCoverageMethod
import de.sebastianruziczka.cobolunit.coverage.model.CoverageStatus
import de.sebastianruziczka.metainf.MetaInfPropertyResolver
import groovy.xml.MarkupBuilder

class XMLReportWriter {
	private CobolExtension configuration

	public XMLReportWriter(CobolExtension configuration) {
		this.configuration = configuration
	}

	public String writeToXML(List<CobolCoverageFile> coveredFiles) {
		/**
		 * <coverage branch-rate="0" 
		 * branches-covered="0" 
		 * branches-valid="0" 
		 * complexity="0" 
		 * line-rate="0.8571" 
		 * lines-covered="6" 
		 * lines-valid="7" 
		 * timestamp="1528354432530" 
		 * version="4.5.1">
		 */

		LineRateComputer statistiker = new LineRateComputer()
		double lineRate = statistiker.compute(coveredFiles)
		HitsMisses result = statistiker.count(coveredFiles)
		int linesCovered = result.hits
		int linesValid = result.hits + result.misses

		def xmlWriter = new StringWriter()
		def xmlMarkup = new MarkupBuilder(xmlWriter)

		Map<String, List<CobolCoverageFile>> cobolPackages = this.resolvePackages(coveredFiles)
		xmlMarkup.
				'coverage'('branch-rate':'0',
				'branch-covered':'0',
				'branch-valid':'0',
				'complexity':'0',
				'line-rate': lineRate + '',
				'lines-covered': linesCovered + '',
				'lines-valid': linesValid + '',
				'timestamp': new Date().getTime() + '',
				'version': this.versionNumber() + '') {
					mkp.comment 'Build by ' + this.versionNumber() + ' further reading: https://github.com/RosesTheN00b/gradle-cobol-plugin-unittest-extension'
					mkp.comment 'Based on https://raw.githubusercontent.com/cobertura/web/master/htdocs/xml/coverage-04.dtd '
					'sources'{
						source this.configuration.projectFileResolver(this.configuration.srcMainPath).absolutePath
					}
					'packages' {
						for (String packageName : cobolPackages.keySet()) {
							'package' ('branch-rate': '0', 'complexity': '0', 'line-rate': statistiker.compute(cobolPackages.get(packageName)), 'name': packageName){
								'classes'{
									for (CobolCoverageFile file : cobolPackages.getAt(packageName)) {
										'class'('branch-rate': '0', 'complexity': '0', 'line-rate': statistiker.compute(file),'filename': file.name(), 'name': new File(file.name()).getName()) {
											'methods' {}
											'lines' {
												for (CobolCoverageMethod method : file.methods()) {
													int methodOffset = method.startLine
													for (CobolCoverageLine coveredLine : method.methodStatus()) {
														if (coveredLine.status() == CoverageStatus.passed) {
															'line' ('hits': '1', 'number' : coveredLine.lineNumber() + methodOffset){}
														} else {
															'line' ('hits': '0', 'number' : coveredLine.lineNumber() + methodOffset){}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
		return xmlWriter
	}

	private Map<String, List<CobolCoverageFile>> resolvePackages(List<CobolCoverageFile> files){
		Map<String, List<CobolCoverageFile>> packages = new HashMap()
		for (CobolCoverageFile file : files) {
			String packageName = '.'
			if (file.name().contains('/')) {
				packageName = new File('/' + file.name()).getParent().substring(1)
			}
			if (! packages.containsKey(packageName)) {
				packages.put(packageName, new LinkedList<>())
			}
			packages.get(packageName).add(file)
		}
		return packages
	}


	private String versionNumber() {
		MetaInfPropertyResolver resolver = new MetaInfPropertyResolver('gradle-cobol-plugin-unittest-extension')
		return resolver.get('Implementation-Version').orElse('No version found!') + ' (' + resolver.get('Build-Date').orElse('No date found') + ')'
	}
}
