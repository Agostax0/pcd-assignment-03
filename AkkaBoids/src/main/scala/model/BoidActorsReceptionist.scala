package it.unibo.pcd


import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import it.unibo.pcd.model.BoidActor.BoidActorMessages
import it.unibo.pcd.model.BoidActor.BoidActorMessages.{SendPosition, StopBoid}
import it.unibo.pcd.model.BoidModelMessages.ReceivePosition
import it.unibo.pcd.model.{BoidActor, BoidModelMessages}

sealed trait ActorReceptionistMessages
object ActorReceptionistMessages:
  trait FromBoid extends ActorReceptionistMessages
  case class Unregister(name: String) extends FromBoid
  case class Register(name: String, ref: ActorRef[BoidActorMessages]) extends FromBoid

  trait FromModel extends ActorReceptionistMessages
  case object SendPositions extends FromModel
  case class UpdateBoidNumber(num: Int) extends FromModel

  trait Control extends ActorReceptionistMessages
  case class RelayTo[M](name: String, msg: M) extends Control
  case class RelayAll(msg: BoidActorMessages) extends Control
  case class GetActors(nameShared: String, replyTo: ActorRef[ActorReceptionistResponses]) extends Control

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
      case msg: FromBoid =>
        msg match
          case Register(name, ref) =>
            apply(model, db :+ (name, ref))
          case Unregister(name) => apply(model, db.filterNot(_._1 == name))
      case msg: FromModel =>
        msg match
          case SendPositions =>
            context.self ! RelayAll(SendPosition)
            Behaviors.same

          case UpdateBoidNumber(n) =>
            val dbSize = db.size
            context.log.info(s"Updating boid number to $n, current size is $dbSize")
            dbSize match
              case size if size > n =>
                // TODO fix
                val toRemove = db.drop(size - n)
                toRemove.foreach { case (name, ref) =>
                  ref ! StopBoid
                }
                apply(model, db.take(n))
              case size if size < n =>
                val newBoids = (db.size until n)
                  .map(i =>
                    (i.toString, context.spawn(BoidActor(receptionist = context.self, myIndex = i), i.toString))
                  )
                apply(model, db ++ newBoids.toList)
              case _ => Behaviors.same
      case msg: Control =>
        msg match
          case GetActors(nameShared, replyTo) =>
            if nameShared == "*" then replyTo ! Response(db)
            else
              val res = db.filter(_._1 equals nameShared)
              replyTo ! Response(res)
            Behaviors.same
          case RelayTo(name, msg) =>
            msg match
              case forBoid: BoidActorMessages =>
                db.filter(_._1 == name).map(_._2).foreach(ref => ref ! forBoid)
              case forModel: ReceivePosition =>
                model ! ReceivePosition(forModel.pos, db.size)
            Behaviors.same
          case RelayAll(toShare) =>
            db.map(_._2).foreach(ref => ref ! toShare)
            Behaviors.same
    }
  }
