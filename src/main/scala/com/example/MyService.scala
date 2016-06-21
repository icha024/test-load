package com.example

import akka.actor.Actor
import spray.http.MediaTypes._
import spray.http._
import spray.routing._

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)

}


// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService {
  this: Actor =>

  implicit def executionContext = context.dispatcher

  val myRoute =
    path("") {
      get {
        respondWithMediaType(`text/html`) {
          onComplete(myFunc()) {
            case Success(res) => {
              complete {
                <html>
                  <body>
                    <h1>Say hello to
                      <i>spray-routing</i>
                      on
                      <i>spray-can</i>
                      !</h1>
                    Rendering Thread (is it?): ${Thread.currentThread().getId}
                    <br/>
                    ContextThread: ${res}
                  </body>
                </html>
              }
            }
            case Failure(ex) => complete(StatusCodes.InternalServerError)
          }
        }
      }
    }

  def myFunc()(implicit ec: ExecutionContext): Future[String] = {
    val p: Promise[String] = Promise[String]()
    Future {
//      println("Start sleep")
//      Thread.sleep(500)
//      println("End sleep")

      var sum: Long = 0
      for (i <- 0 to 1000000) {
        sum += (Math.random() * 100.0).toLong
      }
      p.success(Thread.currentThread().getId.toString + s" (Random number: $sum)")
    }

    p.future
  }
}