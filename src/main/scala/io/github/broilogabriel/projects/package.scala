package io.github.broilogabriel

import io.github.broilogabriel.projects.model.ProjectId

import scala.util.Try

package object projects {

  object ProjectIdVar {

    def unapply(str: String): Option[ProjectId] =
      Try(ProjectId(str.toLong)).toOption

  }

}
