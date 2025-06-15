package main

import (
	"fmt"
	"math/rand"
)

type Move struct {
	player string
	move   string
}

type Player struct {
	name           string
	movesChannel   chan Move
	refereeChannel chan string
	initialScore   int
}

type Referee struct {
	refereeChannel chan string
	movesChannel   chan Move
}

func (p Player) play() {
	for {

		move := move(p.name)

		p.movesChannel <- move

		refereeAnswer := <-p.refereeChannel

		if refereeAnswer == p.name {
			p.initialScore++
			fmt.Println(p.name, "wins \t ", p.initialScore, " points")
		}

		if refereeAnswer == "Draw" {
			//	fmt.Println(p.name, "Draw")
		}

	}
}

func move(name string) Move {
	var moves = []string{"rock", "paper", "scissor"}
	return Move{name, moves[rand.Intn(3)]}
}

func winningMove(p1Move Move, p2Move Move) string {
	if p1Move.move == p2Move.move {
		return "Draw"
	}

	if p1Move.move == "rock" && p2Move.move == "scissor" {
		return p1Move.player
	}
	if p1Move.move == "paper" && p2Move.move == "rock" {
		return p1Move.player
	}
	if p1Move.move == "scissor" && p2Move.move == "paper" {
		return p1Move.player
	}

	return p2Move.player
}

func (r Referee) referee() {
	for {

		p1Move := <-r.movesChannel
		p2Move := <-r.movesChannel

		r.refereeChannel <- winningMove(p1Move, p2Move) //per il player1
		r.refereeChannel <- winningMove(p1Move, p2Move) //per il player2
	}
}

func main() {
	movesChannel := make(chan Move)

	refereeChannel := make(chan string)

	player1 := Player{"Player 1", movesChannel, refereeChannel, 0}
	player2 := Player{"Player 2", movesChannel, refereeChannel, 0}

	referee := Referee{refereeChannel, movesChannel}

	go player1.play()
	go player2.play()

	go referee.referee()

	select {}
}
