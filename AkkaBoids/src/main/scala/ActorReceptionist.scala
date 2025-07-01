package it.unibo.pcd

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

sealed trait ActorReceptionistMessages[T]
object ActorReceptionistMessages:
  case class Register[T](info: (String, ActorRef[T])) extends ActorReceptionistMessages
  case class Query[T](query: String, replyTo: ActorRef[ActorReceptionistResponses]) extends ActorReceptionistMessages
  case class Unregister[T](name: String) extends ActorReceptionistMessages
sealed trait ActorReceptionistResponses
object ActorReceptionistResponses:
  case class Response[T](infos: List[(String, ActorRef[T])]) extends ActorReceptionistResponses
object ActorReceptionist:
  def apply[T](
      db: List[(String, ActorRef[T])] = List.empty,
           ): Behavior[ActorReceptionistMessages[T]] = Behaviors.setup{ context =>
    import ActorReceptionistMessages.*
    import ActorReceptionistResponses.*
    Behaviors.receiveMessage {
      case Register(info) => apply(db :+ info)
      case Unregister(name) => apply(db.filterNot(_._1 == name))
      case Query(query, replyTo) =>
        val res = db.filter(_._1 == query)
        replyTo ! Response(res)
        Behaviors.same
    }
  }
