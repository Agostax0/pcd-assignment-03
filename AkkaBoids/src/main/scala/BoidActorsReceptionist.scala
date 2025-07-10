package it.unibo.pcd

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import it.unibo.pcd.BoidActor.BoidActorMessages
import it.unibo.pcd.BoidActor.BoidActorMessages.{SendPosition, StopBoid}
import it.unibo.pcd.BoidModelMessages.ReceivePosition

sealed trait ActorReceptionistMessages
object ActorReceptionistMessages:
  case class Register(name: String, ref: ActorRef[BoidActorMessages]) extends ActorReceptionistMessages
  case class GetActors(nameShared: String, replyTo: ActorRef[ActorReceptionistResponses])
      extends ActorReceptionistMessages
  case object SendPositions extends ActorReceptionistMessages
  case class RelayTo[M](name: String, msg: M) extends ActorReceptionistMessages
  case class RelayAll(msg: BoidActorMessages) extends ActorReceptionistMessages
  case class Unregister(name: String) extends ActorReceptionistMessages
sealed trait ActorReceptionistResponses
object ActorReceptionistResponses:
  case class Response(infos: List[(String, ActorRef[BoidActorMessages])]) extends ActorReceptionistResponses

object BoidActorsReceptionist:
  def apply(
      model: ActorRef[BoidModelMessages] = null,
      db: List[(String, ActorRef[BoidActorMessages])] = List.empty
  ): Behavior[ActorReceptionistMessages] = Behaviors.setup { context =>
    import ActorReceptionistMessages.*
    import ActorReceptionistResponses.*
    Behaviors.receiveMessage {
      case Register(name, ref) =>
        apply(model, db :+ (name, ref))
      case RelayTo(name, msg) =>
        msg match
          case forBoid: BoidActorMessages =>
            db.filter(_._1 == name).map(_._2).foreach(ref => ref ! forBoid)
          case forModel: ReceivePosition =>
            model ! ReceivePosition(forModel.pos, db.size)
        Behaviors.same
      case RelayAll(msg) =>
        db.map(_._2).foreach(ref => ref ! msg)
        Behaviors.same
      case Unregister(name) => apply(model, db.filterNot(_._1 == name))
      case GetActors(nameShared, replyTo) =>
        if nameShared == "*" then replyTo ! Response(db)
        else
          val res = db.filter(_._1 equals nameShared)
          replyTo ! Response(res)
        Behaviors.same
      case SendPositions =>
        context.self ! RelayAll(SendPosition)
        Behaviors.same
    }
  }
