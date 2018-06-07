package de.sebastianruziczka.cobolunit.coverage

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

		double lineRate = 0.5
		int linesCovered = 5
		int linesValid = 7

		def xmlWriter = new StringWriter()
		def xmlMarkup = new MarkupBuilder(xmlWriter)

		Map<String, List<CobolCoverageFile>> packages = this.resolvePackages(coveredFiles)
		xmlMarkup.
				'coverage'('branch-rage':'0',
				'branch-covered':'0',
				'branch-valid':'0',
				'complexity':'0',
				'line-rate': lineRate + '',
				'lines-covered': linesCovered + '',
				'lines-valid': linesValid + '',
				'timestamp': new Date().getTime() + '',
				'version': this.versionNumber() + '') {

					'sources'{
						source this.configuration.projectFileResolver(this.configuration.srcMainPath).absolutePath
					}
					for (String packageName : packages.keySet()) {
						'package' ('name': packageName){
							'classes'{
								for (CobolCoverageFile file : packages.getAt(packageName)) {
									'class'('name': file.name()) {
										//'methods':'',
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

		println xmlWriter
		println 'CHalo'
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
