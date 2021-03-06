package snakegame

import scala.scalajs.js

import org.scalajs.dom
import org.scalajs.dom.ext.KeyCode

object SnakeGame extends js.JSApp {
    var timer: Option[js.timers.SetIntervalHandle] = None
    val rnd = scala.util.Random
    val hero = new Snake(Config.viewWidth / 2 - 1, Config.viewHeight / 2 - 1)
    var dir = Direction.EAST
    var food: List[Position] = Nil
    var poison: List[Position] = Nil
    var score = 1

    def main(): Unit = {
        val canvas = dom.document.getElementById(Config.canvasId).asInstanceOf[dom.html.Canvas]
        canvas.width = Config.viewWidth * Config.blockSize
        canvas.height = Config.viewHeight * Config.blockSize
        val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

        setupUI(ctx)
        dom.window.addEventListener("keydown", (e: dom.KeyboardEvent) => {
            dir = e.keyCode match {
                case KeyCode.Up    => Direction.NORTH
                case KeyCode.Right => Direction.EAST
                case KeyCode.Down  => Direction.SOUTH
                case KeyCode.Left  => Direction.WEST
                case _             => dir
            }
        }, false)
        
        def update() = {
            // Generate food
            if (rnd.nextInt(100) < 8) {
                val pos = Position(1 + rnd.nextInt(Config.viewWidth - 2), 1 + rnd.nextInt(Config.viewHeight - 2))
                if (!hero.body.contains(pos) && !poison.contains(pos)) {
                    food = pos :: food
                    drawFood(food.head, ctx)
                }
            }

            // Generate poison
            if (rnd.nextInt(100) < 2) {
                val pos = Position(1 + rnd.nextInt(Config.viewWidth - 2), 1 + rnd.nextInt(Config.viewHeight - 2))
                if (!hero.body.contains(pos) && !poison.contains(pos)) {
                    poison = pos :: poison
                    drawPoison(pos, ctx)
                }
            }

            clearSnake(hero, ctx)
            hero.move(dir, food)
            drawSnake(hero, ctx)

            // Remove hero.position from food as it's been just eaten
            if (food.contains(hero.position)) {
                food = food.filter(_ != hero.position)

                score += 1
                updateScore(score)
            }

            if (hero.isBite || hero.isHit || poison.contains(hero.position)) // End game
                timer.foreach(js.timers.clearInterval _)
        }

        updateScore(score)
        timer = Option(js.timers.setInterval(250)(update))
    }

    def clearSnake(hero: Snake, ctx: dom.CanvasRenderingContext2D) = {
        // Optimization: clear the last block only
        val x = hero.body.last.x * Config.blockSize
        val y = hero.body.last.y * Config.blockSize
        ctx.clearRect(x, y, Config.blockSize, Config.blockSize)
    }

    def clearView(ctx: dom.CanvasRenderingContext2D) = {
        ctx.fillStyle = Config.colorBackground
        ctx.fillRect(0, 0, 639, 399)
    }

    def drawBorder(ctx: dom.CanvasRenderingContext2D) = {
        ctx.fillStyle = Config.colorForeground
        for (i <- 0 to Config.viewWidth) {
            ctx.fillRect(i * Config.blockSize, 0, Config.blockSize, Config.blockSize)
            ctx.fillRect(i * Config.blockSize, (Config.viewHeight - 1) * Config.blockSize, Config.blockSize, Config.blockSize)
        }
        for (i <- 0 to Config.viewHeight) {
            ctx.fillRect(0, i * Config.blockSize, Config.blockSize, (i + 1) * Config.blockSize)
            ctx.fillRect((Config.viewWidth - 1) * Config.blockSize, i * Config.blockSize, Config.blockSize, Config.blockSize)
        }
    }

    def drawFood(pos: Position, ctx: dom.CanvasRenderingContext2D) = {
        val x = pos.x * Config.blockSize
        val y = pos.y * Config.blockSize
        ctx.drawImage(Config.imgFood, x, y, Config.blockSize, Config.blockSize)
    }

    def drawPoison(pos: Position, ctx: dom.CanvasRenderingContext2D) = {
        val x = pos.x * Config.blockSize
        val y = pos.y * Config.blockSize
        ctx.drawImage(Config.imgPoison, x, y, Config.blockSize, Config.blockSize)
    }

    def drawSnake(hero: Snake, ctx: dom.CanvasRenderingContext2D) = {
        def drawSnakeHead(pos: Position) = {
            val x = pos.x * Config.blockSize
            val y = pos.y * Config.blockSize
            ctx.fillStyle = Config.colorSnakeHead
            ctx.fillRect(x + 1, y + 1, Config.blockSize - 2, Config.blockSize - 2)
            ctx.strokeRect(x + 1, y + 1, Config.blockSize - 2, Config.blockSize - 2)        
        }
        def drawSnakeBody(pos: Position) = {
            val x = pos.x * Config.blockSize
            val y = pos.y * Config.blockSize
            ctx.fillStyle = Config.colorSnake
            ctx.fillRect(x + 1, y + 1, Config.blockSize - 2, Config.blockSize - 2)
            ctx.strokeRect(x + 1, y + 1, Config.blockSize - 2, Config.blockSize - 2)
        }

        if (!hero.body.tail.isEmpty) {
            drawSnakeBody(hero.body.tail.head)
            drawSnakeBody(hero.body.last)
        }
        drawSnakeHead(hero.body.head)
    }

    def isValidPosition(pos: Position): Boolean = {
        pos.x > 0 && pos.y > 0 && pos.x < Config.viewWidth -1 && pos.y < Config.viewHeight - 1
    }

    def setupUI(ctx: dom.CanvasRenderingContext2D) = {
        clearView(ctx)
        drawBorder(ctx)
        drawSnake(hero, ctx)
    }

    def updateScore(score: Int) = {
        val el = dom.document.getElementById("score")
        val txt = dom.document.createTextNode(s"$score")
        el.removeChild(el.firstChild)
        el.appendChild(txt)
    }
}