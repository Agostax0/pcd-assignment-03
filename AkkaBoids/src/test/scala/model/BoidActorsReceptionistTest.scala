package it.unibo.pcd
package model

import ActorReceptionistMessages.*
import ActorReceptionistResponses.Response

import BoidActor.BoidActorMessages
import BoidActor.BoidActorMessages.StopBoid
import akka.actor.testkit.typed.scaladsl.{ActorTestKit, TestProbe}
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.pcd.ActorReceptionistMessages
import org.scalactic.Prettifier.default
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
  private def genBoidProbe: TestProbe[BoidActorMessages] =
    testKit.createTestProbe[BoidActorMessages]()
  private def genBoidActor(
      receptionist: ActorRef[ActorReceptionistMessages],
      index: Int
  ): ActorRef[BoidActorMessages] =
    testKit.spawn[BoidActorMessages](BoidActor(receptionist, index))

  "A Receptionist" should "support registering" in:
    val receptionistProbe = genReceptionistProbe
    val boidActor = genBoidActor(receptionistProbe.ref, 1)

    import ActorReceptionistMessages.*
    receptionistProbe ! Register(1.toString, boidActor)
    receptionistProbe expectMessage Register(1.toString, boidActor)

  it should "support get registered actors" in:
    val receptionist = genReceptionistActor

    val boid1 = genBoidActor(receptionist, 1)
    val boid2 = genReceptionistResponseProbe
    receptionist ! Register(1.toString, boid1)

    receptionist ! GetActors(1.toString, boid2.ref)
    boid2 expectMessage Response(List((1.toString, boid1.ref)))

  it should "support getting all actor present" in:
    val receptionist = genReceptionistActor
    val boids = List(genBoidProbe, genBoidProbe, genBoidProbe)

    boids.foreach(boid => receptionist ! Register("", boid.ref))

    val probe = testKit.createTestProbe[ActorReceptionistResponses]()

    receptionist ! GetActors("*", probe.ref)

    probe.expectMessage(Response(boids.map(boid => ("", boid.ref))))

  private def genReceptionistResponseProbe =
    testKit.createTestProbe[ActorReceptionistResponses]("")

  it should "support unregistering" in:
    val receptionist = genReceptionistActor

    val boid1 = genBoidActor(receptionist, 1)
    val boid2 = genReceptionistResponseProbe
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
  it should "support relaying to a model" in:
    val probe = testKit.createTestProbe[BoidModelMessages]("modelProbe")
    val receptionist = testKit.spawn[ActorReceptionistMessages](BoidActorsReceptionist(probe.ref))

    receptionist ! RelayTo("", BoidModelMessages.ReceivePosition(Position(1, 2), 3))
    probe expectMessage BoidModelMessages.ReceivePosition(Position(1, 2), 0)

  it should "support relaying to all actors registered" in:
    val receptionist = genReceptionistActor
    val probe1 = genBoidProbe
    val probe2 = genBoidProbe

    receptionist ! Register(1.toString, probe1.ref)
    receptionist ! Register(2.toString, probe2.ref)

    receptionist ! RelayAll(X())

    probe1 expectMessage X()
    probe2 expectMessage X()

  "Updating the boid number" should "introduce new boids from none" in:
    val receptionist = genReceptionistActor
    val probe = testKit.createTestProbe[ActorReceptionistResponses]()

    receptionist ! UpdateBoidNumber(3)

    receptionist ! GetActors("*", probe.ref)

    probe.receiveMessage() match
      case Response(actors) =>
        actors.size shouldBe 3
        actors.map(_._1) should contain theSameElementsAs List("0", "1", "2")

  it should "introduce new boids from already existing" in:
    val receptionist = genReceptionistActor
    val boid1 = genBoidActor(receptionist, 1)
    val boid2 = genBoidActor(receptionist, 2)

    receptionist ! UpdateBoidNumber(5)

    val probe = testKit.createTestProbe[ActorReceptionistResponses]()

    receptionist ! GetActors("*", probe.ref)

    probe.receiveMessage() match
      case Response(actors) =>
        actors should have size 5


  it should "prompt exceeding boids to unsubscribe" in:
    val receptionist = genReceptionistActor
    val realBoid = genBoidActor(receptionist, 0)
    val fakeBoid = genBoidProbe

    receptionist ! Register("0", realBoid)
    receptionist ! Register("1", fakeBoid.ref)

    receptionist ! UpdateBoidNumber(0)
    fakeBoid.expectMessage(StopBoid)

    val probe = testKit.createTestProbe[ActorReceptionistResponses]()

    receptionist ! GetActors("*", probe.ref)

    probe.expectMessage(Response(List.empty))
