package it.unibo.pcd

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import it.unibo.pcd.BoidActor.BoidActorMessages

sealed trait ActorReceptionistMessages
object ActorReceptionistMessages:
  case class Register(name: String, ref: ActorRef[BoidActorMessages]) extends ActorReceptionistMessages
  case class GetActors(nameShared: String, replyTo: ActorRef[ActorReceptionistResponses])
      extends ActorReceptionistMessages
  case class RelayTo(name: String, msg: BoidActorMessages) extends ActorReceptionistMessages
  case class RelayAll(msg: BoidActorMessages) extends ActorReceptionistMessages
  case class Unregister(name: String) extends ActorReceptionistMessages
sealed trait ActorReceptionistResponses
object ActorReceptionistResponses:
  case class Response(infos: List[(String, ActorRef[BoidActorMessages])]) extends ActorReceptionistResponses
object BoidActorsReceptionist:
  def apply(
      db: List[(String, ActorRef[BoidActorMessages])] = List.empty
  ): Behavior[ActorReceptionistMessages] = Behaviors.setup { context =>
    import ActorReceptionistMessages.*
    import ActorReceptionistResponses.*
    Behaviors.receiveMessage {
      case Register(name, ref) =>
        apply(db :+ (name, ref))
      case RelayTo(name, msg) =>
        db.filter(_._1 == name).map(_._2).foreach(ref => ref ! msg)
        Behaviors.same
      case RelayAll(msg) =>
        db.map(_._2).foreach(ref => ref ! msg)
        Behaviors.same
      case Unregister(name) => apply(db.filterNot(_._1 == name))
      case GetActors(nameShared, replyTo) =>
        val res = db.filter(_._1 equals nameShared)
        replyTo ! Response(res)
        Behaviors.same
    }
  }
