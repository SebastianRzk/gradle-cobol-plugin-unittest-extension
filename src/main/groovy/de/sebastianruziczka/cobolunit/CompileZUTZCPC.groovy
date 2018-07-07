package de.sebastianruziczka.cobolunit

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction

import de.sebastianruziczka.cobolunit.steps.ZUTZCPC

class CompileZUTZCPC extends DefaultTask {

	ZUTZCPC zutzcpcInstance = new ZUTZCPC(project.getExtensions().getByName('cobol').absoluteUnitTestFrameworkPath(CobolUnit.getSimpleName()) , project.getExtensions().getByName('cobol'))

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
