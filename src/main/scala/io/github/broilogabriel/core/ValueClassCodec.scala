package io.github.broilogabriel.core

import io.circe.{Decoder, Encoder}

/**
 * Generic codec for encoding and decoding value classes to and from json
 */
trait ValueClassCodec {
  import shapeless._
  implicit def encoderValueClass[T <: AnyVal, V](implicit
    g: Lazy[Generic.Aux[T, V :: HNil]],
    e: Encoder[V]
  ): Encoder[T] = Encoder.instance { value =>
    e(g.value.to(value).head)
  }

  implicit def decoderValueClass[T <: AnyVal, V](implicit
    g: Lazy[Generic.Aux[T, V :: HNil]],
    d: Decoder[V]
  ): Decoder[T] = Decoder.instance { cursor =>
    d(cursor).map { value ⇒ g.value.from(value :: HNil) }
  }
}
