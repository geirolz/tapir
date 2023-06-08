package sttp.tapir.server.jdkhttp

import sttp.tapir.{Defaults, TapirFile}
import sttp.tapir.model.ServerRequest
import sttp.tapir.server.interceptor.log.{DefaultServerLog, ServerLog}
import sttp.tapir.server.interceptor.{CustomiseInterceptors, Interceptor}

import java.util.logging.{Level, Logger}

case class JdkHttpServerOptions(
    interceptors: List[Interceptor[Id]],
    createFile: ServerRequest => TapirFile,
    deleteFile: TapirFile => Unit
) {
  def prependInterceptor(i: Interceptor[Id]): JdkHttpServerOptions = copy(interceptors = i :: interceptors)
  def appendInterceptor(i: Interceptor[Id]): JdkHttpServerOptions = copy(interceptors = interceptors :+ i)
}

object JdkHttpServerOptions {
  val Default: JdkHttpServerOptions = customiseInterceptors.options

  private def default(interceptors: List[Interceptor[Id]]): JdkHttpServerOptions =
    JdkHttpServerOptions(interceptors, _ => Defaults.createTempFile(), Defaults.deleteFile())

  def customiseInterceptors: CustomiseInterceptors[Id, JdkHttpServerOptions] =
    CustomiseInterceptors(
      createOptions = (ci: CustomiseInterceptors[Id, JdkHttpServerOptions]) => default(ci.interceptors)
    ).serverLog(defaultServerLog)

  private val log = Logger.getLogger(classOf[JdkHttpServerInterpreter].getName)

  lazy val defaultServerLog: ServerLog[Id] =
    DefaultServerLog[Id](
      doLogWhenReceived = debugLog(_, None),
      doLogWhenHandled = debugLog,
      doLogAllDecodeFailures = debugLog,
      doLogExceptions = (msg: String, ex: Throwable) => log.log(Level.SEVERE, msg, ex),
      noLog = ()
    )

  private def debugLog(msg: String, exOpt: Option[Throwable]): Unit = exOpt match {
    case Some(e) => log.log(Level.FINE, msg, e)
    case None    => log.log(Level.FINE, msg)
  }
}
