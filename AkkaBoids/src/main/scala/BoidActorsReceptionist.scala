package it.unibo.pcd

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import it.unibo.pcd.BoidActor.BoidActorMessages
import it.unibo.pcd.BoidActor.BoidActorMessages.{SendPosition, StopBoid}
import it.unibo.pcd.BoidModelMessages.ReceivePosition

sealed trait ActorReceptionistMessages
object ActorReceptionistMessages:
  trait FromBoid extends ActorReceptionistMessages
  case class Unregister(name: String) extends FromBoid
  case class Register(name: String, ref: ActorRef[BoidActorMessages]) extends FromBoid

  trait FromModel extends ActorReceptionistMessages
  case class GetActors(nameShared: String, replyTo: ActorRef[ActorReceptionistResponses]) extends FromModel
  case object SendPositions extends FromModel
  case class UpdateBoidNumber(num: Int) extends FromModel

  trait Control extends ActorReceptionistMessages
  case class RelayTo[M](name: String, msg: M) extends Control
  case class RelayAll(msg: BoidActorMessages) extends Control
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

      case UpdateBoidNumber(n) =>
        if n > db.size then
          val newBoids = (db.size until n)
            .map(i => (i.toString, context.spawn(BoidActor(receptionist = context.self, myIndex = i), i.toString)))
          apply(model, db ++ newBoids.toList)
        else if n < db.size then
          val toRemove = db.drop(n)
          toRemove.foreach { case (name, ref) =>
            ref ! StopBoid
            context.stop(ref)
          }
          apply(model, db.take(n))
        else Behaviors.same
    }
  }
