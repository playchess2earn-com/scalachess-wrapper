package com.playchess2earn.scalachesswrapper

import chess.format.pgn.SanStr
import chess.format.{Fen, FullFen}
import chess.variant.Variant
import chess.{Game, Ply, Role, Square}

import java.util
import java.util.{ArrayList, Optional, OptionalInt}
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

class LichessGame(private var game: Game):
  def getCurrentColor: String =
    game.position.color.name

  def getPly: Integer =
    game.ply.asInstanceOf[Integer]

  def getFullMoveNumber: Integer =
    game.fullMoveNumber.asInstanceOf[Integer]

  def getFen: String =
    Fen.write(game).asInstanceOf[String]

  def getSans: java.util.List[String] =
    util.ArrayList(game.sans
      .map:
        _.asInstanceOf[String]
      .asJava)

  def getStartedAtPly: Integer =
    game.startedAtPly.asInstanceOf[Integer]

  def getPieceAt(at: Integer): Optional[Character] =
    game.position.board.pieceAt(at.asInstanceOf[Square])
      .map:
        _.forsyth.asInstanceOf[Character]
      .toJava

  def getLastMoveUci: Optional[String] =
    game.history.lastMove
      .map:
        _.uci
      .toJava

  def getLastMoveSan: Optional[String] =
    game.sans.lastOption
      .map:
        _.asInstanceOf[String]
      .toJava

  def getLegalMoves: java.util.Map[String, java.util.List[String]] =
    game.position.moves.map:
      case (square, moves) =>
        square.key -> util.ArrayList(moves.map:
          _.dest.key
        .asJava)
    .asJava

  def getLegalMovesUci: java.util.List[String] =
    util.ArrayList(game.position.legalMoves
      .map:
        _.toUci.uci
      .asJava)

  def isMoveLegal(from: Integer, to: Integer): java.lang.Boolean =
    game.position.legalMoves.exists: m =>
      m.orig == from.asInstanceOf[Square] && m.dest == to.asInstanceOf[Square]

  def isMoveLegal(from: Integer, to: Integer, promotion: Character): java.lang.Boolean =
    val role = Role.promotable(promotion)

    game.position.legalMoves.exists(move =>
      move.orig == from.asInstanceOf[Square] && move.dest == to
        .asInstanceOf[Square] && ((move.promotion, role) match
        case (Some(promotion), Some(role)) => promotion.name == role.name
        case _ => false
        ))

  def move(from: Integer, to: Integer): Unit =
    game(from.asInstanceOf[Square], to.asInstanceOf[Square], None) match
      case Right((game, _)) =>
        this.game = game
      case _ => throw RuntimeException("Unable to make move")

  def move(from: Integer, to: Integer, promotion: Character): Unit =
    val role = Role.promotable(promotion)

    game(from.asInstanceOf[Square], to.asInstanceOf[Square], role) match
      case Right((game, _)) =>
        this.game = game
      case _ => throw RuntimeException("Unable to make move")

  def drop(square: Integer, dropRole: Character): Unit =
    val role = Role.forsyth(dropRole)

    game.drop(role.get, square.asInstanceOf[Square]) match
      case Right((game, _)) =>
        this.game = game
      case _ => throw RuntimeException("Unable to make drop")

  def undoMove(): Unit =
    if this.game.sans.nonEmpty then
      val sans = game.sans.init
      val initialGame = Game(game.variant)
      game = initialGame.forward(sans).getOrElse(initialGame)

  def isAutoDraw: java.lang.Boolean =
    game.position.autoDraw

  def isCheckMate: java.lang.Boolean =
    game.position.checkMate

  def isEnd: java.lang.Boolean =
    game.position.end

  def isInsufficientMaterial: java.lang.Boolean =
    game.position.opponentHasInsufficientMaterial

  def isStaleMate: java.lang.Boolean =
    game.position.staleMate

  def winner: Optional[String] =
    game.position.winner.map:
      _.name
    .toJava

  def checkSquare: Optional[Integer] =
    game.position.checkSquare
      .map:
        _.asInstanceOf[Integer]
      .toJava

object LichessGame:
  def create(variant: String): LichessGame =
    val realVariant = Variant.byName(variant).getOrElse(throw RuntimeException("No such variant"))

    LichessGame(Game(realVariant))

  def create(variant: String, fen: FullFen): LichessGame =
    val realVariant = Variant.byName(variant).getOrElse(throw RuntimeException("No such variant"))

    val game = Fen.readWithMoveNumber(realVariant, fen)
      .map(_.toGame)
      .getOrElse(throw RuntimeException("Can't create game"))

    LichessGame(game)

  def create(
    variant: String,
    sans: java.util.List[String],
    ply: Integer = 0,
    startedAtPly: Integer = 0
  ): LichessGame =
    val realVariant = Variant.byName(variant).getOrElse(throw RuntimeException("No such variant"))

    val game = Game(
      realVariant.initialPosition,
      sans.asScala.toVector.map:
        _.asInstanceOf[SanStr],
      None,
      ply.asInstanceOf[Ply],
      startedAtPly.asInstanceOf[Ply]
    )

    LichessGame(game)

  def create(
    variant: String,
    sans: java.util.List[String]
  ): LichessGame =
    val realVariant = Variant.byName(variant).getOrElse(throw RuntimeException("No such variant"))

    val initialGame = Game(realVariant)
    val finalGame = initialGame
      .forward(sans.asScala.toVector.map(_.asInstanceOf[SanStr]))
      .getOrElse(initialGame)

    LichessGame(finalGame)

  def getSquareFromKey(key: String): OptionalInt =
    Square
      .fromKey(key)
      .map:
        _.asInstanceOf[Int]
      .toJavaPrimitive
