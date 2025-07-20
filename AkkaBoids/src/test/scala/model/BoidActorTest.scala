package it.unibo.pcd
package model

import ActorReceptionistMessages.{Register, RelayAll, RelayTo, Unregister}
import BoidActor.BoidActorMessages
import BoidActor.BoidActorMessages.*

import akka.actor.testkit.typed.scaladsl.{ActorTestKit, TestProbe}
import akka.actor.typed.ActorRef
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, durations}

import scala.concurrent.duration.FiniteDuration
import scala.language.postfixOps

class BoidActorTest extends AnyFlatSpec with BeforeAndAfterAll with should.Matchers:
  var testKit: ActorTestKit = ActorTestKit()
  var receptionist: ActorRef[ActorReceptionistMessages] = _
  override def beforeAll(): Unit =
    testKit = ActorTestKit()
    receptionist = genReceptionistActor

  override def afterAll(): Unit =
    testKit.system.terminate()
    testKit.shutdownTestKit()

  private def genReceptionistProbe: TestProbe[ActorReceptionistMessages] =
    testKit.createTestProbe[ActorReceptionistMessages]()
  private def genReceptionistActor: ActorRef[ActorReceptionistMessages] =
    testKit.spawn[ActorReceptionistMessages](BoidActorsReceptionist().narrow)
  private def genBoidProbe: TestProbe[BoidActorMessages] =
    testKit.createTestProbe[BoidActorMessages]()
  private def genBoidActor(
      receptionist: ActorRef[ActorReceptionistMessages],
      index: Int
  ): ActorRef[BoidActorMessages] =
    testKit.spawn[BoidActorMessages](BoidActor(receptionist, index))

  "A boid actor" should "work correctly" in:
    val boid = genBoidActor(receptionist, 1)

  "A neighbor request" should "arrive to all registered boids" in:
    val probe1 = genBoidProbe
    val probe2 = genBoidProbe

    receptionist ! Register(1.toString, probe1.ref)
    receptionist ! Register(2.toString, probe2.ref)

    receptionist ! RelayAll(NeighborRequest(2))
    probe1 expectMessage NeighborRequest(2)
    probe2 expectMessage NeighborRequest(2)

  "A neighbor status" should "communicate the boid infos" in:
    val probe = genReceptionistProbe

    val boid1 = genBoidActor(probe.ref, 1)
    val boid2 = genBoidActor(probe.ref, 2)

    boid1 ! NeighborRequest(2)
    probe expectMessage RelayAll(NeighborStatus(Position.zero, Velocity.zero, 1, 2))

    boid2 ! NeighborRequest(2)
    probe expectMessage RelayAll(NeighborStatus(Position.zero, Velocity.zero, 2, 2))

  it should "allow to start a mass boid update when all neighbor have been chosen" in:
    val boid1 = genBoidActor(receptionist, 1)
    val probe = genBoidProbe

    receptionist ! Register(1.toString, boid1)
    receptionist ! Register(2.toString, probe.ref)

    receptionist ! RelayTo(1.toString, NeighborRequest(2))
    val msg = probe.receiveMessage()
    val boid1Info = msg match
      case status: NeighborStatus =>
        (status.position, status.velocity)

    receptionist ! RelayTo(1.toString, NeighborStatus(Position.zero, Velocity.zero, 0, 1))

    receptionist ! RelayTo(1.toString, NeighborRequest(2))
    probe.receiveMessage() match
      case status: NeighborStatus =>
        status.position should not be boid1Info._1
        status.position should not be boid1Info._2

  "A reset boid" should "reset the boid infos" in:
    val probe = genReceptionistProbe

    val boid1 = genBoidActor(probe.ref, 1)
    boid1 ! ResetBoid

    boid1 ! NeighborRequest(1)
    val msg = probe.receiveMessage()

    msg match
      case RelayAll(innerMsg) =>
        innerMsg match
          case status: NeighborStatus =>
            status.position should not be Position.zero
            status.velocity should not be Velocity.zero

      case _ => fail("")

  "An update model" should "update the model of each boid" in:

    val boid1 = genBoidProbe
    val boid2 = genBoidProbe

    receptionist ! Register(1.toString, boid1.ref)
    receptionist ! Register(2.toString, boid2.ref)

    val newModel: BoidsModel = BoidsModel.localModel.copy(separationWeight = 100)

    receptionist ! RelayAll(UpdateModel(newModel))

    val msg1 = boid1 expectMessage UpdateModel(newModel)
    msg1.model.separationWeight shouldBe 100.0

    val msg2 = boid2 expectMessage UpdateModel(newModel)
    msg2.model.separationWeight shouldBe 100.0

  "A boid actor" should "stop when receiving a StopBoid message" in:
    val probe = genReceptionistProbe
    val boid = genBoidActor(probe.ref, 1)
    boid ! StopBoid

    probe expectMessage Unregister(1.toString)
    probe.expectTerminated(boid)
