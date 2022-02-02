import mill._
import mill.scalalib._

object Cross {
  sealed trait PrjKind { val kind: String }
  object PrjKind       {
    case object JvmProject extends PrjKind { val kind = "jvm" }
    case object JsProject  extends PrjKind { val kind = "js"  }
  }

  sealed trait PrjScalaVersion { val version: String }
  object PrjScalaVersion       {
    case object Scala_2_13  extends PrjScalaVersion { val version = "2.13.8" }
    case object Scala_3_1_0 extends PrjScalaVersion { val version = "3.1.0"  }

    def apply(v: String): PrjScalaVersion = v match {
      case Scala_2_13.version  => Scala_2_13
      case Scala_3_1_0.version => Scala_3_1_0
      case v                   => throw new Exception(s"Unknown Scala version <$v>")
    }

    val default = Scala_3_1_0
    val all     = Seq(Scala_2_13, Scala_3_1_0)
  }

  /* ----------------------------------------------------------------------------------------------------------
   * Dependency management
   * ---------------------------------------------------------------------------------------------------------- */
  trait Deps {
    def prjScalaVersion: PrjScalaVersion
    val scalaVersion = prjScalaVersion.version

    val laminarVersion = "0.14.2"
    val scalaJSVersion = "1.8.0"
    val zioVersion     = "2.0.0-RC1"

    val airstream = ivy"com.raquo::airstream::$laminarVersion"
    val laminar   = ivy"com.raquo::laminar::$laminarVersion"

    val scalaJsDom = ivy"org.scala-js::scalajs-dom::2.1.0"

    val zioHttp = ivy"io.d11::zhttp:2.0.0-RC1"

    val zio        = ivy"dev.zio::zio::$zioVersion"
    val zioTest    = ivy"dev.zio::zio-test::$zioVersion"
    val zioTestSbt = ivy"dev.zio::zio-test-sbt::$zioVersion"
  }

  object Deps_213 extends Deps {
    override def prjScalaVersion: PrjScalaVersion = PrjScalaVersion.Scala_2_13
  }

  object Deps_31 extends Deps {
    override def prjScalaVersion: PrjScalaVersion = PrjScalaVersion.Scala_3_1_0
  }
}
