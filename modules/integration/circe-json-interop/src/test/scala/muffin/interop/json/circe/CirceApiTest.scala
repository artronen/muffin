package muffin.interop.json.circe

import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.time.ZoneId

import cats.effect.{IO, Resource}
import cats.syntax.all.{*, given}

import codec.{*, given}
import io.circe.*
import io.circe.syntax.{*, given}
import org.scalatest.*
import org.scalatest.featurespec.AsyncFeatureSpec

import muffin.api.{ApiClient, ApiTest, ClientConfig}
import muffin.codec.CodecSupport
import muffin.dsl.*
import muffin.http.{Body, HttpClient, Method, Params}
import muffin.model.*

class CirceApiTest extends ApiTest[Encoder, Decoder]("circe", codec) {

  protected def httpClient: HttpClient[IO, Encoder, Decoder] =
    new HttpClient[IO, Encoder, Decoder] {

      def request[In: Encoder, Out: Decoder](
          url: String,
          method: Method,
          body: Body[In],
          headers: Map[String, String],
          params: Params => Params
      ): IO[Out] =
        (body match {
          case Body.Empty            => testRequest(url, method, None, params(Params.Empty))
          case Body.Json(value)      =>
            testRequest(url, method, Encoder[In].apply(value).noSpaces.some, params(Params.Empty))
          case Body.RawJson(value)   =>
            testRequest(url, method, parser.parse(value).map(_.noSpaces).toOption, params(Params.Empty))
          case Body.Multipart(parts) => ???
        }).flatMap(parseJson(_))

    }

}
