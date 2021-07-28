/*

   Copyright 2021 Michael Strasser.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

package io.klogging.build

import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

fun Project.configureAssemble() {
    tasks.register<Copy>("assembleRelease") {
        description = "Assembles files for release."
        group = "Distribution"

        project.extensions.getByType<PublishingExtension>().publications.names
            .filter { it != "kotlinMultiplatform" }
            .forEach { publicationName ->
                from(tasks.named("${publicationName}Jar"))
                from(tasks.named("${publicationName}JavadocJar"))
                from(tasks.named("${publicationName}SourcesJar"))

                val filenameRoot = "${project.name}-$publicationName-${project.version}"
                with(
                    copySpec()
                        .from(tasks.named("sign${publicationName.capitalize()}Publication"))
                        .rename { fileName ->
                            when (fileName) {
                                "module.json.asc" -> "$filenameRoot.module.json.asc"
                                "pom-default.xml.asc" -> "$filenameRoot.pom.asc"
                                else -> fileName
                            }
                        }
                )

                with(
                    copySpec()
                        .from(tasks.named("generatePomFileFor${publicationName.capitalize()}Publication"))
                        .rename { fileName -> if (fileName == "pom-default.xml") "$filenameRoot.pom" else fileName }
                )

                with(
                    copySpec()
                        .from(tasks.named("generateMetadataFileFor${publicationName.capitalize()}Publication"))
                        .rename { fileName -> if (fileName == "module.json") "$filenameRoot.module.json" else fileName }
                )
            }

        into(buildDir.toPath().resolve("release"))

        duplicatesStrategy = DuplicatesStrategy.FAIL
    }
}