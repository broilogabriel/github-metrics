package io.github.broilogabriel

import scala.util.Try

import io.github.broilogabriel.projects.model.ProjectId

package object projects {

  object ProjectIdVar {

    def unapply(str: String): Option[ProjectId] =
      Try(ProjectId(str.toLong)).toOption

  }

}
