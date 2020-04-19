package de.sebastianruziczka.cobolunit

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction

import de.sebastianruziczka.CobolExtension
import de.sebastianruziczka.cobolunit.steps.ZUTZCPC

class CompileZUTZCPC extends DefaultTask {

	private ZUTZCPC zutzcpcInstance;
	
	public CompileZUTZCPC() {
		def project = getProject()
		final CobolExtension configuration = getProject().extensions.findByType(CobolExtension.class)
		
		zutzcpcInstance = new ZUTZCPC(configuration.absoluteUnitTestFrameworkPath(CobolUnit.getSimpleName()),
			 configuration)
		
	}
	
	
	@OutputFiles
	public Map<String, File> getInstance() {
		HashMap<String, File> files = new HashMap<>()
		this.zutzcpcInstance.outputFilePaths().each{
			files.put(it, project.file(it))
		}
		return files
	}

	@TaskAction
	public void compile() {
		zutzcpcInstance.setup()
	}
}
