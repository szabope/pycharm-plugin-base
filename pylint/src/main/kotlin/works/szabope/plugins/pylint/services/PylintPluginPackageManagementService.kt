package works.szabope.plugins.pylint.services

import com.intellij.openapi.project.Project
import com.jetbrains.python.packaging.PyRequirement
import com.jetbrains.python.packaging.pyRequirement
import com.jetbrains.python.packaging.requirement.PyRequirementRelation
import works.szabope.plugins.common.services.AbstractPluginPackageManagementService

class PylintPluginPackageManagementService(override val project: Project) : AbstractPluginPackageManagementService() {
    override fun getRequirement(): PyRequirement {
        return pyRequirement("pylint", PyRequirementRelation.COMPATIBLE, "3.0")
    }
}
