package it.unibo.pcd

import org.scalactic.Prettifier.default
import akka.actor.testkit.typed.scaladsl.{ActorTestKit, TestProbe}
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.pcd.ActorReceptionistMessages.{GetActors, Register, RelayAll, RelayTo, Unregister}
import it.unibo.pcd.ActorReceptionistResponses.Response
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class BoidActorsReceptionistTest extends AnyFlatSpec with Matchers with BeforeAndAfterAll:

  var testKit: ActorTestKit = ActorTestKit()

  override def beforeAll(): Unit = testKit = ActorTestKit()
  override def afterAll(): Unit =
    testKit.system.terminate()
    testKit.shutdownTestKit()

  private def genReceptionistProbe: TestProbe[ActorReceptionistMessages] =
    testKit.createTestProbe[ActorReceptionistMessages]()
  private def genReceptionistActor: ActorRef[ActorReceptionistMessages] =
    testKit.spawn[ActorReceptionistMessages](BoidActorsReceptionist())
  private def genBoidProbe: TestProbe[BoidActorMessages | ActorReceptionistResponses] =
    testKit.createTestProbe[BoidActorMessages | ActorReceptionistResponses]()
  private def genBoidActor(
      receptionist: ActorRef[ActorReceptionistMessages],
      index: Int
  ): ActorRef[BoidActorMessages | ActorReceptionistResponses] =
    testKit.spawn[BoidActorMessages | ActorReceptionistResponses](BoidActor(receptionist, index))

  "A Receptionist" should "support registering" in:
    val receptionistProbe = genReceptionistProbe
    val boidActor = genBoidActor(receptionistProbe.ref, 1)

    import ActorReceptionistMessages.*
    receptionistProbe ! Register(1.toString, boidActor)
    receptionistProbe expectMessage Register(1.toString, boidActor)

  it should "support get registered actors" in:
    val receptionist = genReceptionistActor

    val boid1 = genBoidActor(receptionist, 1)
    val boid2 = genBoidProbe
    receptionist ! Register(1.toString, boid1)

    receptionist ! GetActors(1.toString, boid2.ref)
    boid2 expectMessage Response(List((1.toString, boid1.ref)))

  it should "support unregistering" in:
    val receptionist = genReceptionistActor

    val boid1 = genBoidActor(receptionist, 1)
    val boid2 = genBoidProbe
    receptionist ! Register(1.toString, boid1)

    receptionist ! Unregister(1.toString)

    receptionist ! GetActors(1.toString, boid2.ref)
    boid2 expectMessage Response(List())

  case class X() extends BoidActorMessages
  it should "support relaying to a an actor" in:
    val receptionist = genReceptionistActor
    val probe = genBoidProbe

    receptionist ! Register(1.toString, probe.ref)
    receptionist ! RelayTo(1.toString, X())

    probe expectMessage X()

  it should "support relaying to all actors registered" in:
    val receptionist = genReceptionistActor
    val probe1 = genBoidProbe
    val probe2 = genBoidProbe

    receptionist ! Register(1.toString, probe1.ref)
    receptionist ! Register(2.toString, probe2.ref)

    receptionist ! RelayAll(X())

    probe1 expectMessage X()
    probe2 expectMessage X()
